package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

enum Mode{
	CONTRACT_NEW, CONTRACT_CONTINUE, CONTRACT_SEARCH, REFRESH, INIT
}
public class MainFrame extends JFrame implements ChangeListener, ActionListener{
	public static int WIDTH = 500;
	public static int HEIGHT = 800;
	public static int PANNEL_WIDTH = WIDTH -15;

	//현재 선택된 탭
	int idxTab=0;
	//상단 탭
	JTabbedPane jTab = null;
	//패널
	public MainPannel mpContinue = null;
	public MainPannel mpSearch = null;
	public MainPannel mpNew = null;
	//로그창
	public JTextArea taLog = null;
	//다이얼로그창
//	public InputDialog ipDialog = new InputDialog(this,"근로자의 IP를 입력해주세요",Mode.CONTRACT_NEW);
//	public InputDialog keywordDialog = new InputDialog(this,"검색할 키워드를 입력해주세요",Mode.CONTRACT_SEARCH);
//	public InputDialog initDialog = new InputDialog(this,"로그인",Mode.INIT);
	public SignUpDialog signUpDialog = new SignUpDialog(this);
	//계약서 작성창
	public ContractGUI contractGUI;

	public MainFrame() {
		super("근로계약서 시스템");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(WIDTH, HEIGHT);
		this.setLayout(null);

		//init top tab, mainpannel
		makeTopTab();
		//init logConsole
		makeLogConsole();
		//ipDect(로그인 과정)
		this.setVisible(true);
		showInitDialog();
	}

	void makePannel() {
		mpNew = new MainPannel(Mode.CONTRACT_NEW, "시작하기");
		mpNew.button.addActionListener(this);
		mpContinue = new MainPannel(Mode.CONTRACT_CONTINUE,"이어하기");
		mpContinue.button.addActionListener(this);
		mpSearch = new MainPannel(Mode.CONTRACT_SEARCH,"확인하기");
		mpSearch.button.addActionListener(this);
	}
	void makeTopTab() {
		makePannel();
		//상단 탭(계약 시작하기/ 계약 이어하기/ 검색하기/ 동기화)
		jTab = new JTabbedPane();
		jTab.addChangeListener(this);
		jTab.addTab("계약서 검색하기", mpSearch);
		jTab.addTab("계약 작성 시작하기", mpNew);
		jTab.addTab("계약 작성 이어하기", mpContinue);
		jTab.addTab("",new ImageIcon("C:\\Users\\bzt29\\Downloads\\Icon-20.png"), new MainPannel(Mode.REFRESH));

		jTab.setBounds(0, 0,PANNEL_WIDTH, HEIGHT/4); //x, y, width, height
		this.add(jTab);
	}
	void makeLogConsole() {
		taLog=new JTextArea();
		// JTextArea 의 내용을 수정하지 못하도록 함. 즉 출력전용으로 사용
		taLog.setEditable(false);
		//수직 스크롤바는 항상 나타내고 수평 스크롤바는 필요시 나타나도록 함. (JScrollPane)
		JScrollPane scrollPane=new JScrollPane(taLog,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBounds(0, HEIGHT/4, WIDTH-13, 3*HEIGHT/4-37); //13: 스크롤바 가로, 37: 스크롤바추가되면서 추가되는 세로 길이
		this.add(scrollPane);
	}
	void addLog(String log) {
		taLog.append(log+"\n");
		taLog.setCaretPosition(taLog.getDocument().getLength());
	}
	void tabStateChanged() {
		if (jTab.getSelectedIndex() == 3) { // 선택한 탭이 "refresh" 라면 "기존"탭으로 유지하기
			if (idxTab == 2) {// 현재 탭이 "계약 이어하기"
				//relaod data
				//데이터 받아온거 뿌리기
				mpContinue.setComboBoxList(new String[] {"555","666","777"});
			}
			else {

			}
		}
		else {
			idxTab = jTab.getSelectedIndex();
		}
		jTab.setSelectedIndex(idxTab);
		System.out.println("tabStateChanged: "+idxTab);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == this.jTab){
			this.tabStateChanged();
		}
	}
	void showIpDialog() {

		String name = JOptionPane.showInputDialog("계약할 사람의 ip를 입력하세요.");
//		ipDialog.okButton.addActionListener(this);
//		ipDialog.setVisible(true);
		if(name==null)
			JOptionPane.showMessageDialog(null, "계약서 작성을 취소합니다.", "Message", JOptionPane.INFORMATION_MESSAGE);
		else {
			contractGUI = new ContractGUI();
			contractGUI.setStep1Contract(1);
		}
	}
	void showKeywordDialog() {
		String name = JOptionPane.showInputDialog("검색할 키워드를 입력하세요.");
//		keywordDialog.okButton.addActionListener(this);
//		keywordDialog.setVisible(true);
	}
	String showInitDialog() {
		String name = JOptionPane.showInputDialog("my Ip");
		if(name==null) {
			JOptionPane.showMessageDialog(null, "프로그램을 종료합니다.", "Message", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
		}
		else
			JOptionPane.showMessageDialog(null,"회원가입을 진행합니다.","Message",JOptionPane.INFORMATION_MESSAGE);
		//if(name.equals("true"))
			showSignUpDialog();

//		initDialog.okButton.addActionListener(this);
//		initDialog.setVisible(true);
		//ip tf채워넣기
		return name;
	}
	void showSignUpDialog() {

		signUpDialog.okButton.addActionListener(this);
		signUpDialog.setVisible(true);
	}

	public static void main(String[] args) {
		MainFrame frame = new MainFrame();
		for(int i =0 ;i<40;i++) {
			frame.addLog("로그"+i);
		}
		frame.mpContinue.setComboBoxList(new String[] {"111","222","333"});
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == mpNew.button){
			showIpDialog();
		}
		else if(source == mpContinue.button) {

		}
		else if(source == mpSearch.button) {
			showKeywordDialog();
		}

//		else if(source == initDialog.okButton) {
//			initDialog.setVisible(false);
//			showSignUpDialog();
//		}
//		else if(source == ipDialog.okButton) {
//			contractGUI = new ContractGUI();
//			contractGUI.setStep1Contract(1);
//		}
		else if(source == signUpDialog.okButton) {
			String userType = null;
			if(signUpDialog.userType[0].isSelected())
				userType = "점주";
			else if(signUpDialog.userType[1].isSelected())
				userType = "근로자";

			JOptionPane.showMessageDialog(null, userType+"로 회원가입 완료했습니다.", "Message", JOptionPane.INFORMATION_MESSAGE);

			signUpDialog.setVisible(false);
		}
//		else if(source == keywordDialog.okButton) {
//
//		}
	}
}

class MainPannel extends JPanel {
	public JComboBox comboBoxContract;
	public String[] contractList={};
	public JButton button;
	public Mode mode;
	public MainPannel() {
		this.setLayout(null);
	}
	public MainPannel(Mode mode) {
		this.mode = mode;
		this.setLayout(null);
	}
	public MainPannel(Mode mode, String btn) {
		this.mode = mode;
		this.setLayout(null);

		//콤보박스
		//arraylist 이용방법
		//JComboBox<?> comboBox = new JComboBox(account1.toArray(new String[account1.size()]));
		int comboBoxWIDTH = 150;
		int comboBoxHEIGHT=30;
		comboBoxContract = new JComboBox(contractList);
		comboBoxContract.setBounds(MainFrame.PANNEL_WIDTH/2-comboBoxWIDTH-10,MainFrame.HEIGHT/8-comboBoxHEIGHT,comboBoxWIDTH,comboBoxHEIGHT);
		//MyFrame.HEIGHT/4-comboBoxWIDTH: 패널이 전체의 반인데, 콤보박스는 그 패널 내에서 반이니까! + 좀 상단에 있어야해서 상대적으로 긴 WIDTH 뺌

		//버튼
		button= new JButton(btn);
		button.setBounds(MainFrame.PANNEL_WIDTH/2+10,MainFrame.HEIGHT/8-comboBoxHEIGHT,comboBoxWIDTH,comboBoxHEIGHT); //위치 바꿔야함
		//각종 컴포넌트 등록
		if (mode != Mode.CONTRACT_NEW){
			this.add(comboBoxContract);
		}
		else {
			button.setBounds(MainFrame.PANNEL_WIDTH/2-comboBoxWIDTH-10,MainFrame.HEIGHT/8-comboBoxHEIGHT,2*comboBoxWIDTH+20,comboBoxHEIGHT); //위치 바꿔야함
		}
		this.add(button);
	}
	void setComboBoxList(String[] list) {
		this.contractList = list;
		//this.comboBoxContract.removeAllItems();
		for(int i=0; i<contractList.length; i++) {
			comboBoxContract.addItem(contractList[i]);
		}
	}
	void setComboBoxList(ArrayList<String> list) {
		this.comboBoxContract.removeAllItems();
		for(int i=0; i<list.size(); i++) {
			comboBoxContract.addItem(list.get(i));
		}
	}


}
