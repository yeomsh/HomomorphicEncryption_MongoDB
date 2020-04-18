package Blockchain;

import java.net.*;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import util.FileManage;
import util.StringUtil;

import java.io.*;

class Client extends Thread {
   protected int portNum;
   protected String hostName;
   protected JSONObject data = null;
   protected String type;
   String nowHash = "";
   String finishHash = "";
   int nonce;
   long timeStamp;

   Socket socket;
   BufferedReader is;
   PrintWriter os;
   ObjectInputStream ois = null;
   ObjectOutputStream oos = null;
   JSONParser jParser;
   private final String ALGORITHM = "sect163k1";
   private static BouncyCastleProvider bouncyCastleProvider;
   public static final BouncyCastleProvider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();
   private FileManage fm;

   static {
      bouncyCastleProvider = BOUNCY_CASTLE_PROVIDER;
   }

   public Client(String ip, String type, JSONObject data) {
      try {
         hostName = ip;
         portNum = 3000;
         this.type = type;
         this.data = data;
         String path = TotalManager.class.getResource("").getPath();
         path = java.net.URLDecoder.decode(path, "UTF-8");
         this.fm = new FileManage(path + "\\contract");
      } catch (UnsupportedEncodingException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      start();
   }

   public Client() {
      portNum = 3000;
   }

   public void run() {
      connect();
      if (hostName != "disable") {
         try {
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
                  ChainRequest();
               else if (type.equals("blockUpdate"))
                  BlockUpdate();
               else if (type.equals("chainUpdate"))
                  ChainUpdate();
               else if (type.equals("blockSave"))
                  BlockSave();
               // disconnect();
               return;
            }
            String message = is.readLine();
            if (message.equals("ok")) {
               System.out.println(type + "작업을 성공하였습니다");
            } else if (message.equals("fail")) {
               System.out.println(type + "작업을 실패하였습니다");
            }
            // 위 조건문에 total manager에서 쓰는 flag 넣어서 보여주는 문구 조절하면 될듯
            disconnect();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }

   }

   protected void BlockUpdate() throws IOException {
      // TODO Auto-generated method stub
      // 확인할 내용 전송
      os.println(TotalManager.block.toString());
      os.flush();

      String message = is.readLine();
      if(message.equals("success")) {
         TotalManager.countPOW++;
         System.out.println("success받음");
      }
      else if(message.equals("fail")) {
         TotalManager.countPOW--;
         System.out.println("fail받음");
      }
      os.println("finishBlockUpdate");
      os.flush();
      TotalManager.frame.makeContractBtn.setEnabled(true);

      System.out.println("block update 완료");
      disconnect();
   }



   protected void step1() {
      os.println(data);
      os.flush();
      System.out.println("점주의 정보를 담은 계약서 초안을 전송하였습니다.");
   }

   protected void step2() {
      try {
         String hashText = StringUtil.getSha256(data.toString()); // 계약서 해쉬
         data.put("hashText", hashText);

         byte[] sigHash = addSignature(hashText.getBytes("UTF-8"));
         String sigHashStr = Base64.getEncoder().encodeToString(sigHash);
         data.put("sigHash", sigHashStr);
         data.put("wPk", TotalManager.KG.replaceKey(false, "public.pem"));
         os.println(data);
         os.flush();
         String owner = data.get("owner").toString();
         fm.setSrc(fm.getSrc() + "\\" + owner + ".txt");
         fm.addTofile(sigHashStr);

      } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      System.out.println("근로자의 정보와 근로자의 서명을 담은 계약서를 전송하였습니다.");
   }

   protected void step3() {
      // 데이터 내용 contractBlock에 저장
      try {

         String owner = data.get("owner").toString();
         String worker = data.get("worker").toString();
         fm.setSrc(fm.getSrc() + "\\" + worker + ".txt");
         byte[] hashText = fm.getLineString(3).getBytes("UTF-8"); // 점주가 가지고 있는 파일에는 3번이 sigAllHash
         System.out.println(fm.getLineString(4));
         byte[] sigHash = Base64.getDecoder().decode(fm.getLineString(4));
         PublicKey pk = TotalManager.KG.makePublicKey(fm.getLineString(5)); // 근로자의 공개키 ->파일에서 불러오기

         if (isVerify(hashText, sigHash, pk)) {
            TotalManager.frame.sigBtn1.setEnabled(true);
            TotalManager.isVerifySuccess = true;
            data.put("wSigVerify", "success");
            System.out.println("서명검증에 성공하였습니다.");
            byte[] sigAllHash = addSignature(sigHash);
            String sigHashStr = Base64.getEncoder().encodeToString(sigAllHash);
            data.put("sigAllHash", sigHashStr);
            data.put("oPk", TotalManager.KG.replaceKey(false, "public.pem"));
            os.println(data);
            os.flush();
         } else {
            System.out.println("서명검증에 실패하였습니다.");
            data.put("wSigVerify", "fail");
            os.println(data);
            os.flush();
            TotalManager.isVerifySuccess = false;
            // disconnect(); 차피 무조건 disconnect 됨
            // 그럼 아예 프로구램 메뉴 선택으로 돌아가야함
         }
      } catch (SignatureException | IOException | NoSuchAlgorithmException | InvalidAlgorithmParameterException
              | InvalidKeySpecException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   protected void step4() {
      try {
         String owner = data.get("owner").toString();
         fm.setSrc(fm.getSrc() + "\\" + owner + ".txt");
         byte[] sigHash = Base64.getDecoder().decode(fm.getLineString(2));
         byte[] sigHashAll = Base64.getDecoder().decode(fm.getLineString(4));
         PublicKey pk = TotalManager.KG.makePublicKey(fm.getLineString(5)); // 근로자의 공개키 ->파일에서 불러오기

         if (isVerify(sigHash, sigHashAll, pk)) {
            System.out.println("최종 서명검증에 성공하셨습니다.");
            TotalManager.isVerifySuccess = true;
            ProofOfWork(sigHash.toString());
            TotalManager.block.put("type", "blockUpdate");
            TotalManager.block.put("nowHash", nowHash);
            TotalManager.block.put("proofHash", finishHash);
            TotalManager.block.put("nonce", nonce);
            TotalManager.block.put("timeStamp", timeStamp);
            data.put("oSigVerify", "success");
            System.out.println("timeStamp: "+TotalManager.block.get("timeStamp"));
            System.out.println("previousHash: "+TotalManager.block.get("previousHash"));
            System.out.println("contractHash: "+TotalManager.block.get("nowHash"));
            System.out.println("ProofOfWork_Difficulty: "+TotalManager.block.get("difficulty"));
            System.out.println("ProofOfWork_Result: "+TotalManager.block.get("timeStamp"));
            System.out.println("nonce: "+TotalManager.block.get("nonce"));

         } else {
            System.out.println("서명검증에 실패하셨습니다.");
            TotalManager.isVerifySuccess = false;
            data.put("oSigVerify", "fail");
         }
         os.println(data);
         os.flush();
      } catch (IOException | SignatureException | NoSuchAlgorithmException | InvalidAlgorithmParameterException
              | InvalidKeySpecException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

   }

   protected byte[] addSignature(byte[] hashText) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
      Signature ecdsa;
      byte[] bSig = null;
      try {
         ecdsa = Signature.getInstance("SHA256withECDSA");
         ecdsa.initSign(TotalManager.privateKey);
         ecdsa.update(hashText);
         bSig = ecdsa.sign();
      } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e1) {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }
      return bSig;
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

   protected String proof(String data, String previousHash) {
      System.out.println(data);
      System.out.println(previousHash);
      Block b = BlockProvider.mineBlock(data, previousHash);
      String hash = null;
      hash = b.ProofOfWork(); // 작업증명

      nonce = b.getNonce();
      timeStamp = b.getTimeStamp();
      return hash;
   }

   // object대신에 근로자에게 서명을 붙여서 보낸 파일을 해시한 내용 = nowHash
   protected void ProofOfWork(String hashText)
           throws IOException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {

      String latestHash = "";
      synchronized (TotalManager.chainStr) {
         // 체인 업데이트 (두 명 이상의 피어가 동시에 브로드캐스팅을 시도했을 경우는 생각해봐야함 ,,,일단 한명이 브로드캐스팅했을때 업데이트 기준)
         if (TotalManager.chainStr.size() != 0)
            latestHash = TotalManager.chainStr.get(TotalManager.chainStr.size() - 1);
      }
      TotalManager.block.put("previousHash",latestHash);
      nowHash = StringUtil.getSha256(hashText); // 계약서 해쉬
      finishHash = proof(nowHash, latestHash);
   }

   // chain을 요청해서 받는 함수
   protected void ChainRequest() throws IOException {
      JSONObject request = new JSONObject();
      request.put("type", "chainRequest");
      os.println(request.toString());
      os.flush();
      // request에 대한 응답을 확인
      String message = "";
      while (true) {
         // 서버에서 뭔가 메세지를 보내면
         message = is.readLine();
         System.out.println("받은 내용 : " + message);
         if (message != null) {
            JSONParser jsonParser = new JSONParser();
            JSONObject data = null;

            try {
               data = (JSONObject) jsonParser.parse((message));
            } catch (ParseException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
            String type = data.get("type").toString();

            // chain을 받으면
            if (type.equals("chain")) {

               // 더 긴 chain이 들어오면 갱신
               synchronized (TotalManager.chainStr) {
                  int len = Integer.parseInt(data.get("len").toString());
                  if (len > TotalManager.chainStr.size()) {
                     String chain = data.get("content").toString();
                     TotalManager.chainStr.clear();
                     String arr[] = chain.split(" ");
                     for (int i = 0; i < len; i++)
                        TotalManager.chainStr.add(arr[i]);
                  }
               }

               data = new JSONObject();
               data.put("type", "확인");
               os.println(data.toString());
               os.flush();

            }
            // bye를 받으면
            else if (type.equals("bye")) {
               System.out.println("bye받음");
               break;
            }
         } else {
            System.out.println(hostName);
            System.out.println("chainRequest null들어옴");
            break;
         }
      }
      // bye 메세지를 받으면 연결 끊기
      disconnect();

   }

   // block 전송
   protected void BlockSave() throws IOException {

      // finishHash값 없음
      String nowHash = TotalManager.block.get("proofHash").toString();
      String wName = data.get("worker").toString();
      String owner = data.get("owner").toString();
      int wage = Integer.parseInt(data.get("wage").toString());

      TotalManager.insertContract(owner, wName, wage);

      synchronized (TotalManager.chainStr) {
         JSONObject json = new JSONObject();
         json.put("type", "blockSave");
         json.put("content", nowHash);
         json.put("len", TotalManager.chainStr.size());

         os.println(json.toString());
         os.flush();
         FileManage FM = new FileManage(TotalManager.PATH+"chain.txt");
         FM.reWriteFile(TotalManager.chainStr);
      }

      // update에 대한 응답을 확인
      String message = is.readLine();
      if(message.equals("finishSave")) {
         System.out.println("block save 완료");
         disconnect();
      }

      // bye 메세지를 받으면 연결 끊기


   }

   protected void ChainUpdate() throws IOException {

      synchronized (TotalManager.chainStr) {
         String nowChain = "";
         for (int i = 0; i < TotalManager.chainStr.size(); i++)
            nowChain += TotalManager.chainStr.get(i) + " ";
         JSONObject json = new JSONObject();
         json.put("type", "chainUpdate");
         json.put("content", nowChain);
         json.put("len", TotalManager.chainStr.size());

         os.println(json.toString());
         os.flush();
         FileManage FM = new FileManage(TotalManager.PATH+"chain.txt");
         FM.reWriteFile(TotalManager.chainStr);
      }

      // update에 대한 응답을 확인
      String message = "";
      while (true) {
         // 서버에서 뭔가 메세지를 보내면
         message = is.readLine();
         System.out.println("받은 내용 : " + message);
         if (message != null) {
            JSONParser jsonParser = new JSONParser();
            JSONObject data = null;

            try {
               data = (JSONObject) jsonParser.parse((message));
            } catch (ParseException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }

            System.out.println(socket.getInetAddress().getHostAddress() + " :" + message);
            String type = data.get("type").toString();

            if (type.equals("finishSave")) {
               data = new JSONObject();
               data.put("type", "확인");
               os.println(data.toString());
               os.flush();
            } else if (type.equals("bye")) {
               System.out.println("bye받음");
               break;
            }
         } else {
            System.out.println("null들어옴");
            break;
         }
      }
      // bye 메세지를 받으면 연결 끊기
      disconnect();

   }

   protected void sendMessage(JSONObject message) {
      StringUtil.getSha256(message.toString());
      os.println(message);
      if (type == "contract")
         System.out.println(hostName + "에게 계약서를 전송하였습니다.");
      else
         System.out.println(hostName + "에게 체인을 전송하였습니다.");
   }

   protected void connect() {
      int timeout = 3500;
      try {
         InetAddress address = InetAddress.getByName(hostName);
         SocketAddress socketAddress = new InetSocketAddress(address, portNum);
         // socket = new Socket(address.getHostAddress(), portNum);
         socket = new Socket();
         socket.setSoTimeout(20000); // readtime 관련
         socket.connect(socketAddress, timeout); // connect time 관련

         is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         os = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
         //
         // ois=new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
         oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
         // TotalManager.ableIp.add(hostName);
      } catch (IOException ex) {
         System.out.println(ex);
         System.out.println(hostName + " :  Error while connecting to Server!");
         hostName = "disable";
      }

   }

   protected void disconnect() {
      try {
         System.out.println("연결종료");
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