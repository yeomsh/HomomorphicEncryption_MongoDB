package HomomorphicEncryption;

import java.math.BigInteger;
public class AGCDPublicKey implements Comparable<AGCDPublicKey>{

    private BigInteger q;
    public BigInteger r;
    public BigInteger pk;
    public AGCDPublicKey(BigInteger pk){
        this.pk = pk;
    }
    public AGCDPublicKey(BigInteger pk, BigInteger q, BigInteger r){
        this.pk = pk;
        this.q = q;
        this.r = r;
    }
    public void setQ(BigInteger q){
        this.q = this.q.add(q);
    }
    public void setR(BigInteger r){ this.r = this.r.add(r); }
    public BigInteger getQ() {
        return q;
    }
    public BigInteger getR(){return r;}
    public void setPk(BigInteger p){
        this.pk = p.multiply(q).add(r);
    }

    @Override
    public int compareTo(AGCDPublicKey AGCDPublicKey) {
        //return this.getPublicKey().compareTo(publicKey.getPublicKey()); //오름차순
        return AGCDPublicKey.pk.compareTo(this.pk); //내림차순
    }
}
