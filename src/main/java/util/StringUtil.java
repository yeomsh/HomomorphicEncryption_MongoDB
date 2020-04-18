package util;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtil {

	public static String getSha256(String str) {
		String SHA;
		try {
			MessageDigest sh = MessageDigest.getInstance("SHA-256");
			sh.update(str.getBytes("UTF-8"));
			byte byteData[] = sh.digest();
			StringBuffer sb = new StringBuffer();
			for(byte aByteData:byteData){
				sb.append(Integer.toString((aByteData&0xff)+0x100,16).substring(1));
			}
			SHA = sb.toString();
		}catch(NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
			SHA = null;
		}
		return SHA;
	}
}
