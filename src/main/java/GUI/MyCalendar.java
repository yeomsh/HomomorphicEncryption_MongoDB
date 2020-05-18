package GUI;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Panel;
import java.util.Calendar;

import org.json.simple.JSONObject;

class MyCalendar extends Panel {

   int todayDate[] = new int[3];
   int selectDate[] = new int[3];

   Choice cyear = new Choice();
   Choice cmonth = new Choice();
   Choice cdate = new Choice();
   Button btn2 = new Button("확인");

   Calendar nowcal = Calendar.getInstance();

   // 년 초이스 컴포넌트
   public void makeYearChoice() {
      for (int i = 2010; i < 2025; i++) {
         String year = "" + i;
         cyear.add(year);
      }
   }

   // 월 초이스 컴포넌트
   public void makeMonthChoice() {
      for (int i = 1; i < 13; i++) {
         String month = "" + i;
         cmonth.add(month);
      }
   }

   // 일 초이스 컴포넌트
   public void makeDateChoice() {
      for (int i = 1; i < 32; i++) {
         String date = "" + i;
         cdate.add(date);
      }
   }

   public int[] todayDate() {
      todayDate[0] = nowcal.get(Calendar.YEAR);
      // 달은 하루가 적게 나옴 ex) 4월이면 3월로
      todayDate[1] = nowcal.get(Calendar.MONTH);
      todayDate[2] = nowcal.get(Calendar.DATE) - 1;
      return todayDate;
   }

   public void setTodayDate() {
      cyear.select(""+todayDate[0]);
      cmonth.select(todayDate[1]);
      cdate.select(todayDate[2]);
   }

   public JSONObject getSelectDate() {
      JSONObject json = new JSONObject();
      selectDate[0] = cyear.getSelectedIndex() + 2010;
      selectDate[1] = cmonth.getSelectedIndex() + 1;
      selectDate[2] = cdate.getSelectedIndex() + 1;

      json.put("year", selectDate[0]);
      json.put("month", selectDate[1]);
      json.put("date", selectDate[2]);

      return json;
   }

   public void setSelectDate(JSONObject data) {
      //JSONObject data = (JSONObject) json.get(key);

      System.out.println(data);
      cyear.select((int)(long)data.get("year")-2010);
      cmonth.select((int)(long)data.get("month")-1);
      cdate.select((int)(long)data.get("date")-1);
   }

   MyCalendar() {
      makeYearChoice();
      makeMonthChoice();
      makeDateChoice();

      todayDate();
      setTodayDate();

      add(cyear);
      cyear.select("" + nowcal.get(Calendar.YEAR));

      add(cmonth);
      add(cdate);
      //add(bt2);

      // 나중에 확인 버튼이 없어지면 그냥 최종 확인에서 값 가져오기
//
//      btn2.addActionListener(new ActionListener() {
//         public void actionPerformed(ActionEvent e) {
//            setVisible(true);
//            //getSelectDate();
//            JSONObject j = new JSONObject();
//            j.put("key", getSelectDate());
//            System.out.println(selectDate[0] + "." + selectDate[1] + "." + selectDate[2]);
//            System.out.println(j);
//            System.out.println(j.toString());
//            setSelectDate((JSONObject)j.get("key"));
//            setVisible(true);
//
//
//         }
//      });
   }
}