package HomomorphicEncryption;

import Blockchain.BCManager;
import Blockchain.BCServer;
import DataClass.CipherData;
import DataClass.DataSource;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bouncycastle.crypto.MaxBytesExceededException;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import util.FileManager;
import util.StringUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class HEServer2 {
    public KGC kgc;
    private BigInteger p; //서버의 개인키
    private BigInteger a; //서버alpha
    private ArrayList<CipherKeyword> arrCipherKeyword = new ArrayList<>();

    public HEServer2(int port) throws Exception {
        //계약서 계속 생성

        //gs.close();호출할 일 x

        //heserver
        kgc = new KGC();
        p = kgc.getP();
        a = kgc.getSystemAlpha();
        //서버가 실행되면 db를 1회 읽어옴
        readZindex();
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        String host = "192.168.105.224";
        serverSocket.bind(new InetSocketAddress(host, port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            System.out.println("size1: "+selector.keys().size());
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            System.out.println("size: "+selectedKeys.size());
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                try {
                    if (key.isAcceptable()) {
                        register(selector, serverSocket);
                    } else if (key.isReadable()) { //서버가 채널로부터 read 하는 것임
                        readZindex();
                        System.out.println("key.isReadable()");
                        processRead(key);
                    } else if (key.isWritable()) {
                        System.out.println("key.isWritable()");
                        processWrite(key);
                    }
                    System.out.println("작업 끝!");
                    iter.remove();
                } catch (IOException exception) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException cex) {
                    }
                }
            }
        }

    }

    private synchronized void readZindex() throws InterruptedException {
        System.out.println("readZindex 시작");
        int totalCount = (int) HEDataBase.keywordPEKS.countDocuments();
        int size = arrCipherKeyword.size();
        int count = (totalCount - size) / 100 + 1;
        ReadDBThread[] arrThread = new ReadDBThread[count];
        for (int i = 0; i < count; i++) {
            arrThread[i] = new ReadDBThread(size, i, new DataSource.LoadDBCallback() {
                @Override
                public void onDBLoaded(Document document) {
                    arrCipherKeyword.add(new CipherKeyword(document));
                }
            });
        }
        for (ReadDBThread thread : arrThread) {
            thread.join();
        }
        System.out.println("readZindex 끝");
    }
    private void processWrite(SelectionKey key) throws IOException {
        System.out.println("processWrite");
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        if(buffer.hasRemaining()){ //position 과 limit이 차이나면
            client.write(buffer);
        }
        key.interestOps(SelectionKey.OP_READ);
    }
    private void processRead(SelectionKey key) throws IOException {
        System.out.println("processRead");
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(4);
        client.read(buffer);
        buffer.flip();
        int size = buffer.getInt();
        System.out.println("processREAD> get total size: "+size);
        buffer = ByteBuffer.allocate(size);
        if(buffer.hasRemaining()){ //position 과 limit이 차이나면
            int c = client.read(buffer);
            System.out.println("read에서 ㅇ읽은거은거: "+c);
        }
        //client가 보낸 buffer 내용을 server가 가지고 있는 buffer에 담기
        buffer.flip();
        String str = new String(buffer.array(), buffer.position(), buffer.limit());
        System.out.println("str:\n"+str);
        String[] arr = str.split("\n");
        String type = arr[0];
        try {
            switch (type) {
                case "EXIT":
                    if (client.isConnected() && client.isOpen()) {
                        System.out.println("client: close");
                        client.socket().close();
                    }
                    break;
                case "uploadKeyword":
                    uploadKeyword(key, arr);
                    break;
                case "search":
                    searchKeyword(key, client, buffer, arr);
                    break;
                case "uploadContract":
                    uploadContract(key, arr);
                    break;
                case "requestAlpha":
                    requestAlpha(client, buffer);
                    break;
            }
        }catch (IOException | InterruptedException EX){
            key.cancel();
        }catch (Exception ex){

        }
        System.out.println("Not accepting client messages anymore");
        buffer.clear();
    }

    public void requestAlpha(SocketChannel client, ByteBuffer buffer) throws IOException {
        System.out.println("request alpha 시작");
        BigInteger alpha = kgc.shareAlpha();
        BigInteger x0 = kgc.checkX0Condition(kgc.pkSet.firstElement(), alpha).pk;
        String str1 = alpha.toString(16) + "\n" + x0.toString(16);
        System.out.println(str1);
        byte[] message = str1.getBytes();
        System.out.println("messgae length: " + message.length);
        //message 크기 보내기
        buffer.clear();
        buffer.putInt(message.length);
        buffer.flip();
        System.out.println("flip: limit> " + buffer.limit());
        client.write(buffer);
        buffer.clear();
        //alpha, x0 보내기
        ByteBuffer buffer2 = ByteBuffer.wrap(message, 0, message.length);
        client.write(buffer2);
        System.out.println("request alpha 끝");
    }

    public void searchKeyword(SelectionKey key, SocketChannel client, ByteBuffer buffer, String[] arr) throws Exception {
        BigInteger alpha = new BigInteger(arr[4], 16);
        BigInteger sumPk = new BigInteger(arr[5], 16);
        CipherData cipherData1 = new CipherData(arr[1], arr[2], arr[3], alpha, sumPk);
        Vector<byte[][]> keywordFile = searchKeyword(cipherData1);
        buffer.clear();
        buffer.putInt(keywordFile.size());
        buffer.flip();
        client.write(buffer);
        buffer.clear();
        for (byte[][] cipherCON : keywordFile) {
            buffer.putInt(cipherCON[0].length);
            buffer.put(cipherCON[0]);
            buffer.putInt(cipherCON[1].length);
            buffer.put(cipherCON[1]);
        }
        buffer.flip();
        System.out.println("flip: " + buffer.position());
        System.out.println("flip: " + buffer.limit());
//        client.write(buffer);
//        buffer.clear();
        key.interestOps(SelectionKey.OP_WRITE);
        key.attach(buffer);
    }

    public void uploadKeyword(SelectionKey key, String[] arr) throws InterruptedException {
        BigInteger alpha = new BigInteger(arr[5], 16);
        BigInteger sumPk = new BigInteger(arr[6], 16);
        CipherData cipherData1 = new CipherData(arr[1], arr[2], null, alpha, sumPk);
        CipherData cipherData2 = new CipherData(arr[3], arr[4], null, alpha, sumPk);
        Object fileId = new ObjectId(arr[7]);
        CipherData[] cipherDatas = {cipherData1, cipherData2};
        HashMap<Object, Boolean> saveKeywordId = uploadKeyword(cipherDatas);
        System.out.println("올린 키워드: " + saveKeywordId.keySet());
        Boolean ret = HEDataBase.updateZString(saveKeywordId, fileId);
        System.out.println("uploadkeyword> updateZstringret: "+ret);
        if(ret){
            System.out.println("uploadKeyword> write 설정 ");
            ByteBuffer buffer= ByteBuffer.allocate(4); //message lengh+ message
            buffer.putInt(1);
            buffer.flip();
            key.interestOps(SelectionKey.OP_WRITE);
            key.attach(buffer);
        }
    }


    public void uploadContract(SelectionKey key, String[] arr) {
        long start = System.currentTimeMillis();
        BigInteger alpha = new BigInteger(arr[3], 16);
        BigInteger sumPk = new BigInteger(arr[4], 16);
        Object fileId = new ObjectId(arr[5]);
        CipherData cipherData = new CipherData(null, arr[1], arr[2], alpha, sumPk);
        addSystemAlpha(cipherData);

        Boolean ret = HEDataBase.insertContract(cipherData, fileId);
        System.out.println("uploadContract> insertContract: "+ret);
        if(ret){
            System.out.println("uploadContract> write 설정 ");
            ByteBuffer buffer= ByteBuffer.allocate(4); //message lengh+ message
            buffer.putInt(1);
            buffer.flip();
            key.interestOps(SelectionKey.OP_WRITE);
            key.attach(buffer);
        }
        long end = System.currentTimeMillis();
        System.out.println("uploadContract 시간> : "+(end- start));
    }

    private void register(Selector selector, ServerSocketChannel serverSocket)
            throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println("new client connected...");
        System.out.println("currenttime"+ System.currentTimeMillis());
    }

    public void addSystemAlpha(CipherData cipherData) { //user alpha 지우고, system alpha 입히기
        if (cipherData.c1 != null)
            cipherData.c1 = cipherData.makeCi(cipherData.c1.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? cipherData.c1.mod(p).subtract(p) : cipherData.c1.mod(p), a);
        if (cipherData.c3 != null)
            cipherData.c3 = cipherData.makeCi(cipherData.c3.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? cipherData.c3.mod(p).subtract(p) : cipherData.c3.mod(p), a);
    }

    public Vector<byte[][]> searchKeyword(CipherData cipherData) throws Exception {
        //         3. 검색
        //            1) kewordPEKS로 있는 키워드 찾기 -> keywordId기억
        //            2) 해당 keywordId 를 zindex에서 찾아, file : exist 값 가져옴
        //            3) 1인 애들의 fileId로 ci2,ci3 비교하기
        Vector<byte[][]> keywordFile = new Vector<>();
        CipherKeyword searchKeyword = null;
        long start = System.currentTimeMillis();
        System.out.println("arrCipherKeywrod.size(): "+arrCipherKeyword.size());
        for (CipherKeyword keyword : arrCipherKeyword) {
            if (keywordTest(cipherData, keyword)) {
                System.out.println("search : 키워드 같은거 발견 !!");
                searchKeyword = keyword;
            }
        }
        if(searchKeyword == null) return keywordFile;
        BasicDBObject filter = new BasicDBObject();
        filter.put("_id", 0);
        filter.put("fileList", 1);
        MongoCursor<Document> cursor = HEDataBase.zindex.find(Filters.eq("_id", searchKeyword._id)).projection(filter).iterator();
        ArrayList<Document> list = (ArrayList<Document>) cursor.next().get("fileList");
        HashMap<Object, Boolean> zIndexResult = new HashMap<>();
        for (Document doc : list) {
            if (doc.getBoolean("exist")) {
                zIndexResult.put(doc.get("_id"), true);
            }
        }
        if (zIndexResult.isEmpty()) {//일치하는 키워드 0개
            long end = System.currentTimeMillis();
            System.out.println("search keyword 걸린시간: " + (end - start));
            return keywordFile;
        }
        //계약서 파일 가져오기
        for (Object _id : zIndexResult.keySet()) {
            Document doc = HEDataBase.filePEKS.find(Filters.eq("_id", _id)).first();
            if (doc != null) {
                System.out.println("doc22:\n" + doc);
                CipherContract cipherContract = new CipherContract(doc);
                //c2 c3비교
                if (keywordTest(cipherData, cipherContract)) { //파일에 속한 권한 비교
                    byte[][] arr = new byte[2][];
                    arr[0] = cipherContract.cipher;
                    System.out.println("server: " + arr[0].length);
                    arr[1] = cipherContract.IV;
                    System.out.println("server: " + arr[1].length);
                    keywordFile.add(arr);
                    System.out.println("cipher\n:" + new String(cipherContract.cipher));
                }
            }

        }
        long end = System.currentTimeMillis();
        System.out.println("search keyword 걸린시간: " + (end - start));
        System.out.println("this is the correctFile : " + keywordFile.size());
        return keywordFile;
    }

    public Boolean keywordTest(CipherData d1, CipherContract d2) { //c3(권한 테스트 함수)
        //분모
        BigInteger parent = d1.c3.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? d1.c3.mod(p).subtract(p) : d1.c3.mod(p);
        parent = hash(parent.mod(d1.getAlpha()));
        parent = parent.add(d2.c2);
        //분자
        BigInteger child = d2.c3.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? d2.c3.mod(p).subtract(p) : d2.c3.mod(p);
        child = hash(child.mod(a));
        child = child.add(d1.c2);
        return parent.subtract(child).equals(BigInteger.ZERO);
    }

    public Boolean keywordTest(CipherData d1, CipherKeyword d2) {
        //분모
        BigInteger parent = d1.c1.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? d1.c1.mod(p).subtract(p) : d1.c1.mod(p);
        parent = hash(parent.mod(d1.getAlpha()));
        parent = parent.add(d2.c2);

        //분자
        BigInteger child = d2.c1.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? d2.c1.mod(p).subtract(p) : d2.c1.mod(p);
        child = hash(child.mod(a));
        child = child.add(d1.c2);
        return parent.subtract(child).equals(BigInteger.ZERO);
    }

    public BigInteger hash(BigInteger exponent) {
        return exponent;
    }

    public Boolean keywordTest(CipherData[] cipherDatas, CipherKeyword d2) {
        for (CipherData d1 : cipherDatas) {
            //분모
            BigInteger parent = d1.c1.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? d1.c1.mod(p).subtract(p) : d1.c1.mod(p);
            parent = hash(parent.mod(d1.getAlpha()));
            parent = parent.add(d2.c2);

            //분자
            BigInteger child = d2.c1.mod(p).compareTo(p.divide(BigInteger.TWO)) > 0 ? d2.c1.mod(p).subtract(p) : d2.c1.mod(p);
            child = hash(child.mod(a));
            child = child.add(d1.c2);
            if (parent.subtract(child).equals(BigInteger.ZERO)) {
                d1.isExist = true;
                return true;
            }
        }
        return false;
    }

    public HashMap<Object, Boolean> uploadKeyword(CipherData[] cipherDatas) throws InterruptedException {
        HashMap<Object, Boolean> uploadKeywordMap = new HashMap<>(); //<id, 기존 키워드 여부>
        Vector<Object> saveKeywordId = new Vector<>();
        //해당 키워드 찾기 및 키워드 추가
        long start = System.currentTimeMillis();
        System.out.println("arrCipherKeyword.size():"+ arrCipherKeyword.size());
        for (CipherKeyword keyword : arrCipherKeyword) {
            if (keywordTest(cipherDatas, keyword)) {
                System.out.println("같은 키워드 발견!");
                uploadKeywordMap.put(keyword._id, true);
                saveKeywordId.add(keyword._id);
                if (saveKeywordId.size() == 2) { //둘다 찾았을 때
                    long end = System.currentTimeMillis();
                    System.out.println("upload keyword검색 시간> saveKeywordId.size() == 2 > " + (end - start));
                    return uploadKeywordMap;
                }
            }
        }
        //arrkeyword로 바꾸기!
        long end = System.currentTimeMillis();
        System.out.println("upload keyword> 검색 시간: " + (end - start));
        for (CipherData cipherData : cipherDatas) {
            if (!cipherData.isExist) { //존재하지 않는 것이 있다면 키워드 추가
                addSystemAlpha(cipherData);
                Object _id = HEDataBase.insertKeywordPEKS(cipherData); //키워드암호문 id
                saveKeywordId.add(_id);
                uploadKeywordMap.put(_id, false);
            }

        }
        return uploadKeywordMap;
    }

    public static void main(String[] args) {
        try {
            //  GenesisServer gs = new GenesisServer();
            HEServer2 he = new HEServer2(4000);


        } catch (IOException exception) {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

//class ReadArrayThread extends Thread {
//    int idx;
//    int start;
//    int size = 100;
//    private boolean stop;
//    DataSource.LoadDBCallback callback;
//
//    public ReadArrayThread(int start, int idx, DataSource.LoadDBCallback callback) {
//        this.idx = idx * size;
//        this.callback = callback;
//        this.stop = false;
//        this.start = start;
//        this.start();
//    }
//
//    @Override
//    public void run() {
//        for (int i = idx; i <size && stop ; i++) {
//          //  callback.onDBLoaded();
//        }
//    }
//
//    public void setStop(Boolean flag) {
//        this.stop = flag;
//    }
//}

class ReadDBThread extends Thread {
    int idx;
    int start;
    int size = 100;
    private boolean stop;
    DataSource.LoadDBCallback callback;

    public ReadDBThread(int start, int idx, DataSource.LoadDBCallback callback) {
        this.idx = idx * size;
        this.callback = callback;
        this.stop = false;
        this.start = start;
        this.start();
    }

    @Override
    public void run() {
        MongoCursor<Document> cursor = HEDataBase.keywordPEKS.find().skip(start + idx).limit(size).iterator();
        while (cursor.hasNext() && !stop) {
            callback.onDBLoaded(cursor.next());
        }
        cursor.close();
    }

    public void setStop(Boolean flag) {
        this.stop = flag;
    }
}

class GenesisServer extends TimerTask {
    //교수님 컴퓨터에서 돌릴 것임
    //여기서 생성되는 체인도 교수님 컴퓨터(서버)에 저장될 것임
    Timer jobScheduler = new Timer();
    BCManager bcManager;

    public GenesisServer() throws IOException {
        System.out.println("init GenesisServer");
        BCServer server = new BCServer(3000, FileManager.readChainFile());
        ArrayList<String> ipList = new ArrayList<>();
        ipList.add("192.168.56.1"); //확인을 위해 내 아이피하나 넣어뒀어 ! 상히가 확인해보고 싶으면 상히 ip넣어두면도ㅐ
        bcManager = new BCManager(ipList); //실제로 돌릴땐 그냥 기본 생성자 호출
        BCManager.chainStr = FileManager.readChainFile(); //필수
        BCManager.block = new JSONObject(); //필수
        jobScheduler.scheduleAtFixedRate(this, 0, 30000); //호출로부터 0초후에 30s간격으로 task 함수(run함수) 호출 -> 실제로는 10분으로 바꾸면 됨
    }

    public void run() {
        System.out.println("GenesisServer: run");
        try {
            bcManager.chainUpdate();
            bcManager.proofOfWork(StringUtil.randomString());
            if (bcManager.broadCastBlock()) { //작업증명에 성공하면 -> 임시서버에서 지우고 -> 키워드 업로드
                System.out.println("브로드 캐스트에서 작업증명결과가 옳다고 나옴-> 성공");
            } else { //실패하면 그냥 끝
                System.out.println("브로드 캐스트에서 작업증명결과가 옳지않다고 나옴 -> 실패");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void close() {
        jobScheduler.cancel();
    }

}
