import Blockchain.BCManager;
import Blockchain.BCServer;
import DataClass.*;
import ECIES.ECIESManager;
import GUI.MainFrame;
import HomomorphicEncryption.*;
import com.mongodb.client.MongoCursor;
import org.bouncycastle.util.encoders.Base64;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import util.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.Timer;

public class CSManager {
    /*
     * 2020-05-23 추가해야할 내용:
     * 1) swing 끄면서 블록체인 서버 close하고 open했던거 다 꺼야함 !!
     *
     * */
    protected final ArrayList<String> chainStr = FileManager.readChainFile();
    protected String myIp = "";
    public MainFrame frame;
    protected User user;
    public CSEventHandler mHandler;
    public Database db;
    protected BCServer server;

    //testContract를 위한 데이터 SET
    ArrayList<String> first_nameStr;
    ArrayList<String> last_nameStr;
    ArrayList<String> storeStr;
    ECIESManager eciesManager = new ECIESManager();

    public KGC kgc;
    private BigInteger p; //서버의 개인키
    private BigInteger a; //서버alpha
    private ArrayList<CipherKeyword> arrCipherKeyword = new ArrayList<>();
    public CSManager(int i) throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException {
        this.user = new User();
        kgc = new KGC();
        p = kgc.getP();
        a = kgc.getSystemAlpha();
        readZindex();
    }
    private synchronized void readZindex() throws InterruptedException {
        System.out.println("readZindex 시작");
        int totalCount = (int) HEDataBase.keywordPEKS.countDocuments();
        int size = arrCipherKeyword.size();
        int count = (totalCount - size) / 100 + 1;
        ReadDBThread[] arrThread = new ReadDBThread[count];
        for (int i = 0; i < count; i++) {
            arrThread[i] = new ReadDBThread(size, i, new DataSource.LoadDBCallback() {
                @Override
                public void onDBLoaded(Document document) {
                    arrCipherKeyword.add(new CipherKeyword(document));
                }
            });
        }
        for (ReadDBThread thread : arrThread) {
            thread.join();
        }
        System.out.println("readZindex 끝");
    }
    public CSManager() throws Exception {
        //CSManager를 실행시키기위해서 필요한 setup들
        //데이터 set읽기
        //원래 생성자 코드
        initServer();
        initFrame();
        StartLog();
        startLogin();
    }

    public void initFrame() {
        frame = new MainFrame(); //BCManager 처럼 this를 넘겨줘서 handler처리도 가능하긴함 (default package 아닐경우)
        mHandler = new CSEventHandler(frame, this);
        frame.setListener((ActionListener) mHandler);
        frame.setListener((ChangeListener) mHandler);
    }

    //시작과정 로그로 남기기
    public void StartLog() {
        frame.addLog(
                "ContractSystemManager Start\n"
                        + "KeyGenerator 생성\n"
                        + "kgc 및 server 생성\n"
                        + "frame 및 Listener 생성"
        );
    }

    public void initServer() {
        //db 변수 init
        db = new Database();
        //블록체인 서버 OPEN
        server = new BCServer(3000, chainStr);
    }

    //제일 처음 로그인 및 회원가입 과정을 수행하는 함수
    public void startLogin() throws Exception {
        //로그인하면 -> 로그인
        //회원가입하면, 키 발급, 아이디 발급, (kgc공개키는 키워드 등록할 때 받기(?)), 데베 등록
        InetAddress ip = InetAddress.getLocalHost();
        myIp = ip.getHostAddress();
        //  myIp ="127.0.0.1";
        System.out.println("ip : " + myIp);
        String uid = mHandler.showInitDialog(myIp);
        System.out.println("사용자가 입력한 uid : " + uid + "\n 사용자의 ip : " + myIp);
        frame.addLog("사용자가 입력한 uid : " + uid + "\n 사용자의 ip : " + myIp);
        user = db.getUser(StringUtil.getSha256(uid), myIp, new DataSource.LoadDataCallback() {
            @Override
            public void onDataLoaded() throws Exception { //uid랑 ip랑 매칭x
                frame.addLog("잘못된 uid 입력");
                JOptionPane.showMessageDialog(null, "로그인 실패", "Message", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }

            @Override
            public void onDataFailed() { //회원가입
                // idList = db.getIdList();
                JOptionPane.showMessageDialog(null, "회원가입을 진행합니다.", "Message", JOptionPane.INFORMATION_MESSAGE);
                mHandler.showSignUpDialog();
            }

            @Override
            public void onDataLoaded(Vector<JSONObject> keywordFile) {

            }
        });
        if (user != null) { //(정상 로그인)uid랑 ip랑 매칭된 값을 입력했을 떄
            System.out.println(user);
        }
    }

    public void searchKeyword(String keyword, DataSource.LoadDataCallback callback) throws Exception {
        System.out.println("user private key: " + Base64.toBase64String(user.eciesPrivateKey));
        Vector<JSONObject> keywordFile = new Vector<>();
        getAlpha(new DataSource.LoadDataCallback() {
            @Override
            public void onDataLoaded() throws Exception {
                CipherData cipher = new CipherData(user.getC1(new BigInteger(StringUtil.SHA1(keyword), 16)), user.getC2(), user.getC3(), user.getAlpha(), user.pkSum);
                new HEClient("search", cipher, null, new DataSource.HECallback() {
                    @Override
                    public void onHESuccess(byte[][][] arr) throws Exception {
                        System.out.println("onHESuccess");
                        for (int i = 0; i < arr.length; i++) {
                            keywordFile.add(eciesManager.decryptCipherContract(arr[i][0], user.eciesPrivateKey, arr[i][1]));
                            System.out.println(keywordFile.lastElement().toJSONString());
                        }
                        callback.onDataLoaded(keywordFile);
                    }

                    @Override
                    public void onHESuccess(Object fileId) throws Exception {

                    }

                    @Override
                    public void onHESuccess(BigInteger alpha, BigInteger x0) throws Exception {

                    }

                    @Override
                    public void onHEfail() {

                    }
                });
            }

            @Override
            public void onDataFailed() {

            }

            @Override
            public void onDataLoaded(Vector<JSONObject> keywordFile) {

            }

        });
    }

    public void uploadContract(DataClass.Contract contract, Boolean isEmployer) throws Exception {
        getAlpha(new DataSource.LoadDataCallback() {
            @Override
            public void onDataLoaded() throws Exception {
                CipherData cipher = new CipherData(null, user.getC2(), user.getC3(), user.getAlpha(), user.pkSum);
//                new HEClient("uploadContract", cipher, contract, new DataSource.HECallback() {
//                    @Override
//                    public void onHESuccess(byte[][][] arr) {
//
//                    }
//
//                    @Override
//                    public void onHESuccess(Object fileId) throws Exception {
//                        System.out.println("onHESuccess: " + fileId);
//                        if (isEmployer) {
//                            String[] keywordArr = new String[]{((JSONObject) contract.fileData.get("oSign")).get("oSign1").toString(), contract.fileData.get("wName").toString()};
//                            CipherData[] cipherDatas = new CipherData[2];
//                            for (int i = 0; i < 2; i++) { //한 파일에 키워드가 2개니까 !
//                                user.changeR();
//                                cipherDatas[i] = new CipherData(user.getC1(new BigInteger(StringUtil.SHA1(keywordArr[i]), 16)), user.getC2(), user.getC3(), user.getAlpha(), user.pkSum);
//                            }
//                            new HEClient("uploadKeyword", cipherDatas, fileId, null);
//                        }
//                    }
//
//
//                    @Override
//                    public void onHESuccess(BigInteger alpha, BigInteger x0) {
//
//                    }
//
//                    @Override
//                    public void onHEfail() {
//
//                    }
//                });
                readZindex();
                ObjectId fileId = new ObjectId();
                if (isEmployer) {
                    String[] keywordArr = new String[]{((JSONObject) contract.fileData.get("oSign")).get("oSign1").toString(), contract.fileData.get("wName").toString()};
                    CipherData[] cipherDatas = new CipherData[2];
                    for (int i = 0; i < 2; i++) { //한 파일에 키워드가 2개니까 !
                        user.changeR();
                        cipherDatas[i] = new CipherData(user.getC1(new BigInteger(StringUtil.SHA1(keywordArr[i]), 16)), user.getC2(), user.getC3(), user.getAlpha(), user.pkSum);
                    }

                    HashMap<Object, Boolean> saveKeywordId = uploadKeyword(cipherDatas);
                    System.out.println("올린 키워드: " + saveKeywordId.keySet());
                    Boolean ret = HEDataBase.updateZString(saveKeywordId, fileId);

                }

            }

            @Override
            public void onDataFailed() {

            }

            @Override
            public void onDataLoaded(Vector<JSONObject> keywordFile) {

            }
        });
    }
    public BigInteger hash(BigInteger exponent) {
        return exponent;
    }
    public Boolean keywordTest(CipherData[] cipherDatas, CipherKeyword d2) {
        for (CipherData d1 : cipherDatas) {
            //분모
            BigInteger parent = d1.c1.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? d1.c1.mod(p).subtract(p) : d1.c1.mod(p);
            parent = hash(parent.mod(d1.getAlpha()));
            parent = parent.add(d2.c2);

            //분자
            BigInteger child = d2.c1.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? d2.c1.mod(p).subtract(p) : d2.c1.mod(p);
            child = hash(child.mod(a));
            child = child.add(d1.c2);
            if (parent.subtract(child).equals(BigInteger.ZERO)) {
                d1.isExist = true;
                return true;
            }
        }
        return false;
    }
    public HashMap<Object, Boolean> uploadKeyword(CipherData[] cipherDatas) throws InterruptedException {
        HashMap<Object, Boolean> uploadKeywordMap = new HashMap<>(); //<id, 기존 키워드 여부>
        Vector<Object> saveKeywordId = new Vector<>();
        //해당 키워드 찾기 및 키워드 추가
        long start = System.currentTimeMillis();
        for (CipherKeyword keyword : arrCipherKeyword) {
            if (keywordTest(cipherDatas, keyword)) {
                System.out.println("같은 키워드 발견!");
                uploadKeywordMap.put(keyword._id, true);
                saveKeywordId.add(keyword._id);
                if (saveKeywordId.size() == 2) { //둘다 찾았을 때
                    long end = System.currentTimeMillis();
                    System.out.println("upload keyword검색 시간> saveKeywordId.size() == 2 > " + (end - start));
                    return uploadKeywordMap;
                }
            }
        }
        //arrkeyword로 바꾸기!
        long end = System.currentTimeMillis();
        System.out.println("upload keyword> 검색 시간: " + (end - start));
        for (CipherData cipherData : cipherDatas) {
            if (!cipherData.isExist) { //존재하지 않는 것이 있다면 키워드 추가
                addSystemAlpha(cipherData);
                Object _id = HEDataBase.insertKeywordPEKS(cipherData); //키워드암호문 id
                saveKeywordId.add(_id);
                uploadKeywordMap.put(_id, false);
            }

        }
        return uploadKeywordMap;
    }
    public void addSystemAlpha(CipherData cipherData) { //user alpha 지우고, system alpha 입히기
        if (cipherData.c1 != null)
            cipherData.c1 = cipherData.makeCi(cipherData.c1.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? cipherData.c1.mod(p).subtract(p) : cipherData.c1.mod(p), a);
        if (cipherData.c3 != null)
            cipherData.c3 = cipherData.makeCi(cipherData.c3.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? cipherData.c3.mod(p).subtract(p) : cipherData.c3.mod(p), a);
    }
    public void getAlpha(DataSource.LoadDataCallback callback) throws Exception {
        long start = System.currentTimeMillis();
//        new HEClient("requestAlpha", new DataSource.HECallback() {
//            @Override
//            public void onHESuccess(byte[][][] arr) throws Exception {
//
//            }
//
//            @Override
//            public void onHESuccess(Object fileId) throws Exception {
//
//            }
//
//            @Override
//            public void onHESuccess(BigInteger alpha, BigInteger x0) throws Exception {
//                user.setPKSet();
//                user.setAlpha(alpha);
//                System.out.println("alpha: "+alpha);
//                user.setX0(x0);
//                System.out.println("x0:"+x0);
//                user.changeR();
//                callback.onDataLoaded();
//                long end = System.currentTimeMillis();
//                System.out.println("parameter setting 시간> "+(end-start));
//            }
//
//            @Override
//            public void onHEfail() {
//
//            }
//        });
        user.setPKSet();
        user.setAlpha(new BigInteger("117abf2fad07aca03c0baff461520f0a3818aa7709",16));
        user.setX0(new BigInteger("7fa46c8bf4cb795d3fec36ee958bfa512bc1a01bd4fa9e5551064fc4046a6468657446bf4b6882bd30906d2a49aed2c4098b70a31e9c5512f9d66a3728745daedd98726ac6b2b0e477b1fffade24d99729f394915fba8b4532c6c8c236e124c383af7eea13ad34ae5e4a75f4324ec00cc14001fda8ef2ddba25ef8c81fb2335902c29f2445d54e6e1a84ccc963fa7bdcc8974ddbb4eb42bd8f5415adf1e02b8bb264e39246f73fe04ea5a832bd6aca2e60de8e17a11e55318f4963b73ecea8402b1865509e52ac76b53c802784d4b34a08a59bfcf39323e740961f19da0407d0e1bfaa0bcb501025cafa4b4833c08ae4aa8a6b5d959766ea75527e1149583ccfa5f4692137a8288d639cc6b60ea421701889bac70345d641475385949d17bcce02a750b8d04d4255736155698a071e92e6e5a54c3eab71bcf814b1a7ff9456ecda967a206a71449ee66f7b951f730a708f00f62aff024e4e2c755e7a3780f3ee89f2a8704c49ce42a2980f7d98c67cd379d5fd0c61035cc3d84f23a6b03ec2be7b4aa5ba561d0138f08010df6b40b9303e660043ec109d19e299d409e20887867f2f3e55ee2fb930ec271ae2cb245b4a4dc8b003b4ed2684acfc53fb56afd1c55874208c2d75b168c82d9bb66dc1eafa576302ee219984c0c0c8967df88019d29c67f66b7347a213fb073797672551081128f4bd392c7b29cca8fa272c0f888927ff129da01ac0d175aba27b47df4fa2da050d75ee8b2b0cc449215daf9f630993c64e727282709ca32392d8d292bb2364e7dcce053e230f78edf2f094364cbdbbbfbdcc6f5ee971eeca4187e2ff53a338dc30a0df7c68a80a9dbff1e1baf739a3d59a8c1d6a00b36577f1106925b21ee93d571437e97dbf816e5433af0d815ab4751cd3cf4a21b1e70126f7f08a8944f2e873f4726b62e104aa0b0eb57bceb120fd3643769f89190edd9040789de91c4609f99800c7c67019504a57108caf6a37d650af7e73d69501bd3cfd32f04b4d1a566505a3df053c003d8c31ad9e7a882654869f0b5d9cc499b7c8fbb8e4621ec8dea6a50af60f512d4de3ed880a3ababdd268c99e6e6b7cabedf52589ac0589798c2e4b7e02595138df638ca454b5fcf18c8c95bd827d8a97872e03132d4b58a05a010171f596f0f5368e80aa6ddd5305736aaa915a6d03a0773c092432bf741a050835d990d450aa5ceb74c72ff9263501dc30365af2fed994c6d3199c99b701125c318c06c9135b81e8531ba5ef8cba47038e6f6c6311b4aafe3ded2c80e241667b9e0cef9d4ad69bf62290bb9e7e0afe412b5fdbba8cb9f3ff6d425572791a8b11bc000e093e265b1a5e1279ca042bb565da8fa3f7f0c4f9cb20378747466ffbd6398445d2407eee96f757472cd0d8ade6727c4bc967c89fc1c5827fea52080fc8328a35adeff88334f96caca61031928abb9021529f7e06e1265b047f5831d9b195087633ef1302775b27234e13a6f4457070d6c9ce479c66fec7115ff7b185a479d8cd602a62da8381837c21e5583f9c740d14fed54223103b2c2173189ae476eb5e32019a8cf347ec493d894f17614eb210b8d19655116bf477eb3a594334bb11a3c39944ea5be81077e56984fd631ee8129eb2eb449e17ed69fec69c94230a50aff5adc61098c444581503478037884cb36d670781ca1b70f350bae22a63e0984ca1aa3d7636271b5be6e9d21ae5208778fb74473c875c6a26948d7284e7d3305cd537d21ae33a103fe76449107e0f48c5294a69b289b15e0ae061be1e0f8d82ad3beded589a39c9f964682fc04036f1decaf608e11077d3eaab996201066e4d5510e810725dbfc1674db3f0a468314e2bb81c76717457c48fca7b99165621dd683b33b16a57ddf77684a01ba7c0342f9cb14a592cdd806762e09cbab8e823c2948708358d440a4682fb566d14c70c7cc16be96a1fab8a1642d60bca1382f2a27248327e82646f4b366e1a319184931b3b0c5ed65e59479581cb5166beda1ed4bab498c95de2d2c28eb0450b2aa7b6b3a3cf716fdac75918f8b21ae8aa80f85a659f89fd06c745b9119109d06146eace21e344a74f91892a3d2248c1a1e5bd69c2f2ef1fb265bd8c57f2600c533e0bb81000e7b37e953477edaf1dce85eddb51a055c6855fa14d7effe3fe6305f51fe9fc7239e0e0bd433f10428b224bcce8f4d951862c66bc47b683c4ed293d597f8e7688b7d5ad90acb24e3340944b698be09dc875bae8644b5aa4c8c03354bdaf59e77a6ccaa2ce1959ce57a17c3c02b90e619fb6406609f25ed5d4717e072f3234a01116874d4b5bb84104bf9cc64d2f8b98b60ba6093ced5b4e79a6c4bc6dee90b9a0c13818bbbabd77e4fcd98bbf12db7e66f1dc1382cd1f0570db019d266914487fade2fc5f584b314d69cf8473079124e1f0b2ea1f87454122fb9880308ca29877f41e74eba51602baff74e5d17e74ab5ff935d1e7cf7469c87390849a9fd9ba9551ecbed62b090765ad3e8fd7a2177ca5e8b36bc40bd638cf8025cc85029c9323627cc68894b7379a25186202bc511034766568f524ee101517bc214d0e889b76b0563732a8e7d1420280780ad5762acc60c57f2729c3223a2e0becd51bcee2f1b896f3f2c7be67ab7b7473d50f9a4ecccc8fab6e1ec973a5ab743a855a8fdcd09341cb9a7ed4de1542ad4731de8e9aad8f903e4f8b78fd61e97ea78a0bbb316eeb78ad76adc1cee0977f5920785090c6d11619c1b6041dd828426bd3be84946430b6bcb085fbc0597bd07ff6adf270f41f0cec2b3f5127d0c22296a61ab46e10500440ea3616620cfe366ba402580e980e3cc575d09d9f6d44c672cbbfa336c6ce38f81e65a4152d198789d392a92873aef311b7012176970572457e5ca6517e19c22ac173243ca41ed6ea87e1348793413b51ce848a6aaf3df3377d47ef7abe68f49fe8755b7491a51a3d59ae464b91d2abd71a7b9279149982a412786b11716f2910dd97335c60329a2989206f951a0ab69861593a43e90809b6a3eecfaaf934ec95f17802d99f78fad278234c2eb684850b89657fd8629385c92d9644add7dda067ca18a18accae29fca9104c4591618b378546cc73050724705c86d45549135a669950d06f12cc75e300bca22299aeee64749a5caf4c7bff81f34aa44bfc428cd3105267edb4d07bacc4e5208f33892ff76b91f5fc1cc20a1487d4c57016e36b783f53000d4f253ac9dc057839e48ba1377a5cd1418635e59d22274e2990c7190c456a0c982842bfa0faab8d37996de09cd119903b729d69512714bbfdbe58b7601b2e329cf2ea6bdf53cd2ea10dac101f8e427d6024211a81d5c99c5c854ffee2b1d7b3d9b373dd87d6809edb03c3531fd71ad622542b58746906da7ef907fe4fd016652f380306581ff6900c3d2d1fa78d2d7cee98899314a192e3897f9c2b3d48ec241fc385860ff2f92378e94741d23ea0a619689786c62ecd186c0a28daa866ebf6cc33bd4b84748ddcb572f3d5571b061eb2ccd1c7f1107eb1d9003e9ebba99f497749d4d45a5ede20da76ba5f8128d12ef01fb0cdc1eb63d545a1ba11990e021097fcecf1be63d316b98af1e8ecf4346cf8f990831b9e75660d504170430c5db8af5a4dd83665d58b057cc5e3f6cccb160f8931ad023bbb498008622caeaf7a792d2f9d6a4353949d27561ad4820a1241f852bd6c5518be9f8ecbd185ea13fadc7966078b73724812fda7a3dc0197ffc3dd164819a30f3812a007ca1dc8a12c18b2f753c0840d8e92d75500ab736615d3143a8a1eeac0ca71de1422f731f1d9187724beaee8e567aeaa91f3740d191fd4904c74bcb2085e8d4938b8f00d803f4e2777ea54118d0b829eff3e9d1ac73467f685e87e7e8c84c4bffeba76b7b0d69dcd38b8a953d6b503b4534b9df463ca129751528687730d27ca3d3314ab48fc39e7c1da9f1c806f7a1ea7f7a9c99ba50f95ef8ab549623f68a1978435c02ea92c4a5a60fd6442ed059baf8cd830ef607c849c9e151a48a2bc58901be5ed981c02fd93a3c837c24a2bdf0765a111ee1e6cdc327b5a7aff3baf76ad4883832b52a7fa21d5930f94c928995cea827c278a5b2ecfb24c12baba573e1364414386bcaa25f5ff1f881fb79ae9d16eb24724949bf50fbe5ea6e7b8a5ce3be66b2f2a31f360694bedb849696f3e3b0710c24dce5bcd17a682f7e233f1d1f022fe313ba617229bfafff4cec1259cf35b3e0bdc99465769b9c1b2ad7c7abd62d07a8f7f90643020f16b0fa24bdab79a82d0d1dfb840a1c67d6322112f9cb074524601ba6973cb420c06b2caea1285ef999674e1d1c24fabc2ba7677869e2488ceba8a1bfffe8ed3a3fa2618c362f02f5239bbca2309c6aebe0a80e4c2e54eeb6ea467b21b6f8ef3fc0e800bbf4476d139213d7c04e199eca358da3a6c5492b30bf90f9ea245e0fa639ea2a1113d75de1c3f61cd0da6932934f08759aa005632961f60831844ce3fc6d0410f4047266e1a33f1a6232c6dda1ab42e128d8ffab753965a4d54c82692d7e612ceb51887926cf8d100aae9fb26596a45c022e8112ca3cf99642d171456a4ba7e2e952192475edab78636cdafeed783405113a2ca5b8d44f2f6b61fbe32c4a2ec418fd05f77e5eb0abc88fa69681c3c72aeda194ba0525b25a137132191e611024e290d85c0ca46380703d64d214754505f57b62ae5a9c05615a83b89b3b070421acdb49edc20ec05f25233087611ba72edf3bfa4f220843f69b80d1dc57685f626b9f179d3127bd54f816bd7fd49c77d12564053d38c5e198e337645e8ba74c77c864ca51e2bee04272069e536ca01aa26dc9cc7182a6a6954dcd64c9162e63373aecb3808e2a157d0c3615b8f1c3afbc9c64b6c4d1c3dcbcd3f561ab12f4bbb8f7bd2fdf207eb76e73c347a99e68fb423fd42ba139090bb2f78618c0af57c2d8840d9da78d2bd2a04b0774f037cad50179867b3d2849ad4e63657ef280062a88894b12ea962608b1a3092af834eef53ebe72e9002c0d24101dd78ea782f61e8a3799acdbb7e982253e3134befa7f426adb54a7370b189b1cfda34d2902d0e1dad044d0371e0389e58ff1b3f4d306eff66c1d57fd8ecaf63acc9d74cdcf0316f5b170d4b58309db1db35195739ab472db11c535eecdd542a62838895f319f277add957ec8eff0483dbded3ecf404468e02f5ec69ac96b8e02041a51c3cba1defec62bb0e242aa224a58e816563a72fd083252b03f0d8b5f1c3e0eb2e404177b2825168e1c17bd34e9d85edda32259e4cb2bfe3abf9cce12dd64f03aa177686c998db8a72398ebb910184c955f144ffef2e576125bbab02c60f57fcc8491f3fd3a643617a2381a4e6b4520fa724a8d3103f8900b3dbed48569d637ae63491d2cca78e8183a4d8a01ca7eebd91f37a54096c9d726b6d2cb79f4f37611d77757aded82c7b09881ff60b1d476f733d89edb1a3423e5118ea1ca7fce607953db9f7fb81569238fc730f083360975df8109a99a37acc9df1128662b61d0aa76932ba7dc92fe43afcd59501a2c43b8ecd6dc8cff5be081b34314f000b1d7d66d49328463eebaf2198cae81137ff99c4668a4f33771886c5776c9fbbe6ac659c2b219b5eb3d49f07a1788a2cee7f8de2f8eabd65f74748ccec5699d342e8e660117a482323fc5a05610d880cbca4899cc589ad391fa8eedb7be07795afa02a30c2cd2755b7bbfab24864f42818c03192e7ce1215d740746b8225976730d2747a0b39913da779d66f71166591e9142f0322c8725e50f25b597a94cf240b0c98bac9c21f754aae1ea5f9b3666ef099bb50ca62ba3908fe966aa6b2886ffe994e4392999a31e66884bbc4df4c613564795e791e2c259762ea49f19ddbaaccdf82d84c9fb149270462001ca0c90e0954f7ee0ea94d62c88c48da4fba60028a8e5113bccbda9de3a218c4ff8d9c70b6922bbe2da06f90bc11b83012ec1eeb9fe419fcd1bad1c4e6c7bdc75a761d5056fceec84c2e2e435c8af7dc95d57c770779fc7fcde0cbdfeece7fa2aab431ae75c9acc02502e8c778ff5d1f4f4e11c934cfb5e06e9e5875b64699ab05cb8ec60b4c537a1935f04667eb02d38d13d97dea807ad69722db709f1e3056e2e74e1e41afab4b52bea6fb6ca824a237b5d450b546cfbb00ebc58128ded448024e5fbca0d97e26589a58052c9d3cb8a9c1e5d5839f1cdcd52a781227b3be2821d4b6db6cf996da19a4d252697bd4989750dfa6b1020714bba04b1100d5cad75564e2266a5628d73a52fc2a19ee9f6db53a5a02529d2465240f4508af6aaef62d991ff0eb66dfe1e4e4c0293eaca892a27c1faed194ae2d06d889eb2489bad6a2b7cfb05eb8b5044209b4a6e3dbd6f835749218796687326a5f3cd68499a9640a969bacc3209c39523ea1d4f4daf7cc9418a16006fd1b647828cf1ceda973957f93ee79e9a479e0864c5d66c222a8aa9eac2cfed2608fca2f41670044c0b0d23580e2e70a7c64b582d0f71e9b4892",16));
        user.changeR();
        callback.onDataLoaded();
    }

    public void loadContractData() throws ParseException {
        user.setContractList(db.getUserContractList(user.uid));
    }

    public void uploadUser(String uid, int userType) throws InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        user = new User(myIp, uid, userType);
        db.insertUser(user);
    }

    public void uploadUser() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException, IOException {
        db.insertUser(user);
    }

    public void readStoreAndName() {
        first_nameStr = FileManager.readFirst_name();
        last_nameStr = FileManager.readLast_name();
        storeStr = FileManager.readStore();
    }

    public JSONObject makeTestContract(String name1, String name2) {

        String randStore = name1;
        String randName = name2;

        System.out.println("설정된 상호명 : " + randStore + ", 이름 : " + randName);
        JSONParser p = new JSONParser();
        JSONObject testContract = null;
        try {
            testContract = (JSONObject) p.parse(
                    "{\"timer1\":{\"min\":0,\"hour\":0}," +
                            "\"insurance\":{\"국민연금\":false,\"건강보험\":false,\"고용보험\":false,\"산재보험\":false}," +
                            "\"wSign\":{\"wSign3\":\"\",\"wSign2\":\"\",\"wSign1\":\"\"}," +
                            "\"timer2\":{\"min\":0,\"hour\":0}," +
                            "\"timer3\":{\"min\":0,\"hour\":0}," +
                            "\"restDay\":{\"토\":false,\"월\":false,\"화\":false,\"수\":false,\"금\":false,\"목\":false,\"일\":false}," +
                            "\"contractDate\":{\"date\":1,\"month\":6,\"year\":2020}," +
                            "\"timer4\":{\"min\":0,\"hour\":0}," +
                            "\"contractTermEnd\":{\"date\":1,\"month\":6,\"year\":2020}," +
                            "\"workDay\":{\"토\":false,\"월\":false,\"화\":false,\"수\":false,\"금\":false,\"목\":false,\"일\":false}," +
                            "\"content\":\"\"," +
                            "\"oSign\":{\"oSign4\":\"\",\"oSign1\":\"" + randStore + "\",\"oSign2\":\"\",\"oSign3\":\"\"}," +
                            "\"wName\":\"" + randName + "\",\"contractTermStart\":{\"date\":1,\"month\":6,\"year\":2020}," +
                            "\"oName\":\"oname\",\"place\":\"\"," +
                            "\"wage\":{\"wage1\":\"\",\"wage4\":\"\",\"wage5\":{\"직접 지급\":false,\"근로자 명의 통장 입금\":false},\"wage2_2\":\"\",\"wage3_1\":{\"x\":false,\"o\":false},\"wage2_1\":{\"x\":false,\"o\":false},\"wage3_2\":\"\"}}"
            );
            System.out.println(testContract);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return testContract;
    }

    public JSONObject makeTestContract() {

        String randStore = storeStr.get((int) (Math.random() * storeStr.size()));
        String randName = last_nameStr.get((int) (Math.random() * last_nameStr.size()))
                + first_nameStr.get((int) (Math.random() * first_nameStr.size()));

        System.out.println("설정된 상호명 : " + randStore + ", 이름 : " + randName);
        JSONParser p = new JSONParser();
        JSONObject testContract = null;
        try {
            testContract = (JSONObject) p.parse(
                    "{\"timer1\":{\"min\":0,\"hour\":0}," +
                            "\"insurance\":{\"국민연금\":false,\"건강보험\":false,\"고용보험\":false,\"산재보험\":false}," +
                            "\"wSign\":{\"wSign3\":\"\",\"wSign2\":\"\",\"wSign1\":\"\"}," +
                            "\"timer2\":{\"min\":0,\"hour\":0}," +
                            "\"timer3\":{\"min\":0,\"hour\":0}," +
                            "\"restDay\":{\"토\":false,\"월\":false,\"화\":false,\"수\":false,\"금\":false,\"목\":false,\"일\":false}," +
                            "\"contractDate\":{\"date\":1,\"month\":6,\"year\":2020}," +
                            "\"timer4\":{\"min\":0,\"hour\":0}," +
                            "\"contractTermEnd\":{\"date\":1,\"month\":6,\"year\":2020}," +
                            "\"workDay\":{\"토\":false,\"월\":false,\"화\":false,\"수\":false,\"금\":false,\"목\":false,\"일\":false}," +
                            "\"content\":\"\"," +
                            "\"oSign\":{\"oSign4\":\"\",\"oSign1\":\"" + randStore + "\",\"oSign2\":\"\",\"oSign3\":\"\"}," +
                            "\"wName\":\"" + randName + "\",\"contractTermStart\":{\"date\":1,\"month\":6,\"year\":2020}," +
                            "\"oName\":\"oname\",\"place\":\"\"," +
                            "\"wage\":{\"wage1\":\"\",\"wage4\":\"\",\"wage5\":{\"직접 지급\":false,\"근로자 명의 통장 입금\":false},\"wage2_2\":\"\",\"wage3_1\":{\"x\":false,\"o\":false},\"wage2_1\":{\"x\":false,\"o\":false},\"wage3_2\":\"\"}}"
            );
            System.out.println(testContract);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return testContract;
    }


    public static void main(String[] args) throws Exception {
//        String dburl = "mongodb://id:pw@192.168.43.253:27017/mydb";
//        MongoClient mongoClient = new MongoClient("192.168.43.253",27017);

        //       CSManager t = new CSManager();

        //Genesis server 호출 코드
//        Server server = new Server(3000,FileManager.readChainFile()); //필수는 아니고 확인해보려고 넣은거얌
//        BCManager.chainStr = FileManager.readChainFile(); //필수
//        BCManager.block = new JSONObject(); //필수
//        GenesisServer gs = new GenesisServer();
//        gs.close(); //서버 꺼질 때 호출하면됨 -> 사실상 호출안된다고 보면 됨


        //성능 테스트용 코드
        CSManager t = new CSManager(1);
        t.readStoreAndName();
        //은하갤러리, 맹민석, 호키태권도장 ,림카페, 주성민, 해나라어린이집,이화미술교습소, 정서아
        JSONObject contract = t.makeTestContract("최승연", "써브웨이");
//        t.searchKeyword("화공방현", new DataSource.LoadDataCallback() {
//            @Override
//            public void onDataLoaded() throws Exception {
//
//            }
//
//            @Override
//            public void onDataFailed() {
//
//            }
//
//            @Override
//            public void onDataLoaded(Vector<JSONObject> keywordFile) {
//                System.out.println(keywordFile.firstElement().toJSONString());
//            }
//        });
        for(int j = 0;;j++) { //커넥션 max가 30개인듯함
            for (int i = 0; i < 30; i++) {
                contract = t.makeTestContract();
                Contract con = new Contract(contract, t.user);
                System.out.println("iv length" + con.IV.length);
                System.out.println("cipher: " + con.cipher.length);
                t.uploadContract(con, true);
                System.out.println("i : "+i + ", j: "+j);
            }

          //  Thread.sleep(50000);
        }

        //    t.uploadContract(con);
//        new HEClient("requestAlpha", new DataSource.HECallback() {
//            @Override
//            public void onHESuccess(byte[][][] arr) throws Exception {
//
//            }
//
//            @Override
//            public void onHESuccess(Object fileId) throws IOException {
//
//            }
//
//            @Override
//            public void onHESuccess(BigInteger alpha, BigInteger x0) throws Exception {
//
//                t.user.setAlpha(alpha);
//                t.user.setX0(new AGCDPublicKey(x0));
//
// //               t.uploadContract(con);
//  //              t.user.setPKSet();
//   //             t.user.setAlpha(t.he.KGC.shareAlpha());
//  //              t.user.setX0(t.he.KGC.checkX0Condition(t.user.x0,t.user.getAlpha()));
//  //              t.searchKeyword("유미세탁소");
////                CipherData cipher = new CipherData(null,t.user.getC2(), t.user.getC3(),t.user.getAlpha(),t.user.pkSum);
////                new HEClient("uploadContract", cipher, con, new DataSource.HECallback() {
////
////                        @Override
////                        public void onHESuccess(byte[][][] arr) {
////
////                        }
////
////                        @Override
////                        public void onHESuccess(Object fileId) throws Exception {
////                            System.out.println("onHESuccess: " + fileId);
////                            String[] keywordArr = new String[]{((JSONObject) con.fileData.get("oSign")).get("oSign1").toString(), con.fileData.get("wName").toString()};
////                            CipherData[] cipherDatas = new CipherData[2];
////                            for (int i = 0; i < 2; i++) { //한 파일에 키워드가 2개니까 !
////                                t.user.ChangeUserR();
////                                cipherDatas[i] =  new CipherData(t.user.getC1(new BigInteger(StringUtil.SHA1(keywordArr[i]),16)),t.user.getC2(),t.user.getC3(),t.user.getAlpha(),t.user.pkSum);
////                            }
////                            new HEClient("uploadKeyword", cipherDatas, fileId, null);
////                            t.user.setPKSet();
////                            t.user.setAlpha(t.he.KGC.shareAlpha());
////                            t.user.setX0(t.he.KGC.checkX0Condition(t.user.x0,t.user.getAlpha()));
////                            t.searchKeyword(keywordArr[0]);
////                        }
////
////                        @Override
////                        public void onHESuccess(BigInteger alpha, BigInteger x0) {
////
////                        }
////
////                        @Override
////                        public void onHEfail() {
////
////                        }
////                    });
//                CipherData cipher = new CipherData(t.user.getC1(new BigInteger(StringUtil.SHA1("만리동커피"),16)),t.user.getC2(),t.user.getC3(),t.user.getAlpha(),t.user.pkSum);
//                new HEClient("search", cipher, con, new DataSource.HECallback() {
//                    @Override
//                    public void onHESuccess(byte[][][] arr) throws Exception {
//                        System.out.println("onHESuccess");
//                        for (int i = 0; i < arr.length ; i++) {
//                            System.out.println(i+"번째: ");
//                            ECIESManager eciesManager = new ECIESManager();
//                            System.out.println("arr[i][0]"+ arr[i][0].length); //cipher
//                            System.out.println("arr[i][1]"+ arr[i][1].length); //iv
//                            System.out.println(t.user.eciesPrivateKey);
//                            JSONObject obj = eciesManager.decryptCipherContract(arr[i][0], t.user.eciesPrivateKey,arr[i][1]);
//                            System.out.println(obj.toJSONString());
//                        }
//                    }
//
//                    @Override
//                    public void onHESuccess(Object fileId) throws Exception {
//
//                    }
//
//                    @Override
//                    public void onHESuccess(BigInteger alpha, BigInteger x0) throws Exception {
//
//                    }
//
//                    @Override
//                    public void onHEfail() {
//
//                    }
//                });
//
//            }
////
//            @Override
//            public void onHEfail() {
//
//            }
//        });
//                // t.he.setUserPKSet(t.user);
//
//                //여기서부터반복
//                //랜덤 데이터 넣어서 test용 contract 만들기
//
//
//
//      //  t.searchKeyword("해마로생닭");
    }
}

class GenesisServer extends TimerTask {
    //교수님 컴퓨터에서 돌릴 것임
    //여기서 생성되는 체인도 교수님 컴퓨터(서버)에 저장될 것임
    Timer jobScheduler = new Timer();

    public GenesisServer() {
        System.out.println("init GenesisServer");
        jobScheduler.scheduleAtFixedRate(this, 0, 30000); //호출로부터 0초후에 30s간격으로 task 함수(run함수) 호출 -> 실제로는 10분으로 바꾸면 됨
    }

    public void run() {
        System.out.println("GenesisServer: run");
        ArrayList<String> ipList = new ArrayList<>();
        ipList.add("192.168.56.1"); //확인을 위해 내 아이피하나 넣어뒀어 ! 상히가 확인해보고 싶으면 상히 ip넣어두면도ㅐ
        BCManager bcManager = new BCManager(ipList);
        try {
            bcManager.chainUpdate();
            bcManager.proofOfWork(StringUtil.randomString());
            if (bcManager.broadCastBlock()) { //작업증명에 성공하면 -> 임시서버에서 지우고 -> 키워드 업로드
                System.out.println("브로드 캐스트에서 작업증명결과가 옳다고 나옴-> 성공");
            } else { //실패하면 그냥 끝
                System.out.println("브로드 캐스트에서 작업증명결과가 옳지않다고 나옴 -> 실패");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void close() {
        jobScheduler.cancel();
    }

}

class ReadDBThread extends Thread {
    int idx;
    int start;
    int size = 100;
    private boolean stop;
    DataSource.LoadDBCallback callback;

    public ReadDBThread(int start, int idx, DataSource.LoadDBCallback callback) {
        this.idx = idx * size;
        this.callback = callback;
        this.stop = false;
        this.start = start;
        this.start();
    }

    @Override
    public void run() {
        MongoCursor<Document> cursor = HEDataBase.keywordPEKS.find().skip(start + idx).limit(size).iterator();
        while (cursor.hasNext() && !stop) {
            callback.onDBLoaded(cursor.next());
        }
        cursor.close();
    }

    public void setStop(Boolean flag) {
        this.stop = flag;
    }
}