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

import ecies.ECIES;
import ecies.EllipticCurve;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

public class KeyGenerator {
	private final String ALGORITHM = "sect163k1";
	private static BouncyCastleProvider bouncyCastleProvider;
	public static final BouncyCastleProvider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();
	public ECIES ecies = new ECIES();
	public EllipticCurve ellipticCurve = new EllipticCurve(ecies);
	static {
		bouncyCastleProvider = BOUNCY_CASTLE_PROVIDER;
	}

	public KeyGenerator() {
		System.out.println("KeyGenerator 실행 ");

	}
	public void makeECDSAKey() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException,
			IOException {
		Security.addProvider(new BouncyCastleProvider());
		KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", bouncyCastleProvider);
		ECGenParameterSpec ecsp = new ECGenParameterSpec(ALGORITHM);
		generator.initialize(ecsp, new SecureRandom());
		KeyPair keyPair = generator.generateKeyPair();
		writePemFile(keyPair.getPrivate(), "ECDSA PRIVATE KEY", "ECDSAprivate.pem");
		writePemFile(keyPair.getPublic(), "ECDSA PUBLIC KEY", "ECDSApublic.pem");
	}

	public void makeECIESKey() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeySpecException, IOException {
		this.ellipticCurve = new EllipticCurve(new ECIES());
		byte[][] eciesKeypair = ellipticCurve.generateKeyPair();
		writeECIESKey(eciesKeypair[0],eciesKeypair[1]);

	}
	public void writeECIESKey(byte[] privateKey, byte[] publicKey) throws IOException {
		writePemFile(privateKey, "ECIES PRIVATE KEY", "ECIESprivate.pem");
		writePemFile(publicKey, "ECIES PUBLIC KEY", "ECIESpublic.pem");
	}
	private void writePemFile(byte[] key, String description, String filename) throws IOException {
		Pem pemFile = new Pem(key, description);
		pemFile.write(filename);
		System.out.println(String.format("EC 암호키 %s을(를) %s 파일로 내보냈습니다.", description, filename));
	}
	private void writePemFile(Key key, String description, String filename) throws IOException {
		Pem pemFile = new Pem(key, description);
		pemFile.write(filename);
		System.out.println(String.format("EC 암호키 %s을(를) %s 파일로 내보냈습니다.", description, filename));
	}
	public String replaceKey(Boolean isPrivate,String keyName, String tag) throws IOException {
		String data = StringUtil.readPemString(keyName);
		System.out.print(data + "\n");
		// 불필요한 설명 구문을 제거합니다.
		if(isPrivate) {
			data = data.replaceAll("-----BEGIN "+tag+" PRIVATE KEY-----", "");
			data = data.replaceAll("-----END "+tag+" PRIVATE KEY-----", "");
		}
		else {
			data = data.replaceAll("-----BEGIN "+tag+" PUBLIC KEY-----", "");
			data = data.replaceAll("-----END "+tag+" PUBLIC KEY-----", "");
		}
		System.out.print(data + "\n");
		return data;
	}

	public byte[] readECIESPublicKeyFromPemFile(String publicKeyName)
			throws IOException {
		System.out.println("EC 공개키를 " + publicKeyName + "로부터 불러왔습니다.");
		String data=replaceKey(false,publicKeyName,"ECIES");
		return Base64.decode(data);
	}

	public byte[] readECIESPrivateKeyFromPemFile(String privateKeyName)
			throws IOException {
		System.out.println("EC 개인키를 " + privateKeyName + "로부터 불러왔습니다.");
		String data=replaceKey(true,privateKeyName,"ECIES");
		return Base64.decode(data);
	}
	public PrivateKey readECDSAPrivateKeyFromPemFile(String privateKeyName)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		System.out.println("EC 개인키를 " + privateKeyName + "로부터 불러왔습니다.");
		String data=replaceKey(true,privateKeyName,"ECDSA");
		byte[] decoded = Base64.decode(data);
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
		KeyFactory factory = KeyFactory.getInstance("EC", bouncyCastleProvider);
		return factory.generatePrivate(spec);
	}

	// 문자열 형태의 인증서에서 공개키를 추출하는 함수입니다.
	public PublicKey readECDSAPublicKeyFromPemFile(String publicKeyName)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

		System.out.println("EC 공개키를 " + publicKeyName + "로부터 불러왔습니다.");
		String data=replaceKey(false,publicKeyName,"ECDSA");
		// PEM 파일은 Base64로 인코딩 되어있으므로 디코딩해서 읽을 수 있도록 합니다.

		return makePublicKey(data);
	}
	public PublicKey makePublicKey(String data) throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] decoded = Base64.decode(data);
		X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
		KeyFactory factory = KeyFactory.getInstance("EC", bouncyCastleProvider);
		return factory.generatePublic(spec);
	}

	// 특정한 파일에 작성되어 있는 문자열을 그대로 읽어오는 함수



}