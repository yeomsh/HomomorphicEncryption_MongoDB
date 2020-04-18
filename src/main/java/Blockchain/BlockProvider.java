package Blockchain;

import java.util.ArrayList;
import java.util.Scanner;

public class BlockProvider {

	private static BlockRepository blockRepository= new BlockRepository();

//	public static void main(String[] args) {
//		while(true) {
//			System.out.println("1. 블록 생성    2. 종료");
//			int select = scan.nextInt();
//			scan.nextLine();
//			switch(select) {
//			case 1:
//				System.out.println("생성할 데이터를 입력하세요");
//				String data = scan.nextLine();
//				mineBlock(data);
//				break;
//			case 2:
//				return;
//			}
//		}
//	}

	//모든 block chain 반환
	public static ArrayList<Block> findAllBlockChain(){
		return blockRepository.findAllBlockChain();
	}

	//블록 생성
	public static Block mineBlock(String data) {
		//이전 해쉬값 추가
		//처음이면 0
		String previousHash = findAllBlockChain().isEmpty()? "0" : findAllBlockChain().get(findAllBlockChain().size()-1).getHash();
		Block b = new Block(data, previousHash);
		return b;

	}

	//이전해쉬값+ data로 블록 생성
	public static Block mineBlock(String data, String previousHash) {
		//이전 해쉬값 추가
		//처음이면 0
		Block b = new Block(data, previousHash);
		return b;

	}

	public static void addBlock(Block b){
		findAllBlockChain().add(b);
	}

	public void readLog(){
		//파일에서 읽어와서 hash 저장하는 것 추가해야 함
	}
}
