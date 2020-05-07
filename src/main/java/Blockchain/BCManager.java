package Blockchain;

import DataClass.Contract;
import DataClass.Database;
import GUI.ContractGUI;
import org.json.simple.JSONObject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BCManager {
    public int step;
    public Contract contract = null; //이전까지 생성된 계약서 데이터
    public JSONObject data; //이번 step에서 작성한 가장 최근의 데이터가 저장되는 변수
    public ContractGUI contractGUI;
    public BCEventHandler eventHandler = new BCEventHandler();
    public Database db; //이걸 csmanager껄 가져와도 되는데 너무 참조의참조를 하는 느낌이라 일단 새로 만들었어! - 승연

    public BCManager(Database db, String receiverIP){
        this.db = db;
        this.contract = new Contract(0, receiverIP);
        this.contractGUI = new ContractGUI(this);
    }
    public BCManager(Contract contract){
        this.contract = contract; //이거 두개를 Contract로 묶을 수 있는뎁
        this.contractGUI = new ContractGUI(this);
    }


    class BCEventHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            data = contractGUI.getStepContract();
            if(step == 4){ //블록체인
                //db.insertUserContract 에서도 step 거르긴 함
            }
            else{
                contract.step++;
                contract.data = data;
                db.insertUserContract( contract);
            }

//            switch (step) {
//                //total manager의 data를 여기서 생성한 데이터로 바꾸기만 하면됨
//                case 1:
//                    //생성하기 눌렀을 때 입력된 ip user에게 data 추가
//                    break;
//                case 2:
////                    whatStep = 3;
////                    setStep3Contract(whatStep, data2);
////                    setVisible(true);
//                    break;
//                case 3:
////                    whatStep = 4;
////                    setStep4Contract(whatStep, data2);
////                    setVisible(true);
//                    break;
//                case 4:
////                    setVisible(false);
//
//                    //letchainupdate
//                    break;
//            }
            contractGUI.setVisible(false); //모든 작업끝나면 계약서 작성창 닫기
            System.out.println("제출");
            // 데베 검색 후 있는 사용자라면 isJoin = true
            // 데베 검색 후 없는 사용자라면 isJoin = false
            // tf.setText("127.0.0.1");
            // tf.setEnabled(false);
        }
    }
}
