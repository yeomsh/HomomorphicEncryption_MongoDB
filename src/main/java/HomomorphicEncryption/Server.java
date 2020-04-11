package HomomorphicEncryption;

import com.mongodb.client.MongoCursor;
import org.bson.Document;

import javax.print.Doc;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Vector;

import static com.mongodb.client.model.Filters.eq;

public class Server {

    private BigInteger p; //서버의 개인키
    private BigInteger a; //서버alpha
    public NoSQLDB nosqldb;


    public Server(BigInteger p, BigInteger a) {
        this.p = p;
        this.a = a;
        this.nosqldb = new NoSQLDB();
    }

    public void addSystemAlpha(Data data) { //user alpha 지우고, system alpha 입히기
        data.c1 = data.makeCi(data.c1.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? data.c1.mod(p).subtract(p) : data.c1.mod(p), a);
        data.c3 = data.makeCi(data.c3.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? data.c3.mod(p).subtract(p) : data.c3.mod(p), a);
    }

    public Boolean keywordTest(Data d1, KeywordPEKS d2) {
        //분모
        BigInteger parent = d1.c1.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? d1.c1.mod(p).subtract(p) : d1.c1.mod(p);
        parent = hash(parent.mod(d1.getUser().getAu()));
        System.out.println("H(Ci1 mod p mod a)(2^hexadecimal): 2^" + parent.toString(16));
        parent = parent.add(d2.c2);

        //분자
        BigInteger child = d2.c1.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? d2.c1.mod(p).subtract(p) : d2.c1.mod(p);
        child = hash(child.mod(a));
        System.out.println("H(Cj1 mod p mod a)(2^hexadecimal) : 2^" + child.toString(16));
        child = child.add(d1.c2);

        System.out.println();
        System.out.println("H(Ci1 mod p mod a)*Cj2(2^hexadecimal) : 2^" + parent);
        System.out.println("H(Cj1 mod p mod a)*Ci2(2^hexadecimal) : 2^" + child);

        return parent.subtract(child).equals(BigInteger.ZERO);
    }

    public Boolean keywordTest(Data[] datas, KeywordPEKS d2) {
        for (Data d1 : datas) {
            //분모
            BigInteger parent = d1.c1.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? d1.c1.mod(p).subtract(p) : d1.c1.mod(p);
            parent = hash(parent.mod(d1.getUser().getAu()));
            System.out.println("H(Ci1 mod p mod a)(2^hexadecimal): 2^" + parent.toString(16));
            parent = parent.add(d2.c2);

            //분자
            BigInteger child = d2.c1.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? d2.c1.mod(p).subtract(p) : d2.c1.mod(p);
            child = hash(child.mod(a));
            System.out.println("H(Cj1 mod p mod a)(2^hexadecimal) : 2^" + child.toString(16));
            child = child.add(d1.c2);

            System.out.println();
            System.out.println("H(Ci1 mod p mod a)*Cj2(2^hexadecimal) : 2^" + parent);
            System.out.println("H(Cj1 mod p mod a)*Ci2(2^hexadecimal) : 2^" + child);
            if (parent.subtract(child).equals(BigInteger.ZERO)) {
                d1.isExist = true;
                return true;
            }
        }
        return false;
    }

    public Boolean keywordTest(Data d1, Contract d2) { //c3(권한 테스트 함수)
        //분모
        BigInteger parent = d1.c3.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? d1.c3.mod(p).subtract(p) : d1.c3.mod(p);
        parent = hash(parent.mod(d1.getUser().getAu()));
        System.out.println("H(Ci1 mod p mod a)(2^hexadecimal): 2^" + parent.toString(16));
        parent = parent.add(d2.c2);

        //분자
        BigInteger child = d2.c3.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? d2.c3.mod(p).subtract(p) : d2.c3.mod(p);
        child = hash(child.mod(a));
        System.out.println("H(Cj1 mod p mod a)(2^hexadecimal) : 2^" + child.toString(16));
        child = child.add(d1.c2);

        System.out.println();
        System.out.println("H(Ci1 mod p mod a)*Cj2(2^hexadecimal) : 2^" + parent);
        System.out.println("H(Cj1 mod p mod a)*Ci2(2^hexadecimal) : 2^" + child);

        return parent.subtract(child).equals(BigInteger.ZERO);
    }

    public BigInteger hash(BigInteger exponent) {
        return exponent;
    }

    //계약서 업로드
    public Object uploadContract_nosql(Data data) {
        //만약 updateData 함수를 바꾼다면 여기서 updateData한 다음 파일 추가
        //단, 복사본을 생성해서 바꾼 데이터로 사용

        Data copydata = data;
        addSystemAlpha(copydata);

        //파일 업로드
        //file id값 기억 필요 (data에 fileid추가)
        Object fileId = nosqldb.insertContract(copydata);

        return fileId;
    }

    //키워드 업로드 및 처음 생성하는 키워드에 대한 zindex 생성
    public Vector<Object> uploadKeyword_nosql(Data[] datas) {

        MongoCursor<Document> cursor;
        Vector<Object> saveKeywordId = new Vector<>();

        //해당 키워드 찾기 및 키워드 추가
        cursor = nosqldb.keywordPEKS.find().iterator();
        try {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                KeywordPEKS keyword = new KeywordPEKS(d);
                System.out.println(d.toJson());
                System.out.println(keyword._id);
                //키워드와 비교해서 같은 것이 있으면 키워드 id 기억
                if (keywordTest(datas, keyword)) {
                    saveKeywordId.add(keyword._id);
                }
            }
            for (Data data : datas) {
                if (!data.isExist) { //존재하지 않는 것이 있다면 키워드 추가
                    addSystemAlpha(data);
                    Object _id = nosqldb.insertKeywordPEKS(data); //키워드암호문 id
                    saveKeywordId.add(_id);
                }
            }
        } finally {
            cursor.close();
        }

        return saveKeywordId;
    }

    public void updateZString_nosql(Vector<Object> saveKeywordId, Object fileId) {
        //zindex 모두 update하기
        nosqldb.updateZString(saveKeywordId, fileId);
    }


    public Vector<Object> searchKeyword_nosql(Data data) {
        //         3. 검색
        //            1) kewordPEKS로 있는 키워드 찾기 -> keywordId기억
        //            2) 해당 keywordId 중 file의 string 만들어서 비교
        //            3) 1인 애들의 fileId로 ci2,ci3 비교하기
        Vector<Object> correctFile = new Vector<>();

        MongoCursor<Document> cursor;
        Vector<Object> saveKeywordId = new Vector<>();

        String zstring = "";

        cursor = nosqldb.keywordPEKS.find().iterator();
        try {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                KeywordPEKS keyword = new KeywordPEKS(d);
                //키워드와 비교해서 같은 것이 있으면 키워드 id 기억
                if (keywordTest(data, keyword)) {
                    zstring += "1";
                    saveKeywordId.add(keyword._id);
                } else
                    zstring += "0";
            }
        } finally {
            cursor.close();
        }
        //키워드의 아이디를 찾았으면 -> 해당 키워드 id의 파일들 가져와서 exist 비교
        //하나는 string 만들고 (파일들 다 가지고 와야할까?)
        Vector<Document> sample = new Vector<>();
        Vector<ArrayList> temp = new Vector<>();
        for (int i = 0; i < saveKeywordId.size(); i++) {
            //해당 키워드의 zindex 중 file을 가지고 옴
            sample.add(nosqldb.zindex.find(eq("_id", saveKeywordId.get(i))).first());
            temp.add((ArrayList) sample.get(i).get("file"));
        }
        System.out.println(saveKeywordId.toString());
        System.out.println(sample.toString());
        System.out.println(temp.toString());
        //ArrayList의 파일들끼리 비교
        //파일이 1개이상 있다면 zString만들기,,,?
        //지금은 키워드 1개니깐 pass
        int fileCnt = temp.size();
        Vector<String> z = new Vector<>();
        for (int i = 0; i < temp.size(); i++) {
            String s = "";
            for (int j = 0; j < temp.get(i).size(); j++) {
                s += ((Document) temp.get(i).get(j)).get("exist");
            }
            z.add(s);
            System.out.println("nowwww : " + s);
        }
        if (z.size() == 0) System.out.println("noooooo keyword");
            //temp.get(0).get(j)의 exist가 1인 애들의 fileid get한 후
            //filepeks에서 파일 아이디 찾아서
            //contract로 만들고
            //비교 후 반환
        else {
            for (int i = 0; i < z.get(0).length(); i++) {
                if (z.get(0).charAt(i) == '1') {
                    Document d = nosqldb.filePEKS.find(eq("_id", ((Document) temp.get(0).get(i)).get("fileId"))).first();
                    System.out.println(d.toJson());
                    if (d != null) {
                        Contract res = new Contract(d);
                        //c2 c3비교
                        if (keywordTest(data, res)) { //파일에 속한 권한 비교
                            correctFile.add(res._id);
                            //                      System.out.println(res.id+ "번째 파일이 키워드/ 권한 동일함");
                        }
                    }
                }
            }
        }
        System.out.println("this is the correctFile : " + correctFile);
        return correctFile;
    }
}
