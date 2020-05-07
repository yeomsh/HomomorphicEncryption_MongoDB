package DataClass;

import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Contract {
    public int step; //1,2,3, 4
    public String receiverIP; //상대방 IP
    Object _id; //step1단계에서 최초로 mongodb에 업로드 될 떄 부여되는 유니크한 식별자
    public JSONObject data;
    public Contract(int step, String receiverIP){
        this.step = step;
        this.receiverIP = receiverIP;
    }
    public Contract(Document d)  {
        //contract List에 document가 바로 들어간게 아니고 array형식으로 들어가야함
        this._id = d.get("_id");
        if (d.containsKey("step")){
            System.out.println("key yes");
        }
        this.step = Integer.parseInt(d.get("step").toString());
        this.receiverIP = d.getString("receiverIP");
        JSONParser parser = new JSONParser();
        try{
            Object obj = parser.parse(d.toJson());
            data = (JSONObject) obj;
            data.remove("step");
            data.remove("receiverIP");
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Contract(JSONObject data){
        data = data;
    }
}
