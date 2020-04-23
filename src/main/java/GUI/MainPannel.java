package GUI;

import javax.swing.*;
import java.util.ArrayList;

public class MainPannel extends JPanel {
    public JComboBox comboBoxContract;
    public String[] contractList={};
    public JButton button;
    public Mode mode;
    public MainPannel() {
        this.setLayout(null);
    }
    public MainPannel(Mode mode) {
        this.mode = mode;
        this.setLayout(null);
    }
    public MainPannel(Mode mode, String btn) {
        this.mode = mode;
        this.setLayout(null);

        //콤보박스
        //arraylist 이용방법
        //JComboBox<?> comboBox = new JComboBox(account1.toArray(new String[account1.size()]));
        int comboBoxWIDTH = 150;
        int comboBoxHEIGHT=30;
        comboBoxContract = new JComboBox(contractList);
        comboBoxContract.setBounds(MainFrame.PANNEL_WIDTH/2-comboBoxWIDTH-10,MainFrame.HEIGHT/8-comboBoxHEIGHT,comboBoxWIDTH,comboBoxHEIGHT);
        //MyFrame.HEIGHT/4-comboBoxWIDTH: 패널이 전체의 반인데, 콤보박스는 그 패널 내에서 반이니까! + 좀 상단에 있어야해서 상대적으로 긴 WIDTH 뺌

        //버튼
        button= new JButton(btn);
        button.setBounds(MainFrame.PANNEL_WIDTH/2+10,MainFrame.HEIGHT/8-comboBoxHEIGHT,comboBoxWIDTH,comboBoxHEIGHT); //위치 바꿔야함
        //각종 컴포넌트 등록
        if (mode != Mode.CONTRACT_NEW){
            this.add(comboBoxContract);
        }
        else {
            button.setBounds(MainFrame.PANNEL_WIDTH/2-comboBoxWIDTH-10,MainFrame.HEIGHT/8-comboBoxHEIGHT,2*comboBoxWIDTH+20,comboBoxHEIGHT); //위치 바꿔야함
        }
        this.add(button);
    }
    void setComboBoxList(String[] list) {
        this.contractList = list;
        //this.comboBoxContract.removeAllItems();
        for(int i=0; i<contractList.length; i++) {
            comboBoxContract.addItem(contractList[i]);
        }
    }
    void setComboBoxList(ArrayList<String> list) {
        this.comboBoxContract.removeAllItems();
        for(int i=0; i<list.size(); i++) {
            comboBoxContract.addItem(list.get(i));
        }
    }


}