package HomomorphicEncryption;

import com.mongodb.client.*;
import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Vector;

import static com.mongodb.client.model.Filters.*;

public class NoSQLDB {

    MongoClient mongoClient;
    MongoDatabase database;

    MongoCollection<Document> keywordPEKS;
    MongoCollection<Document> filePEKS;
    MongoCollection<Document> zindex;
    MongoCollection<Document> user;
    MongoCursor<Document> cursor;
    Vector<Object> saveKeywordId = new Vector<>();

    //
    public NoSQLDB(){
        mongoClient = MongoClients.create();
        database = mongoClient.getDatabase("mydb");
        keywordPEKS = database.getCollection("keywordPEKS");
        filePEKS = database.getCollection("filePEKS");
        zindex = database.getCollection("zindex");
        user = database.getCollection("user");
    }
    public void delete(){
        keywordPEKS.drop();
        filePEKS.drop();
        zindex.drop();
        user.drop();
    }
    public Object insertContract(Data data){
        Document filedoc = fileDoc(data.c2,data.c3);
        filePEKS.insertOne(filedoc);
        data.setFileId(filedoc.get("_id"));
        return filedoc.get("_id");
    }

    public Object insertKeywordPEKS(Data data){
        Document keyworddoc = keywordDoc(data.c1,data.c2);
        keywordPEKS.insertOne(keyworddoc);
        data.setFileId(keyworddoc.get("_id"));

        return keyworddoc.get("_id");
    }

    public Document keywordDoc(BigInteger c1, BigInteger c2){
        Document doc = new Document("c1", c1.toString(16))
                .append("c2", c2.toString(16));
        return doc;
    }

    public Document fileDoc(BigInteger c2, BigInteger c3){
        Document doc = new Document("c2", c2.toString(16))
                .append("c3", c3.toString(16))
                .append("file","");
        return doc;
    }

    public Document userDoc(User user){
        Document doc = new Document("id",user.id.toString(16)).append("ip",user.ip).append("userType",user.userType);
        return doc;
    }

    public Document keywordDoc(String c1, String c2){
        Document doc = new Document("c1", c1)
                .append("c2", c2);
        return doc;
    }

    public Document fileDoc(String c2, String c3){
        Document doc = new Document("c2", c2)
                .append("c3", c3)
                .append("file","");
        return doc;
    }

    public ArrayList<User> getUser(){ //loadip와 같은 역할
        ArrayList<User> uList = new ArrayList<User>();
        cursor = user.find().iterator();
        try {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                uList.add(new User(d));
            }
        } finally {
            cursor.close();
        }
        return uList;
    }
    public void insertUser(User u){
        Document userDoc = userDoc(u);
        user.insertOne(userDoc);
    }

    public void updateZString(Vector<Object> saveKeywordId, Object fileId){

        cursor = zindex.find().iterator();
        //find(): zindex의 모든 document 가져오기

        ArrayList<Document> arraylist = new ArrayList<>();
        Document sample;
        if(cursor.hasNext()) //기존에 키워드가 하나라도 등록되어 있으면 (그 idx의 파일 리스트로부터 id들을 쫙 가져옴)
            sample = cursor.next();
        else //그냥 내껏만 추가하면되어서 새로운 list
            sample = new Document("file",arraylist);
        System.out.println(saveKeywordId.toString());

        //savekeywordid에 대한 zstring 넣는 작업
        for(int i = 0;i< saveKeywordId.size();i++){
            if(zindex.find(eq("_id",saveKeywordId.get(i))).first()==null){ //현재 등록하려는 파일 아이디가 이미 들어있을 때
                //eq: if _id == savekeywordid.get(i)
                sample.put("_id",saveKeywordId.get(i));
                ArrayList temp = (ArrayList) sample.get("file");
                for(i=0;i<temp.size();i++) {
                    if(((Document) temp.get(i)).get("fileId").equals(fileId)) //기존에 fileId가 file z-index에 있다면?
                        temp.remove(i); //만약에 안지우면, 127line에서 temp.add를 하는데, 그럼 같은 파일아이디,같은 키워드에 대해 값이 11 두개 생기니까 미리지워줘
                    else
                        ((Document) temp.get(i)).put("exist", "0"); //put: 값 바꾸기
                }
                temp.add(new Document("fileId",fileId).append("exist","1")); //append: 추가하기
                sample.put("file",temp);
                System.out.println(sample.toJson());
                zindex.insertOne(sample);
            }
            else
                zindex.updateOne(eq("_id",saveKeywordId.get(i)),new Document("$push",new Document("file",new Document("fileId",fileId).append("exist","1"))));
                //사람이름 2개일 때 2개가 추가되는지 확인하고 수정하기
        }
        zindex.updateMany(not(elemMatch("file",eq("fileId",fileId))), new Document("$push",new Document("file",new Document("fileId",fileId).append("exist","0"))));
        //file에 내가 넣으려는 fildId가 없으면 싹다 0으로 바꿈 -> 모든 keywordid에 대해 file (document)에 대해 {exitst:0 ,fileid: fileid} 추가
        System.out.println(zindex.find(not(elemMatch("file",eq("fileId",fileId)))).first());
    }
}
