package util;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtil {

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
}
