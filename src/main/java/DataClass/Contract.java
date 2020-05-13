package DataClass;

import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Contract {
    public int step; //1,2,3, 4
    public String receiverIP; //상대방 IP
    public Object _id; //step1단계에서 최초로 mongodb에 업로드 될 떄 부여되는 유니크한 식별자
    public JSONObject fileData;
    public Contract(int step, String receiverIP){
        this.step = step;
        this.receiverIP = receiverIP;
    }
    public Contract(Object d){
        System.out.println(d.toString());
        JSONParser parser = new JSONParser();
        try{
            Object obj = parser.parse(d.toString());
            this.fileData = (JSONObject) obj;
            if(fileData.containsKey("_id")){
                this._id = fileData.get("_id");
                this.step = Integer.parseInt(fileData.get("step").toString());
                this.receiverIP = fileData.get("receiverIP").toString();
                fileData.remove("_id");
                fileData.remove("step");
                fileData.remove("receiverIP");
            }
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public Contract(Document d)  {
        //contract List에 document가 바로 들어간게 아니고 array형식으로 들어가야함
        System.out.println(d);
        JSONParser parser = new JSONParser();
        try{
            Object obj =  parser.parse(d.toJson());
            if(d.containsKey("_id")){
                this._id = d.get("_id");
                this.step = Integer.parseInt(d.get("step").toString());
                this.receiverIP = d.getString("receiverIP");
                obj = parser.parse(((Document) d.get("file")).toJson());
            }
            this.fileData = (JSONObject) obj;
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
