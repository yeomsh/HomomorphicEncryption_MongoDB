package HomomorphicEncryption;

import org.bson.Document;

import java.math.BigInteger;

public class KeywordPEKS {
    public int id;
    public Object _id;
    public BigInteger c1;
    public BigInteger c2;
    public KeywordPEKS(int id, BigInteger c1, BigInteger c2){ //from contract table
        this.id = id;
        this.c1 = c1;
        this.c2 = c2;
    }

    public KeywordPEKS(String c1, String c2){ //from contract table
        this.c1 = new BigInteger(c1,16);
        this.c2 = new BigInteger(c2,16);
    }

    public KeywordPEKS(Document d){ //from contract table
        this._id = d.get("_id");
        this.c1 = new BigInteger(d.get("c1").toString(),16);
        this.c2 = new BigInteger(d.get("c2").toString(),16);
    }

    public KeywordPEKS(BigInteger c1, BigInteger c2){ //from contract table
        this.c1 = c1;
        this.c2 = c2;
    }

}
