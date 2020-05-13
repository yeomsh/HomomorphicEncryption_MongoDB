package DataClass;

import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import static com.mongodb.client.model.Filters.elemMatch;
import static com.mongodb.client.model.Filters.eq;

public class Database {
    protected MongoClient mongoClient;
    protected MongoDatabase database;
    protected MongoCollection<Document> user;
    protected MongoCursor<Document> cursor;
    public User myUser;
    public Database(){
        mongoClient = MongoClients.create();
        database = mongoClient.getDatabase("mydb");
        user = database.getCollection("user");
    }
    public Document userDoc(User user){
        return new Document("id",user.id.toString(16)).append("userType",user.userType.ordinal()).append("ip",user.ip);
    }
    public Document ContractDoc(Contract contract){
        return new Document("_id",new ObjectId()).append("step",contract.step).append("receiverIP",contract.receiverIP).append("file",contract.fileData);
    }
    public void removeStepContract(Contract contract){
        //$(index) 위치의 element 지우기(using unset)->null로 바뀜
        BasicDBObject data = new BasicDBObject();
        data.put("contractList.$",1);
        BasicDBObject command = new BasicDBObject();
        command.put("$unset", data);
        user.updateOne(Filters.and(eq("ip", myUser.ip), elemMatch("contractList", eq(contract._id))), command); //내꺼 업로드
        user.updateOne(Filters.and(eq("ip", contract.receiverIP), elemMatch("contractList", eq(contract._id))), command);
        //array에 있는 null 모두 삭제(using pull)
        data.remove("contractList.$");
        data.put("contractList", null);
        command.remove("$unset");
        command.put("$pull", data);
        user.updateOne(Filters.eq("ip", myUser.ip), command);
        user.updateOne(Filters.eq("ip", contract.receiverIP), command);
    }
    public void insertStepContract(Contract contract) {
        //문제의 user의 contractlist 업데이트 함수
//    System.out.println(server.nosqldb.zindex.find(Filters.and(eq("ip","000.000.000.000"),elemMatch("contractList",eq("cid",1)))).first());

        Document contractDoc = ContractDoc(contract);
        System.out.println("database> insertUserContract: doc's _id : " + contractDoc.get("_id").toString());
        if (contract._id == null) { //step 1 이란 의미 (아직 _id x)
            user.updateOne(eq("ip", myUser.ip), Updates.addToSet("contractList", contractDoc));
            user.updateOne(eq("ip", contract.receiverIP), Updates.addToSet("contractList", contractDoc));
        } else {
            contractDoc.put("_id", contract._id);
            BasicDBObject data = new BasicDBObject();
            data.put("contractList.$.step", contract.step);
            data.put("contractList.$.file", contract.fileData);
            BasicDBObject command = new BasicDBObject();
            command.put("$set", data);
            user.updateOne(Filters.and(eq("ip", myUser.ip), elemMatch("contractList", eq(contract._id))), command); //내꺼 업로드
            user.updateOne(Filters.and(eq("ip", contract.receiverIP), elemMatch("contractList", eq(contract._id))), command); //내꺼 업로드
        }

        //만약 contractDOC이 step 1 이 아니라 2,3,4 라면 replace 해야함
    }
    public ArrayList<Contract> getUserContractList(String ip){
        ArrayList<Contract> contractList = new ArrayList<>();
        BasicDBObject filter = new BasicDBObject();
        filter.put("_id",0);
//        filter.put("ip",0);
//        filter.put("userType",0); //filter는 모두 0 이던가 하나만 1이던가 해야함! 단, _id flag설정은 상관없음
        filter.put("contractList",1);
        cursor = user.find(Filters.eq("ip",ip)).projection(filter).iterator();
        Document d = cursor.next();
        ArrayList<Document> list = (ArrayList<Document>) d.get("contractList");
        if(list != null){
            for (Document doc : list){
                if(doc != null)
                    contractList.add(new Contract(doc));
            }
        }
        return contractList;
    }
    public void insertUser(User uData) throws InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        Document userDoc = userDoc(uData);
        user.insertOne(userDoc);
        this.myUser = new User(userDoc);
    }
    public ArrayList<String> getIdList(){
        ArrayList<String> idList = new ArrayList<>();
        BasicDBObject filter = new BasicDBObject();
        filter.put("id", 1);
        filter.put("_id", 0);
        cursor = user.find().projection(filter).iterator();
        try {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                idList.add(d.get("id").toString()); //그냥 d하면 Document{{ip=1}} 로 string 생성됨
            }
        } finally {
            cursor.close();
        }
        return idList;
    }
    public ArrayList<String> getIpList(String myIp, DataSource.Callback callback) throws InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        ArrayList<String> ipList = new ArrayList<>();
        BasicDBObject filter = new BasicDBObject();
        filter.put("ip", 1);
        filter.put("_id", 0);
        Boolean isAlreadyUser = false;
        cursor = user.find().projection(filter).iterator();
        try {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                if(d.get("ip").toString().equals(myIp)) {
                    isAlreadyUser= true;
                    continue;
                }
                ipList.add(d.get("ip").toString()); //그냥 d하면 Document{{ip=1}} 로 string 생성됨
            }
        } finally {
            cursor.close();
        }
        if(isAlreadyUser) callback.onDataLoaded();
        else callback.onDataFailed(); //회원가입 요청
        return ipList;
    }
    public User getUser(String ip) throws InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        BasicDBObject filter = new BasicDBObject();
        filter.put("contractList", 0);
        cursor = user.find(Filters.eq("ip",ip)).projection(filter).iterator();
        Document d = cursor.next();
        this.myUser = new User(d);
        System.out.println(myUser.toString());
        return myUser;
    }
}
