package util;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class StringUtil {

	public static String randomString(){
		StringBuilder sb = new StringBuilder();
		Random rnd = new Random();
		for (int i = 0; i < 256; i++) {
			int rIndex = rnd.nextInt(3);
			switch (rIndex) {
				case 0:
					// a-z
					sb.append((char) ((int) (rnd.nextInt(26)) + 97));
					break;
				case 1:
					// A-Z
					sb.append((char) ((int) (rnd.nextInt(26)) + 65));
					break;
				case 2:
					// 0-9
					sb.append((rnd.nextInt(10)));
					break;
			}
		}
		return sb.toString();
	}
	public static String getSha256(String str) {
		String SHA;
		try {
			MessageDigest sh = MessageDigest.getInstance("SHA-256");
			sh.update(str.getBytes(StandardCharsets.UTF_8));
			byte byteData[] = sh.digest();
			StringBuilder sb = new StringBuilder();
			for(byte aByteData:byteData){
				sb.append(Integer.toString((aByteData&0xff)+0x100,16).substring(1));
			}
			SHA = sb.toString();
		}catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
			SHA = null;
		}
		return SHA;
	}
	public static String SHA1(String str){
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1"); // 이 부분을 SHA-256, MD5로만 바꿔주면 된다.
			md.update(str.getBytes()); // "세이프123"을 SHA-1으로 변환할 예정!

			byte byteDatas[] = md.digest();
			StringBuilder sb = new StringBuilder();
			for (byte byteData: byteDatas){
				sb.append(Integer.toString((byteData&0xff) + 0x100, 16).substring(1));
			}
			return sb.toString();
		} catch(NoSuchAlgorithmException e){
			e.printStackTrace();
		}
		return null;
	}
	public static String readPemString(String filename) throws IOException {
		StringBuilder pem = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		while ((line = br.readLine()) != null)
			pem.append(line);
		br.close();
		return pem.toString();
	}
}
