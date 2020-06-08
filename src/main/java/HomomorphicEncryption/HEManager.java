//package HomomorphicEncryption;
//
//import DataClass.CipherData;
//import DataClass.Contract;
//import DataClass.User;
//import ECIES.ECIESManager;
//import org.json.simple.JSONObject;
//import util.StringUtil;
//import java.math.BigInteger;
//import java.util.HashMap;
//import java.util.Vector;
//
//public class HEManager {
//
//    public static KGC KGC;
//    public static HEServer HEServer;
//
//    public HEManager(User user){
//        KGC = new KGC();
//        HEServer = new HEServer(KGC.getP(), KGC.getSystemAlpha(), new NoSQLDB(user));
//    }
////    public void setUserPKSet(User user){
////        user.setPKSet(KGC.pkSet);
////    }
//    public static void main(String args[]) {
//
//        //시작 전 kgc 및 server 생성
//        //     settingToStart();
//
//        //user생성
////        User userA = new User();
////        userA.setPKSet(KGC.pkSet);
//////
//////
////        userA.setAu(KGC.shareAlpha()); //kgc -> user에 alpha 공유 (임의로)4
////        userA.qid = new BigInteger("cb066fe11fed84bc5dcb04bbb", 16);
////
////        String a = "최승연";
////        String b = "염상희";
////
////        JSONObject file = new JSONObject();
////        file.put("key","value");
////        file.put("key2","염");
//
//        // requestToUpload(userA, new String[]{a, b},file);
//        //현재 키워드 20개
//        //파일 10개
////        for (int i = 0; i < 500; i++)
////                requestToUpload(userA, new String[]{a + i, b + i});
//
////        for(int j=0;j<2;j++) {
////            for (int i = 0; i < 50; i++)
////                requestToUpload(userA, new String[]{a + i, b + i});
////        }
////        for(int j=0;j<10;j++) {
////            for (int i = 0; i < 8; i++)
////                requestToUpload(userA, new String[]{a + i,b});
////            requestToUpload(userA, new String[]{a,b});
////            requestToUpload(userA, new String[]{a,b + 1});
////        }
//
//        //Vector<CipherContract> c = searchKeyword(userA,a);
//
////       for(JSONObject cont : c)
////           System.out.println(cont.toString());
//        //일단 테스트 용
//        /*
//        1. keywordPEKS, filePEKS, zindex 생성
//        2. 업로드
//            1) filePEKS에 등록 (e(file)빼고, ci2 : "ci2", ci3 : "ci3") (오케이)
//            2) keywordPEKS에 추가 OR 그대로
//                - C1 = "C1", C2 = "C1" ...
//                - keywordPEKS 다 가지고 와서 c1 == input && c2 == input -> save keywordId
//                - if keyword is not exist, add keyword to keywordPEKS
//
//            3) zindex 변경
//                - if 새로운 키워드 업로드 -> add zindex (copy 후 값 변경)
//                - 새로운 키워드 아니라면 -> 파일에 있는 키워드라면 file추가 1, 아니면 0
//
//         3. 검색
//            1) kewordPEKS로 있는 키워드 찾기 -> keywordId기억
//            2) 해당 keywordId 중 file의 string 만들어서 비교
//            3) 1인 애들의 fileId로 ci2,ci3 비교하기
//         */
//
//    }
//
//
//    //시작전 kgc 및 server 생성
//    public static void settingToStart(User user){
//        KGC = new KGC();
//        HEServer = new HEServer(KGC.getP(), KGC.getSystemAlpha(), new NoSQLDB(user));
//    }
//
//
//    public void requestToUpload(User user, Contract contract) throws Exception {
//
//        long start = System.currentTimeMillis();
//        //근로자 or 점주 둘 중한명만 파일등록함
//        System.out.println("user.getAu() : " + user.getAlpha());
//        System.out.println("kgc.pkset0 : " + KGC.pkSet.get(0));
//        System.out.println("contract.fileData : "+ contract.fileData);
//        if(contract.fileData == null){
//            ECIESManager eciesManager = new ECIESManager();
//            contract.fileData = eciesManager.decryptCipherContract(contract.cipher, user.eciesPrivateKey,contract.IV); //이걸 static으로 만들고싶음
//        }
//        String[] keywordArr = new String[]{((JSONObject)contract.fileData.get("oSign")).get("oSign1").toString(), contract.fileData.get("wName").toString()};
//        System.out.println("oSign: "+((JSONObject)contract.fileData.get("oSign")).get("oSign1").toString());
//        System.out.println("wName: "+contract.fileData.get("wName").toString());
//        //점주는 노동자의 qid를 모르기때뭉네 한꺼번에 업로드할 수 없음
//        user.changeR();
//        //일단 oName이라 해두는데 , 이건 자기 타입에 맞게 oName or wName으로 분기시키면 될듯
//        Object fileId = HEServer.uploadContract_nosql(new CipherData(null,user.getC2(), user.getC3(),user.getAlpha(),user.pkSum),contract);
//        System.out.println("file id: "+fileId);
//        //키워드 기반 암호문 생성
//        CipherData[] cipherDatas = new CipherData[2];
//        for(int i = 0; i<2;i++){ //한 파일에 키워드가 2개니까 !
//            user.changeR();
//            cipherDatas[i] = new CipherData(user.getC1(new BigInteger(StringUtil.SHA1(keywordArr[i]),16)),user.getC2(),user.getC3(),user.getAlpha(),user.pkSum);
//        }
//        System.out.println("requestToUpload: uploadKeyword_nosql");
//        HashMap<Object,Boolean> saveKeywordId = HEServer.uploadKeyword_nosql(cipherDatas);
//        System.out.println("requestToUpload: updateZString_nosql");
//        HEServer.updateZString_nosql(saveKeywordId,fileId);
//        long end = System.currentTimeMillis();
//        System.out.println("파일 업로드 시간 : " + (end-start));
//    }
//    //    //파일 업로드
////    public static void requestToUpload(User user, String[] keywords, JSONObject file){
////        user.setAu(KGC.shareAlpha());
////        long start = System.currentTimeMillis();
////        //근로자 or 점주 둘 중한명만 파일등록함
////        user.ChangeUserR();
////        System.out.println("user.pkset " + user.pkSet);
////        System.out.println("user.getAu() : " + user.getAu());
////        System.out.println("kgc.pkset0 : " + KGC.pkSet.get(0));
////        Object fileId = HEServer.uploadContract_nosql(new CipherData(user, new BigInteger(StringUtil.SHA1(keywords[0]),16),user.getAu(), KGC.pkSet),file);
////        //키워드 기반 암호문 생성
////        CipherData[] cipherDatas = new CipherData[2];
////        for(int i = 0; i<2;i++){ //한 파일에 키워드가 2개니까 !
////            user.ChangeUserR();
////            cipherDatas[i] = new CipherData(user, new BigInteger(StringUtil.SHA1(keywords[i]),16),user.getAu(), KGC.pkSet);
////        }
////        System.out.println("requestToUpload: uploadKeyword_nosql");
////        //Vector<Object> saveKeywordId = HEServer.uploadKeyword_nosql(cipherDatas);
////        HashMap<Object,Boolean> uploadKeywordMap = HEServer.uploadKeyword_nosql(cipherDatas);
////        System.out.println("requestToUpload: updateZString_nosql");
////        HEServer.updateZString_nosql(uploadKeywordMap,fileId);
////        long end = System.currentTimeMillis();
////        System.out.println("파일 업로드 시간 : " + (end-start));
////
////    }
//    //키워드 검색
//    public static Vector<JSONObject> searchKeyword(User user, String keyword) throws Exception {
//
//        long start = System.currentTimeMillis();
//        user.changeR();
//        CipherData cipherData = new CipherData(user.getC1(new BigInteger(StringUtil.SHA1(keyword),16)),user.getC2(),user.getC3(),user.getAlpha(),user.pkSum);
//        Vector<JSONObject> keywordFile = HEServer.searchKeyword_nosql(cipherData);
//        System.out.println(keyword);
//        long end = System.currentTimeMillis();
//        System.out.println("time to find file : " + (end-start));
//        return keywordFile;
//    }
//}