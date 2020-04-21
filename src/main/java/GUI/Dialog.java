package GUI;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

//class InputDialog extends JDialog{
//    JButton okButton = new JButton("OK");
//    JButton cancelButton = new JButton("CANCEL");
//    JLabel tl;
//    JTextField tf = new JTextField(10);
//    public InputDialog(JFrame frame, String title, Mode mode) {
//        super(frame,title);
//        this.setLayout(new FlowLayout());
//        this.setSize(300,150);
//        tl = new JLabel();
//        this.add(tl);
//        this.add(tf);
//        cancelButton.addActionListener(new ActionListener(){
//            public void actionPerformed(ActionEvent e) {
//                setVisible(false);
////	             추가할 내용
////	             공개키, 비밀키, id, qid, userType, ip 생성 후
////	             user 데베에 추가
//            }
//        });
//        this.add(okButton);
//        this.add(cancelButton);
//
//        if (mode == Mode.CONTRACT_NEW){
//            makeIpDialog();
//        }
//        else if(mode == Mode.CONTRACT_SEARCH){// Mode.CONTRACT_SEARCH
//            makeKeywordDialog();
//        }
//        else { //INIT
//            makeInitDialog();
//        }
//    }
//    void makeIpDialog() {
//        tl.setText("ip");
//    }
//    void makeKeywordDialog() {
//        tl.setText("키워드");
//    }
//    void makeInitDialog() {
//        tl.setText("ip");
//        tf.setText("127.000.000.001");
//        tf.setEnabled(false);
//    }
//}
class SignUpDialog extends JDialog{
    JButton okButton = new JButton("OK");
    JButton cancelButton = new JButton("CANCEL");
    JRadioButton []userType = new JRadioButton[2];

    public SignUpDialog(JFrame frame) {
        super(frame,"회원가입");
        this.setLayout(new FlowLayout());
        this.setSize(200,130);

        JPanel jp1 = new JPanel();
        JPanel jp2 = new JPanel();

        jp1.setLayout(new FlowLayout());
        jp2.setLayout(new FlowLayout());


        //ButtonGroup은 라디오 버튼 중 한가지만 선택하기 위해서
        ButtonGroup bg = new ButtonGroup();

        userType[0] = new JRadioButton("점주");
        userType[0].setSelected(true);
        userType[1] = new JRadioButton("근로자");


        for(JRadioButton i : userType) {
            jp1.add(i);
            bg.add(i);
        }
        cancelButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "프로그램을 종료합니다.", "Message", JOptionPane.INFORMATION_MESSAGE);
                setVisible(false);
                System.exit(0);
//	             추가할 내용
//	             공개키, 비밀키, id, qid, userType, ip 생성 후
//	             user 데베에 추가
            }
        });
        jp2.add(okButton);
        jp2.add(cancelButton);

        this.add(jp1);
        this.add(jp2);

    }
}