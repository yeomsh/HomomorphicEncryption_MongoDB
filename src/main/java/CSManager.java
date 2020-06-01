import Blockchain.BCManager;
import Blockchain.Server;
import DataClass.DataSource;
import DataClass.Database;
import DataClass.User;
import GUI.MainFrame;
import HomomorphicEncryption.*;
import org.bouncycastle.util.encoders.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import util.*;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
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
    //protected ArrayList<String> ipList;
    public ArrayList<String> idList;
    protected final ArrayList<String> chainStr = FileManager.readChainFile();
    protected String myIp = "";
    protected KeyGenerator KG;
    public MainFrame frame;
    public HEManager he;
    protected User user;
    public CSEventHandler mHandler;
    public Database db;
    protected Server server;

    //testContract를 위한 데이터 SET
    ArrayList<String> first_nameStr;
    ArrayList<String> last_nameStr;
    ArrayList<String> storeStr;


    public CSManager() throws Exception {
        //CSManager를 실행시키기위해서 필요한 setup들

        //데이터 set읽기
        readStoreAndName();
        //랜덤 데이터 넣어서 test용 contract 만들기
        makeTestContract();

        initKGCAndServer();
        initFrame();
        StartLog();
        //제일 처음 로그인 및 회원가입 과정을 수행하는 함수
        initLogin();
        //DataClass.Database dd = new NoSQLDB(); //부모는 자식을 품을 수 있는데, 자식은 부모를 품지 못함(자식에게만 있는 변수를 부모껄론 접근을 못하니까!?!)


    }
    public void initFrame(){
        frame = new MainFrame(); //BCManager 처럼 this를 넘겨줘서 handler처리도 가능하긴함 (default package 아닐경우)
        mHandler = new CSEventHandler(frame,this);
        frame.setListener((ActionListener) mHandler);
        frame.setListener((ChangeListener) mHandler);
    }

    //시작과정 로그로 남기기
    public void StartLog(){
        frame.addLog(
                "ContractSystemManager Start\n"
                        + "KeyGenerator 생성\n"
                        +"kgc 및 server 생성\n"
                        +"frame 및 Listener 생성"
        );
    }
//    public void readChain() { // chain text 가져옴
//        try {
//            chainStr = FileManage.fileLineRead();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
    //KGC 및 서버 생성 (kgc는 나중에 데베에서 받아오는 형식으로,,,?)
    public void initKGCAndServer() { //근데 he서버는 he동작을 할 떄 init을 하는게 낫지 않우까!?!?!?!? -승연
        //db 변수 init
        KG = new KeyGenerator();
        db= new Database();
        //server에서 noSQLDB생성 -> 근데 he와 연관없는 db작업이 필요해서 db변수를 만들었어 ! - 승연
        //블록체인 서버 OPEN
        server = new Server(3000,chainStr);
    }
    public void setHE(){
        he = new HEManager(user);
    }
//8c08c74b3ade39532c5e347a8bb94291b558086d3b24e332fe1a1fa92469edbb
//e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
    //제일 처음 로그인 및 회원가입 과정을 수행하는 함수
    public void initLogin() throws Exception {
        //로그인하면 -> 로그인
        //회원가입하면, 키 발급, 아이디 발급, (kgc공개키는 키워드 등록할 때 받기(?)), 데베 등록
        InetAddress ip = InetAddress.getLocalHost();
        myIp = ip.getHostAddress();
        System.out.println("ip : " + myIp);
        String uid = mHandler.showInitDialog(myIp);
        System.out.println("사용자가 입력한 uid : "+uid + "\n 사용자의 ip : " + myIp);
        frame.addLog("사용자가 입력한 uid : "+uid + "\n 사용자의 ip : " + myIp);
        user = db.getUser(StringUtil.getSha256(uid), myIp, new DataSource.Callback() {
            @Override
            public void onDataLoaded() throws Exception { //uid랑 ip랑 매칭x
                frame.addLog("잘못된 uid 입력");
                JOptionPane.showMessageDialog(null, "로그인 실패", "Message", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }
            @Override
            public void onDataFailed() { //회원가입
                idList = db.getIdList();
                JOptionPane.showMessageDialog(null,"회원가입을 진행합니다.","Message",JOptionPane.INFORMATION_MESSAGE);
                mHandler.showSignUpDialog();
            }
        });
        if(user != null){ //(정상 로그인)uid랑 ip랑 매칭된 값을 입력했을 떄
            System.out.println(user);
            setHE();
        }
    }
    public Vector<JSONObject> searchKeyword(String keyword) throws Exception {
        System.out.println("user private key: "+Base64.toBase64String(user.eciesPrivateKey));
        he.setUserPKSet(user);
        return he.searchKeyword(user,keyword);
    }
    public void uploadContract(DataClass.Contract contract) throws Exception {
        he.setUserPKSet(user);
        he.requestToUpload(user,contract);
    }
    public void loadContractData() throws ParseException {
        user.setContractList(db.getUserContractList(user.uid));
    }
    public void uploadUser() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException, IOException {
        System.out.println(user.uid);
        System.out.println(user.id);
        db.insertUser(user);
    }

    public void readStoreAndName(){
        FileManager fileManager = new FileManager();

        first_nameStr = fileManager.readFirst_name();
        last_nameStr = fileManager.readLast_name();
        storeStr = fileManager.readStore();
    }
    public void makeTestContract(){

        String randStore = storeStr.get((int)(Math.random()*storeStr.size()));
        String randName = last_nameStr.get((int) (Math.random()*last_nameStr.size()))
                + first_nameStr.get((int) (Math.random()*first_nameStr.size()));

        System.out.println("설정된 상호명 : "+  randStore + ", 이름 : " + randName);
        JSONParser p = new JSONParser();
        try {
            JSONObject testContract = (JSONObject)p.parse(
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
    }


    public static void main(String[] args) throws Exception {
//        String dburl = "mongodb://id:pw@192.168.43.253:27017/mydb";
//        MongoClient mongoClient = new MongoClient("192.168.43.253",27017);

        CSManager t = new CSManager();

        //Genesis server 호출 코드
//        Server server = new Server(3000,FileManager.readChainFile()); //필수는 아니고 확인해보려고 넣은거얌
//        BCManager.chainStr = FileManager.readChainFile(); //필수
//        BCManager.block = new JSONObject(); //필수
//        GenesisServer gs = new GenesisServer();
//        gs.close(); //서버 꺼질 때 호출하면됨 -> 사실상 호출안된다고 보면 됨
    }
}

class GenesisServer extends TimerTask  {
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
            if(bcManager.broadCastBlock()){ //작업증명에 성공하면 -> 임시서버에서 지우고 -> 키워드 업로드
                System.out.println("브로드 캐스트에서 작업증명결과가 옳다고 나옴-> 성공");
            }
            else { //실패하면 그냥 끝
                System.out.println("브로드 캐스트에서 작업증명결과가 옳지않다고 나옴 -> 실패");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void close(){
        jobScheduler.cancel();
    }

}


