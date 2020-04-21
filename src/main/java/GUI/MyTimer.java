package GUI;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Label;
import java.awt.Panel;

import org.json.simple.JSONObject;

class MyTimer extends Panel {

   int selectTime[] = new int[2];

   Choice chour = new Choice();
   Choice cmin = new Choice();
   Label lhour = new Label("시");
   Label lmin = new Label("분");
   Button bt2 = new Button("확인");

   // 시 초이스 컴포넌트
   public void makeHourChoice() {
      for (int i = 0; i < 25; i++) {
         String hour = "" + i;
         chour.add(hour);
      }
   }

   // 분 초이스 컴포넌트
   public void makeMinChoice() {
      for (int i = 0; i < 61; i++) {
         String min = "" + i;
         cmin.add(min);
      }
   }


   public JSONObject getSelectTime() {
      JSONObject json = new JSONObject();

      selectTime[0] = chour.getSelectedIndex();
      selectTime[1] = cmin.getSelectedIndex();

      json.put("hour", selectTime[0]);
      json.put("min", selectTime[1]);

      return json;
   }

   public void setSelectTime(JSONObject data) {

      chour.select((int) data.get("hour")+1);
      cmin.select((int)data.get("min")+1);
   }


   MyTimer() {

      makeHourChoice();
      makeMinChoice();

      add(chour);
      add(lhour);
      add(cmin);
      add(lmin);
      //add(bt2);

      // 나중에 확인 버튼이 없어지면 그냥 최종 확인에서 값 가져오기
//      bt2.addActionListener(new ActionListener() {
//         public void actionPerformed(ActionEvent e) {
//            setVisible(true);
//            JSONObject j = new JSONObject();
//            j.put("key", getSelectTime());
//            System.out.println(j);
//            System.out.println(selectTime[0] + "." + selectTime[1]);
//            setSelectTime((JSONObject)j.get("key"));
//            setVisible(true);
//
//         }
//      });
   }

}