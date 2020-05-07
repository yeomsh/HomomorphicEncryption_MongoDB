package Blockchain;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class MyFrame extends JFrame implements ActionListener {
	protected static int WIDTH = 800;
	protected static int HEIGHT = 600;
	protected static int pHeight = 500; // 메인 패널 높이
	protected static int fHeight = 100; // 뒤로가기 패널 높이
	protected static int ITEMCOUNT = 6; // 사업주, 근로자 ip/ 계약서 항목(임시로 3개)/ 서명 or 전송 버튼 => 6개
	// 뒤로가기 화면(하단)
	protected JPanel backPanel = new JPanel(new BorderLayout(0, 0));
	protected JButton homeBtn = new JButton("취소하기");

	// 계약서 저장하는 용도

	protected Vector<ContractBlock> temp = new Vector<ContractBlock>();
	protected Vector<JButton> contract = new Vector<JButton>();
	protected int contractCount = 2;
	protected ButtonGroup contractBtnGroup = new ButtonGroup();

	// 초기화면
	protected JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, WIDTH, 50));
	protected String mode = "owner";
	protected boolean checkContract = false;
	protected int ContractNumber = -1;
	protected JLabel mainTitle = new JLabel("근로 계약서 시스템");
	protected JButton ownerBtn = new JButton("사업주");
	protected JButton workerBtn = new JButton("근로자");
	protected JTextField search = new JTextField(15);
	protected JButton makeContractBtn = new JButton("작성하기   ");
	protected JButton askedContractBtn = new JButton("계약서가 도착하면 버튼이 활성화됩니다. 계약서를 이어서 작성하고 싶은 경우에 클릭해주세요!");
	protected JButton showContractBtn = new JButton("   확인하기");
	protected JPanel ContractMenu = new JPanel(new FlowLayout());
	// 계약서 작성하기 화면
	protected JPanel mcPanel = new JPanel(new FlowLayout());
	protected JLabel contractTitle = new JLabel("계약서");
	protected JLabel oIpLabel1 = new JLabel("사업주 ip(작성자 ip)");
	protected JTextField oIpField1 = new JTextField(15);
	protected JLabel wIpLabel1 = new JLabel("근로자 이름(ip)");
	protected JTextField wIpField1 = new JTextField(15);
	protected JLabel oNameLabel1 = new JLabel("사업주 이름");
	protected JTextField oNameField1 = new JTextField(15);
	protected JLabel wageLabel1 = new JLabel("시급");
	protected JLabel ectLabel = new JLabel("기타내용");
	protected JTextField ectField = new JTextField(15);
	protected JTextField wageField1 = new JTextField(15);
	protected JLabel wNameLabel1 = new JLabel("근로자 이름");
	protected JTextField wNameField1 = new JTextField(15);
	protected JButton sigBtn1 = new JButton("서명하기");
	protected JButton sendBtn1 = new JButton("전송하기");

	protected JButton OKBtn1 = new JButton("확인하기");

	// 계약서 작성 요청 들어왔을 때 화면
	protected JPanel acPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, WIDTH / 4, 60));
	protected JLabel oIpLabel2 = new JLabel("사업주 ip(작성자 ip)");
	protected JTextField oIpField2 = new JTextField(15);
	protected JLabel wIpLabel2 = new JLabel("근로자 이름(ip)");
	protected JTextField wIpField2 = new JTextField(15);
	protected JLabel oNameLabel2 = new JLabel("사업주 이름");
	protected JTextField oNameField2 = new JTextField(15);
	protected JLabel wageLabel2 = new JLabel("시급");
	protected JTextField wageField2 = new JTextField(15);
	protected JLabel wNameLabel2 = new JLabel("근로자 이름");
	protected JTextField wNameField2 = new JTextField(15);
	protected JButton sigBtn2 = new JButton("서명하고 전송하기");

	protected JButton OKBtn2 = new JButton("확인하기");
	public Font bigFont = search.getFont().deriveFont(Font.BOLD, 40);

	protected Color c_blue = new Color(76, 116, 185);
	protected Color c_gray = new Color(182, 187, 196);

	// 프로그램운영변수
	protected JPanel nowPanel = mainPanel;

	public MyFrame() {
		super("근로계약서 시스템");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(WIDTH, WIDTH);
		this.setVisible(true);
		this.setLayout(new BorderLayout());

		init();
		mainPanel.setBackground(c_blue);
		this.add(mainPanel, "Center");

		backPanel.setBackground(Color.GRAY);
		backPanel.add(new JLabel("작업을 취소하고 처음화면으로 돌아가려면 우측 버튼을 눌러주세요"), "West");
		backPanel.add(homeBtn, "East");
		homeBtn.addActionListener(this);

		this.add(backPanel, "South");

		// makeContractPanel1();
		askedContractPanel();

	}

	public void init() { // 초기화면
		JPanel jp = new JPanel(new GridLayout(2, 1));
		JPanel jp2 = new JPanel(new GridLayout(1, 2));
		JPanel jp3 = new JPanel(new GridLayout(1, 2));

		jp.setBackground(c_blue);
		jp2.setBackground(c_blue);
		jp3.setBackground(c_blue);

		mainTitle.setFont(bigFont);
		mainTitle.setForeground(Color.white);
		jp.add(mainTitle);

		ownerBtn.setSelected(true);
		ownerBtn.setBackground(c_blue);
		ownerBtn.setForeground(Color.white);
		ownerBtn.setBorder(null);
		ownerBtn.addActionListener(this);
		jp2.add(ownerBtn);

		workerBtn.addActionListener(this);
		workerBtn.setBackground(c_blue);
		workerBtn.setForeground(Color.white);
		workerBtn.setBorder(null);
		jp2.add(workerBtn);

		jp.add(jp2);

		ButtonGroup modeBtnGroup = new ButtonGroup();
		modeBtnGroup.add(ownerBtn);
		modeBtnGroup.add(workerBtn);

		mainPanel.add(jp);

		search.setBackground(Color.white);
		search.setFont(bigFont);
		mainPanel.add(search);

		mainPanel.add(jp3);
		makeContractBtn.addActionListener(this);
		makeContractBtn.setBackground(c_blue);
		makeContractBtn.setForeground(Color.white);
		makeContractBtn.setFont(bigFont);
		makeContractBtn.setBorder(null);
		makeContractBtn.setEnabled(false);

		/// makeContractBtn.addActionListener(this);

		showContractBtn.addActionListener(this);
		showContractBtn.setBackground(c_blue);
		showContractBtn.setForeground(Color.white);
		showContractBtn.setFont(bigFont);
		showContractBtn.setBorder(null);
		showContractBtn.setEnabled(false);

		jp3.add(makeContractBtn);
		jp3.add(showContractBtn);

		// askedContractBtn.setEnabled(false);

		// askedContractBtn.addActionListener(this);
		mainPanel.add(askedContractBtn);

		ContractMenu.setBackground(Color.white);
		ContractMenu.setPreferredSize(new Dimension(800, 400));

		contract.clear();
		// for(int i=0;i<contractCount;i++) {
		// String name = "contract" + (i+1);
		// temp.add(new ContractBlock()); //나중에 보낼 때 추가하는 걸로 변경해도 ok
		// temp.get(i).contract.addActionListener(this);
		// contractBtnGroup.add(temp.get(i).contract);
		// ContractMenu.add(temp.get(i).contract);
		// }

		for (int i = 0; i < contractCount; i++) {
			String name = "contract" + (i + 1);
			contract.add(new JButton(name));
			contract.get(i).setBackground(c_gray);
			contract.get(i).setPreferredSize(new Dimension(150, 250));
			contract.get(i).addActionListener(this);
			contractBtnGroup.add(contract.get(i));
			ContractMenu.add(contract.get(i));
		}
		mainPanel.add(ContractMenu);
	}

	/*
	 * public void makeContractPanel() {
	 *
	 * mcPanel.setBackground(c_blue); contractTitle.setFont(bigFont);
	 * contractTitle.setPreferredSize(new Dimension(400,100));
	 * contractTitle.setForeground(Color.white);
	 * mcPanel.add(contractTitle,"Center"); JPanel[] pn = new JPanel[ITEMCOUNT]; for
	 * (int i = 0; i < ITEMCOUNT; i++) { pn[i] = new JPanel(new BorderLayout(0,2));
	 * mcPanel.add(pn[i]); }
	 *
	 * oIpField1.setEnabled(false); oIpLabel1.setPreferredSize(new
	 * Dimension(100,50)); oIpLabel1.setBorder(null); pn[0].add(oIpLabel1, "West");
	 * pn[0].add(oIpField1, "Center"); // 자동 채움
	 *
	 * wIpLabel1.setPreferredSize(new Dimension(100,50)); wIpLabel1.setBorder(null);
	 * pn[1].add(wIpLabel1, "West"); pn[1].add(wIpField1, "Center");
	 *
	 * oNameLabel1.setPreferredSize(new Dimension(100,50));
	 * oNameLabel1.setBorder(null); pn[2].add(oNameLabel1, "West");
	 * pn[2].add(oNameField1, "Center");
	 *
	 * wageLabel1.setPreferredSize(new Dimension(100,50));
	 * wageLabel1.setBorder(null); pn[3].add(wageLabel1, "West");
	 * pn[3].add(wageField1, "Center");
	 *
	 * wNameLabel1.setPreferredSize(new Dimension(100,50));
	 * wNameLabel1.setBorder(null); wNameField1.setEnabled(false);
	 * pn[4].add(wNameLabel1, "West"); pn[4].add(wNameField1, "Center"); // 값 유무에 따라
	 * 달라짐
	 *
	 * // sigBtn1.addActionListener(this); // sendBtn1.addActionListener(this);
	 * pn[5].setBackground(c_blue); pn[5].add(sigBtn1, "West"); pn[5].add(sendBtn1,
	 * "East"); sigBtn1.setBackground(c_blue); sigBtn1.setForeground(Color.white);
	 * sigBtn1.setFont(bigFont); sendBtn1.setBackground(c_blue);
	 * sendBtn1.setForeground(Color.white); sendBtn1.setFont(bigFont);
	 *
	 * for(int i=0;i<5;i++) { pn[i].setBackground(Color.white);
	 * pn[i].setPreferredSize(new Dimension(500,50)); } }
	 */

	//계약하기 누른 후 사업주 화면
	public void makeContractPanel1() {

		ITEMCOUNT = 6;
		mcPanel.setBackground(c_blue);
		contractTitle.setFont(bigFont);
		contractTitle.setPreferredSize(new Dimension(400, 100));
		contractTitle.setForeground(Color.white);
		mcPanel.add(contractTitle, "Center");
		JPanel[] pn = new JPanel[ITEMCOUNT];
		for (int i = 0; i < ITEMCOUNT; i++) {
			pn[i] = new JPanel(new BorderLayout(0, 2));
			mcPanel.add(pn[i]);
		}

		oIpField1.setEnabled(false);
		oIpLabel1.setPreferredSize(new Dimension(100, 50));
		oIpLabel1.setBorder(null);
		pn[0].add(oIpLabel1, "West");
		pn[0].add(oIpField1, "Center"); // 자동 채움

		wIpLabel1.setPreferredSize(new Dimension(100, 50));
		wIpLabel1.setBorder(null);
		pn[1].add(wIpLabel1, "West");
		pn[1].add(wIpField1, "Center");

		oNameLabel1.setPreferredSize(new Dimension(100, 50));
		oNameLabel1.setBorder(null);
		pn[2].add(oNameLabel1, "West");
		pn[2].add(oNameField1, "Center");

		wageLabel1.setPreferredSize(new Dimension(100, 50));
		wageLabel1.setBorder(null);
		pn[3].add(wageLabel1, "West");
		pn[3].add(wageField1, "Center");

		ectLabel.setPreferredSize(new Dimension(100, 50));
		ectLabel.setBorder(null);
		pn[4].add(ectLabel, "West");
		pn[4].add(ectField, "Center");
		// sigBtn1.addActionListener(this);
		OKBtn1.addActionListener(this);
		pn[5].setBackground(c_blue);
		OKBtn1.setBackground(c_blue);
		OKBtn1.setForeground(Color.white);
		OKBtn1.setPreferredSize(new Dimension(600, 50));

		OKBtn1.setFont(bigFont);
		pn[5].add(OKBtn1, "Center");

		for (int i = 0; i < 5; i++) {
			pn[i].setBackground(Color.white);
			pn[i].setPreferredSize(new Dimension(500, 50));
		}
		ITEMCOUNT = 6;
	}

	public void makeContractPanel2() {

		ITEMCOUNT = 6;
		mcPanel.setBackground(c_blue);
		contractTitle.setFont(bigFont);
		contractTitle.setPreferredSize(new Dimension(400, 100));
		contractTitle.setForeground(Color.white);
		mcPanel.add(contractTitle, "Center");
		JPanel[] pn = new JPanel[ITEMCOUNT];
		for (int i = 0; i < ITEMCOUNT; i++) {
			pn[i] = new JPanel(new BorderLayout(0, 2));
			mcPanel.add(pn[i]);
		}

		oIpField1.setEnabled(false);
		oIpLabel1.setPreferredSize(new Dimension(100, 50));
		oIpLabel1.setBorder(null);
		pn[0].add(oIpLabel1, "West");
		pn[0].add(oIpField1, "Center"); // 자동 채움

		wIpField1.setEnabled(false);
		wIpLabel1.setPreferredSize(new Dimension(100, 50));
		wIpLabel1.setBorder(null);
		pn[1].add(wIpLabel1, "West");
		pn[1].add(wIpField1, "Center");

		oNameField1.setEnabled(false);
		oNameLabel1.setPreferredSize(new Dimension(100, 50));
		oNameLabel1.setBorder(null);
		pn[2].add(oNameLabel1, "West");
		pn[2].add(oNameField1, "Center");

		wageField1.setEnabled(false);
		wageLabel1.setPreferredSize(new Dimension(100, 50));
		wageLabel1.setBorder(null);
		pn[3].add(wageLabel1, "West");
		pn[3].add(wageField1, "Center");

		wNameField1.setEnabled(false);
		wNameLabel1.setPreferredSize(new Dimension(100, 50));
		wNameLabel1.setBorder(null);
		pn[4].add(wNameLabel1, "West");
		pn[4].add(wNameField1, "Center"); // 값 유무에 따라 달라짐

		// sigBtn1.addActionListener(this);
//		OKBtn1.addActionListener(this);
		pn[5].setBackground(c_blue);
		sigBtn1.setBackground(c_blue);
		sigBtn1.setForeground(Color.white);
		sigBtn1.setPreferredSize(new Dimension(600, 50));

		sigBtn1.setFont(bigFont);
		sigBtn1.setBorder(null);
		pn[5].add(sigBtn1, "Center");

		for (int i = 0; i < 5; i++) {
			pn[i].setBackground(Color.white);
			pn[i].setPreferredSize(new Dimension(500, 50));
		}
		ITEMCOUNT = 6;

	}
	public void askedContractPanel() {
		acPanel.setBackground(c_blue);
		contractTitle.setFont(bigFont);
		contractTitle.setPreferredSize(new Dimension(400, 100));
		contractTitle.setForeground(Color.white);
		mcPanel.add(contractTitle, "Center");
		JPanel[] pn = new JPanel[ITEMCOUNT];
		for (int i = 0; i < ITEMCOUNT; i++) {
			pn[i] = new JPanel(new BorderLayout(0, 2));
			acPanel.add(pn[i]);
		}

		oIpField2.setEnabled(false);
		oIpLabel2.setPreferredSize(new Dimension(100, 50));
		oIpLabel2.setBorder(null);
		pn[0].add(oIpLabel2, "West");
		pn[0].add(oIpField2, "Center"); // 자동 채움

		wIpField2.setEnabled(false);
		wIpLabel2.setPreferredSize(new Dimension(100, 50));
		wIpLabel2.setBorder(null);
		pn[1].add(wIpLabel2, "West");
		pn[1].add(wIpField2, "Center");

		oNameField2.setEnabled(false);
		oNameLabel2.setPreferredSize(new Dimension(100, 50));
		oNameLabel2.setBorder(null);
		pn[2].add(oNameLabel2, "West");
		pn[2].add(oNameField2, "Center");

		wageField2.setEnabled(false);
		wageLabel2.setPreferredSize(new Dimension(100, 50));
		wageLabel2.setBorder(null);
		pn[3].add(wageLabel2, "West");
		pn[3].add(wageField2, "Center");

		wNameLabel2.setPreferredSize(new Dimension(100, 50));
		wNameLabel2.setBorder(null);
		pn[4].add(wNameLabel2, "West");
		pn[4].add(wNameField2, "Center"); // 값 유무에 따라 달라짐

		// sigBtn1.addActionListener(this);
		OKBtn2.addActionListener(this);
		pn[5].setBackground(c_blue);
		OKBtn2.setBackground(c_blue);
		OKBtn2.setForeground(Color.white);
		OKBtn2.setPreferredSize(new Dimension(600, 50));

		OKBtn2.setFont(bigFont);
		pn[5].add(OKBtn2, "Center");

		for (int i = 0; i < 5; i++) {
			pn[i].setBackground(Color.white);
			pn[i].setPreferredSize(new Dimension(500, 50));
		}
	}

	public void askedContractPanel2() {
		acPanel.setBackground(c_blue);
		contractTitle.setFont(bigFont);
		contractTitle.setPreferredSize(new Dimension(400, 100));
		contractTitle.setForeground(Color.white);
		mcPanel.add(contractTitle, "Center");
		JPanel[] pn = new JPanel[ITEMCOUNT];
		for (int i = 0; i < ITEMCOUNT; i++) {
			pn[i] = new JPanel(new BorderLayout(0, 2));
			acPanel.add(pn[i]);
		}

		oIpField2.setEnabled(false);
		oIpLabel2.setPreferredSize(new Dimension(100, 50));
		oIpLabel2.setBorder(null);
		pn[0].add(oIpLabel2, "West");
		pn[0].add(oIpField2, "Center"); // 자동 채움

		wIpField2.setEnabled(false);
		wIpLabel2.setPreferredSize(new Dimension(100, 50));
		wIpLabel2.setBorder(null);
		pn[1].add(wIpLabel2, "West");
		pn[1].add(wIpField2, "Center");

		oNameField2.setEnabled(false);
		oNameLabel2.setPreferredSize(new Dimension(100, 50));
		oNameLabel2.setBorder(null);
		pn[2].add(oNameLabel2, "West");
		pn[2].add(oNameField2, "Center");

		wageField2.setEnabled(false);
		wageLabel2.setPreferredSize(new Dimension(100, 50));
		wageLabel2.setBorder(null);
		pn[3].add(wageLabel2, "West");
		pn[3].add(wageField2, "Center");

		wNameField2.setEnabled(false);
		wNameLabel2.setPreferredSize(new Dimension(100, 50));
		wNameLabel2.setBorder(null);
		pn[4].add(wNameLabel2, "West");
		pn[4].add(wNameField2, "Center"); // 값 유무에 따라 달라짐

		// sigBtn1.addActionListener(this);
		sigBtn2.addActionListener(this);
		pn[5].setBackground(c_blue);
		sigBtn2.setBackground(c_blue);
		sigBtn2.setForeground(Color.white);
		sigBtn2.setPreferredSize(new Dimension(600, 50));

		sigBtn2.setFont(bigFont);
		pn[5].add(sigBtn2, "Center");

		for (int i = 0; i < 5; i++) {
			pn[i].setBackground(Color.white);
			pn[i].setPreferredSize(new Dimension(500, 50));
		}

	}

	public void changePanel(JPanel newPn) {
		this.remove(nowPanel);
		this.add(newPn, "Center");
		this.revalidate();
		this.repaint();
		nowPanel = newPn;
	}

	public void clear() {
		oIpField1.setText("");
		wIpField1.setText("");
		oNameField1.setText("");
		wageField1.setText("");
		wNameField1.setText("");

		// 계약서 작성 요청 들어왔을 때 화면
		oIpField2.setText("");
		wIpField2.setText("");
		oNameField2.setText("");
		wageField2.setText("");
		wNameField2.setText("");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		JOptionPane.showMessageDialog(null, e.getSource());
		if (e.getSource() == ownerBtn) {
			mode = "owner";
			makeContractBtn.setEnabled(true);
		} else if (e.getSource() == workerBtn) {
			mode = "worker";
			makeContractBtn.setEnabled(false);
		} else if (e.getSource() == makeContractBtn) {
			changePanel(mcPanel);
		} else if (e.getSource() == askedContractBtn) {
			changePanel(acPanel);
		} else if (e.getSource() == sigBtn1) {

		} else if (e.getSource() == sendBtn1) {

		} else if (e.getSource() == OKBtn1) {

		} else if (e.getSource() == homeBtn) {
			clear();
//			if (mode == "worker")
//				makeContractBtn.setEnabled(false);
//			askedContractBtn.setEnabled(false);
			changePanel(mainPanel);
		} else {
//			for (int i = 0; i < contract.size(); i++) {
//				if (e.getSource() == contract.get(i)) {
//					ContractNumber = i;
//					checkContract = true;
//					JOptionPane.showMessageDialog(null, i);
//				}
//			}
		}
	}

}