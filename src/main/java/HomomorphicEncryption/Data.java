package HomomorphicEncryption;

import java.math.BigInteger;
import java.util.Vector;

public class Data {

    private User user;
    private BigInteger w;
    public BigInteger c1;
    public BigInteger c2;
    public BigInteger c3;
    public Boolean isExist = false;
    public Object fileId = null;

    public Data(User user, BigInteger w, BigInteger a, Vector<PublicKey> pkSet){
        this.user = user;
        this.w = w;
        makeC1(a, pkSet.get(0));
        makeC2();
        makeC3(a,pkSet.get(0));

    }

    public Data(User user, BigInteger w, PublicKey x0){
        this.user = user;
        this.w = w;

        makeC1(user.getAu(), x0);
        makeC2();
        makeC3(user.getAu(), x0);
    }

    public Data(User user, BigInteger w){
        this.user = user;
        this.w = w;

        makeC1(user.getAu(), user.pk.firstElement());
        makeC2();
        makeC3(user.getAu(), user.pk.firstElement());
    }

    public Data(BigInteger c1, BigInteger c2, BigInteger c3){
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.fileId = null;
    }

    public BigInteger makeCi(BigInteger ci, BigInteger systemAlpha){ //userALPHA 벗기고 systemalpha 입히기
        if (ci.mod(user.getAu()).equals(w.add(user.r.multiply(user.qid)))){
            System.out.println("벗겻더니 동일해");
        }
        else{
            System.out.println("안동일해");
        }
        if (ci.mod(user.getAu()).equals(user.qid.add(user.r.multiply(user.qid)))){
            System.out.println("벗겻더니 동일해");
        }
        else{
            System.out.println("안동일해");
        }
        return ci.mod(user.getAu()).add(systemAlpha.multiply(sumPk()));
    }
    void makeC1(BigInteger a, PublicKey x0){
        x0 = HomomorphicEncryption.kgc.checkX0Condition(x0,user.getAu());
        c1 = w.add(user.r.multiply(user.qid)).add(a.multiply(sumPk())); //w+(user.r*user.qid)+(a*sumPk);
        System.out.println("w(hexadecimal) = " + w.toString(16) + ", r(hexadecimal) = " + user.r.toString(16) + ", qid(hexadecimal) = " + user.qid.toString(16));
        System.out.println();
        c1 = c1.mod(x0.pk).compareTo(x0.pk.divide(BigInteger.TWO))>0 ? c1.mod(x0.pk).subtract(x0.pk) : c1.mod(x0.pk);
        // System.out.println("c1(hexadecimal): "+c1.toString(16));
    }
    void makeC2(){
        // System.out.println("riqid : " +user.r.multiply(user.qid));
        c2 = hash(user.r.multiply(user.qid));
        //System.out.println("c2(2^hexadecimal): 2^"+c2.toString(16));
    }
    void makeC3(BigInteger a, PublicKey x0){
        //ci1계산하기 (mod범위에 맞추어서)
        x0 = HomomorphicEncryption.kgc.checkX0Condition(x0,user.getAu());
        c3 = user.qid.add(user.r.multiply(user.qid)).add(a.multiply(sumPk())); //user.qid+(user.r*user.qid)+(a*sumPk);
        c3 = c3.mod(x0.pk).compareTo(x0.pk.divide(BigInteger.TWO))>0 ? c3.mod(x0.pk).subtract(x0.pk) : c3.mod(x0.pk);
        //    System.out.println("c3(hexadecimal): "+c3.toString(16));
    }
    public BigInteger sumPk(){
        BigInteger sumPk = BigInteger.ZERO;
        for (PublicKey publicKey : user.pk) {
            sumPk = sumPk.add(publicKey.pk);
        }
        return sumPk.subtract(user.pk.firstElement().pk);
    }
    public static BigInteger hash(BigInteger exponent){
        //return BigInteger.valueOf(2).pow(exponent.intValue());
        return exponent;
    }

    public User getUser() {
        return user;
    }


    public void setFileId(Object fileId) {
        this.fileId = fileId;
    }

    public Object getFileId(){ return fileId; }

}