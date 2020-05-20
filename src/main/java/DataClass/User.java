package DataClass;

import HomomorphicEncryption.AGCDPublicKey;
import org.bson.Document;
import util.KeyGenerator;
import util.StringUtil;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

public class User {
    public Object _id; //get by mongoDB
    public String ip;
    public USERTYPE userType;
    public ArrayList<Contract> contractList = new ArrayList<>(); //체결중인 계약서 리스트
    public PrivateKey sigPrivateKey;
    public PublicKey sigPublicKey;
    public byte[] eciesPrivateKey;
    public byte[] eciesPublicKey;

    //HE에서 사용
    //private int qidRange = 100;
    private int rRange = 60;
    private int pkSize = 5;
    public BigInteger qid; //.pem파일에서 읽어왕!
    public String uid;
    public BigInteger id; //qid(100bit)생성시 고정값 80bit
    public BigInteger r; //data만들 때마다 랜덤으로 생성
    public Vector<AGCDPublicKey> pkSet = new Vector<>();
    private BigInteger au;
    KeyGenerator KG = new KeyGenerator();
    Random rand = new Random();
    public  User(){

    }
    public User(String ip, String uid, int type, ArrayList<String> idList) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException, IOException {
        this.ip = ip;
        this.uid = StringUtil.getSha256(uid);
        this.userType = type == 0 ? USERTYPE.EMPLOYER : USERTYPE.WORKER;
        setECDSAKeySet();
        setECIESKeySet();
        setQid(idList);
    }
    public User(Document d) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException, IOException {
        this._id = d.get("_id");
        this.id = new BigInteger(d.get("id").toString(),16);
        this.ip = d.get("ip").toString();
        this.uid = d.get("uid").toString();
        this.userType = d.getInteger("userType") == 0 ? USERTYPE.EMPLOYER : USERTYPE.WORKER;
//        this.eciesPublicKey = d.get("ECIESpk").toString().getBytes();
        setECDSAKeySet();
        setECIESKeySet();
        setQid(null);
    }
    void setECDSAKeySet() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        File keyFile = new File("ECDSAprivate.pem");
        if (!keyFile.exists()) {
            KG.makeECDSAKey();
        }
        this.sigPrivateKey = KG.readECDSAPrivateKeyFromPemFile("ECDSAprivate.pem");
        this.sigPublicKey = KG.readECDSAPublicKeyFromPemFile("ECDSApublic.pem");
    }

    //public 없애기
    public void setECIESKeySet() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        File keyFile = new File("ECIESprivate.pem");
        if (!keyFile.exists()) {
            KG.makeECIESKey();
        }
        this.eciesPrivateKey = KG.readECIESPrivateKeyFromPemFile("ECIESprivate.pem");
        this.eciesPublicKey = KG.readECIESPublicKeyFromPemFile("ECIESpublic.pem");
    }

    public String toString(){
        return "ip : "+ this.ip + ", id : " + this._id + ", userType : " + this.userType;
    }
    public void setContractList(ArrayList<Contract> cList){
        contractList = cList;
    }

    //HE에서 넘어옴
    public void setQid(ArrayList<String> idList) throws IOException {
        File qidFile = new File("qid.txt");
        FileWriter writer = null;
        BufferedWriter bWriter = null;
        BufferedReader bReader = null;
        if (!qidFile.exists()) { //서비스 이용 이력이 없는 유저 -> qid 생성해야함
            System.out.println("qidFile 없음");
            writer = new FileWriter(qidFile, false);
            bWriter = new BufferedWriter(writer);
            makeQid(idList);
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
                System.out.println("qid: "+this.qid);
            }
        }
    }
    public void setPKSet(Vector<AGCDPublicKey> pkSet){
        this.r = new BigInteger(rRange,rand);
        makeUserKeySet(pkSet);
    }
    void makeQid(ArrayList<String> idList){
        this.id = new BigInteger(80,rand); //80, 20은 임의로!
        for (int i = 0; i < idList.size(); i++) {
            BigInteger existId = new BigInteger(idList.get(i),16);
            if(this.id.equals(existId)){ //중복 발견
                this.id = new BigInteger(80,rand);
                i = -1; //0번쨰 index부터 다시 검사
            }
        }
        BigInteger randVal = new BigInteger(20,rand);
        this.qid = this.id.multiply(randVal);
    }

    public void ChangeUserR(){
        r = new BigInteger(rRange,rand);
    }

    void makeUserKeySet(Vector<AGCDPublicKey> pkSet){
        //KGC 서버를 따로 돌리면서, 100개쯤 PK쌍을 생성해놓으면 유저가 그것중에 랜덤으로 10개 취하는것도 좋을것 같음 !
        if(this.pkSet.isEmpty()) {
            this.pkSet.add(pkSet.firstElement());
            boolean usedpk[] = new boolean[pkSet.size()]; //default = false
            usedpk[0] = true; //x0 넣기
            while (this.pkSet.size() < pkSize) {
                int pknum = (int) (Math.random() * pkSet.size());
                if (usedpk[pknum]) continue;
                usedpk[pknum] = true;
                this.pkSet.add(pkSet.get(pknum));
            }
            System.out.println("user-selected pkSet's index : " + usedpk);
            for (int i = 0; i < this.pkSet.size(); i++) {
                if (i == 0) System.out.println("x0(hexadecimal) : " + this.pkSet.get(i).pk.toString(16));
                else System.out.println(i + "(hexadecimal) : " + this.pkSet.get(i).pk.toString(16));
            }
        }
    }

    public void setAu(BigInteger au){
        this.au = au;
    }
    public BigInteger getAu(){
        return au;
    }
}