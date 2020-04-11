package HomomorphicEncryption;

import org.bson.Document;

import java.math.BigInteger;

public class Contract {
    public int id;
    public Object _id;
    //public ?? file
    public BigInteger c2;
    public BigInteger c3;

    public Contract(int id, BigInteger c2, BigInteger c3){
        this.id = id;
        this.c2 = c2;
        this.c3 = c3;
    }

    public Contract(Document d){ //from contract table
        this._id = d.get("_id");
        this.c2 = new BigInteger(d.get("c2").toString(),16);
        this.c3 = new BigInteger(d.get("c3").toString(),16);
    }
}
