package util;

import java.io.FileNotFoundException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.Key;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

public class Pem {

    private PemObject pemObject;
    public Pem(Key key, String description) {  // 키 데이터와 키에 대한 설명 정보를 PEM 객체에 저장
        this.pemObject = new PemObject(description, key.getEncoded());
    }
    public Pem(byte[] key, String description){  // byte[] 형식의 키 데이터와 키에 대한 설명 정보를 PEM 객체에 저장
        this.pemObject = new PemObject(description,key);
    }

    // 특정한 파일 이름으로 PEM 파일을 저장
    public void write(String filename) throws IOException{
        PemWriter pemWriter = new PemWriter(new OutputStreamWriter(new FileOutputStream(filename)));
        pemWriter.writeObject(this.pemObject);
        pemWriter.close();
    }
}
