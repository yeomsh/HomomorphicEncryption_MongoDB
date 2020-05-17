package Blockchain;

import DataClass.Contract;
import DataClass.DataSource;
import DataClass.Database;
import DataClass.User;
import GUI.ContractGUI;
import org.json.simple.JSONObject;
import util.KeyGenerator;
import util.StringUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Vector;

public class BCManager {
    //블록체인 관련
    protected Vector<Client> cList;
    protected static JSONObject block;
    public static ArrayList<String> chainStr;
    ArrayList<String> ipList;
    static int countPOW = 0;
    //관련X
    public Contract contract; //이전까지 생성된 계약서 데이터
    public ContractGUI contractGUI;
    public BCEventHandler eventHandler = new BCEventHandler();
    public Database db;
    public User user;
    public KeyGenerator KG = new KeyGenerator();
    public DataSource.Callback callback = null;
    public BCManager(Database db, String receiverIP) {
        this.db = db;
        this.contract = new Contract(0, receiverIP);
        this.contractGUI = new ContractGUI(this);
    }

    public BCManager(User user, Database db, ArrayList<String> ipList,Contract contract) {
        this.db = db;
        this.user = user;
        this.ipList = ipList;
        this.contract = contract;
        this.contractGUI = new ContractGUI(this);
    }
    public BCManager(User user, Database db, ArrayList<String> ipList, Contract contract, DataSource.Callback callback) {
        this(user,db,ipList,contract);
        this.callback = callback;
        //점주 /근로자 마다 할 수 있는 step이 다른데 그것도 체크해야함
    }

    public void chainUpdate() {
        //의문사항 체인 가장 긴걸로 업데이트하라고 전부 뿌리는데, 그럼 악의적인 사용자가 가장 길게 만들어서 뿌리면 어떡하지 . .  ?!?
        try {
            cList = new Vector<>();
            for (String ip: ipList){
                cList.addElement(new Client(ip, "chainRequest"));
            }
            for (Client i : cList)
                i.join();
            for (String ip: ipList){
                cList.addElement(new Client(ip, "chainUpdate"));
            }
            for (Client i : cList)
                i.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public Boolean broadCastBlock(){
        Boolean result = false;
        try {
            for (String ip: ipList){
                cList.addElement(new Client(ip, "blockUpdate"));
            }
            for (Client i : cList)
                i.join();
            synchronized (chainStr) {
                chainStr.add(block.get("proofHash").toString());
            }
            System.out.println("countPOW: "+ countPOW);
            if (countPOW > 0) {
                for (String ip: ipList){
                    cList.addElement(new Client(ip, "blockSave"));
                }
                for (Client i : cList)
                    i.join();
                result = true;
                System.out.println("BCManager: block save 완료");
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        cList.clear();
        return result;
    }
        // object대신에 근로자에게 서명을 붙여서 보낸 파일을 해시한 내용 = nowHash
    protected void proofOfWork(String hashData){
        String latestHash = "";
        synchronized (chainStr) {
            // 체인 업데이트 (두 명 이상의 피어가 동시에 브로드캐스팅을 시도했을 경우는 생각해봐야함 ,,,일단 한명이 브로드캐스팅했을때 업데이트 기준)
            if (chainStr.size() != 0)
                latestHash = chainStr.get(chainStr.size() - 1);
        }
        block.put("previousHash",latestHash);
        String nowHash = StringUtil.getSha256(hashData);
        block.put("nowHash",nowHash); // 계약서 해쉬
        //블록생성
        Block b = new Block(nowHash, latestHash);
        block.put("proofHash",b.ProofOfWork());
        block.put("nonce",b.getNonce());
        block.put("timeStamp", b.getTimeStamp());
    }
    protected byte[] addSignature(byte[] hashData){
        Signature ecdsa;
        byte[] bSig = null;
        try {
            ecdsa = Signature.getInstance("SHA256withECDSA");
            ecdsa.initSign(user.sigPrivateKey);
            ecdsa.update(hashData);
            bSig = ecdsa.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return bSig;
    }

    public boolean isVerify(byte[] nowHash, byte[] sigHash, PublicKey pk)
            throws SignatureException{
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

    class BCEventHandler implements ActionListener {
        void step4() throws InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, InvalidAlgorithmParameterException, IOException, NoSuchProviderException {
            JSONObject data = contract.fileData;
            byte[] hashDataByte = ((JSONObject) data.get("wHashSignature")).get("plain").toString().getBytes(StandardCharsets.UTF_8);
            byte[] sigHashDataByte = Base64.getDecoder().decode(((JSONObject) data.get("wHashSignature")).get("sig").toString());
            PublicKey sigPublicKey = KG.makePublicKey(((JSONObject) data.get("oHashSignature")).get("publicKey").toString());
            if (isVerify(hashDataByte, sigHashDataByte, sigPublicKey)) {
                System.out.println("점주가 근로자 서명 검증 성공>_<");
                block = new JSONObject();
                //체인 리퀘스트부터 쫙쫙 하면 될듯함
                chainUpdate();
                proofOfWork(((JSONObject) data.get("wHashSignature")).get("plain").toString());
                if(broadCastBlock()){ //작업증명에 성공하면 -> 임시서버에서 지우고 -> 키워드 업로드
                    db.removeStepContract(contract); //임시서버에서 지우기
                    //키워드 업로드 -> 파일 업로드 -> zindex 업데이트
                    callback.onDataLoaded();
                }
                else{ //실패하면 그냥 끝
                    System.out.println("브로드 캐스트에서 작업증명이 옳지않다고 나옴 -> 실패");
                    callback.onDataFailed();
                }
            } else {
                System.out.println("점주가 근로자 서명 검증 실패");
            }
        }
        void step2(JSONObject data) throws IOException {
            JSONObject obj = new JSONObject();
            String hashData = StringUtil.getSha256(data.toString());
            obj.put("plain", hashData);
            byte[] sigHashData = addSignature(hashData.getBytes(StandardCharsets.UTF_8));
            obj.put("sig", Base64.getEncoder().encodeToString(sigHashData));
            obj.put("publicKey", KG.replaceKey(false, "ECDSApublic.pem","ECDSA"));
            data.put("oHashSignature", obj);
            contract.step++;
            contract.fileData = data;
            db.insertStepContract(contract);
        }
        void step3() throws InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, IOException {
            JSONObject data = contract.fileData;
            System.out.println("data: \n" + data);
            byte[] hashDataByte = ((JSONObject) data.get("oHashSignature")).get("plain").toString().getBytes(StandardCharsets.UTF_8);
            byte[] sigHashDataByte = Base64.getDecoder().decode(((JSONObject) data.get("oHashSignature")).get("sig").toString());
            PublicKey sigPublicKey = KG.makePublicKey(((JSONObject) data.get("oHashSignature")).get("publicKey").toString());
            if (isVerify(hashDataByte, sigHashDataByte, sigPublicKey)) {
                System.out.println("근로자가 점주 서명 검증 성공");
                //서명하기 버튼 열리면 될 듯 !!
                //서명붙이기
                JSONObject obj = new JSONObject();
                String hashData = StringUtil.getSha256(data.toString());
                obj.put("plain", hashData);
                byte[] sigHashData = addSignature(hashData.getBytes(StandardCharsets.UTF_8));
                obj.put("sig", Base64.getEncoder().encodeToString(sigHashData));
                obj.put("publicKey", KG.replaceKey(false, "ECDSApublic.pem","ECDSA"));
                data.put("wHashSignature", obj);
                contract.step++;
                contract.fileData = data;
                db.insertStepContract(contract);
            } else {
                System.out.println("서명 검증 실패");
            }
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            JSONObject data = contractGUI.getStepContract();
            System.out.println("contract btnsubmin 클릭됨, step: " + contract.step);
            try {
                switch (contract.step){
                    case 4:
                        step4();
                        break;
                    case 3:
                        step3();
                        break;
                    case 2:
                        step2(data);
                        break;
                    case 1:
                    case 0:
                        contract.step++;
                        contract.fileData = data;
                        db.insertStepContract(contract);
                        break;
                    default:
                        System.out.println("BCManager: undefined step: "+contract.step);
                        break;
                }
                contractGUI.setVisible(false); //모든 작업끝나면 계약서 작성창 닫기
                System.out.println("제출");
            } catch (InvalidKeySpecException | NoSuchAlgorithmException | SignatureException | IOException | InvalidAlgorithmParameterException | NoSuchProviderException invalidKeySpecException) {
                invalidKeySpecException.printStackTrace();
            }

        }
    }
}
