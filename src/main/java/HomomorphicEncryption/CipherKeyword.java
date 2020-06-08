package HomomorphicEncryption;

import org.bson.Document;

import java.math.BigInteger;

public class CipherKeyword {
    public Object _id;
    public BigInteger c1;
    public BigInteger c2;

    public CipherKeyword(Document d){ //from contract table
        this._id = d.get("_id");
        this.c1 = new BigInteger(d.get("c1").toString(),16);
        this.c2 = new BigInteger(d.get("c2").toString(),16);
    }
}
