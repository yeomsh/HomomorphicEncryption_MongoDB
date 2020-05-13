package GUI;

import DataClass.Contract;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Vector;

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
    public void setComboBoxContract(ArrayList<Contract> contractList){
        this.comboBoxContract.removeAllItems();
        for (Contract con: contractList){
            comboBoxContract.addItem(con._id); //addItem(인자: 콤보박스 string)
        }
    }
    public void setComboBoxContract(Vector<JSONObject> contractList){
        this.comboBoxContract.removeAllItems();
        for (JSONObject obj: contractList){
            comboBoxContract.addItem(obj.get("oName").toString()+" "+obj.get("wName").toString()); //addItem(인자: 콤보박스 string)
        }
    }
}