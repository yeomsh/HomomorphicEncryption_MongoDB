package Blockchain;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.json.simple.JSONObject;

import util.FileManage;
import util.KeyGenerator;


public class TotalManager {

	public Scanner scan = new Scanner(System.in);

	protected Vector<String> ipList;
	protected Vector<Client> cList;
	protected static ArrayList<String> chainStr = new ArrayList<String>();
	public static String PATH;
	public FileManage chainFile;
	protected Server server;
	protected Vector<String> ableIp;
	protected static PrivateKey privateKey = null;
	protected static PublicKey publicKey = null;
	protected static Boolean isVerifySuccess = true; // 검증 실패 성공 구분하는 변수
	protected static int countPOW = 0;
	protected static JSONObject block = new JSONObject(); // 작업증명시 timestamp 등등 해쉬값
	protected static MyFrame frame;
	protected String myIp = "";

	JSONObject data = new JSONObject();
	protected static KeyGenerator KG = new KeyGenerator();

	public TotalManager(MyFrame frame) {
		this.frame = frame;
		MyActionListener2 mListener = new MyActionListener2(frame, this);
		frame.ownerBtn.addActionListener(mListener);
		frame.workerBtn.addActionListener(mListener);
		frame.makeContractBtn.addActionListener(mListener);
		frame.askedContractBtn.addActionListener(mListener);
		frame.sigBtn1.addActionListener(mListener);
		frame.sendBtn1.addActionListener(mListener);
		frame.sigBtn2.addActionListener(mListener);
		frame.homeBtn.addActionListener(mListener);
		frame.addWindowListener(mListener);

	}

	public void readChain() { // chain의 text를 찾는 함수

		try {
			PATH = TotalManager.class.getResource("").getPath();
			PATH = java.net.URLDecoder.decode(PATH, "UTF-8");
			String dir = PATH + "chain.txt";
			chainFile = new FileManage(PATH);
			chainFile.makeContractFolder();
			chainStr.clear();
			try {
				chainStr = chainFile.fileLineRead(dir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	public static void loadIpList(Vector<String> ipList) { // ipList찾는 함수

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			String url = "jdbc:mysql://203.252.166.243:3306/project?serverTimezone=UTC";
			String userId = "user";
			String userPw = "useraccess";
			conn = DriverManager.getConnection(url, userId, userPw);
			System.out.println("연결 성공");

			// insert("203.252.166.243");
			stmt = conn.createStatement();
			String sql = "SELECT ip from ipList";

			// 5. 쿼리 수행
			// 레코드들은 ResultSet 객체에 추가된다.
			rs = stmt.executeQuery(sql);

			// 6. 실행결과 출력하기
			while (rs.next()) {
				// 레코드의 칼럼은 배열과 달리 0부터 시작하지 않고 1부터 시작한다.
				// 데이터베이스에서 가져오는 데이터의 타입에 맞게 getString 또는 getInt 등을 호출한다.
				String ip = rs.getString(1);
				ipList.addElement(ip);
				System.out.println(ipList.lastElement());
			}

		} catch (ClassNotFoundException e) {
			System.out.println("드라이버 로딩 실패");
		} catch (SQLException e) {
			System.out.println("에러: " + e);
		} finally {
			try {
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
				if (stmt != null && !stmt.isClosed()) {
					stmt.close();
				}
				if (rs != null && !rs.isClosed()) {
					rs.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	public static boolean hasMyIp(Vector<String> ipList, String ip) { // ip찾는 함수

		for (int i = 0; i < ipList.size(); i++) {

			if (ipList.get(i).equals(ip)) {
				ipList.remove(i);
				return true;
			}
		}
		return false;
	}

	public static void insertIp(String ip) { // ip추가하는 함수
		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			// 1. 드라이버 로딩
			Class.forName("com.mysql.cj.jdbc.Driver");
			String url = "jdbc:mysql://203.252.166.243:3306/project?serverTimezone=UTC";
			String userId = "user";
			String userPw = "useraccess";
			conn = DriverManager.getConnection(url, userId, userPw);
			System.out.println("연결 성공");

			// SQL 쿼리 준비
			// 추가하려는 데이터의 값은 전달된 인자를 통해 동적으로 할당되는 값이다.

			String sql = "INSERT INTO ipList VALUES (?)";
			pstmt = conn.prepareStatement(sql);

			// 데이터 ing
			pstmt.setString(1, ip);

			// 5. 쿼리 실행 및 결과 처리
			// SELECT와 달리 INSERT는 반환되는 데이터들이 없으므로
			// ResultSet 객체가 필요 없고, 바로 pstmt.executeUpdate()메서드를 호출하면 됩니다.
			// INSERT, UPDATE, DELETE 쿼리는 이와 같이 메서드를 호출하며
			// SELECT에서는 stmt.executeQuery(sql); 메서드를 사용했었습니다.
			// @return int - 몇 개의 row가 영향을 미쳤는지를 반환
			int count = pstmt.executeUpdate();
			if (count == 0) {
				System.out.println("데이터 입력 실패");
			} else {
				System.out.println("데이터 입력 성공");
			}
		} catch (ClassNotFoundException e) {
			System.out.println("드라이버 로딩 실패");
		} catch (SQLException e) {
			System.out.println("에러 " + e);
		} finally {
			try {
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
				if (pstmt != null && !pstmt.isClosed()) {
					pstmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static void insertContract(String oName, String wName, int wage) { // ip추가하는 함수
		Connection conn = null;
		Statement stmt = null;

		try {
			// 1. 드라이버 로딩
			Class.forName("com.mysql.cj.jdbc.Driver");
			String url = "jdbc:mysql://203.252.166.243:3306/project?serverTimezone=UTC";
			String userId = "user";
			String userPw = "useraccess";
			conn = DriverManager.getConnection(url, userId, userPw);
			System.out.println("연결 성공");

			// SQL 쿼리 준비
			// 추가하려는 데이터의 값은 전달된 인자를 통해 동적으로 할당되는 값이다.

			stmt = conn.createStatement();

			// 데이터 binding
			StringBuilder sb = new StringBuilder();
			String sql = sb.append("INSERT INTO contract(oName, wName, wage) VALUES(").append("'" + oName + "',")
					.append("'" + wName + "',").append(wage).append(");").toString();
			try {
				stmt.executeUpdate(sql);
			} catch (SQLException e) {
				System.out.println("에러 " + e);
			} finally {
				try {
					if (conn != null && !conn.isClosed()) {
						conn.close();
					}
					if (stmt != null && !stmt.isClosed()) {
						stmt.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		} catch (ClassNotFoundException e) {
			System.out.println("드라이버 로딩 실패");
		} catch (SQLException e) {
			System.out.println("에러 " + e);
		} finally {
			try {
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
				if (stmt != null && !stmt.isClosed()) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void init() throws FileNotFoundException, IOException, NoSuchAlgorithmException, NoSuchProviderException,
			InvalidAlgorithmParameterException, InvalidKeySpecException {
		File keyFile = new File("private.pem");
		if (!keyFile.exists()) {
			KG.makeKey();
		}
		privateKey = KG.readPrivateKeyFromPemFile("private.pem");
		publicKey = KG.readPublicKeyFromPemFile("public.pem");
		JSONObject data = new JSONObject();
		cList = new Vector<Client>(); // 계약서를 보내야하는 객체들의 컬렉션

		// 1. ipList 갱신 및 목록에서 본인 아이피 제외
		ipList = new Vector<String>();
		loadIpList(ipList);
		myIp = "";
		try {
			InetAddress ip = InetAddress.getLocalHost();
			myIp = ip.getHostAddress();
		} catch (Exception e) {
		}

		if (!hasMyIp(ipList, myIp)) {
			insertIp(myIp);
		}

		// 2. 서버 실행시키기
		readChain();
		server = new Server(3000, chainStr);
		server.start();// port번호 임의로 3000
		if (frame.mode == "owner")
			frame.makeContractBtn.setEnabled(true); // 점주모드일땐 활성화, 아닐 땐 비활성화 (나중에 예외처리)
		else
			JOptionPane.showMessageDialog(null, "ip리스트 로딩이 완료되었습니다");

		while (true) {
			System.out.println("번호를 선택해주세요");
			int num = scan.nextInt();
			switch (num) {
				case 1:
					step1();
					break;
				case 2:
					step2();
					break;
				case 3:
					step3();
					break;
				case 4:
					step4();
					break;
				default:
					return;
			}
		}
	}

	/*
	 * 계약서 생성하기 1. 점주가 계약서 초안을 작성해서 이미 알고 있는 근로자의 ip로 계약서를 전송 2. 근로자는 받은 계약서의 내용을
	 * 채우고 서명을 해서 점주한테 보냄 3. 점주는 근로자의 서명을 검증한 후 자신의 서명을 넣어 근로자에게 보냄 4. 다른 피어들들에게
	 * chainRequest함 5. 근로자는 검증 후 작업증명해서 블록체인에 올림 6. 모든 피어들에게 최근 체인 보내기 7. 블록체인에 올릴
	 * 때 파일도 올림
	 */
	public void letChainUpdate() {

		try {
			JSONObject data = new JSONObject();
			for (int i = 0; i < ipList.size(); i++) {
				cList.addElement(new Client(ipList.get(i), "chainRequest", data));
			}
			for (Client i : cList)
				i.join();
			for (int i = 0; i < ipList.size(); i++) {
				cList.addElement(new Client(ipList.get(i), "chainUpdate", data));
			}

			for (Client i : cList)
				i.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void step1() { // 점 : 내 정보 작성하기
		try {
			letChainUpdate();
			data.clear();
			data.put("type", "step1");
//         data.put("owner", frame.oNameField1.getText());
//         data.put("wage", frame.wageField1.getText());
			data.put("owner", "ddun1");
			data.put("wage", "80000");
			Client c = new Client("127.0.0.1", "step1", data);
			c.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		// 점: 근로자 정보 작성 대기 , 근 : 내 정보 작성 대기
	}

	public void step2() { // 점: 근로자 정보 작성 대기 , 근 : 내 정보 작성 대기
		try {
			data.clear();
			data.put("type", "step2");
			data.put("owner", "ddun1");
			data.put("wage", "80000");
			// 받았던 점주정보가 담긴 계약서 불러와서 data에 넣음
			data.put("worker", "ddeungddeung"); // frame에서 불러오는걸로 변경
			Client c = new Client("127.0.0.1", "step2", data); // 점주 아이피, 단계, 데이타
			c.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 점: 근로자 서명 검증 후 내 서명 대기 , 근 : 점주가 내 서명 검증해주는거 대기
	}

	public void step3() {// 점: 근로자 서명 검증 후 내 서명 대기 , 근 : 점주가 내 서명 검증해주는거 대기
		try {
			data.clear();
			data.put("type", "step3");
			data.put("owner", "ddun1");
			data.put("wage", "80000");
			data.put("worker", "ddeungddeung");
			// 점주 정보 + 노동자 정보 불러와서 data에 넣기
			Client c = new Client("127.0.0.1", "step3", data); // 점주 아이피, 단계, 데이타
			c.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 점: 근로자가 내 서명 검증후 성공하면 작업증명 -> 성공하면 다른 피어들로 부터 검증받고 브로드 캐스트 , 근 : 점주 서명 검증
		// 후작업증명하고 브로드캐스트해서 검증받고 아예 계약 완료하기 대기
	}

	public void step4() { // 점: 근로자가 내 서명 검증후 성공하면 작업증명 -> 성공하면 다른 피어들로 부터 검증받고 브로드 캐스트 , 근 : 점주 서명 검증
		// 후작업증명하고 브로드캐스트해서 검증받고 아예 계약 완료하기 대기
		try {
			data.clear();
			data.put("type", "step4");
			data.put("owner", "ddun1");
			data.put("wage", "80000");
			data.put("worker", "ddeungddeung");
			// 점주 정보 + 노동자 정보 불러와서 data에 넣기
			Client c = new Client("127.0.0.1", "step4", data); // 점주 아이피, 단계, 데이타
			c.join();
			letMakeContract2();// 브로드캐스트해서 검증받고 아예 계약 완료하기 대기
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 점: 계약 체결완료, 근 : 계약 체결 완료 **:중간에 fail되면 그냥 없었던 계약 됨
	}

	public void makeDatafromContractFile() throws IOException {
		data.clear();
		FileManage fm = new FileManage(PATH + "\\contract\\ddun1.txt"); // 클릭한 파일 이름을 가져와서 ddun1(점주) 자리 대체
		// 계약서 파일 이름을 특정한 기준으로 정해놔야할듯!? 1, 2, 3, ... 하고 목록을 한쪽에 띄워준다던가 , ,,
		data.put("owner", fm.getLineString(0));
		data.put("wage", fm.getLineString(1));
		data.put("worker", fm.getLineString(3));
	}

	public void letMakeContract2() throws IOException {
		makeDatafromContractFile();

		try {
			for (int i = 0; i < ipList.size(); i++) {
				cList.addElement(new Client(ipList.get(i), "blockUpdate", data));
			}
			for (Client i : cList)
				i.join();
			System.out.println(countPOW);

			synchronized (chainStr) {
				chainStr.add(block.get("proofHash").toString());
			}

			if (TotalManager.countPOW > 0) {
				for (int i = 0; i < ipList.size(); i++) {
					cList.addElement(new Client(ipList.get(i), "blockSave", data));
				}

				for (Client i : cList)
					i.join();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		frame.homeBtn.doClick();
		cList.clear();
	}

	public static void main(String[] args) throws FileNotFoundException, NoSuchAlgorithmException,
			NoSuchProviderException, InvalidAlgorithmParameterException, InvalidKeySpecException, IOException {
		MyFrame frame = new MyFrame();
		TotalManager t = new TotalManager(frame);
		t.init();
	}
}

class MyActionListener2 implements ActionListener, WindowListener {

	private MyFrame frame;
	private TotalManager tm;

	public MyActionListener2(MyFrame frame, TotalManager tm) {
		this.frame = frame;
		this.tm = tm;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == frame.ownerBtn) {

		} else if (e.getSource() == frame.workerBtn) {

		} else if (e.getSource() == frame.makeContractBtn) {
			frame.changePanel(frame.mcPanel);
			frame.oIpField1.setText(tm.myIp);
			frame.oIpField2.setText(tm.myIp);

			tm.letChainUpdate();
		} else if (e.getSource() == frame.askedContractBtn) {

		} else if (e.getSource() == frame.sigBtn1) {
//         try {
//            tm.worker.letSendSigContract();
//         } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | IOException e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//         }
//         tm.letMakeContract2();
		} else if (e.getSource() == frame.sendBtn1) {
			tm.step1();

		} else if (e.getSource() == frame.sigBtn2) {

		} else if (e.getSource() == frame.homeBtn) {

		}
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		if (tm.server.isAlive())
			tm.server.stop();
		tm.server.close();
		System.out.println("프로그램을 끝냅니다");
		System.exit(1);
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

}