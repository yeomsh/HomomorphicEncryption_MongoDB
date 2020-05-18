package GUI;

import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.*;
import javax.swing.event.ChangeListener;


enum Mode{
	CONTRACT_NEW, CONTRACT_CONTINUE, CONTRACT_SEARCH, REFRESH, INIT
}

public class MainFrame extends JFrame{
	public static int WIDTH = 500;
	public static int HEIGHT = 800;
	public static int PANNEL_WIDTH = WIDTH -15;

	//현재 선택된 탭
	public int idxTab=0;
	//상단 탭
	public JTabbedPane jTab = null;
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

	public MainFrame() {
		super("근로계약서 시스템");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(WIDTH, HEIGHT);
		this.setLayout(null);
		//init handler

		//init mainPanel
		makePannel();
		//init top tab
		makeTopTab();
		//init logConsole
		makeLogConsole();
		//ipDect(로그인 과정)
		this.setVisible(true);
		//showInitDialog();
	}
	public void setListener(ActionListener mListener){
		//버튼에 리스너들 달기
		mpNew.button.addActionListener(mListener);
		mpContinue.button.addActionListener(mListener);
		mpSearch.button.addActionListener(mListener);
		mpSearch.button2.addActionListener(mListener);
		signUpDialog.okButton.addActionListener(mListener);
	}
	public void setListener(ChangeListener mListener){
		jTab.addChangeListener(mListener);
	}
	public void makePannel() {
		mpNew = new MainPannel(Mode.CONTRACT_NEW, "시작하기");
		mpContinue = new MainPannel(Mode.CONTRACT_CONTINUE,"이어하기");
		mpSearch = new MainPannel(Mode.CONTRACT_SEARCH,"검색하기","보기");
	}
	public void makeTopTab() {
		//상단 탭(계약 시작하기/ 계약 이어하기/ 검색하기/ 동기화)
		jTab = new JTabbedPane();
	//	jTab.addChangeListener(mainListener);
		jTab.addTab("계약서 검색하기", mpSearch);
		jTab.addTab("계약 작성 시작하기", mpNew);
		jTab.addTab("계약 작성 이어하기", mpContinue);
		jTab.addTab("",new ImageIcon(".\\src\\main\\java\\GUI\\drawable\\refresh.png"), new MainPannel(Mode.REFRESH));

		jTab.setBounds(0, 0,PANNEL_WIDTH, HEIGHT/4); //x, y, width, height
		this.add(jTab);
	}
	public void makeLogConsole() {
		taLog=new JTextArea();
		// JTextArea 의 내용을 수정하지 못하도록 함. 즉 출력전용으로 사용
		taLog.setEditable(false);
		//수직 스크롤바는 항상 나타내고 수평 스크롤바는 필요시 나타나도록 함. (JScrollPane)
		JScrollPane scrollPane=new JScrollPane(taLog,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBounds(0, HEIGHT/4, WIDTH-13, 3*HEIGHT/4-37); //13: 스크롤바 가로, 37: 스크롤바추가되면서 추가되는 세로 길이
		this.add(scrollPane);
	}
	public void addLog(String log) {
		taLog.append(log+"\n");
		taLog.setCaretPosition(taLog.getDocument().getLength());
	}
}



