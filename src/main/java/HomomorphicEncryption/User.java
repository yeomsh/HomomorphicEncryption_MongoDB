package HomomorphicEncryption;

import java.util.Random;
import java.util.Vector;
import java.math.BigInteger;

public class User {
    private int qidRange = 2;
    private int rRange = 2;
    private int pkSize = 5;

    public BigInteger qid;
    public BigInteger r;
    public Vector<PublicKey> pk = new Vector<>();

    private BigInteger au;

    Random rand = new Random();
    public User(){

    }
    public User(Vector<PublicKey> pkSet){
        this(100,60,4,pkSet);
    }

    public User(int qidRange, int rRange, int pkSize, Vector<PublicKey> pkSet){
        this.qidRange = qidRange;
        this.rRange = rRange;
        this.pkSize = pkSize;
        UserKeyGen(pkSet);
    }

    //사용자의 키 생성 (qid, r, pk)
    void UserKeyGen(Vector<PublicKey> pkSet){
        qid = new BigInteger(qidRange,rand);
        r = new BigInteger(rRange,rand);
        makeUserKeySet(pkSet);
    }

    //나중에 검색문에서 사용할 r 변경 가능하도록
    void ChangeUserR(){
        r = new BigInteger(rRange,rand);
    }

    //사용자마다 랜덥의 public key set 만드는 함수
    void makeUserKeySet(Vector<PublicKey> pkSet){
        this.pk.add(pkSet.firstElement());
        boolean usedpk[] = new boolean[pkSet.size()]; //default = false
        usedpk[0] = true; //x0 넣기
        while(this.pk.size() < pkSize) {
            int pknum = (int)(Math.random()*pkSet.size());
            if(usedpk[pknum]) continue;
            usedpk[pknum] = true;
            this.pk.add(pkSet.get(pknum));
        }
        System.out.println("user-selected pkSet's index : " + usedpk);
        for(int i = 0; i<this.pk.size(); i++) {
            if (i == 0) System.out.println("x0(hexadecimal) : " + this.pk.get(i).pk.toString(16) );
            else System.out.println(i + "(hexadecimal) : " + this.pk.get(i).pk.toString(16) );
        }
    }
    public void setAu(BigInteger au){
        this.au = au;
    }

    public BigInteger getAu(){
        return au;
    }

}
