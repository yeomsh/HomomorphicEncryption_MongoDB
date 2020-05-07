import DataClass.Database;
import GUI.MainFrame;
import HomomorphicEncryption.*;
import util.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionListener;
import java.net.UnknownHostException;
import java.util.*;

public class CSManager {
    public Scanner scan = new Scanner(System.in);

    protected ArrayList<String> ipList;
//    protected Vector<Client> cList;
//    protected static ArrayList<String> chainStr = new ArrayList<String>();
//    public static String PATH;
//    public FileManage chainFile;
//    protected Server server;
//    protected Vector<String> ableIp;
//    protected static PrivateKey privateKey = null;
//    protected static PublicKey publicKey = null;
//    protected static Boolean isVerifySuccess = true; // 검증 실패 성공 구분하는 변수
//    protected static int countPOW = 0;
//    protected static JSONObject block = new JSONObject(); // 작업증명시 timestamp 등등 해쉬
    protected String myIp = "";
    //JSONObject data = new JSONObject();
    protected static KeyGenerator KG;
    public MainFrame frame;
    public HomomorphicEncryption he;
    protected ArrayList<User> userList;
    //protected User myUser;
    protected DataClass.User user;
    public CSEventHandler mHandler;
    public Database db;
    public CSManager() throws UnknownHostException {
        //CSManager를 실행시키기위해서 필요한 setup들
        KG = new KeyGenerator();
        initKGCAndServer();
        initFrame();
        StartLog();
        //DataClass.Database dd = new NoSQLDB(); //부모는 자식을 품을 수 있는데, 자식은 부모를 품지 못함(자식에게만 있는 변수를 부모껄론 접근을 못하니까!?!)
        //제일 처음 로그인 및 회원가입 과정을 수행하는 함수
        initLogin();
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

    //KGC 및 서버 생성 (kgc는 나중에 데베에서 받아오는 형식으로,,,?)
    public void initKGCAndServer(){ //근데 he서버는 he동작을 할 떄 init을 하는게 낫지 않우까!?!?!?!? -승연
        //db 변수 init
        db= new Database();
        //server에서 noSQLDB생성 -> 근데 he와 연관없는 db작업이 필요해서 db변수를 만들었어 ! - 승연
        he = new HomomorphicEncryption();
        he.settingToStart();
    }

    //제일 처음 로그인 및 회원가입 과정을 수행하는 함수
    public void initLogin() throws UnknownHostException {
        //로그인하면 -> 로그인
        //회원가입하면, 키 발급, 아이디 발급, (kgc공개키는 키워드 등록할 때 받기(?)), 데베 등록
        myIp = mHandler.showInitDialog();
        frame.addLog("사용자의 Ip : "+myIp);

        ipList = db.getIpList();
        for (String ip: ipList)
            System.out.println(ip);
        //userList = he.server.getuList();
        //System.out.println("userList: "+userList.size());
        //for(User i : userList) frame.addLog(i.toString());

        //test용 변수 myIp에 없는 사용자로 등록하고 싶으면 임의의 ip주소 지정 후 사용
        //myIp = "192.192.192.0";
        if(ipList.contains(myIp)){
            //이미 등록된 사용
            user = db.getUser(myIp);
            frame.addLog("등록된 사용자 로그인 : " + user.toString());
            //qid바로 가져와서 user에 저장
            //setAu와 pkset정하는 것은 필요할 때 init()
        }
        else{
            JOptionPane.showMessageDialog(null,"회원가입을 진행합니다.","Message",JOptionPane.INFORMATION_MESSAGE);
            mHandler.showSignUpDialog();
            //ok버튼 누르면 리스너 작동
        }

    }
    public Vector<Contract> searchKeyword(String keyword, User userA){
        Vector<Contract> keywordFile = new Vector<>();
        keywordFile = he.searchKeyword(userA,keyword);
        return keywordFile;
    }

    public void loadContractData(){
        user.setContractList(db.getUserContractList(user.ip));
    }
    public void uploadUser(){
        db.insertUser(user);
    }
    public static void main(String[] args) throws UnknownHostException {
        CSManager t = new CSManager();
    }
}

