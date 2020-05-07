package util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;

import HomomorphicEncryption.User;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

public class KeyGenerator {
	private final String ALGORITHM = "sect163k1";
	private static BouncyCastleProvider bouncyCastleProvider;
	public static final BouncyCastleProvider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();

	public Scanner scan = new Scanner(System.in);
	static {
		bouncyCastleProvider = BOUNCY_CASTLE_PROVIDER;
	}

	public KeyGenerator() {
		System.out.println("KeyGenerator 실행 ");

	}

	public void makeKey() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException,
			FileNotFoundException, IOException {
		Security.addProvider(new BouncyCastleProvider());
		KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", bouncyCastleProvider);
		ECGenParameterSpec ecsp = new ECGenParameterSpec(ALGORITHM);
		generator.initialize(ecsp, new SecureRandom());
		KeyPair keyPair = generator.generateKeyPair();
		writePemFile(keyPair.getPrivate(), "EC PRIVATE KEY", "private.pem");
		writePemFile(keyPair.getPublic(), "EC PUBLIC KEY", "public.pem");
	}

	public void makeKey(PublicKey pk, String name) throws FileNotFoundException, IOException { //암호학적으로 public이 옳지 않느건가 . . .?
		writePemFile(pk, "EC PUBLIC KEY", name);
	}


	private void writePemFile(Key key, String description, String filename) throws FileNotFoundException, IOException {
		Pem pemFile = new Pem(key, description);
		pemFile.write(filename);
		System.out.println(String.format("EC 암호키 %s을(를) %s 파일로 내보냈습니다.", description, filename));
	}
	public String replaceKey(Boolean isPrivate,String keyName) throws FileNotFoundException, IOException {
		String data = readString(keyName);

		System.out.print(data + "\n");
		// 불필요한 설명 구문을 제거합니다.
		if(isPrivate) {
			data = data.replaceAll("-----BEGIN EC PRIVATE KEY-----", "");
			data = data.replaceAll("-----END EC PRIVATE KEY-----", "");
		}
		else {
			data = data.replaceAll("-----BEGIN EC PUBLIC KEY-----", "");
			data = data.replaceAll("-----END EC PUBLIC KEY-----", "");
		}
		System.out.print(data + "\n");
		return data;
	}
	public PrivateKey readPrivateKeyFromPemFile(String privateKeyName)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		System.out.println("EC 개인키를 " + privateKeyName + "로부터 불러왔습니다.");
		String data=replaceKey(true,privateKeyName);
		byte[] decoded = Base64.decode(data);
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);

		KeyFactory factory = KeyFactory.getInstance("EC", bouncyCastleProvider);
		PrivateKey privateKey = factory.generatePrivate(spec);
		return privateKey;
	}

	// 문자열 형태의 인증서에서 공개키를 추출하는 함수입니다.
	public PublicKey readPublicKeyFromPemFile(String publicKeyName)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {

		System.out.println("EC 공개키를 " + publicKeyName + "로부터 불러왔습니다.");
		String data=replaceKey(false,publicKeyName);
		// PEM 파일은 Base64로 인코딩 되어있으므로 디코딩해서 읽을 수 있도록 합니다.

		return makePublicKey(data);
	}
	public PublicKey makePublicKey(String data) throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] decoded = Base64.decode(data);
		X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
		KeyFactory factory = KeyFactory.getInstance("EC", bouncyCastleProvider);
		PublicKey publicKey = factory.generatePublic(spec);
		return publicKey;
	}

	// 특정한 파일에 작성되어 있는 문자열을 그대로 읽어오는 함수
	private String readString(String filename) throws FileNotFoundException, IOException {
		String pem = "";
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		while ((line = br.readLine()) != null)
			pem += line;
		br.close();
		return pem;
	}


}
