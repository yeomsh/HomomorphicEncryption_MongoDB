package GUI;

import java.awt.Choice;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.json.simple.JSONObject;

public class ContractGUI extends JFrame {

   JSONObject data2 = new JSONObject();

   Label oNameLb, wNameLb, contractTermLb, placeLb, contentLb, workTimLb, restTimeLb, workDayLb, restDayLb;
   Label wageTitleLb, wage1Lb, wage2_1Lb, wage2_2Lb, wage3_1Lb, wage3_2Lb, wage4Lb, wage5Lb;
   Label insuranceLb, item12Lb, item13Lb;
   Label contractDateLb;
   Label signTitleLb, oSignTitleLb, wSignTitleLb;
   Label oSign1Lb, oSign2Lb, oSign3Lb, oSign4Lb;
   Label wSign1Lb, wSign2Lb, wSign3Lb;

   JTextField oNameTxt, wNameTxt, contractTermTxt, placeTxt, contentTxt, workDayTxt, restDayTxt;
   JTextField wage1Txt, wage2_2Txt, wage3_2Txt, wage4Txt;
   JLabel item12Txt;
   JLabel item13Txt;

   JTextField oSign1Txt, oSign2Txt, oSign3Txt, oSign4Txt;
   JTextField wSign1Txt, wSign2Txt, wSign3Txt;

   Panel workDayPl, restDayPl;
   Panel wage2_1Pl, wage3_1Pl, wage5Pl;
   Panel insurancePl;

   ButtonGroup wage2_1G, wage3_1G, wage5G;
   // JTextField txtHp1, txtHp2, txtHp3;
   // JCheckBox cbMale,cbFeMale;

   int whatStep = 0;

   JCheckBox[] workDayCb = new JCheckBox[7];
   JCheckBox[] restDayCb = new JCheckBox[7];

   JRadioButton[] wage2_1Cb = new JRadioButton[2];
   JRadioButton[] wage3_1Cb = new JRadioButton[2];
   JRadioButton[] wage5Cb = new JRadioButton[2];

   JCheckBox[] insuranceCb = new JCheckBox[4];

   MyCalendar contractTermStartPl;
   MyCalendar contractTermEndPl;
   MyCalendar contractDatePl;

   MyTimer[] timer = new MyTimer[4];

   String[] dayString = { "월", "화", "수", "목", "금", "토", "일" };
   String[] yesno = { "o", "x" };
   String[] wage5String = { "직접 지급", "근로자 명의 통장 입금" };
   String[] insuranceString = { "고용보험", "산재보험", "국민연금", "건강보험" };

   Choice chJob;
   JTextField txtAddr, txtAge;
   JButton btnSubmit, btnCancel;

   public ContractGUI() {

      super("근로계약서 작성");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setLayout(null);

      oNameLb = new Label("1. 사업주 이름");
      wNameLb = new Label("2. 근로자 이름");
      contractTermLb = new Label("3. 근로계약기간");
      placeLb = new Label("4. 근무장소");
      contentLb = new Label("5. 업무내용");
      workTimLb = new Label("6. 소정근로시간");
      restTimeLb = new Label("7. 휴게시간");
      workDayLb = new Label("8. 근무일(요일)");
      restDayLb = new Label("9. 휴일(요일)");

      wageTitleLb = new Label("10.임금");
      wage1Lb = new Label("금액");
      wage2_1Lb = new Label("상여금 여부");
      wage2_2Lb = new Label("금액");
      wage3_1Lb = new Label("기타급여 여부");
      wage3_2Lb = new Label("금액");
      wage4Lb = new Label("지급날짜");
      wage5Lb = new Label("지급방법");

      insuranceLb = new Label("11. 사회보험 적용 여부");
      item12Lb = new Label("12. 근로계약서 교부");
      item12Txt = new JLabel(
              "<html><body>: 사업주는 근로계약을 체결함과 동시에 본 계약서를 사본하여 근로자의 교부요구와 관계없이 근로자에게 교부함<br>(근로기준법 제 17조 이행)</body></html>");
      item12Txt.setFont(new Font(null, Font.PLAIN, item12Txt.getFont().getSize()));
      item13Lb = new Label("13. 기타");
      item13Txt = new JLabel(": 이 계약에 정함이 없는 사항은 근로기준법령에 의함");
      item13Txt.setFont(new Font(null, Font.PLAIN, item13Txt.getFont().getSize()));

      contractDateLb = new Label("14. 날짜");
      contractDatePl = new MyCalendar();

      signTitleLb = new Label("15. 서명");
      oSignTitleLb = new Label("사업주");
      wSignTitleLb = new Label("근로자");
      oSign1Lb = new Label("-> 사업체명");
      oSign2Lb = new Label("-> 전화번호");
      oSign3Lb = new Label("-> 주소");
      oSign4Lb = new Label("-> 대표자");

      wSign1Lb = new Label("-> 성명");
      wSign2Lb = new Label("-> 전화번호");
      wSign3Lb = new Label("-> 주소");

      wage1Txt = new JTextField(20);
      wage2_2Txt = new JTextField(20);
      wage3_2Txt = new JTextField(20);
      wage4Txt = new JTextField(20);

      oSign1Txt = new JTextField(20);
      oSign2Txt = new JTextField(20);
      oSign3Txt = new JTextField(20);
      oSign4Txt = new JTextField(20);

      wSign1Txt = new JTextField(20);
      wSign2Txt = new JTextField(20);
      wSign3Txt = new JTextField(20);

      // 상여금 여부
      wage2_1Pl = new Panel(new FlowLayout(FlowLayout.LEFT));
      wage2_1G = new ButtonGroup();
      // 기타급여(기본금 외 수당) 여부
      wage3_1Pl = new Panel(new FlowLayout(FlowLayout.LEFT));
      wage3_1G = new ButtonGroup();
      // 지급방법
      wage5Pl = new Panel(new FlowLayout(FlowLayout.LEFT));
      wage5G = new ButtonGroup();
      // 지급방법
      insurancePl = new Panel(new FlowLayout(FlowLayout.LEFT));

      for (int i = 0; i < 2; i++) {
         wage2_1Cb[i] = new JRadioButton(yesno[i]);
         wage3_1Cb[i] = new JRadioButton(yesno[i]);
         wage5Cb[i] = new JRadioButton(wage5String[i]);

         wage2_1Pl.add(wage2_1Cb[i]);
         wage3_1Pl.add(wage3_1Cb[i]);
         wage5Pl.add(wage5Cb[i]);

         wage2_1G.add(wage2_1Cb[i]);
         wage3_1G.add(wage3_1Cb[i]);
         wage5G.add(wage5Cb[i]);
      }

      for (int i = 0; i < 4; i++) {
         insuranceCb[i] = new JCheckBox(insuranceString[i]);
         insurancePl.add(insuranceCb[i]);
      }

      oNameLb.setBounds(20, 50, 100, 20);
      wNameLb.setBounds(20, 80, 100, 20);
      contractTermLb.setBounds(20, 110, 100, 20);
      placeLb.setBounds(20, 170, 100, 20);
      contentLb.setBounds(20, 200, 100, 20);
      workTimLb.setBounds(20, 230, 100, 20);
      restTimeLb.setBounds(20, 260, 100, 20);
      workDayLb.setBounds(20, 290, 100, 20);

      restDayLb.setBounds(20, 320, 100, 20);

      wageTitleLb.setBounds(20, 350, 100, 20);

      wage1Lb.setBounds(120, 370, 80, 20);
      wage1Txt.setBounds(200, 370, 200, 20);

      wage2_1Lb.setBounds(120, 400, 80, 20);
      wage2_1Pl.setBounds(200, 390, 100, 30);
      wage2_2Lb.setBounds(300, 400, 50, 20);
      wage2_2Txt.setBounds(350, 400, 100, 20);

      wage3_1Lb.setBounds(120, 430, 80, 20);
      wage3_1Pl.setBounds(200, 420, 100, 30);
      wage3_2Lb.setBounds(300, 430, 50, 20);
      wage3_2Txt.setBounds(350, 430, 100, 20);

      wage4Lb.setBounds(120, 460, 80, 20);
      wage4Txt.setBounds(200, 460, 200, 20);

      wage5Lb.setBounds(120, 490, 80, 20);
      wage5Pl.setBounds(200, 480, 300, 30);

      insuranceLb.setBounds(20, 530, 100, 20);
      insurancePl.setBounds(120, 520, 400, 30);

      item12Lb.setBounds(20, 560, 200, 20);
      item12Txt.setBounds(120, 580, 360, 50);

      item13Lb.setBounds(20, 650, 100, 20);
      item13Txt.setBounds(120, 650, 300, 20);

      contractDateLb.setBounds(20, 680, 100, 20);
      contractDatePl.setBounds(100, 670, 200, 30);

      signTitleLb.setBounds(20, 710, 100, 20);
      oSignTitleLb.setBounds(120, 720, 100, 20);
      oSign1Lb.setBounds(120, 750, 80, 20);
      oSign1Txt.setBounds(200, 750, 200, 20);
      oSign2Lb.setBounds(120, 780, 80, 20);
      oSign2Txt.setBounds(200, 780, 200, 20);
      oSign3Lb.setBounds(120, 810, 80, 20);
      oSign3Txt.setBounds(200, 810, 200, 20);
      oSign4Lb.setBounds(120, 840, 80, 20);
      oSign4Txt.setBounds(200, 840, 200, 20);

      wSignTitleLb.setBounds(120, 870, 100, 20);
      wSign1Lb.setBounds(120, 900, 80, 20);
      wSign1Txt.setBounds(200, 900, 200, 20);
      wSign2Lb.setBounds(120, 930, 80, 20);
      wSign2Txt.setBounds(200, 930, 200, 20);
      wSign3Lb.setBounds(120, 960, 80, 20);
      wSign3Txt.setBounds(200, 960, 200, 20);

      add(oNameLb);
      add(wNameLb);
      add(contractTermLb);
      add(placeLb);
      add(contentLb);
      add(workTimLb);
      add(restTimeLb);
      add(workDayLb);
      add(restDayLb);
      add(wageTitleLb);
      add(wage1Lb);
      add(wage1Txt);
      add(wage2_1Lb);
      add(wage2_1Pl);
      add(wage2_2Lb);
      add(wage2_2Txt);

      add(wage3_1Lb);
      add(wage3_1Pl);
      add(wage3_2Lb);
      add(wage3_2Txt);
      add(wage4Lb);
      add(wage4Txt);

      add(wage5Lb);
      add(wage5Pl);

      add(insuranceLb);
      add(insurancePl);
      add(item12Lb);
      add(item12Txt);
      add(item13Lb);
      add(item13Txt);
      add(contractDateLb);
      add(contractDatePl);
      add(signTitleLb);
      add(oSignTitleLb);
      add(oSign1Lb);
      add(oSign1Txt);
      add(oSign2Lb);
      add(oSign2Txt);
      add(oSign3Lb);
      add(oSign3Txt);
      add(oSign4Lb);
      add(oSign4Txt);

      add(wSignTitleLb);
      add(wSign1Lb);
      add(wSign1Txt);
      add(wSign2Lb);
      add(wSign2Txt);
      add(wSign3Lb);
      add(wSign3Txt);

      oNameTxt = new JTextField(20);
      wNameTxt = new JTextField(20);
      contractTermTxt = new JTextField(20);
      // Label lblhipen1 = new Label("-");
      // Label lblhipen2 = new Label("-");
      placeTxt = new JTextField(20);
      contentTxt = new JTextField(20);
      // workTimeTxt = new JTextField(20);
      // restTimeTxt = new JTextField(20);

      // Panel panGen = new Panel(new FlowLayout(FlowLayout.LEFT));
      // JCheckBoxGroup group = new JCheckBoxGroup();
      // cbMale = new JCheckBox("남자",group,true);
      // cbFeMale = new JCheckBox("여자",group,false);
      //
      // panGen.add(cbMale);
      // panGen.add(cbFeMale);
      // panGen.setBounds(120, 162, 100, 30);

      workDayPl = new Panel(new FlowLayout(FlowLayout.LEFT));
      restDayPl = new Panel(new FlowLayout(FlowLayout.LEFT));

      for (int i = 0; i < 7; i++) {
         workDayCb[i] = new JCheckBox(dayString[i]);
         restDayCb[i] = new JCheckBox(dayString[i]);
         workDayPl.add(workDayCb[i]);
         restDayPl.add(restDayCb[i]);
      }

      chJob = new Choice();
      chJob.add("자바프로그래머");
      chJob.add("임베디드프로그래머");
      chJob.add("웹프로그래머");
      chJob.add("모바일프로그래머");

      txtAddr = new JTextField();
      txtAge = new JTextField();

      btnCancel = new JButton("취소");
      btnSubmit = new JButton("작성완료");
      btnCancel.setSize(150, 40);
      btnSubmit.setSize(150, 40);

      oNameTxt.setBounds(120, 50, 180, 20);
      wNameTxt.setBounds(120, 80, 180, 20);
      // contractTermTxt.setBounds(120, 110, 180, 20);
      // txtHp1.setBounds(120, 140, 60, 20);
      // lblhipen1.setBounds(190, 140, 10, 20);
      // txtHp2.setBounds(210, 140, 60, 20);
      // lblhipen2.setBounds(275, 140, 10, 20);
      // txtHp3.setBounds(290, 140, 60, 20);

      contractTermStartPl = new MyCalendar();
      contractTermEndPl = new MyCalendar();

      contractTermStartPl.setBounds(150, 100, 180, 30);
      Label lblhipen1 = new Label("시작:");
      Label lblhipen3 = new Label("---");
      Label lblhipen4 = new Label("---");
      lblhipen1.setBounds(120, 110, 30, 20);
      contractTermEndPl.setBounds(150, 130, 180, 30);
      Label lblhipen2 = new Label("종료:");
      lblhipen2.setBounds(120, 140, 30, 20);

      placeTxt.setBounds(120, 170, 180, 20);
      contentTxt.setBounds(120, 200, 180, 20);

      for (int i = 0; i < 4; i++) {
         timer[i] = new MyTimer();
         timer[i].setBounds(120 + 190 * (i % 2), 220 + i / 2 * 30, 160, 30);
         add(timer[i]);
      }
      lblhipen3.setBounds(280, 220, 180, 40);
      lblhipen4.setBounds(280, 250, 180, 40);

      workDayPl.setBounds(120, 280, 400, 30);
      restDayPl.setBounds(120, 310, 350, 30);

      Panel paButton = new Panel();
      paButton.add(btnSubmit);
      paButton.add(btnCancel);
      paButton.setBounds(50, 990, 370, 370);

      add(lblhipen3);
      add(lblhipen4);
      add(oNameTxt);
      add(wNameTxt);
      add(contractTermTxt);
      // add(txtHp1);
      // add(lblhipen1);
      // add(txtHp2);
      // add(lblhipen2);
      // add(txtHp3);
      add(placeTxt);
      add(contentTxt);
      // add(workTimeTxt);
      add(lblhipen1);
      add(contractTermStartPl);
      add(lblhipen2);
      add(contractTermEndPl);

      add(workDayPl);
      add(restDayPl);
      add(paButton);

      setSize(500, 1400);
      setResizable(false);
      setVisible(false);

      btnSubmit.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            switch (whatStep) {
               case 1:
                  data2 = getStepContract(whatStep);
                  whatStep = 2;
                  setStep2Contract(whatStep, data2);
                  setVisible(true);
                  break;
               case 2:
                  data2 = getStepContract(whatStep);
                  whatStep = 3;
                  setStep3Contract(whatStep, data2);
                  setVisible(true);
                  break;
               case 3:
                  data2 = getStepContract(whatStep);
                  whatStep = 4;
                  setStep4Contract(whatStep, data2);
                  setVisible(true);
                  break;
               case 4:
                  data2 = getStepContract(whatStep);
                  setVisible(false);
                  break;
            }
            System.out.println("제출");
            // 데베 검색 후 있는 사용자라면 isJoin = true
            // 데베 검색 후 없는 사용자라면 isJoin = false
            // tf.setText("127.0.0.1");
            // tf.setEnabled(false);
         }
      });

      btnCancel.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            setVisible(false);
            System.out.println("취소");
            System.exit(0);
            // 추가할 내용
            // 공개키, 비밀키, id, qid, userType, ip 생성 후
            // user 데베에 추가

         }
      });

   }

   // step1 점주가 근로계약서 작성
   public void setStep1Contract(int step) {

      setVisiableAllFalse();

      oNameTxt.setEnabled(true);
      contractTermStartPl.setEnabled(true);
      contractTermEndPl.setEnabled(true);
      placeTxt.setEnabled(true);
      contentTxt.setEnabled(true);
      for (MyTimer tm : timer)
         tm.setEnabled(true);
      workDayPl.setEnabled(true);
      restDayPl.setEnabled(true);
      wage1Txt.setEnabled(true);
      wage2_1Pl.setEnabled(true);
      wage2_2Txt.setEnabled(true);
      wage3_1Pl.setEnabled(true);
      wage3_2Txt.setEnabled(true);
      wage4Txt.setEnabled(true);
      wage5Pl.setEnabled(true);
      insurancePl.setEnabled(true);
      oSign1Txt.setEnabled(true);
      oSign2Txt.setEnabled(true);
      oSign3Txt.setEnabled(true);
      oSign4Txt.setEnabled(true);


      setVisible(true);

      whatStep = step;
   }

   public void setVisiableAllFalse() {
      oNameTxt.setEnabled(false);
      wNameTxt.setEnabled(false);
      contractTermStartPl.setEnabled(false);
      contractTermEndPl.setEnabled(false);
      placeTxt.setEnabled(false);
      contentTxt.setEnabled(false);
      for (MyTimer tm : timer)
         tm.setEnabled(false);
      workDayPl.setEnabled(false);
      restDayPl.setEnabled(false);
      wage1Txt.setEnabled(false);
      wage2_1Pl.setEnabled(false);
      wage2_2Txt.setEnabled(false);
      wage3_1Pl.setEnabled(false);
      wage3_2Txt.setEnabled(false);
      wage4Txt.setEnabled(false);
      wage5Pl.setEnabled(false);
      insurancePl.setEnabled(false);
      contractDatePl.setEnabled(false);
      oSign1Txt.setEnabled(false);
      oSign2Txt.setEnabled(false);
      oSign3Txt.setEnabled(false);
      oSign4Txt.setEnabled(false);
      wSign1Txt.setEnabled(false);
      wSign2Txt.setEnabled(false);
      wSign3Txt.setEnabled(false);
   }
   // step1 점주가 근로계약서 작성
   public void setStep2Contract(int step, JSONObject json) {
      setContractField(json);
      setVisiableAllFalse();
      wNameTxt.setEnabled(true);
      wSign1Txt.setEnabled(true);
      wSign2Txt.setEnabled(true);
      wSign3Txt.setEnabled(true);

      setVisible(true);

      whatStep = step;
   }

   // step1 점주가 근로계약서 작성
   public void setStep3Contract(int step, JSONObject json) {
      setContractField(json);

      setVisiableAllFalse();
      btnSubmit.setText("서명하기");

      setVisible(true);
      whatStep = step;
   }

   // step4 근로자가 근로계약서 서명
   // 같은 버튼인데 기능을 나눠서 실행시킬려면, true or false 설정,,?
   // or step설정
   public void setStep4Contract(int step, JSONObject json) {
      setContractField(json);

      setVisiableAllFalse();
      btnSubmit.setText("서명하기");

      setVisible(true);

      whatStep = step;
   }

   // null인지 check하는 함수 추가하기

   // step1 점주가 근로계약서 작성
   public void setContractField(JSONObject json) {
      JSONObject data = json;
      JSONObject checkDay = new JSONObject();
      JSONObject ox = new JSONObject();
      JSONObject ox2 = new JSONObject();
      JSONObject wage = new JSONObject();
      JSONObject insur = new JSONObject();
      JSONObject oSign = new JSONObject();
      JSONObject wSign = new JSONObject();

      oNameTxt.setText((String) data.get("oName"));
      wNameTxt.setText((String) data.get("wName"));
      System.out.println((JSONObject) data.get("oSign"));
      contractTermStartPl.setSelectDate((JSONObject) data.get("contractTermStart"));
      contractTermEndPl.setSelectDate((JSONObject) data.get("contractTermEnd"));

      placeTxt.setText((String) data.get("place"));
      contentTxt.setText((String) data.get("content"));

      for (int i = 0; i < 4; i++)
         timer[i].setSelectTime((JSONObject) data.get("timer" + (i + 1)));

      for (int i = 0; i < 7; i++) {
         workDayCb[i].setSelected((boolean) ((JSONObject) data.get("workDay")).get(dayString[i]));
      }
      for (int i = 0; i < 7; i++) {
         restDayCb[i].setSelected((boolean) ((JSONObject) data.get("restDay")).get(dayString[i]));
      }

      wage1Txt.setText((String) ((JSONObject) data.get("wage")).get("wage1"));

      for (int i = 0; i < 2; i++) {
         wage2_1Cb[i]
                 .setSelected((boolean) ((JSONObject) ((JSONObject) data.get("wage")).get("wage2_1")).get(yesno[i]));
      }
      wage2_2Txt.setText((String) ((JSONObject) data.get("wage")).get("wage2_2"));

      for (int i = 0; i < 2; i++) {
         wage3_1Cb[i]
                 .setSelected((boolean) ((JSONObject) ((JSONObject) data.get("wage")).get("wage3_1")).get(yesno[i]));
      }
      wage3_2Txt.setText((String) ((JSONObject) data.get("wage")).get("wage3_2"));

      wage4Txt.setText((String) ((JSONObject) data.get("wage")).get("wage4"));

      for (int i = 0; i < 2; i++) {
         wage5Cb[i].setSelected(
                 (boolean) ((JSONObject) ((JSONObject) data.get("wage")).get("wage5")).get(wage5String[i]));
      }

      for (int i = 0; i < insuranceString.length; i++) {
         insuranceCb[i].setSelected(
                 (boolean) ((JSONObject) data.get("insurance")).get(insuranceString[i]));
      }

      contractDatePl.setSelectDate((JSONObject) data.get("contractDate"));

      oSign1Txt.setText((String) ((JSONObject) data.get("oSign")).get("oSign1"));
      oSign2Txt.setText((String) ((JSONObject) data.get("oSign")).get("oSign2"));
      oSign3Txt.setText((String) ((JSONObject) data.get("oSign")).get("oSign3"));
      oSign4Txt.setText((String) ((JSONObject) data.get("oSign")).get("oSign4"));

      wSign1Txt.setText((String) ((JSONObject) data.get("wSign")).get("wSign1"));
      wSign2Txt.setText((String) ((JSONObject) data.get("wSign")).get("wSign2"));
      wSign3Txt.setText((String) ((JSONObject) data.get("wSign")).get("wSign3"));

      setVisible(true);
   }

   public JSONObject getStepContract(int step) {

      whatStep = step;

      JSONObject data = new JSONObject();
      JSONObject checkDay = new JSONObject();
      JSONObject ox = new JSONObject();
      JSONObject ox2 = new JSONObject();
      JSONObject wage = new JSONObject();
      JSONObject insur = new JSONObject();
      JSONObject oSign = new JSONObject();
      JSONObject wSign = new JSONObject();

      data.put("oName", oNameTxt.getText());
      data.put("wName", wNameTxt.getText());

      data.put("contractTermStart", contractTermStartPl.getSelectDate());
      data.put("contractTermEnd", contractTermEndPl.getSelectDate());

      data.put("place", placeTxt.getText());
      data.put("content", contentTxt.getText());

      for (int i = 0; i < 4; i++)
         data.put("timer" + (i + 1), timer[i].getSelectTime());

      for (int i = 0; i < 7; i++) {
         checkDay.put(dayString[i], workDayCb[i].isSelected());
      }
      data.put("workDay", checkDay);
      for (int i = 0; i < 7; i++) {
         checkDay.put(dayString[i], restDayCb[i].isSelected());
      }
      data.put("restDay", checkDay);

      wage.put("wage1", wage1Txt.getText());
      for (int i = 0; i < 2; i++) {
         ox.put(yesno[i], wage2_1Cb[i].isSelected());
      }
      wage.put("wage2_1", ox);
      wage.put("wage2_2", wage2_2Txt.getText());

      for (int i = 0; i < 2; i++) {
         ox.put(yesno[i], wage3_1Cb[i].isSelected());
      }
      wage.put("wage3_1", ox);
      wage.put("wage3_2", wage3_2Txt.getText());

      wage.put("wage4", wage4Txt.getText());

      for (int i = 0; i < 2; i++) {
         ox2.put(wage5String[i], wage5Cb[i].isSelected());
      }
      wage.put("wage5", ox2);

      data.put("wage", wage);

      for (int i = 0; i < 4; i++) {
         insur.put(insuranceString[i], insuranceCb[i].isSelected());
      }

      data.put("insurance", insur);

      data.put("contractDate", contractDatePl.getSelectDate());

      oSign.put("oSign1", oSign1Txt.getText());
      oSign.put("oSign2", oSign2Txt.getText());
      oSign.put("oSign3", oSign3Txt.getText());
      oSign.put("oSign4", oSign4Txt.getText());

      data.put("oSign", oSign);

      wSign.put("wSign1", wSign1Txt.getText());
      wSign.put("wSign2", wSign2Txt.getText());
      wSign.put("wSign3", wSign3Txt.getText());

      data.put("wSign", wSign);

      System.out.println(data);

      return data;
   }


}