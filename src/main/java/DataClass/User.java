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
    public BigInteger pkSum;
    private BigInteger alpha;
    public AGCDPublicKey x0 = null;
    KeyGenerator KG = new KeyGenerator();
    Random rand = new Random();

    public User() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeySpecException, IOException {
        this.ip = "192.168.56.1"; //이거 바꿔서 상희꺼쓰면 될듯행
        this.uid = StringUtil.getSha256("bb");
        this.userType = USERTYPE.EMPLOYER;
        setECDSAKeySet();
        setECIESKeySet();
        setQid();
        this.r = new BigInteger(rRange, rand);
    }

    public User(String ip, String uid, int type) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException, IOException {
        this.ip = ip;
        this.uid = StringUtil.getSha256(uid);
        this.userType = type == 0 ? USERTYPE.EMPLOYER : USERTYPE.WORKER;
        setECDSAKeySet();
        setECIESKeySet();
        setQid();
    }

    public User(Document d) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException, IOException {
        this._id = d.get("_id");
        this.id = new BigInteger(d.get("id").toString(), 16);
        this.ip = d.get("ip").toString();
        this.uid = d.get("uid").toString();
        this.userType = d.getInteger("userType") == 0 ? USERTYPE.EMPLOYER : USERTYPE.WORKER;
//        this.eciesPublicKey = d.get("ECIESpk").toString().getBytes();
        setECDSAKeySet();
        setECIESKeySet();
        setQid();
    }

    void setECDSAKeySet() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        File keyFile = new File("ECDSAprivate.pem");
        if (!keyFile.exists()) {
            KG.makeECDSAKey();
            this.sigPrivateKey = KG.readECDSAPrivateKeyFromPemFile("ECDSAprivate.pem");
            this.sigPublicKey = KG.readECDSAPublicKeyFromPemFile("ECDSApublic.pem");
        } else {
            this.sigPrivateKey = KG.readECDSAPrivateKeyFromPemFile("ECDSAprivate.pem");
            this.sigPublicKey = KG.readECDSAPublicKeyFromPemFile("ECDSApublic.pem");
        }
    }

    //public 없애기
    public void setECIESKeySet() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        File keyFile = new File("ECIESprivate.pem");
        if (!keyFile.exists()) {
            KG.makeECIESKey();
            this.eciesPrivateKey = KG.readECIESPrivateKeyFromPemFile("ECIESprivate.pem");
            this.eciesPublicKey = KG.readECIESPublicKeyFromPemFile("ECIESpublic.pem");
        } else {
            this.eciesPrivateKey = KG.readECIESPrivateKeyFromPemFile("ECIESprivate.pem");
            this.eciesPublicKey = KG.readECIESPublicKeyFromPemFile("ECIESpublic.pem");
        }

    }

    public String toString() {
        return "ip : " + this.ip + ", id : " + this._id + ", userType : " + this.userType;
    }

    public void setContractList(ArrayList<Contract> cList) {
        contractList = cList;
    }

    public void setX0(AGCDPublicKey x0) {
        this.pkSet.remove(0);
        this.pkSet.add(0, x0);
        this.x0 = x0;
    }

    public void setX0(BigInteger x0) {
        this.x0.pk= x0;
    }
    //HE에서 넘어옴
    public void setQid() throws IOException {
        File qidFile = new File("qid.txt");
        FileWriter writer = null;
        BufferedWriter bWriter = null;
        BufferedReader bReader = null;
        if (!qidFile.exists()) { //서비스 이용 이력이 없는 유저 -> qid 생성해야함
            System.out.println("qidFile 없음");
            writer = new FileWriter(qidFile, false);
            bWriter = new BufferedWriter(writer);
            makeQid();
            bWriter.write(this.qid.toString(16));
            bWriter.flush();
        } else { //qid.txt 있으면 기존에 서비스 이용 이력 1회 이상
            System.out.println("qidFile 있음");
            String s;
            File file = new File("qid.txt");
            bReader = new BufferedReader(new FileReader(file));
            // 더이상 읽어들일게 없을 때까지 읽어들이게 합니다.
            while ((s = bReader.readLine()) != null) {
                this.qid = new BigInteger(s, 16);
                System.out.println("qid: " + this.qid);
            }
        }
    }

    public void setPKSet() {
        this.pkSet = Database.getRandomPublicKeySet(pkSize);
        BigInteger sumPk = BigInteger.ZERO;
        for (AGCDPublicKey AGCDPublicKey : this.pkSet) {
            sumPk = sumPk.add(AGCDPublicKey.pk);
        }
        this.pkSum = sumPk.subtract(this.pkSet.firstElement().pk);
        this.x0 = this.pkSet.firstElement();
    }
    void makeQid() {
        this.id = new BigInteger(80, rand); //80, 20은 임의로!
        ArrayList<String> idList = Database.getIdList();
        for (int i = 0; i < idList.size(); i++) {
            BigInteger existId = new BigInteger(idList.get(i), 16);
            if (this.id.equals(existId)) { //중복 발견
                this.id = new BigInteger(80, rand);
                i = -1; //0번쨰 index부터 다시 검사
            }
        }
        BigInteger randVal = new BigInteger(20, rand);
        this.qid = this.id.multiply(randVal);
    }

    public void changeR() {
        r = new BigInteger(rRange, rand);
    }

    public void setAlpha(BigInteger alpha) {
        this.alpha = alpha;
    }

    public BigInteger getAlpha() {
        return alpha;
    }

    public BigInteger getC1(BigInteger w) {

        // AGCDPublicKey x00 = HEManager.KGC.checkX0Condition(x0,alpha);
        BigInteger c1 = w.add(r.multiply(qid)).add(alpha.multiply(pkSum)); //w+(user.r*user.qid)+(a*sumPk);
        //  c1 = c1.mod(x00.pk).compareTo(x00.pk.divide(BigInteger.TWO))>0 ? c1.mod(x00.pk).subtract(x00.pk) : c1.mod(x00.pk);
        c1 = c1.mod(x0.pk).compareTo(x0.pk.divide(BigInteger.TWO)) > 0 ? c1.mod(x0.pk).subtract(x0.pk) : c1.mod(x0.pk);
        return c1;
    }

    public static BigInteger hash(BigInteger exponent) {
        //return BigInteger.valueOf(2).pow(exponent.intValue());
        return exponent;
    }

    public BigInteger getC2() {
        // System.out.println("riqid : " +user.r.multiply(user.qid));
        return hash(r.multiply(qid));
        //System.out.println("c2(2^hexadecimal): 2^"+c2.toString(16));
    }

    public BigInteger getC3() {
        //ci1계산하기 (mod범위에 맞추어서)
        //AGCDPublicKey x00 = HEManager.KGC.checkX0Condition(x0,alpha);
        BigInteger c3 = qid.add(r.multiply(qid)).add(alpha.multiply(pkSum)); //user.qid+(user.r*user.qid)+(a*sumPk);
        c3 = c3.mod(x0.pk).compareTo(x0.pk.divide(BigInteger.TWO)) > 0 ? c3.mod(x0.pk).subtract(x0.pk) : c3.mod(x0.pk);
        //  c3 = c3.mod(x00.pk).compareTo(x00.pk.divide(BigInteger.TWO))>0 ? c3.mod(x00.pk).subtract(x00.pk) : c3.mod(x00.pk);
        return c3;
    }

}