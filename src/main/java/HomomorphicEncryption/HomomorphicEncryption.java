package HomomorphicEncryption;

import com.mongodb.client.*;
import org.bson.Document;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import static com.mongodb.client.model.Filters.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//qid: (1번째 최승연) fe10876d3bcaac90ba0968a4c
//qid: (2번째 최승연) cb066fe11fed84bc5dcb04c08
//qid: (3번째 최승연) 8efe655fd42e129d02264320a
//qid: (4번째 염상희) cb4bfb4affd69a854de71aa0c
//qid: (5번째 염상희) cb4bfb4affd69a854de71aa0c

public class HomomorphicEncryption {

    public static KGC kgc;
    public static Server server;
    public static void main(String args[]) {

        //시작 전 kgc 및 server 생성
        settingToStart();

        //user생성
        User userA = new User(kgc.pkSet);
//
//
        userA.setAu(kgc.shareAlpha()); //kgc -> user에 alpha 공유 (임의로)4
//        userA.qid = new BigInteger("cb066fe11fed84bc5dcb04c08", 16);

        String a = "최승연연";
        String b = "염상희희";

       // requestToUpload(userA, new String[]{a, b});
        //현재 키워드 20개
        //파일 10개
//        for (int i = 0; i < 500; i++)
//                requestToUpload(userA, new String[]{a + i, b + i});

//        for(int j=0;j<2;j++) {
//            for (int i = 0; i < 50; i++)
//                requestToUpload(userA, new String[]{a + i, b + i});
//        }
//        for(int j=0;j<10;j++) {
//            for (int i = 0; i < 8; i++)
//                requestToUpload(userA, new String[]{a + i,b});
//            requestToUpload(userA, new String[]{a,b});
//            requestToUpload(userA, new String[]{a,b + 1});
//        }

       searchKeyword(userA,a);

        //일단 테스트 용
        /*
        1. keywordPEKS, filePEKS, zindex 생성
        2. 업로드
            1) filePEKS에 등록 (e(file)빼고, ci2 : "ci2", ci3 : "ci3") (ok)
            2) keywordPEKS에 추가 OR 그대로
                - C1 = "C1", C2 = "C1" ...
                - keywordPEKS 다 가지고 와서 c1 == input && c2 == input -> save keywordId
                - if keyword is not exist, add keyword to keywordPEKS

            3) zindex 변경
                - if 새로운 키워드 업로드 -> add zindex (copy 후 값 변경)
                - 새로운 키워드 아니라면 -> 파일에 있는 키워드라면 file추가 1, 아니면 0

         3. 검색
            1) kewordPEKS로 있는 키워드 찾기 -> keywordId기억
            2) 해당 keywordId 중 file의 string 만들어서 비교
            3) 1인 애들의 fileId로 ci2,ci3 비교하기
         */

    }


    //시작전 kgc 및 server 생성
    public static void settingToStart(){
        kgc = new KGC();
        server = new Server(kgc.getP(),kgc.getA());
    }

    public static String SHA1(String str){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1"); // 이 부분을 SHA-256, MD5로만 바꿔주면 된다.
            md.update(str.getBytes()); // "세이프123"을 SHA-1으로 변환할 예정!

            byte byteData[] = md.digest();
            StringBuffer sb = new StringBuffer();
            for(int i=0; i<byteData.length; i++) {
                sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return "fail";

    }

    //파일 업로드
    public static void requestToUpload(User user, String[] keywords){
        long start = System.currentTimeMillis();
        //근로자 or 점주 둘 중한명만 파일등록함
        user.ChangeUserR();

        Object fileId = server.uploadContract_nosql(new Data(user, new BigInteger(SHA1(keywords[0]),16),user.getAu(),kgc.pkSet));
        //키워드 기반 암호문 생성
        Data[] datas = new Data[2];
        for(int i = 0; i<2;i++){ //한 파일에 키워드가 2개니까 !
            user.ChangeUserR();
            datas[i] = new Data(user, new BigInteger(SHA1(keywords[i]),16),user.getAu(),kgc.pkSet);
        }
        System.out.println("requestToUpload: uploadKeyword_nosql");
        Vector<Object> saveKeywordId = server.uploadKeyword_nosql(datas);
        System.out.println("requestToUpload: updateZString_nosql");
        server.updateZString_nosql(saveKeywordId,fileId);

        long end = System.currentTimeMillis();

        System.out.println("파일 업로드 시간 : " + (end-start));

    }

    //키워드 검색
    public static void searchKeyword(User user, String keyword){
        long start = System.currentTimeMillis();
        Vector<Object> correctFile = new Vector<>();
        Data data = new Data(user, new BigInteger(SHA1(keyword),16));
        correctFile = server.searchKeyword_nosql(data);
        long end = System.currentTimeMillis();

        System.out.println("time to find file : " + (end-start));

    }
}