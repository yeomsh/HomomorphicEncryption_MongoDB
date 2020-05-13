package HomomorphicEncryption;

import DataClass.Contract;
import DataClass.Database;
import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.json.simple.JSONObject;

import javax.print.Doc;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static com.mongodb.client.model.Filters.*;

public class NoSQLDB extends Database{

    MongoCollection<Document> keywordPEKS;
    MongoCollection<Document> filePEKS;
    MongoCollection<Document> zindex;

    public NoSQLDB(){
        keywordPEKS = database.getCollection("keywordPEKS");
        filePEKS = database.getCollection("filePEKS");
        zindex = database.getCollection("zindex");
    }
    public void delete(){
        keywordPEKS.drop();
        filePEKS.drop();
        zindex.drop();
        user.drop();
    }
    public Object insertContract(CipherData cipherData, JSONObject file){
        Document filedoc = fileDoc(cipherData.c2, cipherData.c3,file);
        filePEKS.insertOne(filedoc);
        cipherData.setFileId(filedoc.get("_id"));
        return filedoc.get("_id");
    }

    public Object insertKeywordPEKS(CipherData cipherData){
        Document keyworddoc = keywordDoc(cipherData.c1, cipherData.c2);
        keywordPEKS.insertOne(keyworddoc);
        cipherData.setFileId(keyworddoc.get("_id"));

        return keyworddoc.get("_id");
    }

    public Document keywordDoc(BigInteger c1, BigInteger c2){
        return new Document("c1", c1.toString(16)).append("c2", c2.toString(16));
    }

    public Document fileDoc(BigInteger c2, BigInteger c3, JSONObject file){
        return new Document("c2", c2.toString(16)).append("c3", c3.toString(16)).append("file",file);
    }

    public Document zIndexFileInfoDoc(Object _id, Boolean exist){
        return new Document("_id",_id).append("exist",exist);
    }
    public void updateZString(HashMap<Object,Boolean> uploadKeywordMap, Object fileId){
        //기존 데이터 백업
        cursor = zindex.find().iterator();
        ArrayList<Document> list = new ArrayList<>();
        if(cursor.hasNext()){
            list = (ArrayList<Document>) cursor.next().get("fileList");
            for(Document doc: list) doc.replace("exist", false); //백업데이터는 새로들어올 키워드를 위해 사용할껀데, 새로들어왔단 의미는 기존 파일엔 해당 키워드가 없단 얘기니까 false 로 처리
        }
        //이번 fileId에 대해 update
        Document doc = zIndexFileInfoDoc(fileId,false);
        zindex.updateMany(Filters.exists("_id"),Updates.addToSet("fileList",doc)); //일단 모든 file list에 {fileid: 0} 추가
        BasicDBObject data = new BasicDBObject();
        data.put("fileList.$.exist", true);
        BasicDBObject command = new BasicDBObject();
        command.put("$set", data);
        //키워드 2개에 대해 fileList 작업
        for(Object keywordId: uploadKeywordMap.keySet()){
            System.out.println("KeywordId: "+keywordId);
            if(uploadKeywordMap.get(keywordId)){
                System.out.println("true 실행");
                zindex.updateOne(Filters.and(eq("_id", keywordId), elemMatch("fileList", eq(fileId))), command); //기존에 있던 키워드라면, 해당 키워드 id로 접근하여, fileid 의 exist 값을 1로 업데이트
            }
            else{ //처음 insert되는 키워드id라면 백업해뒀던 리스트에 이번에 추가할 데이터를 더해서 insert
                System.out.println("else 실행");
                doc.put("exist",true);
                list.add(doc);
                zindex.insertOne(new Document("_id",keywordId).append("fileList",list));
                list.remove(list.size()-1);
            }
        }
    }
}
