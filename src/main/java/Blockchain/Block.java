package Blockchain;

import java.util.Date;

import util.StringUtil;

public class Block {

	private String hash; //이벙네 생성된 hash 값
	private String previousHash; //바로 이전 블록 hash 값
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

}
