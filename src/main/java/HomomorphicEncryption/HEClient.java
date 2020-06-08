package HomomorphicEncryption;

import DataClass.CipherData;
import DataClass.Contract;
import DataClass.DataSource;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


public class HEClient extends Thread {
    String type;
    CipherData cipherData1;
    CipherData cipherData2;
    Object fileId;
    SocketChannel client;
    DataSource.HECallback callBack;
    Contract contract;
    BigInteger alpha;
    BigInteger sumPk;

    public HEClient(String type, DataSource.HECallback callBack) throws IOException {
        client = SocketChannel.open();
        client.configureBlocking(true);
        client.connect(new InetSocketAddress("localhost", 4000));
        this.type = type;
        this.callBack = callBack;
        start();
    }
    public HEClient(String type, CipherData[] cipherDatas, Object fileId,DataSource.HECallback callBack) throws IOException {
        client = SocketChannel.open();
        client.configureBlocking(true);
        client.connect(new InetSocketAddress("localhost", 4000));
        System.out.println("Connection Success");
        this.type = type;
        this.cipherData1 = cipherDatas[0];
        this.cipherData2 = cipherDatas[1];
        this.callBack = callBack;
        this.fileId = fileId;
        System.out.println("fileId" + fileId);
        this.alpha =  cipherDatas[0].getAlpha();
        this.sumPk = cipherDatas[0].getPkSum();
        start();
    }

    public HEClient(String type, CipherData cipherData, Contract contract,DataSource.HECallback callBack) throws IOException {
        client = SocketChannel.open();
        client.configureBlocking(true);
        client.connect(new InetSocketAddress("localhost", 4000));
        System.out.println("Connection Success");
        this.type = type;
        this.cipherData1 = cipherData;
        this.callBack = callBack;
        this.contract = contract;
        this.alpha = cipherData.getAlpha();
        this.sumPk = cipherData.getPkSum();
        start();
    }

    public void run() {
        try {
            System.out.println("type: " + type);
            switch (type) {
                case "requestAlpha":
                    requestAlpha();
                    break;
                case "uploadKeyword":
                    uploadKeyword();
                    break;
                case "uploadContract":
                    uploadContract();
                    break;
                case "search":
                    search();
                    break;
            }
            if (client.isOpen() && client.isConnected()) {
                System.out.println("client.isOpen() && client.isConnected()");
                System.out.println(type);
                sendClose();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }
    public void sendClose() throws IOException {
        System.out.println("sendClose() 시작");
        String str = "EXIT";
        byte[] message = str.getBytes();
        ByteBuffer buffer= ByteBuffer.allocate(4+message.length); //message lengh+ message
        buffer.putInt(message.length);
        buffer.put(message);
        buffer.flip();
        client.write(buffer);
        System.out.println("sendClose() 끝");
        if(client.isConnected() && client.isOpen()){
            System.out.println("client.close() 실행");
            //client.close();
        }
    }
    public void requestAlpha() throws Exception {
        //alpha, x0 요청
        String str = type;
        byte[] message = str.getBytes();
        System.out.println("request> message.length: "+message.length);
        ByteBuffer buffer= ByteBuffer.allocate(4+message.length); //message lengh+ message
        buffer.putInt(message.length);
        System.out.println("requst>size: "+(4+message.length));
        System.out.println("request>filp>position(): "+buffer.position());
        System.out.println("request>filp>position(): "+buffer.limit());
        buffer.put(message);
        buffer.flip();
        client.write(buffer);

        //alpha, x0 받기
        buffer.clear();
        client.read(buffer);
        buffer.flip();
        int val = buffer.getInt();
        System.out.println("request> val: "+val); //9267
        buffer = ByteBuffer.allocate(val);
        client.read(buffer);
        buffer.flip();
        str = new String(buffer.array(), buffer.position(), buffer.limit());
        String[] arr = str.split("\n"); //arr[0]:alpha, arr[1]: x0
        System.out.println("alpha: "+arr[0]);
        System.out.println("x0: "+arr[1]);
        System.out.println("request alpha 끝1");
        callBack.onHESuccess(new BigInteger(arr[0],16),new BigInteger(arr[1],16));
        System.out.println("request alpha 끝2");

    }
    public void uploadContract() throws Exception {
        //c2,c3,
        Object fileId = HEDataBase.insertContract(contract);
        String str = type + "\n" + cipherData1.c2.toString(16) + "\n"+cipherData1.c3.toString(16) + "\n" + alpha.toString(16) + "\n" + sumPk.toString(16)+"\n"+fileId.toString();
        byte[] message = str.getBytes();
        System.out.println("message size: " + message.length);
        ByteBuffer buffer= ByteBuffer.allocate(4+message.length); //message lengh+ message
        buffer.putInt(message.length);
        buffer.put(message);
        buffer.flip();
        client.write(buffer);
        System.out.println("uploadContract끝1");
        callBack.onHESuccess(fileId);
        System.out.println("uploadContract끝2");
    }

    public void uploadKeyword() throws IOException {
        String str = type + "\n" +cipherData1.c1.toString(16) + "\n" + cipherData1.c2.toString(16) + "\n"+cipherData2.c1.toString(16) + "\n" + cipherData2.c2.toString(16) + "\n"
                + alpha.toString(16) + "\n" + sumPk.toString(16) + "\n" + fileId.toString();
        System.out.println("client string: \n" + str);
        byte[] message = str.getBytes();
        System.out.println("message size: " + message.length);
        ByteBuffer buffer= ByteBuffer.allocate(4+message.length); //message lengh+ message
        buffer.putInt(message.length);
        buffer.put(message);
        buffer.flip();
        client.write(buffer);
        System.out.println("uploadKeyword 끝");
        buffer.clear();
        client.read(buffer);
        buffer.flip();
        int ret = buffer.getInt();
        System.out.println("upload keyword ret: "+ret);
    }

    public void search() throws Exception {
        String str = type + "\n" +cipherData1.c1.toString(16) + "\n" + cipherData1.c2.toString(16) + "\n"+cipherData1.c3.toString(16) + "\n" + alpha.toString(16) + "\n" + sumPk.toString(16);
        System.out.println("client string: \n" + str);
        byte[] message = str.getBytes();
        System.out.println("message size: " + message.length);

        ByteBuffer buffer= ByteBuffer.allocate(4+message.length); //message lengh+ message
        buffer.putInt(message.length);
        buffer.put(message);
        buffer.flip();
        client.write(buffer);

        buffer.clear();
        System.out.println("Clear: "+buffer.position());
        System.out.println("Clear: "+buffer.limit());

        int cnt = client.read(buffer);
        System.out.println("cnt: "+cnt);
        System.out.println("read: "+buffer.position());
        System.out.println("read: "+buffer.limit());

        buffer.flip();
        System.out.println("flip: "+buffer.position());
        System.out.println("flip: "+buffer.limit());

        int val = buffer.getInt();

        System.out.println("getInt: "+buffer.position());
        System.out.println("getInt: "+buffer.limit());
        System.out.println("val: "+val);
        byte[][][] arr= new byte[val][2][];
        buffer.clear();
        int cnt1 = client.read(buffer);
        buffer.flip();
        for (int i = 0; i < val; i++) {
            System.out.println("cnt1: "+cnt1);
            System.out.println("flip: "+buffer.position());
            System.out.println("flip: "+buffer.limit());
            int cipherSize = buffer.getInt();
            System.out.println("cipherSize: "+cipherSize);
            arr[i][0] = new byte[cipherSize];
            buffer.get(arr[i][0]);
            int ivSize = buffer.getInt();
            arr[i][1] = new byte[ivSize];
            buffer.get(arr[i][1]);
//            System.out.println("cnt: "+ cnt1);
//            System.out.println("flip: "+buffer.position());
//            System.out.println("flip: "+buffer.limit());
//            if(cnt1 ==992){
//                arr[i][0] = new byte[buffer.limit()];
//                buffer.get(arr[i][0]);
//                buffer.clear();
//                client.read(buffer);
//                buffer.flip();
//                System.out.println("flip: "+buffer.position());
//                System.out.println("flip: "+buffer.limit());
//                arr[i][1] = new byte[buffer.limit()];
//                //arr[i][1] = new String(buffer.array(),0,buffer.limit()).getBytes();
//                buffer.get(arr[i][1]);
//                System.out.println("client: arr[i][0].length: "+arr[i][0].length);
//                System.out.println("client: arr[i][1].length: "+arr[i][1].length);
//            }
//            else if(cnt1 == 1008){
//                arr[i][0] = new byte[buffer.limit()];
//                buffer.get(arr[i][0]);
//                buffer.clear();
//                cnt1 = client.read(buffer);
//                if(cnt1==0){
//
//                }
//                else{
//                    buffer.flip();
//                    System.out.println("flip: "+buffer.position());
//                    System.out.println("flip: "+buffer.limit());
//                    arr[i][1] = new byte[buffer.limit()];
//                    //arr[i][1] = new String(buffer.array(),0,buffer.limit()).getBytes();
//                    buffer.get(arr[i][1]);
//                }
//                System.out.println("client: arr[i][0].length: "+arr[i][0].length);
//                System.out.println("client: arr[i][1].length: "+arr[i][1].length);
//            }
//            else if(cnt1 == 1024){ //100*+16 //cipher byte array length 가 1008 임 (iv는 16)
//                System.out.println("contract cipher 한번에 옴");
//                arr[i][0] = new byte[1008];
//                arr[i][1] = new byte[16];
//                buffer.get(arr[i][0]);
//                buffer.get(arr[i][1]);
//            }
//
//            else{
//                arr[i][0] = new byte[buffer.limit()];
//                buffer.get(arr[i][0]);
//                buffer.clear();
//                client.read(buffer);
//                buffer.flip();
//                System.out.println("flip: "+buffer.position());
//                System.out.println("flip: "+buffer.limit());
//                arr[i][1] = new byte[buffer.limit()];
//                //arr[i][1] = new String(buffer.array(),0,buffer.limit()).getBytes();
//                buffer.get(arr[i][1]);
//                System.out.println("client: arr[i][0].length: "+arr[i][0].length);
//                System.out.println("client: arr[i][1].length: "+arr[i][1].length);
//            }
        }
        System.out.println("search 끝1");
        callBack.onHESuccess(arr);
        System.out.println("search 끝2");
    }

    public static void main(String[] args) throws IOException {
        //  new HEClient("upload",null,null);
//        SocketChannel client = SocketChannel.open();
//        client.configureBlocking(true);
//        client.connect(new InetSocketAddress("localhost", 3000));
//        System.out.println("Connection Success");
//        BigInteger a = new BigInteger("abbcd", 16);
//        BigInteger b = new BigInteger("12345", 16);
//        ByteBuffer buffer = ByteBuffer.allocate(a.toByteArray().length + b.toByteArray().length + 4); //개행문자 2개
//        buffer.put(a.toByteArray());
////        buffer.put((byte) '\r');
////        buffer.put((byte) '\n');
////        buffer.put(b.toByteArray());
////        buffer.put((byte) '\r');
////        buffer.put((byte) '\n');
//////        buffer.flip();
////        client.write(buffer);
////
////        System.out.println("Sending Success");
////
////        // Server로부터 데이터 받기
//        //       buffer = ByteBuffer.allocate(100);
//        //  while(true) {
//        buffer.flip();
//        client.write(buffer);

//        client.read(buffer);
//        buffer.flip();
//        BigInteger d = new BigInteger(buffer.array());
//        System.out.println("integer: " + d.toString(16));
//        String data = Charset.defaultCharset().decode((buffer)).toString();
//        System.out.println("Received Data : " + data);

        //     }

    }
}