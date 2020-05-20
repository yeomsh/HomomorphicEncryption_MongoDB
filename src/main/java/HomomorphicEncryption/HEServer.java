package HomomorphicEncryption;

import DataClass.Contract;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import ECIES.ECIESManager;
import org.bson.Document;
import org.json.simple.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import static com.mongodb.client.model.Filters.eq;

public class HEServer {

    private BigInteger p; //서버의 개인키
    private BigInteger a; //서버alpha
    public NoSQLDB nosqldb;
    public HEServer(BigInteger p, BigInteger a, NoSQLDB noSQLDB) {
        this.p = p;
        this.a = a;
        this.nosqldb = noSQLDB;
    }

    public void addSystemAlpha(CipherData cipherData) { //user alpha 지우고, system alpha 입히기
        cipherData.c1 = cipherData.makeCi(cipherData.c1.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? cipherData.c1.mod(p).subtract(p) : cipherData.c1.mod(p), a);
        cipherData.c3 = cipherData.makeCi(cipherData.c3.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? cipherData.c3.mod(p).subtract(p) : cipherData.c3.mod(p), a);
    }

    public Boolean keywordTest(CipherData d1, KeywordPEKS d2) {
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

    public Boolean keywordTest(CipherData[] cipherDatas, KeywordPEKS d2) {
        for (CipherData d1 : cipherDatas) {
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

    public Boolean keywordTest(CipherData d1, CipherContract d2) { //c3(권한 테스트 함수)
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
    public Object uploadContract_nosql(CipherData cipherData, Contract contract) {
        //만약 updateData 함수를 바꾼다면 여기서 updateData한 다음 파일 추가
        //단, 복사본을 생성해서 바꾼 데이터로 사용
        CipherData copydata = cipherData;
        addSystemAlpha(copydata);
        //파일 업로드
        //file id값 기억 필요 (data에 fileid추가)
        return nosqldb.insertContract(copydata,contract);
    }

    //키워드 업로드 및 처음 생성하는 키워드에 대한 zindex 생성
    public HashMap<Object,Boolean> uploadKeyword_nosql(CipherData[] cipherDatas) {
        MongoCursor<Document> cursor;
        HashMap<Object,Boolean> uploadKeywordMap = new HashMap<>(); //<id, 기존 키워드 여부>
        Vector<Object> saveKeywordId = new Vector<>();
        //해당 키워드 찾기 및 키워드 추가
        cursor = nosqldb.keywordPEKS.find().iterator();
        try {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                KeywordPEKS keyword = new KeywordPEKS(d);
                //키워드와 비교해서 같은 것이 있으면 키워드 id 기억
                if (keywordTest(cipherDatas, keyword)) {
                    uploadKeywordMap.put(keyword._id,true);
                    saveKeywordId.add(keyword._id);
                }
            }
            if (saveKeywordId.size() < 2) {//size가 2라는건, 일치하는 키워드로 이미 다 찾았다는 얘기
                for (CipherData cipherData : cipherDatas) {
                    if (!cipherData.isExist) { //존재하지 않는 것이 있다면 키워드 추가
                        addSystemAlpha(cipherData);
                        Object _id = nosqldb.insertKeywordPEKS(cipherData); //키워드암호문 id
                        saveKeywordId.add(_id);
                        uploadKeywordMap.put(_id,false);
                    }
                }
            }
        } finally {
            cursor.close();
        }
        return uploadKeywordMap;
    }

    public void updateZString_nosql(HashMap<Object,Boolean> saveKeywordId, Object fileId) {
        //zindex 모두 update하기
        nosqldb.updateZString(saveKeywordId, fileId);
    }


    public Vector<JSONObject> searchKeyword_nosql(CipherData cipherData) throws Exception {
        //         3. 검색
        //            1) kewordPEKS로 있는 키워드 찾기 -> keywordId기억
        //            2) 해당 keywordId 를 zindex에서 찾아, file : exist 값 가져옴
        //            3) 1인 애들의 fileId로 ci2,ci3 비교하기
        //Vector<Object> correctFile = new Vector<>();

        Vector<JSONObject> keywordFile = new Vector<>();
        MongoCursor<Document> cursor;
        cursor = nosqldb.keywordPEKS.find().iterator();
        KeywordPEKS keyword = null;
        Boolean isExist = false;
        try {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                keyword = new KeywordPEKS(d);
                //키워드와 비교해서 같은 것이 있으면 키워드 id 기억
                if (keywordTest(cipherData, keyword)) {
                    isExist = true;
                    break;
                    //saveKeywordId.add(keyword._id);
                }
            }
        } finally {
            cursor.close();
        }
        System.out.println("isExist: "+isExist);
        if(!isExist) return keywordFile;
        //키워드의 아이디를 찾았으면 -> 해당 키워드 id의 파일들 가져와서 exist 비교
        //하나는 string 만들고 (파일들 다 가지고 와야할까?)
        BasicDBObject filter = new BasicDBObject();
        filter.put("_id",0);
        filter.put("fileList",1);
        cursor = nosqldb.zindex.find(Filters.eq("_id",keyword._id)).projection(filter).iterator();
        Document d = cursor.next();
        ArrayList<Document> list = (ArrayList<Document>) d.get("fileList");
        HashMap<Object,Boolean> zIndexResult = new HashMap<>();
        ECIESManager eciesManager = new ECIESManager();
        for (Document doc: list){
            System.out.println("doc:\n"+doc);
            if(doc.getBoolean("exist")){
                zIndexResult.put(doc.get("_id"),true);
            }
        }
        if (zIndexResult.isEmpty()) return keywordFile; //일치하는 키워드 0개
        //계약서 파일 가져오기
        for(Object _id: zIndexResult.keySet()){
            Document doc = nosqldb.filePEKS.find(Filters.eq("_id",_id)).first();
            if(doc != null){
                CipherContract cipherContract = new CipherContract(doc);
                //c2 c3비교
                if (keywordTest(cipherData, cipherContract)) { //파일에 속한 권한 비교
                    keywordFile.add(eciesManager.decryptCipherContract(cipherContract.cipher, nosqldb.myUser.eciesPrivateKey,cipherContract.IV));
                }
            }

        }
        System.out.println("this is the correctFile : " + keywordFile.size());
        return keywordFile;
    }
}
