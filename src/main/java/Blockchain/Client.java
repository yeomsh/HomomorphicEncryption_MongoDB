package Blockchain;

import java.net.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import util.FileManager;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

class Client extends Thread {
    protected int portNum;
    protected String hostName;
    protected String type;
    Socket socket;
    BufferedReader is;
    PrintWriter os;
    ObjectInputStream ois = null;
    ObjectOutputStream oos = null;
    JSONParser jsonParser = new JSONParser();

    public Client(String ip, String type) {
        hostName = ip;
        portNum = 3000;
        this.type = type;
        start();
    }

    public void run() {
        if (!hostName.equals("disable")) {
            try {
                connect();
                switch (type){
                    case "chainRequest":
                        ChainRequest();
                        break;
                    case "chainUpdate":
                        ChainUpdate();
                        break;
                    case "blockUpdate":
                        BlockUpdate();
                        break;
                    case "blockSave":
                        BlockSave();
                        break;
                    default:
                        System.out.println("client: undefined type: "+type);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    protected void BlockUpdate() throws IOException {
        // TODO Auto-generated method stub
        // 확인할 내용 전송
        System.out.println("client: send block: " + BCManager.block.toString());
        BCManager.block.put("type","blockUpdate");
        os.println(BCManager.block.toString());
        os.flush();
        String message = is.readLine();
        if (message.equals("success")) {
            BCManager.countPOW++;
            System.out.println("success받음");
        } else if (message.equals("fail")) {
            BCManager.countPOW--;
            System.out.println("fail받음");
        }
        JSONObject data = new JSONObject();
        data.put("type","ok");
        os.println(data.toString());
        os.flush();
        System.out.println("block update 완료");
        disconnect();
    }
    // chain을 요청해서 받는 함수
    protected void ChainRequest() throws IOException {
        System.out.println("client: chainRequest");
        JSONObject request = new JSONObject();
        request.put("type", "chainRequest");
        os.println(request.toString());
        os.flush();
        // request에 대한 응답을 확인
        String message = is.readLine();
        try{
           JSONObject data = (JSONObject) jsonParser.parse((message));
           String type = data.get("type").toString();
           if (type.equals("chain")) {
              // 더 긴 chain이 들어오면 갱신
              synchronized (BCManager.chainStr) {
                 int len = Integer.parseInt(data.get("len").toString());
                 if (len > BCManager.chainStr.size()) {
                    String chain = data.get("content").toString();
                    BCManager.chainStr.clear();
                    BCManager.chainStr = new ArrayList<>(Arrays.asList(chain.split(" ")));
                 }
              }
              data.clear();
              data.put("type","ok");
              os.println(data.toString());
              os.flush();
           }
        } catch (ParseException e) {
           e.printStackTrace();
        }
        disconnect();
    }

    // block 전송
    protected void BlockSave() throws IOException {
        JSONObject data = new JSONObject();
        synchronized (BCManager.chainStr) {
            data.put("type", "blockSave");
            data.put("proofHash", BCManager.block.get("proofHash").toString());
            data.put("len", BCManager.chainStr.size());
            os.println(data.toString());
            os.flush();
            FileManager.reWriteFile(BCManager.chainStr);
        }
        // update에 대한 응답을 확인
        String message = is.readLine();
        if (message.equals("finishSave")) {
            data.clear();
            data.put("type", "ok");
            os.println(data.toString());
            os.flush();
            disconnect();
        }
    }

    protected void ChainUpdate() throws IOException {

        synchronized (BCManager.chainStr) {
            StringBuilder sb = new StringBuilder();
            for (String chain: BCManager.chainStr)
                sb.append(chain+" ");
            JSONObject json = new JSONObject();
            json.put("type", "chainUpdate");
            json.put("content", sb.toString());
            json.put("len", BCManager.chainStr.size());
            os.println(json.toString());
            os.flush();
            FileManager.reWriteFile(BCManager.chainStr);
        }
        // update에 대한 응답을 확인
        try {
            String message = is.readLine();
            JSONObject data = (JSONObject) jsonParser.parse((message));
            String type = data.get("type").toString();
            if (type.equals("finishSave")) {
                data.clear();
                data.put("type", "ok");
                os.println(data.toString());
                os.flush();
            }
            else{
                System.out.println("client: chainupdate type is not finish save ( type: "+type);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // bye 메세지를 받으면 연결 끊기
        disconnect();

    }

    protected void connect() {
        int timeout = 3500;
        try {
            InetAddress address = InetAddress.getByName(hostName);
            SocketAddress socketAddress = new InetSocketAddress(address, portNum);
            // socket = new Socket(address.getHostAddress(), portNum);
            socket = new Socket();
            socket.setSoTimeout(10000); // readtime 관련
            socket.connect(socketAddress, timeout); // connect time 관련

            is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            os = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            //
            // ois=new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            // TotalManager.ableIp.add(hostName);
            System.out.println("client: connect 완료");
        } catch (IOException ex) {
            System.out.println(ex);
            System.out.println(hostName + " :  Error while connecting to Server!");
            hostName = "disable";
        }

    }

    protected void disconnect() {
        try {
            System.out.println("client: 연결종료");
            is.close();
            os.close();
            if (oos != null)
                oos.close();
            if (ois != null)
                ois.close();
            socket.close();
        } catch (IOException ex) {
        }
    }
}