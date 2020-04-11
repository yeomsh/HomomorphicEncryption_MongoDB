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

    MongoCursor<Document> cursor;

    Vector<Object> saveKeywordId = new Vector<>();

    //
    public NoSQLDB(){
        mongoClient = MongoClients.create();
        database = mongoClient.getDatabase("mydb");

        keywordPEKS = database.getCollection("keywordPEKS");
        filePEKS = database.getCollection("filePEKS");
        zindex = database.getCollection("zindex");
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

    public void updateZString(Vector<Object> saveKeywordId, Object fileId){

        cursor = zindex.find().iterator();

        ArrayList<Document> arraylist = new ArrayList<>();
        Document sample;
        if(cursor.hasNext())
            sample = cursor.next();
        else
            sample = new Document("file",arraylist);

        System.out.println(saveKeywordId.toString());

        for(int i = 0;i< saveKeywordId.size();i++){
            if(zindex.find(eq("_id",saveKeywordId.get(i))).first()==null){
                sample.put("_id",saveKeywordId.get(i));
                ArrayList temp = (ArrayList) sample.get("file");
                for(i=0;i<temp.size();i++) {
                    if(((Document) temp.get(i)).get("fileId").equals(fileId))
                        temp.remove(i);
                    else
                        ((Document) temp.get(i)).put("exist", "0");
                }
                temp.add(new Document("fileId",fileId).append("exist","1"));
                sample.put("file",temp);
                System.out.println(sample.toJson());
                zindex.insertOne(sample);
            }
            else
                zindex.updateOne(eq("_id",saveKeywordId.get(i)),new Document("$push",new Document("file",new Document("fileId",fileId).append("exist","1"))));
        }

        zindex.updateMany(not(elemMatch("file",eq("fileId",fileId))), new Document("$push",new Document("file",new Document("fileId",fileId).append("exist","0"))));

        System.out.println(zindex.find(not(elemMatch("file",eq("fileId",fileId)))).first());
    }
}
