package HomomorphicEncryption;

import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonReader;
import org.bson.json.JsonWriterSettings;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.math.BigInteger;

public class Contract {
    public int id;
    public Object _id;
    //public ?? file
    public BigInteger c2;
    public BigInteger c3;

    public JSONObject file = new JSONObject();

    public Contract(int id, BigInteger c2, BigInteger c3,JSONObject file){
        this.id = id;
        this.c2 = c2;
        this.c3 = c3;
        this.file = file;
    }

    public Contract(Document d){ //from contract table
        this._id = d.get("_id");
        this.c2 = new BigInteger(d.get("c2").toString(),16);
        this.c3 = new BigInteger(d.get("c3").toString(),16);

        //수동으로 넣는 방법 말고 라이브러리가 있는 듯한데 좀 더 찾아보기
        Document temp = ((Document) d.get("file"));
        file.put("key",temp.get("key").toString());
        file.put("key2",temp.get("key2").toString());
        System.out.println(file);
    }

    public String toString(){
        return "ContractaInfo _id : " + _id.toString() + ", c2 : " + c2 + ", c3 : " + c3;
    }
}
