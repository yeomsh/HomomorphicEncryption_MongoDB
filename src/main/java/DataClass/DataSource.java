package DataClass;

import org.json.simple.JSONObject;

import org.bson.Document;
import java.math.BigInteger;
import java.util.Vector;

public interface DataSource {
    interface LoadDataCallback {
        void onDataLoaded() throws Exception;
        void onDataFailed();
        void onDataLoaded(Vector<JSONObject> keywordFile);
    }
    interface HECallback{
        void onHESuccess(byte[][][] arr) throws Exception;
        void onHESuccess(Object fileId) throws Exception;
        void onHESuccess(BigInteger alpha, BigInteger x0) throws Exception;
        void onHEfail();
    }
    interface LoadDBCallback{
        void onDBLoaded(Document document);
    }
}