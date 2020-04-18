package Blockchain;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.json.simple.JSONObject;

public class ContractBlock extends JFrame{
	protected JButton contract = new JButton("contractBlock");
	protected JSONObject content = new JSONObject();
	public ContractBlock(){
		contract.setBackground(new Color(182,187,196));
		contract.setPreferredSize(new Dimension(150,250));
		content.put("key", "content");
	}
}
