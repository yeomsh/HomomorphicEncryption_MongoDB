package DataClass;

import org.bson.Document;

import java.util.ArrayList;

enum USERTYPE{
    EMPLOYER, WORKER
}
public class User {
    public Object _id; //get by mongoDB
    public String ip;
    public USERTYPE userType;
    public ArrayList<Contract> contractList; //체결중인 계약서 리스트
    public User(String ip, int type){
        this.ip = ip;
        this.userType = type == 0 ? USERTYPE.EMPLOYER : USERTYPE.WORKER;
    }
    public User(Document d){
        this._id = d.get("_id");
        this.ip = d.get("ip").toString();
        this.userType = d.getInteger("userType") == 0 ? USERTYPE.EMPLOYER : USERTYPE.WORKER;
    }
    public String toString(){
        return "ip : "+ this.ip + ", id : " + this._id + ", userType : " + this.userType;
    }
    public void setContractList(ArrayList<Contract> cList){
        contractList = cList;
    }
}
