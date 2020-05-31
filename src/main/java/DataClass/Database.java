package DataClass;

import com.mongodb.BasicDBObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bouncycastle.util.encoders.Base64;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.parser.ParseException;
import util.StringUtil;

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
    public User myUser = null;
    public Database(){
//        String dburl = "mongodb://id:pw@192.168.43.253:27017/mydb";
 //       MongoClient mongoClient = new MongoClient("192.168.43.253",27017);
        //203.252.157.85(교수님)
        mongoClient = new MongoClient("203.252.166.224", 27017); //(501컴퓨터)

        //mongoClient = MongoClients.create();
        database = mongoClient.getDatabase("mydb");
        user = database.getCollection("user");
    }
    public Document userDoc(User user){
        return new Document("id",user.id.toString(16))
                .append("userType",user.userType.ordinal())
                .append("ip",user.ip)
                .append("uid", user.uid)
                .append("ECIESpk", Base64.toBase64String(user.eciesPublicKey));
    }
    public Document ContractDoc(Contract contract){
        return new Document("_id",new ObjectId())
                .append("step",contract.step)
                .append("receiverUid",contract.receiverUid)
                //.append("file",contract.fileData)
                .append("IV",Base64.toBase64String(contract.IV))
                .append("cipher",Base64.toBase64String(contract.cipher));
    }
    public Boolean isValidUser(String uid){
        cursor = user.find(Filters.eq("uid",StringUtil.getSha256(uid))).iterator();
        if(cursor.hasNext()) return true;
        return false;
    }
    public void removeStepContract(Object _id, String uid){
        //$(index) 위치의 element 지우기(using unset)->null로 바뀜
        BasicDBObject data = new BasicDBObject();
        data.put("contractList.$",1);
        BasicDBObject command = new BasicDBObject();
        command.put("$unset", data);
        user.updateOne(Filters.and(eq("uid", uid), elemMatch("contractList", eq(_id))), command); //내꺼 업로드
        //user.updateOne(Filters.and(eq("uid", contract.receiverUid), elemMatch("contractList", eq(contract._id))), command);
        //array에 있는 null 모두 삭제(using pull)
        data.remove("contractList.$");
        data.put("contractList", null);
        command.remove("$unset");
        command.put("$pull", data);
        user.updateOne(Filters.eq("uid", uid), command);
        // user.updateOne(Filters.eq("uid", contract.receiverUid), command);
    }
    public void insertStep5contract(Object _id, String receiverUid, byte[] iv, byte[] cipher){

        BasicDBObject data = new BasicDBObject();
        data.put("contractList.$.step", 5);
        data.put("contractList.$.IV", Base64.toBase64String(iv));
        data.put("contractList.$.cipher", Base64.toBase64String(cipher));
        BasicDBObject command = new BasicDBObject();
        command.put("$set", data);
        user.updateOne(Filters.and(eq("uid", receiverUid), elemMatch("contractList", eq(_id))), command); //내꺼 업로드

    }
    public void insertStep5contract(Contract contract){
        BasicDBObject data = new BasicDBObject();
        data.put("contractList.$.step", 5);
        data.put("contractList.$.IV", Base64.toBase64String(contract.IV));
        data.put("contractList.$.cipher", Base64.toBase64String(contract.cipher));
        BasicDBObject command = new BasicDBObject();
        command.put("$set", data);
        user.updateOne(Filters.and(eq("uid", contract.receiverUid), elemMatch("contractList", eq(contract._id))), command); //내꺼 업로드

    }
    public void insertStepContract(Contract contract, byte[] cipher) {
        //문제의 user의 contractlist 업데이트 함수
//    System.out.println(server.nosqldb.zindex.find(Filters.and(eq("ip","000.000.000.000"),elemMatch("contractList",eq("cid",1)))).first());

        Document contractDoc = ContractDoc(contract);
        if (contract._id == null) { //step 1 이란 의미 (아직 _id x)
            user.updateOne(eq("uid", myUser.uid), Updates.addToSet("contractList", contractDoc));
            contractDoc.replace("receiverUid",myUser.uid);
            contractDoc.replace("cipher",Base64.toBase64String(cipher)); //receiver의 공개키로 암호화한 contract cipher로 교체
            user.updateOne(eq("uid", contract.receiverUid), Updates.addToSet("contractList", contractDoc));
        } else {
            contractDoc.put("_id", contract._id);
            BasicDBObject data = new BasicDBObject();
            data.put("contractList.$.step", contract.step);
            //data.put("contractList.$.file", contract.fileData);
            data.put("contractList.$.IV", Base64.toBase64String(contract.IV));
            data.put("contractList.$.cipher", Base64.toBase64String(contract.cipher));
            BasicDBObject command = new BasicDBObject();
            command.put("$set", data);
            user.updateOne(Filters.and(eq("uid", myUser.uid), elemMatch("contractList", eq(contract._id))), command); //내꺼 업로드
            data.replace("contractList.$.cipher", Base64.toBase64String(cipher));
            command.replace("$set", data);
            user.updateOne(Filters.and(eq("uid", contract.receiverUid), elemMatch("contractList", eq(contract._id))), command); //내꺼 업로드
        }
        System.out.println("database> insertUserContract: doc's _id : " + contractDoc.get("_id").toString());
        //만약 contractDOC이 step 1 이 아니라 2,3,4 라면 replace 해야함
    }
    public ArrayList<Contract> getUserContractList(String uid) throws ParseException {
        ArrayList<Contract> contractList = new ArrayList<>();
        BasicDBObject filter = new BasicDBObject();
        filter.put("_id",0);
//        filter.put("ip",0);
//        filter.put("userType",0); //filter는 모두 0 이던가 하나만 1이던가 해야함! 단, _id flag설정은 상관없음
        filter.put("contractList",1);
        cursor = user.find(Filters.eq("uid",uid)).projection(filter).iterator();
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
    public ArrayList<String> getIpList() throws Exception {
        ArrayList<String> ipList = new ArrayList<>();
        BasicDBObject filter = new BasicDBObject();
        filter.put("ip", 1);
        filter.put("_id", 0);
        cursor = user.find().projection(filter).iterator();
        try {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                System.out.println(d.getString("ip"));
                if(d.get("ip").toString().equals(myUser.ip)) {
                    continue;
                }
                ipList.add(d.get("ip").toString()); //그냥 d하면 Document{{ip=1}} 로 string 생성됨
            }
        } finally {
            cursor.close();
        }
        return ipList;
    }
    public User getUser(String uid, String ip, DataSource.Callback callback) throws Exception {
        BasicDBObject filter = new BasicDBObject();
        filter.put("contractList", 0);
        cursor = user.find(Filters.eq("ip",ip)).projection(filter).iterator();
        if(cursor.hasNext()){
            Document d = cursor.next();
            System.out.println(d);
            if(d.get("uid").equals(uid)){
                this.myUser = new User(d);
                System.out.println(myUser.toString());
            }
            else{//there is no match uid and ip
                callback.onDataLoaded();
            }
        }
        else{ //there is no such uid
            callback.onDataFailed();
        }
        return myUser;
    }

    public String getReceiperECIESpk(String receiperUid){
        System.out.println(receiperUid);
        cursor = user.find(Filters.eq("uid",receiperUid)).iterator();
        if(cursor.hasNext()){
            String temp = cursor.next().get("ECIESpk").toString();
            System.out.println("receiperuid, ECEISpk : " + receiperUid + "," + temp);
            return temp;
            //return cursor.next().get("ECIESpk").toString();
        }
        else
            System.out.println("receiperuid에 해당하는 ECEISpk 없음");
        return null;
    }

}
