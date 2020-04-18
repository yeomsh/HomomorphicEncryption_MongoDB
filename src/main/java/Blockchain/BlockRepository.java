package Blockchain;
import java.util.ArrayList;

public class BlockRepository {
	private static ArrayList<Block> blockChain = new ArrayList<>();
	public ArrayList<Block> findAllBlockChain(){
		return blockChain;
	}
}
