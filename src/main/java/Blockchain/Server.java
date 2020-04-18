package Blockchain;

import java.net.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Vector;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import util.FileManage;
import util.KeyGenerator;
import util.StringUtil;

public class Server extends Thread {

   protected int portNum;
   protected Vector clientVector;
   protected ArrayList<String> chainStr;
   protected ServerSocket server;
   protected BlockProvider blockProvider;
   protected MyFrame frame = TotalManager.frame;
   protected MyActionListener mListener;

   public Server(int port, ArrayList<String> Chain) {
      portNum = port;
      chainStr = Chain;
      clientVector = new Vector();

      mListener = new MyActionListener(frame, this);
      addListener(mListener);

      try {
         server = new ServerSocket(port);
      } catch (IOException ex) {
         System.out.println("Cannot execute Chat Server!");
         ex.printStackTrace();
         System.exit(1);
      }
   }

   public void addListener(MyActionListener ml) {
      frame.askedContractBtn.addActionListener(ml);
      frame.sigBtn2.addActionListener(ml);
   }

   public void run() {
      try {
         while (true) {
            System.out.println("client의 접속 기다리는 중");
            Socket client = server.accept();
            frame.oIpField2.setText(client.getInetAddress().getHostAddress());
            frame.wIpField2.setText(InetAddress.getLocalHost().getHostAddress());
            System.out.println(client.getInetAddress().getHostAddress() + " 로부터 연결되었습니다.");
            processorText cp = new processorText(client, chainStr, this);
            mListener.setPt(cp);
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

class MyActionListener implements ActionListener {

   private MyFrame frame;
   private Server server;
   private processorText pt = null;

   public MyActionListener(MyFrame frame, Server server) {
      this.frame = frame;
      this.server = server;
   }

   void setPt(processorText pt) {
      this.pt = pt;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      if (e.getSource() == frame.askedContractBtn) {
         frame.changePanel(frame.acPanel);
      } else if (e.getSource() == frame.sigBtn2) {
         try {
            pt.makeContract(pt.data);
         } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | ClassNotFoundException
                 | SignatureException | IOException e1) {
            e1.printStackTrace();
         }
      }
   }

}

class processorText extends Thread {
   protected Server server;
   protected Socket socket;
   protected ArrayList<String> chainStr;
   protected BufferedReader is;
   protected PrintWriter os;
   protected ObjectInputStream ois = null;
   protected ObjectOutputStream oos = null;
   protected JSONObject data = new JSONObject();
   protected byte[] sigHash = null;
   protected FileManage fm;

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

         String path = TotalManager.class.getResource("").getPath();
         path = java.net.URLDecoder.decode(path, "UTF-8");
         this.fm = new FileManage(path + "\\contract");
      } catch (IOException ex) {
         System.out.println("Error while openning I/O!");
         System.out.println(ex);
      }

   }

   public void chainRequest() {
      synchronized (chainStr) {
         String nowChain = "";
         for (int i = 0; i < chainStr.size(); i++)
            nowChain += chainStr.get(i) + " ";
         data.put("type", "chain");
         data.put("content", nowChain);
         data.put("len", chainStr.size());

         os.println(data.toString());
         os.flush();
      }
   }

   public void blockUpdate(JSONObject data) throws IOException {

      JSONObject json = new JSONObject();

      // 내용가져오고 블록체인 업데이트 확인
      String nowHash = data.get("nowHash").toString();
      String proofHash = data.get("proofHash").toString();
      String nonce = data.get("nonce").toString();
      Long timeStamp = (Long) data.get("timeStamp");
      int last = TotalManager.chainStr.size() - 1;

      String checkPOW = StringUtil.getSha256(TotalManager.chainStr.get(last) + Long.toString(timeStamp) + nowHash + nonce);
      if (checkPOW.equals(proofHash))
         os.println("success");
      else
         os.println("fail");
      os.flush();

      String message = is.readLine();
      if(message.equals("finishBlockUpdate"))
         return;

   }

   public void chainUpdate(JSONObject data) {
      // block이 추가된 chain전송
      synchronized (chainStr) {
         // 체인 업데이트 (두 명 이상의 피어가 동시에 브로드캐스팅을 시도했을 경우는 생각해봐야함 ,,,일단 한명이 브로드캐스팅했을때 업데이트 기준)
         int len = Integer.parseInt(data.get("len").toString());
         String chain = data.get("content").toString();
         chainStr.clear();
         String arr[] = chain.split(" ");
         for (int i = 0; i < len; i++)
            chainStr.add(arr[i]);
         FileManage FM = new FileManage(TotalManager.PATH + "chain.txt");
         FM.reWriteFile(chainStr);
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

         String block = data.get("content").toString();
         chainStr.add(block);
         FileManage FM = new FileManage(TotalManager.PATH + "chain.txt");
         FM.addTofile(block);
      }
      os.println("finishSave");
      os.flush();
   }

   public boolean isVerify(byte[] nowHash, byte[] sigHash, PublicKey pk)
           throws SignatureException, UnsupportedEncodingException {
      Signature ecdsa = null;
      try {
         ecdsa = Signature.getInstance("SHA256withECDSA");
         ecdsa.initVerify(pk);
         ecdsa.update(nowHash);

      } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return ecdsa.verify(sigHash);

   }

   protected byte[] addSignature(String hashText) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

      Signature ecdsa;
      byte[] bSig = null;
      try {
         byte[] bText = hashText.getBytes("UTF-8");
         ecdsa = Signature.getInstance("SHA256withECDSA");
         ecdsa.initSign(TotalManager.privateKey);
         ecdsa.update(bText);
         bSig = ecdsa.sign();
      } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException
              | UnsupportedEncodingException e1) {
         e1.printStackTrace();
      }
      return bSig;
   }

   protected void makeContract(JSONObject data) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException,
           IOException, ClassNotFoundException, SignatureException {
      data.put("wName", server.frame.wNameField2.getText());
      os.println(data);
      os.flush();

      String hashText = StringUtil.getSha256(data.toString()); // 계약서 해쉬
      data.put("hashText", hashText);

      sigHash = addSignature(hashText);

      oos.writeObject(sigHash);
      oos.flush();
      oos.writeObject(TotalManager.publicKey);
      oos.flush();
      System.out.println("계약서 완성본을 전달하였습니다.");

   }

   protected void makeContract2() throws IOException, ClassNotFoundException, SignatureException {

      ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
      byte[] sigHashAll = (byte[]) ois.readObject(); // 서명된 계약서 최종본
      System.out.println("sighashall: " + sigHashAll);
      PublicKey pk = (PublicKey) ois.readObject();
      System.out.println("pk: " + pk);
      if (isVerify(sigHash, sigHashAll, pk)) {
         System.out.println("서명검증에 성공하셨습니다.");
         TotalManager.isVerifySuccess = true;
         os.println("성공");
         os.flush();
      } else {
         System.out.println("서명검증에 실패하셨습니다.");
         TotalManager.isVerifySuccess = true;
         data.put("type", "실패");
         os.println(data);
         os.flush();
      }
   }

   public void step1() {
      try {
         fm.makeFile(data.get("owner").toString()); // 근로자는 점주 이름으로 파일 생성
         fm.addTofile(data.get("owner").toString());
         fm.addTofile(data.get("wage").toString());
         os.println("ok");
         os.flush();
      } catch (IOException e) {
         os.println("fail");
         os.flush();
         e.printStackTrace();
      }

   }

   public void step2() throws IOException {
      String worker = data.get("worker").toString();
      fm.makeFile(worker); // 점주는 근로자 이름으로 파일 생성
      fm.addTofile(data.get("owner").toString());
      fm.addTofile(data.get("wage").toString());
      fm.addTofile(worker);
      fm.addTofile(data.get("hashText").toString());
      fm.addTofile(data.get("sigHash").toString());
      fm.addTofile(data.get("wPk").toString());
      os.println("ok");
      os.flush();
   }

   public void step3() { // 근로자가 받는 부분

      String owner = data.get("owner").toString();
      fm.setSrc(fm.getSrc() + "\\" + owner + ".txt");
      fm.addTofile(data.get("worker").toString());
      // fm.addTofile(data.get("hashText").toString());
      fm.addTofile(data.get("sigAllHash").toString());
      fm.addTofile(data.get("oPk").toString());
      os.println("ok");
      os.flush();
   }

   public void step4() {
      if (data.get("oSigVerify").toString().equals("success"))
         os.println("ok");
      else if (data.get("oSigVerify").toString().equals("fail"))
         os.println("fail");
      os.flush();
   }

   public void run() {
      try {
         String message;
         // while (true) {
         message = is.readLine();
         System.out.println("넘어온 데이터: " + message);
         if (message != null) {
            JSONParser jsonParser = new JSONParser();
            // add
            ArrayList<String> newfile = null;
            // finish
            data = (JSONObject) jsonParser.parse((message));
            String type = data.get("type").toString();
            if (type.equals("step1"))
               step1();
            else if (type.equals("step2"))
               step2();
            else if (type.equals("step3"))
               step3();
            else if (type.equals("step4"))
               step4();
            else {
               if (type.equals("chainRequest"))
                  chainRequest();
               else if (type.equals("blockUpdate"))
                  blockUpdate(data);
               else if (type.equals("chainUpdate"))
                  chainUpdate(data);
               else if (type.equals("blockSave"))
                  BlockSave(data);
               else if (type.equals("chain")) {
                  synchronized (chainStr) {
                     // 체인 업데이트 (두 명 이상의 피어가 동시에 브로드캐스팅을 시도했을 경우는 생각해봐야함 ,,,일단 한명이 브로드캐스팅했을때 업데이트 기준)
                     int len = Integer.parseInt(data.get("len").toString());
                     if (len > chainStr.size()) {
                        String chain = data.get("content").toString();
                        chainStr.clear();
                        String arr[] = chain.split(" ");
                        for (int i = 0; i < len; i++)
                           chainStr.add(arr[i]);
                        FileManage FM = new FileManage(TotalManager.PATH + "chain.txt");
                        FM.reWriteFile(chainStr);
                     }
                  }
               }
            }
         }
      } catch (IOException | ParseException ex) {

      } finally {
         try {
            System.out.println("tm닫음");
            //
            server.frame.homeBtn.doClick();
            server.clientVector.removeElement(this);
            is.close();
            os.close();

            socket.close();
         } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Error while closing socket!");
         }
      }
   }

   public void setContractField(JSONObject data) {
      String oName = data.get("owner").toString();
      String wage = data.get("wage").toString();
      server.frame.oNameField2.setText(oName);
      server.frame.oNameField2.setEnabled(false);
      server.frame.wageField2.setText(wage);
      server.frame.wageField2.setEnabled(false);
   }

}