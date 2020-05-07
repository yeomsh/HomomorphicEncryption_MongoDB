package HomomorphicEncryption;


import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.Random;
import java.util.Vector;
import java.math.BigInteger;

import netscape.javascript.JSObject;
import org.bson.Document;
import org.json.simple.JSONObject;
import util.KeyGenerator;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class User  { //extends dataclass.user로 바꾸기
    private int qidRange = 100;
    private int rRange = 60;
    private int pkSize = 5;

    public BigInteger qid; //.pem파일에서 읽어왕!
    public BigInteger id;
    public String ip;
    public BigInteger r; //data만들 때마다 랜덤으로 생성
    public Vector<AGCDPublicKey> pkSet = new Vector<>();

    private BigInteger au;

    public String userType = "";
    public PublicKey publicKey;
    private PrivateKey privateKey;
    Random rand = new Random();

    public User(String ip, String userType){
        this.ip = ip;
        this.userType = userType;
        //makeQid();
    }
    public User(Document d){
   //     this.id = new BigInteger(d.get("id").toString(),16);
        this.ip = d.get("ip").toString();
     //   this.userType = d.get("userType").toString();
    }

    public User(Vector<AGCDPublicKey> pkSet){
        File qidFile = new File("qid.txt");
        FileWriter writer = null;
        BufferedWriter bWriter = null;
        BufferedReader bReader = null;
        try {
            //Ri 생성
            this.r = new BigInteger(rRange,rand);
            //pkSize 지정
            this.pkSize = pkSize;
            makeUserKeySet(pkSet);
            //ip 찾기
            InetAddress ip = InetAddress.getLocalHost();
            this.ip = ip.getHostAddress(); //ip 지정
            //this.ip = "127.0.0.1";
            //qid 찾기
            if (!qidFile.exists()) { //서비스 이용 이력이 없는 유저 -> qid 생성해야함
                System.out.println("qidFile 없음");
                writer = new FileWriter(qidFile, false);
                bWriter = new BufferedWriter(writer);
                makeQid();
                bWriter.write(this.qid.toString(16));
                bWriter.flush();
            }
            else{ //qid.txt 있으면 기존에 서비스 이용 이력 1회 이상
                System.out.println("qidFile 있음");
                String s;
                File file = new File("qid.txt");
                bReader = new BufferedReader(new FileReader(file));
                // 더이상 읽어들일게 없을 때까지 읽어들이게 합니다.
                while((s = bReader.readLine()) != null) {
                    this.qid = new BigInteger(s,16);
                }
                //id 찾기 -> 필요하면 방법을 구상해야겠지만, 현재 뇌피셜로는 qid가 있는 상태에서 id는 필요없음
            }
            System.out.println("DONE");
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(bWriter != null) bWriter.close();
                if(writer != null) writer.close();
                if(bReader != null) bReader.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    //사용자의 키 생성 (qid, r, pk)
    void makeQid(){
        this.id = new BigInteger(80,rand); //80, 20은 임의로!
        for (int i = 0; i < HomomorphicEncryption.server.getuList().size(); i++) {
            if(this.id == HomomorphicEncryption.server.getuList().get(i).id){ //중복 발견
                this.id = new BigInteger(80,rand);
                i = -1; //0번쨰 index부터 다시 검사
            }
        }
        BigInteger randVal = new BigInteger(20,rand);
        this.qid = this.id.multiply(randVal);
        HomomorphicEncryption.server.nosqldb.insertUser(this);
    }
    public void setKey(PublicKey publicKey, PrivateKey privateKey){
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }
    String encrypteData(JSONObject data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException {
        byte[] byteData = data.toString().getBytes(StandardCharsets.UTF_8);
        System.out.println(publicKey.getAlgorithm());
        Cipher cipher = Cipher.getInstance("DES",KeyGenerator.BOUNCY_CASTLE_PROVIDER);
        //EC", "SunEC"
        //"ECIESwithAES",KeyGenerator.BOUNCY_CASTLE_PROVIDER
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] bytePlain= cipher.doFinal(byteData);
        return Base64.getEncoder().encodeToString(bytePlain);
    }
    String decrypteData(String encrytedData) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance("ECDSA", KeyGenerator.BOUNCY_CASTLE_PROVIDER);
        byte[] byteEncrypted = Base64.getDecoder().decode(encrytedData.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] bytePlain = cipher.doFinal(byteEncrypted);
        String decrypted = new String(bytePlain, "utf-8");
        return decrypted;
        //decrypted 를 JSON OBJECT로 만드는 작어이 필요함
    }
    //나중에 검색문에서 사용할 r 변경 가능하도록
    void ChangeUserR(){
        r = new BigInteger(rRange,rand);
    }

    //사용자마다 랜덥의 public key set 만드는 함수
    void makeUserKeySet(Vector<AGCDPublicKey> pkSet){
        this.pkSet.add(pkSet.firstElement());
        boolean usedpk[] = new boolean[pkSet.size()]; //default = false
        usedpk[0] = true; //x0 넣기
        while(this.pkSet.size() < pkSize) {
            int pknum = (int)(Math.random()*pkSet.size());
            if(usedpk[pknum]) continue;
            usedpk[pknum] = true;
            this.pkSet.add(pkSet.get(pknum));
        }
        System.out.println("user-selected pkSet's index : " + usedpk);
        for(int i = 0; i<this.pkSet.size(); i++) {
            if (i == 0) System.out.println("x0(hexadecimal) : " + this.pkSet.get(i).pk.toString(16) );
            else System.out.println(i + "(hexadecimal) : " + this.pkSet.get(i).pk.toString(16) );
        }
    }
    public void setAu(BigInteger au){
        this.au = au;
    }

    public BigInteger getAu(){
        return au;
    }

    public String toString(){
        return "ip : "+ this.ip + ", id : " + this.id + ", userType : " + this.userType;
    }

}
