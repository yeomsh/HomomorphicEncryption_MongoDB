package HomomorphicEncryption;

import java.math.BigInteger;

public class AGCDPublicKey implements Comparable<AGCDPublicKey>{
    private BigInteger p;
    private BigInteger q;
    public BigInteger r;
    public BigInteger pk;
    public AGCDPublicKey(BigInteger p, BigInteger q, BigInteger r){
        this.p = p;
        this.q = q;
        this.r = r;
        setPk();
    }
    public void setQ(BigInteger q){
        this.q = this.q.add(q);
        setPk();
    }
    public void setR(BigInteger r){
        if(r.compareTo(p)<0)
            this.r = this.r.add(r);
        else {
            this.q = this.q.add(r.divide(p));
            this.r = this.r.add(r.mod(p));
        }
        setPk();
    }
    public BigInteger getQ() {
        return q;
    }
    public void setPk(){
        this.pk = p.multiply(q).add(r);
    }

    @Override
    public int compareTo(AGCDPublicKey AGCDPublicKey) {
        //return this.getPublicKey().compareTo(publicKey.getPublicKey()); //오름차순
        return AGCDPublicKey.pk.compareTo(this.pk); //내림차순
    }
}
