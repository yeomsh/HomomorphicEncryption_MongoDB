package Blockchain;

import GUI.MainFrame;
import HomomorphicEncryption.*;
import org.json.simple.JSONObject;
import util.*;

import javax.swing.*;
import java.awt.event.*;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

public class CSManager {
    public Scanner scan = new Scanner(System.in);

    protected Vector<String> ipList = new Vector<>();
    protected Vector<Client> cList;
    protected static ArrayList<String> chainStr = new ArrayList<String>();
    public static String PATH;
    public FileManage chainFile;
    protected Server server;
    protected Vector<String> ableIp;
    protected static PrivateKey privateKey = null;
    protected static PublicKey publicKey = null;
    protected static Boolean isVerifySuccess = true; // 검증 실패 성공 구분하는 변수
    protected static int countPOW = 0;
    protected static JSONObject block = new JSONObject(); // 작업증명시 timestamp 등등 해쉬값
    //protected static MyFrame frame;
    protected String myIp = "";

    JSONObject data = new JSONObject();
    protected static KeyGenerator KG;
    public MainFrame frame;
    public HomomorphicEncryption he;

    protected ArrayList<User> userList;
    protected User myUser;

    CMActionListener mListener;

    public CSManager() throws UnknownHostException {
        //CSManager를 실행시키기위해서 필요한 setup들
        KG = new KeyGenerator();
        setKGCAndServer();
        setFrameAndListener();
        StartLog();

        //제일 처음 로그인 및 회원가입 과정을 수행하는 함수
        initLogin();

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
    public void setKGCAndServer(){
        //server에서 noSQLDB생성
        he = new HomomorphicEncryption();
        he.settingToStart();
    }

    //MainFrame 및 버튼에 리스너 달기
    public void setFrameAndListener(){
        frame = new MainFrame();
        mListener = new CMActionListener(frame, this);
        //버튼에 리스너들 달기
        frame.mpNew.button.addActionListener(mListener);
        frame.mpContinue.button.addActionListener(mListener);
        frame.mpSearch.button.addActionListener(mListener);
        frame.signUpDialog.okButton.addActionListener(mListener);
        frame.addWindowListener(mListener);
    }

    //제일 처음 로그인 및 회원가입 과정을 수행하는 함수
    public void initLogin() throws UnknownHostException {
        //로그인하면 -> 로그인
        //회원가입하면, 키 발급, 아이디 발급, (kgc공개키는 키워드 등록할 때 받기(?)), 데베 등록
        myIp = frame.showInitDialog();
        frame.addLog("사용자의 Ip : "+myIp);
        userList = he.server.getuList();
        for(User i : userList) frame.addLog(i.toString());

        for(User i : userList){
            ipList.add(i.ip);
        }

        //test용 변수 myIp에 없는 사용자로 등록하고 싶으면 임의의 ip주소 지정 후 사용
        //myIp = "192.192.192.0";
        if(ipList.contains(myIp)){
            //이미 등록된 사용
            for(User i : userList){
                if(i.ip.equals(myIp)) {
                    myUser = i;
                    break;
                }
            }
            frame.addLog("등록된 사용자 로그인 : " + myUser.toString());
        }
        else{
            JOptionPane.showMessageDialog(null,"회원가입을 진행합니다.","Message",JOptionPane.INFORMATION_MESSAGE);
            frame.showSignUpDialog();
            //ok버튼 누르면 리스너 작동
        }

    }


    public static void main(String[] args) throws UnknownHostException {
        CSManager t = new CSManager();
    }
}


class CMActionListener implements ActionListener, WindowListener {

    private MainFrame frame;
    private CSManager csManager;

    public CMActionListener(MainFrame frame, CSManager csManager) {
        this.frame = frame;
        this.csManager = csManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == frame.mpNew.button){
            System.out.println("mpNew");

        }
        else if(source == frame.mpContinue.button) {
            System.out.println("mpContinue");

        }
        else if(source == frame.mpSearch.button) {
            //frame.showKeywordDialog();
            System.out.println("mpSearch");

        }

        else if(source == frame.signUpDialog.okButton) {
            System.out.println("signUpDialog");
            String userType = null;
            if(frame.signUpDialog.userType[0].isSelected())
                userType = "점주";
            else if(frame.signUpDialog.userType[1].isSelected())
                userType = "근로자";

            csManager.myUser = new User(csManager.myIp,userType);
            JOptionPane.showMessageDialog(null, userType+"로 회원가입 완료했습니다.", "Message", JOptionPane.INFORMATION_MESSAGE);

            frame.signUpDialog.setVisible(false);
            frame.addLog("사용자 회원가입 완료 : " + csManager.myUser.toString());
        }

    }

    @Override
    public void windowOpened(WindowEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void windowClosing(WindowEvent e) {
        // TODO Auto-generated method stub
//        if (tm.server.isAlive())
//            tm.server.stop();
//        tm.server.close();
//        System.out.println("프로그램을 끝냅니다");
//        System.exit(1);
    }

    @Override
    public void windowClosed(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowIconified(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowActivated(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        // TODO Auto-generated method stub

    }

}