package HomomorphicEncryption;

import java.util.Random;
import java.util.Vector;
import java.util.Collections;
import java.math.BigInteger;


/* 20200222 수정사항
 * w + riqid < a <p 해당 항목들 모두 다 biginteger
 * 기존에서 변경된 부분
 * 1. a,p,
 * kgc부분 biginteger로 수정 및 계산 가능하게 변경
 */
public class KGC {
    Random r = new Random();

    public  BigInteger lamda = new BigInteger("164"); //한글 한글자로 test할려면 최소 5이성
    public BigInteger eta = new BigInteger("2000"); //원래 조건 -> (int)(Math.random()*Math.pow(lamda,2)), 개인키의 길이
    public  BigInteger gamma = new BigInteger("36896"); //원래 조건 -> (int)(Math.random()*Math.pow(lamda,5)) -> 현재 lamda^3
    public  BigInteger pkSetSize; //감마 + 람다 (but 너무 커서 일단 감마^3+람다로)



    private  BigInteger p = new BigInteger("1090aab060716f966f554558a15e41f6c05cb1159bc6aa7eb9077471b35f1599bf3f0d22b4ce61d85dd9f4150701d23999a15f24f2b8befb100f8a156b15d71a3a766a77665823c7f227c1b367b97394b417fe1092a8173d7accbf2270a6c39315a3ee3578d828550b1599a14b811f017482eb8ed88d9f418abb0fc20a2df00479351cf6443df344269bd7aee8b6fc0f21069b53c95c15fc248558f86ff79574b2e07e5c7ca359ce3a16b68ecbbd0f4ff8f04347ba713e53c81aad9be4c1b3a06da29cba7aa1c81ca9f6213ebf539a5dacf220e79957399dc8a06c417d57272de9b0ea7f72d8841ac22a5f98749cd91006d414a623f7b2bd3485f",16); // 비트의 수가 eta (lamda^2)
    private  BigInteger a = new BigInteger("174682bb762fb605edd9dea02a610fd6cc6ad6ea2b",16); //system alpha

    public Vector<PublicKey> pkSet = new Vector<>();

    private BigInteger au;

    public KGC(){
        this(new BigInteger("10"));
    }

    public KGC(BigInteger pkSetSize){
        this.pkSetSize = pkSetSize;
        System.out.println("lamda : " + lamda +", eta : "+eta+", gamma : "+ gamma + "pkSetSize : " + pkSetSize);
        System.out.println("bit length of p : "+eta + ", bit length of a(alpha) : " + lamda + "\n");
        makePublicKeySet(this.pkSetSize);
    }

    public KGC(BigInteger pkSetSize, BigInteger lamda, BigInteger gamma, BigInteger p, BigInteger a){
        this.pkSetSize = pkSetSize;
        this.lamda = lamda;
        this.gamma = gamma;
        this.p = p;
        this.a = a;
        makePublicKeySet(this.pkSetSize);
    }

    private void makePublicKeySet(BigInteger pkSetSize) {
    /*
        Xi = p*qi + ri
        qi = 0 ~ (2^lamda)/2 // 0~ any integer
        ri = - (2^lamda) ~ (2^lamda)
         */
        pkSet.clear(); //pkSet 초기화

        System.out.println("p(16) = " + p.toString(16));
        System.out.println("a(16) = " + a.toString(16));

        //pkSet 뽑기 (kgc의 공개키)
        BigInteger qMax = new BigInteger("2").pow(gamma.intValue()).divide(p);
        for(int i = 0; i<pkSetSize.intValue(); i++){
            pkSet.add(new PublicKey(p,new BigInteger(qMax.bitLength()-1,r),new BigInteger(lamda.intValue()-1,r).multiply(new BigInteger("-1").pow(new BigInteger(1,r).intValue()))));
        }

        Collections.sort(pkSet); //X0 is the largest element

        //조건에 맞는 xo만들기
        pkSet.set(0,checkX0Condition(pkSet.get(0),a));
//        if (!pkSet.get(0).pk.mod(p).equals(a)){ //X0 mod p = a 조건 체크
//            BigInteger rest = p.subtract(pkSet.get(0).pk.mod(p).subtract(a));
//            pkSet.get(0).setR(rest);
//            while (pkSet.get(0).pk.mod(a).equals(BigInteger.valueOf(0))){ //x0 mod a =0 이면, x0 값 증가시킴 //근데 따져보니까 (p/a)*x =(k -1 ) 을 만족하는 x와 k가 있으면, 이 조건이 성립하는데 아마 그런경우가 많이 없을듯해
//                pkSet.get(0).setQ(p);
//            }
//        }
        System.out.println("x0 길이 : "+pkSet.get(0).pk.toString(16).length());
        System.out.println("Q 길이 : "+pkSet.get(0).getQ().toString(16).length());
        System.out.println("\nKGC-selected pkSet");
        for(int i=0;i<pkSet.size();i++) {
            if (i == 0) System.out.println("x0(hexadecimal) : " + pkSet.get(i).pk.toString(16) );
            else System.out.println(i + "(hexadecimal) : " + pkSet.get(i).pk.toString(16) );
        }
    }

    //au 값 설정 후 return
    public BigInteger shareAlpha(){
        //함수 사용 방법 -> user와 kgc에 au변수 추가 -> makeC1(a 대신 au)
        //KGC로 함수 옮기는 게 좋을 듯?
        //알파 값 설정
        au = BigInteger.ONE;
        for(int i=0;i<lamda.intValue();i++){
            au = au.multiply(BigInteger.TWO);
        }
        au = au.add(new BigInteger(lamda.intValue()-1,r)).nextProbablePrime();//최대 2^19

        return au;
    }
    public PublicKey checkX0Condition(PublicKey x0, BigInteger alpha){
        System.out.println(x0.pk.toString(16));
        System.out.println(x0.r.toString(16));
        if (!x0.pk.mod(p).equals(alpha)){ //X0 mod p = a 조건 체크
            System.out.println("checkX0Condition: 조건안맞아!");
            BigInteger rest = p.subtract(x0.pk.mod(p).subtract(alpha));
            x0.setR(rest);
            while (x0.pk.mod(alpha).equals(BigInteger.ZERO)){ //x0 mod a =0 이면, x0 값 증가시킴 //근데 따져보니까 (p/a)*x =(k -1 ) 을 만족하는 x와 k가 있으면, 이 조건이 성립하는데 아마 그런경우가 많이 없을듯해
                x0.setQ(p);
            }
        }
        System.out.println(x0.pk.toString(16));
        System.out.println(p.toString(16));
        System.out.println(x0.r.toString(16));
        return x0;
    }


    //주어진 au값으로 설정
    public void setAu(BigInteger au){
        this.au = au;
    }

    public BigInteger getAu(){
        return au;
    }
    public BigInteger getP() {
        return p;
    }

    public BigInteger getA() {
        return a;
    }
}
