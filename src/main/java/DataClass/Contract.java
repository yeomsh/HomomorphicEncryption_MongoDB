package DataClass;

import ECIES.ECIESManager;
import org.bouncycastle.util.encoders.Base64;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Contract {
    public int step; //1,2,3, 4
    public String receiverUid; //상대방 IP
    public Object _id; //step1단계에서 최초로 mongodb에 업로드 될 떄 부여되는 유니크한 식별자
    public JSONObject fileData;
    public byte[] IV;
    public byte[] cipher;
    public Contract(JSONObject file, User user) throws Exception {
        this.fileData = file;
        ECIESManager eciesManager = new ECIESManager();
        this.IV= eciesManager.makeIV();
        String PkString = Base64.toBase64String(user.eciesPublicKey);
        this.cipher = eciesManager.senderEncrypt(PkString,file.toJSONString(),this.IV);
        System.out.println("contract>iv: "+IV.length);
        System.out.println("contract>cipher: "+cipher.length);
        JSONObject obj = eciesManager.decryptCipherContract(this.cipher,user.eciesPrivateKey,IV);
        System.out.println("contract>data:\n"+obj.toString());
    }
    public Contract(int step, String receiverUid){
        this.step = step;
        this.receiverUid = StringUtil.getSha256(receiverUid);
    }

    public Contract(Object d){
        System.out.println(d.toString());
        //JSONParser parser = new JSONParser();
        //Object obj = parser.parse(d.toString());
        //this.fileData = (JSONObject) obj;
        if(fileData.containsKey("_id")) {
            this._id = fileData.get("_id");
            this.step = Integer.parseInt(fileData.get("step").toString());
            this.receiverUid = fileData.get("receiverUid").toString();
            fileData.remove("_id");
            fileData.remove("step");
            fileData.remove("receiverUid");
        }
    }

    public Contract(Document d) throws ParseException {
        //contract List에 document가 바로 들어간게 아니고 array형식으로 들어가야함
        System.out.println(d);
        //JSONParser parser = new JSONParser();
        //Object obj =  parser.parse(d.toJson());
        if(d.containsKey("_id")){
            this._id = d.get("_id");
            this.step = Integer.parseInt(d.get("step").toString());
            this.receiverUid = d.getString("receiverUid");
            //obj = parser.parse(((Document) d.get("file")).toJson());
//            this.IV = Base64.decode(d.get("IV").toString());
//            this.cipher = Base64.decode(d.get("cipher").toString());
        }
        else {
//            JSONParser parser = new JSONParser();
//            Object obj =  parser.parse(d.toJson());
//            this.fileData = (JSONObject) obj;
        }
        this.IV = Base64.decode(d.get("IV").toString());
        this.cipher = Base64.decode(d.get("cipher").toString());
        //this.fileData = (JSONObject) obj;
    }
}