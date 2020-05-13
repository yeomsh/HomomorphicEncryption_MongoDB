package Blockchain;

import java.util.Date;

import util.StringUtil;

public class Block {

	private String hash;
	private String previousHash;
	private String data; // Transaction
	private long timeStamp;
	private int nonce = 0;
	private String target = "00000";
	private int targetDepth = 5;

	// 만약 들어온 순서와 다르게 블럭이 생성된다면?
	public Block(String data, String previousHash) {
		System.out.println("---------------");
		System.out.println(data);
		this.data = data;
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
	}

	public String ProofOfWork() {
		mineNewBlock();
		return hash;
		// 파일에 저장
		// makeLog();
		// 구한 값 보내기 (hash값)
	}

	// 신규 블록 생성
	private void mineNewBlock() {
		// 해쉬 앞부분이 00000으로 시작하는 순간이 올 때까지 반복
		// 조건에 맞는 hash값을 찾을 때까지 계속 반복
		while (hash == null || !hash.substring(0, targetDepth).equals(target)) {
			nonce++;
			hash = makeHashBlock();
		}
	}

	// hash값 만들기 (이전 해쉬값 + 시간 + data + nonce)
	public String makeHashBlock() {
		return StringUtil.getSha256(previousHash + timeStamp + data + nonce);
	}

	public String getHash() {
		return this.hash;
	}

	public int getNonce() {
		return nonce;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	// hash 값 파일에 저장
	// public void makeLog(){
	//
	// String filePath = "C:\\Users\\염상희\\Desktop\\log.txt";
	// try {
	// PrintWriter pw= new PrintWriter(new FileWriter(filePath, true));
	// pw.write(hash + "\r\n");
	// pw.close();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
}
