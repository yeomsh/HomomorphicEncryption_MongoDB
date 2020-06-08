package DataClass;

import java.math.BigInteger;

public class CipherData {

    private BigInteger pkSum;
    private BigInteger alpha;
    public BigInteger c1;
    public BigInteger c2;
    public BigInteger c3;
    public Boolean isExist = false;
    public Object fileId = null;
    public CipherData(String c1, String c2, String c3, BigInteger alpha, BigInteger pkSum){
        if(c1 != null)
            this.c1 = new BigInteger(c1,16);
        this.c2 = new BigInteger(c2,16);
        if(c3 != null)
            this.c3 = new BigInteger(c3,16);
        if(alpha!=null)
            this.alpha = alpha;
        if(pkSum!=null)
            this.pkSum = pkSum;
    }
    public CipherData(BigInteger c1, BigInteger c2, BigInteger c3){
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
    }
    public CipherData(BigInteger c1, BigInteger c2, BigInteger c3, BigInteger alpha, BigInteger pkSum){
        this(c1,c2,c3);
        this.alpha = alpha;
        this.pkSum = pkSum;
    }


    public BigInteger makeCi(BigInteger ci, BigInteger systemAlpha){ //userALPHA 벗기고 systemalpha 입히기
//        if (ci.mod(alpha).equals(w.add(user.r.multiply(user.qid)))){
//            System.out.println("벗겻더니 동일해");
//        }
//        else{
//            System.out.println("안동일해");
//        }
//        if (ci.mod(user.getAu()).equals(user.qid.add(user.r.multiply(user.qid)))){
//            System.out.println("벗겻더니 동일해");
//        }
//        else{
//            System.out.println("안동일해");
//        }
        return ci.mod(alpha).add(systemAlpha.multiply(pkSum));
    }
    public void setFileId(Object fileId) {
        this.fileId = fileId;
    }
    public BigInteger getAlpha(){
        return alpha;
    }
    public BigInteger getPkSum(){
        return pkSum;
    }
}