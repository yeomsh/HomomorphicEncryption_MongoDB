package DataClass;

import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import javax.print.Doc;
import java.util.ArrayList;

import static com.mongodb.client.model.Filters.elemMatch;
import static com.mongodb.client.model.Filters.eq;

public class Database {
    protected MongoClient mongoClient;
    protected MongoDatabase database;
    protected MongoCollection<Document> user;
    protected MongoCursor<Document> cursor;
    protected User myUser;
    public Database(){
        mongoClient = MongoClients.create();
        database = mongoClient.getDatabase("mydb");
        user = database.getCollection("user");
    }
    public Document userDoc(User user){
        Document doc = new Document("ip",user.ip).append("userType",user.userType.ordinal());
        return doc;
    }
    public Document ContractDoc(Contract contract){
        //step1,2,3
        ObjectId _id = new ObjectId();
        Document doc = new Document("_id",_id).append("step",contract.step).append("receiverIP",contract.receiverIP).append("file",contract.data);
        //step4
        return doc;
    }
    public void insertUserContract(Contract contract){
        //문제의 user의 contractlist 업데이트 함수

//        Document contractDoc = ContractDoc(contract);
//        if (contract.step == 4){
//        }
//        else {
//
//            if (user.find(elemMatch("contractList",eq("_id",contract._id))).first() == null) { //이렇게 해도 되는데 user를 특정지을 수 있는 쿼리를 찾아봐야함
//                user.updateOne(eq("ip", myUser.ip),Updates.addToSet("contractList", contractDoc));
//                //ip가 myUSer.ip 인 튜플의 contractList 값에 contractDoc을 추가하기
//                user.updateOne(eq("ip", contract.receiverIP),Updates.addToSet("contractList", contractDoc));
//                //object id 같은 게 잇다 -> set , 없다 -> insert
//            }
//            else{
//                user.updateOne(Filters.eq("ip", myUser.ip), Updates.set("contractList", contractDoc),new UpdateOptions().upsert(true).bypassDocumentValidation(true)); //내꺼 업로드
//                user.updateOne(Filters.eq("ip", contract.receiverIP), Updates.set("contractList", contractDoc)); //상대방 ip 업로드
//                // 조건에 일치하는 데이터 없으면 insert
//                //같은사람이랑 계약을 여러개 체결할 수 도 있는데
//            }
//        }
        //만약 contractDOC이 step 1 이 아니라 2,3,4 라면 replace 해야함
    }
    public ArrayList<Contract> getUserContractList(String ip){
        ArrayList<Contract> contractList = new ArrayList<Contract>();
        BasicDBObject filter = new BasicDBObject();
        filter.put("_id",0);
//        filter.put("ip",0);
//        filter.put("userType",0); //filter는 모두 0 이던가 하나만 1이던가 해야함! 단, _id flag설정은 상관없음
        filter.put("contractList",1);
        cursor = user.find(Filters.eq("ip",ip)).projection(filter).iterator();
        try {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                if (!d.isEmpty()) { //{{}} 로 반환됨
                    System.out.println(d.toString());
                    contractList.add(new Contract(d)); //그냥 d하면 Document{{ip=1}} 로 string 생성됨
                }
            }
        } finally {
            cursor.close();
        }
        return contractList;
    }
    public void insertUser(User uData){
        Document userDoc = userDoc(uData);
        user.insertOne(userDoc);
    }
    public ArrayList<String> getIpList(){
        ArrayList<String> ipList = new ArrayList<String>();
        BasicDBObject filter = new BasicDBObject();
        filter.put("ip", 1);
        filter.put("_id", 0);
        cursor = user.find().projection(filter).iterator();
        try {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                ipList.add(d.get("ip").toString()); //그냥 d하면 Document{{ip=1}} 로 string 생성됨
            }
        } finally {
            cursor.close();
        }
        return ipList;
    }
    public User getUser(String ip){
        BasicDBObject filter = new BasicDBObject();
        filter.put("contractList", 0);
        cursor = user.find(Filters.eq("ip",ip)).projection(filter).iterator();
        Document d = cursor.next();
        this.myUser = new User(d);
        return myUser;
    }
}
