package Blockchain;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import util.FileManager;
import util.StringUtil;

public class Server extends Thread {

   protected int portNum;
   protected Vector<processorText> clientVector;
   protected ArrayList<String> chainStr;
   protected ServerSocket server;

   public Server(int port, ArrayList<String> Chain) {
      portNum = port;
      chainStr = Chain;
      clientVector = new Vector<>();
      try {
         server = new ServerSocket(port);
         start();
      } catch (IOException ex) {
         System.out.println("Cannot execute Chat Server!");
         ex.printStackTrace();
         System.exit(1);
      }
   }

   public void run() {
      try {
         while (true) {
            System.out.println("server: client의 접속 기다리는 중");
            Socket client = server.accept();
            System.out.println("server : "+client.getInetAddress().getHostAddress() + " 로부터 연결되었습니다.");
            processorText cp = new processorText(client, chainStr, this);
            cp.start();
            synchronized (clientVector) { // synchronized: 누군가 clientVector 사용시 접근 못하게 lock (동기화)
               clientVector.addElement(cp);
            }
         }
      } catch (SocketException ex) {
         ex.printStackTrace();
      } catch (IOException ex) {
         System.out.println("Error while connecting to Client!");
         System.exit(1);
      }
   }

   public void close() {
      try {
         server.close();
         System.out.println("서버를 닫습니다 .");

      } catch (IOException ex) {
         System.out.println("Cannot close the server");
         ex.printStackTrace();
         System.exit(1);
      }
   }
}

class processorText extends Thread {
   protected Server server;
   protected Socket socket;
   protected ArrayList<String> chainStr;
   protected BufferedReader is;
   protected PrintWriter os;
   protected ObjectOutputStream oos = null;
   protected JSONObject data = new JSONObject();

   public processorText(Socket socket, ArrayList<String> chainStr, Server server) {
      this.server = server;
      this.socket = socket;
      this.chainStr = chainStr;
      try {
         OutputStream oss = socket.getOutputStream();
         InputStream iss = socket.getInputStream();
         is = new BufferedReader(new InputStreamReader(iss));
         os = new PrintWriter(new BufferedWriter(new OutputStreamWriter(oss)));
         oos = new ObjectOutputStream(new BufferedOutputStream(oss));
      } catch (IOException ex) {
         System.out.println("Error while openning I/O!");
         System.out.println(ex);
      }

   }

   public void chainRequest() {
      synchronized (chainStr) {
         StringBuilder sb = new StringBuilder();
         for (String chain: chainStr)
            sb.append(chain+" ");
         data.put("type", "chain");
         data.put("content", sb.toString());
         data.put("len", chainStr.size());
         os.println(data.toString());
         os.flush();
      }
   }

   public void blockUpdate(JSONObject data){
      System.out.println("server: blcokupdate");
      // 내용가져오고 블록체인 업데이트 확인
      String nowHash = data.get("nowHash").toString();
      String proofHash = data.get("proofHash").toString();
      String nonce = data.get("nonce").toString();
      Long timeStamp = (Long) data.get("timeStamp");
      int last = BCManager.chainStr.size() - 1; //음수에 대한 예외처리가 필요한가..? 제네시스블록이 있어서 체인이 없을린 X
      String checkPOW = StringUtil.getSha256(BCManager.chainStr.get(last) + timeStamp + nowHash + nonce);
      if (checkPOW.equals(proofHash)) os.println("success");
      else os.println("fail");
      os.flush();
   }

   public void chainUpdate(JSONObject data) throws IOException {
      synchronized (chainStr) {
         String chain = data.get("content").toString();
         chainStr = new ArrayList<>(Arrays.asList(chain.split(" ")));
         FileManager.reWriteFile(chainStr);
         BCManager.chainStr = FileManager.readChainFile();
      }
      JSONObject json = new JSONObject();
      json.put("type", "finishSave");
      os.println(json.toString());
      os.flush();
   }


   public void BlockSave(JSONObject data) {
      // block이 추가된 chain전송
      synchronized (chainStr) {
         // 체인 업데이트 (두 명 이상의 피어가 동시에 브로드캐스팅을 시도했을 경우는 생각해봐야함 ,,,일단 한명이 브로드캐스팅했을때 업데이트 기준)
         int len = Integer.parseInt(data.get("len").toString());
         String block = data.get("proofHash").toString();
         chainStr.add(block);
         FileManager.addTofile(block);
      }
      os.println("finishSave");
      os.flush();
   }

   public void run() {
      try {
         loop: while(true) {
            String message = is.readLine();
            System.out.println("server: 넘어온 데이터: " + message);
            if (message != null) {
               JSONParser jsonParser = new JSONParser();
               data = (JSONObject) jsonParser.parse((message));
               String type = data.get("type").toString();
               switch (type){
                  case "chainRequest":
                     chainRequest();
                     break;
                  case "chainUpdate":
                     chainUpdate(data);
                     break;
                  case "blockUpdate":
                     blockUpdate(data);
                     break;
                  case "blockSave":
                     BlockSave(data);
                     break;
                  case "ok":
                     break loop;
                  default:
                     System.out.println("server: undefined type: "+type);
               }
            }
         }
      } catch (IOException | ParseException ex) {
         System.out.println(ex);
      } finally {
         try {
            System.out.println("tm닫음");
            is.close();
            os.close();
            socket.close();
         } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Error while closing socket!");
         }
      }
   }
}