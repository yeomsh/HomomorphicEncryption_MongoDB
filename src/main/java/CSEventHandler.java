import Blockchain.BCManager;
import DataClass.DataSource;
import DataClass.User;
import GUI.ContractGUI;
import GUI.MainFrame;
import HomomorphicEncryption.CipherContract;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Vector;

public class CSEventHandler implements ActionListener, ChangeListener, WindowListener {

    private MainFrame frame;
    private CSManager manager;
    private Vector<JSONObject> keywordFile = new Vector<>();

    public CSEventHandler(MainFrame frame, CSManager manager) {
        this.frame = frame;
        this.manager = manager;
    }
    public void showIpDialog() throws Exception {
        String name = JOptionPane.showInputDialog("계약할 사람의 uid를 입력하세요.");
//		ipDialog.okButton.addActionListener(this);
//		ipDialog.setVisible(true);
        if(name==null)
            JOptionPane.showMessageDialog(null, "계약서 작성을 취소합니다.", "Message", JOptionPane.INFORMATION_MESSAGE);
        else {
            new BCManager(manager.db,name); //새로하기니까 무조건 1단계
        }
    }
    public void showSignUpDialog() {
        frame.signUpDialog.setVisible(true);
    }
    public String showKeywordDialog() {
        return JOptionPane.showInputDialog("검색할 키워드를 입력하세요.");
    }
    public String showInitDialog(String myIp) throws UnknownHostException {
//        InetAddress ip = InetAddress.getLocalHost();
//        String myIp = ip.getHostAddress();
        //myIp = "127.0.0.1";
//        int select = JOptionPane.showConfirmDialog(null,"my Ip : "+myIp,"로그인",JOptionPane.OK_CANCEL_OPTION);
        String uid = JOptionPane.showInputDialog("myIP : " + myIp + "\n아이디(uid) : ");
        System.out.println(uid);
        if(uid == null) {
            JOptionPane.showMessageDialog(null, "프로그램을 종료합니다.", "Message", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }
        //ip확인후 없으면 else내용 진행
//		else
//			JOptionPane.showMessageDialog(null,"회원가입을 진행합니다.","Message",JOptionPane.INFORMATION_MESSAGE);
//		//if(name.equals("true"))
//			showSignUpDialog();

//		initDialog.okButton.addActionListener(this);
//		initDialog.setVisible(true);
        //ip tf채워넣기
        return uid;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == frame.mpNew.button){
            System.out.println("mpNew");
            try {
                showIpDialog();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        else if(source == frame.mpContinue.button) {
            if(!manager.user.contractList.isEmpty()) {
                int index = frame.mpContinue.comboBoxContract.getSelectedIndex();
                BCManager.chainStr = manager.chainStr;
                if (manager.user.contractList.get(index).step == 4) {
                    DataClass.Contract contract = manager.user.contractList.get(index);
                    try {
                        new BCManager(manager.user, manager.db, manager.ipList, contract
                                , new DataSource.Callback() {
                            @Override //HE 작업하기
                            public void onDataLoaded(){

                                manager.uploadContract(contract);
                            }
                            @Override //그냥 끝내기
                            public void onDataFailed() {

                            }
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    try {
                        new BCManager(manager.user, manager.db, manager.ipList,manager.user.contractList.get(index));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        else if(source == frame.mpSearch.button) {
            //Vector<JSONObject> keywordFile = new Vector<>();
            keywordFile.clear();
            //manager.user.qid = new BigInteger("cb066fe11fed84bc5dcb04bbb", 16);

            //csManager.he.requestToUpload(userA,new String[]{"a","c"});
            String keyword = showKeywordDialog();
            frame.addLog("검색할 키워드 : " + keyword);
            if(keyword!=null){
                try {
                    keywordFile = manager.searchKeyword(keyword,manager.user);
                } catch (ParseException ex) {
                    ex.printStackTrace();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                for(JSONObject i : keywordFile)
                    frame.addLog(i.toString() + "\n file : " + i);
                //데이터 받아온거 뿌리기
                frame.mpSearch.setComboBoxContract(keywordFile);
                try {
                    manager.loadContractData();
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
                //키워드 검색하기
                //검색끝나면 파일 보여주는 항목 update한 후 보여주기
                //뭔가 콤보박스 선택못하게 하거나 안보이게 한 후 파일 다 받아온 다음에 쓸 수 있게
            }
            System.out.println("mpSearch");
        }
        else if(source == frame.mpSearch.button2) {
            int index = frame.mpSearch.comboBoxContract.getSelectedIndex();//
            if(index == -1)
                JOptionPane.showMessageDialog(null, index + " : 계약서 선택하지 않음", "Message", JOptionPane.INFORMATION_MESSAGE);
            else{
                JOptionPane.showMessageDialog(null, index + " : 계약서 선택", "Message", JOptionPane.INFORMATION_MESSAGE);
                //계약서 보여주는 내용 추가
                try {
                    ContractGUI gui = new ContractGUI(keywordFile.get(index));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        else if(source == frame.signUpDialog.okButton) {
            System.out.println("signUpDialog");
            String uid = frame.signUpDialog.uidTxt.getText();
            int userType = 0;
            if(frame.signUpDialog.userType[1].isSelected())
                userType = 1;
            try {
                System.out.println(uid + "myIp : " + manager.myIp);
                manager.user = new User(manager.myIp,uid, userType, manager.idList);
                manager.uploadUser();
            } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                noSuchAlgorithmException.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (InvalidAlgorithmParameterException invalidAlgorithmParameterException) {
                invalidAlgorithmParameterException.printStackTrace();
            } catch (NoSuchProviderException noSuchProviderException) {
                noSuchProviderException.printStackTrace();
            } catch (InvalidKeySpecException invalidKeySpecException) {
                invalidKeySpecException.printStackTrace();
            }
            //db에 업로드
            JOptionPane.showMessageDialog(null, "회원가입 완료했습니다.", "Message", JOptionPane.INFORMATION_MESSAGE);
            frame.signUpDialog.setVisible(false);
            frame.addLog("사용자 회원가입 완료 : " + manager.user.toString());
            //pk,sk만들기 -> 파일 만들고

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
    public void tabStateChanged() throws ParseException {
        if (frame.jTab.getSelectedIndex() == 3) { // 선택한 탭이 "refresh" 라면 "기존"탭으로 유지하기
            if (frame.idxTab == 2) {// 현재 탭이 "계약 이어하기"
                //relaod data
                manager.loadContractData();
                //데이터 받아온거 뿌리기
                frame.mpContinue.setComboBoxContract(manager.user.contractList);
            }
            else {
                //다른 탭에서는 반응없음
            }
        }
        else {
            frame.idxTab = frame.jTab.getSelectedIndex();
        }
        frame.jTab.setSelectedIndex(frame.idxTab);
        System.out.println("tabStateChanged: "+frame.idxTab);
    }

    @Override
    public void stateChanged(ChangeEvent e) {

        if (e.getSource() == frame.jTab){
            try {
                tabStateChanged();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }
    }
}

