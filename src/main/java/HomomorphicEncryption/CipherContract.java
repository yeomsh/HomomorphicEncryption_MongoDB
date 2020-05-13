package HomomorphicEncryption;

import DataClass.Contract;
import org.bson.Document;
import org.json.simple.JSONObject;
import java.math.BigInteger;

public class CipherContract extends Contract { //data class의 contract 상속으로 바꾸기
    public int id;
    public Object _id;
    public BigInteger c2;
    public BigInteger c3;

    public CipherContract(Document d){
        super((Document)d.get("file")); //from contract table
        this._id = d.get("_id");
        this.c2 = new BigInteger(d.get("c2").toString(),16);
        this.c3 = new BigInteger(d.get("c3").toString(),16);
    }

    public String toString(){
        return "ContractaInfo _id : " + _id.toString() + ", c2 : " + c2 + ", c3 : " + c3;
    }
}
