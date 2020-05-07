//package HomomorphicEncryption;
//import java.math.BigInteger;
//import java.sql.*;
//import java.util.Vector;
//
//public class Database {
//    private String user_name = "sangseung";
//    private String password = "konkuk17sw";
//    private String host = "seouldb.cwnpn1rxhuaq.ap-northeast-2.rds.amazonaws.com:3306"; // MySQL 서버 주소
//    private String database = ""; // MySQL DATABASE 이름 -> blank 일때가 오류가 안남 이상행 !
//    private String mainDB = "mydb";
//    PreparedStatement pstmt = null;
//    Statement stmt = null;
//    ResultSet rs = null;
//    private Connection conn = null;
//
//    public Database(){
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//        } catch (ClassNotFoundException e) {
//            System.out.println("jdbc 오류");
//            e.printStackTrace();
//        }
//        connect();
//    }
//
//    void insertZindex(int id, String zString){
//        try{
//            String sql = "INSERT INTO "+mainDB+".Zindex(keywordId,zString) VALUES (?,?)";
//            pstmt = conn.prepareStatement(sql);
//            pstmt.setInt(1, id);
//            pstmt.setString(2, zString);
//            isSuccess(pstmt.executeUpdate());
//        }catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//    void insertContract(Data data){
//        try{
//            String sql = "INSERT INTO "+mainDB+".Contract(c2,c3) VALUES (?,?)";
//            pstmt = conn.prepareStatement(sql);
//            pstmt.setString(1, data.c2.toString(16));
//            pstmt.setString(2, data.c3.toString(16));
//            isSuccess(pstmt.executeUpdate());
//        }catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    int insertKeywordPEKS(Data data){
//        try{
//            int idx = getTupleNum("KeywordPEKS") + 1;
//            String sql = "INSERT INTO "+mainDB+".KeywordPEKS(keywordId,c1,c2) VALUES (?,?,?)";
//            pstmt = conn.prepareStatement(sql);
//            pstmt.setInt(1, idx);
//            pstmt.setString(2, data.c1.toString(16));
//            pstmt.setString(3, data.c2.toString(16));
//            isSuccess(pstmt.executeUpdate());
//            return idx;
//        }catch (SQLException e) {
//            e.printStackTrace();
//
//        }
//        return -1; //-1 means fail
//    }
//
//    void insertUser(User user, int id){
//        try{
//            String sql = "INSERT INTO "+mainDB+".userTable(userId) VALUES (?)";
//            pstmt = conn.prepareStatement(sql);
//            pstmt.setInt(1, id);
//            isSuccess(pstmt.executeUpdate());
//        }catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    Vector<KeywordPEKS> selectKeywordPEKS(){
//        Vector<KeywordPEKS> arr = new Vector<KeywordPEKS>();
//        try{
//            String sql = "SELECT * from "+mainDB+".KeywordPEKS";
//            rs = stmt.executeQuery(sql);
//            while (rs.next()) {
//                int idx = rs.getInt(1);
//                String c1 = rs.getString(2);
//                String c2 = rs.getString(3);
//                arr.add(new KeywordPEKS(idx, new BigInteger(c1,16), new BigInteger(c2,16)));
//            }
//        }catch (SQLException e){
//            e.printStackTrace();
//        }
//        return arr;
//    }
//
//    String selectZindex(int id){
//        System.out.println("selectZindex id: "+id);
//        String result = "";
//        try{
//            String sql = "SELECT zString from "+mainDB+".Zindex where keywordId = "+id;
//            rs = stmt.executeQuery(sql);
//            while (rs.next()){
//                result = rs.getString(1);
//            }
//        }catch (SQLException e){
//            e.printStackTrace();
//        }
//        return result;
//    }
//
////    Vector<Contract> selectContract(String zString){
////        Vector<Contract> arr = new Vector<Contract>();
////        try{
////            String sql = "SELECT c2,c3 from "+mainDB+".Contract where contractId = ?";
////
////            pstmt = conn.prepareStatement(sql);
////            for(int i = 0; i<zString.length(); i++){
////                if (zString.charAt(i) == '1') {
////                    pstmt.setInt(1, i+1);
////                    rs = pstmt.executeQuery();
////                    while (rs.next()) {
////                        String c2 = rs.getString(1);
////                        String c3 = rs.getString(2);
////                        arr.add(new Contract(i + 1, new BigInteger(c2, 16), new BigInteger(c3, 16)));
////                    }
////                }
////            }
////        }catch (SQLException e){
////            e.printStackTrace();
////        }
////        return arr;
////    }
//
//    void updateZindex(int id, String zString){
//        try{
//            String sql = "UPDATE "+mainDB+".Zindex set zString=? where keywordId="+id;
//            pstmt = conn.prepareStatement(sql);
//            pstmt.setString(1, zString);
//            isSuccess(pstmt.executeUpdate());
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    void connect(){
//        System.out.println("db connect");
//        try {
//            conn = DriverManager.getConnection("jdbc:mysql://" + host + "/" + database + "?useSSL=false", user_name, password);
//            stmt = conn.createStatement();
//            System.out.println("정상적으로 연결되었습니다.");
//        } catch(SQLException e) {
//            System.err.println("conn 오류:" + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    void disconnect(){
//        try {
//            if(conn != null)
//                conn.close();
//            if (pstmt != null)
//                pstmt.close();
//            if (stmt != null)
//                stmt.close();
//            if(rs != null)
//                rs.close();
//        } catch (SQLException e) {
//
//        }
//    }
//
//    void isSuccess(int result){
//        if (result == 0) {
//            System.out.println("fail to insert: ");
//        } else {
//            System.out.println("success to insert: ");
//        }
//    }
//
//
//    void updateZString(Vector<Integer> keywordNum){
//        try{
//            String sql = "SELECT * from "+mainDB+".Zindex";
//            rs = stmt.executeQuery(sql);
//            while (rs.next()) {
//                int id = rs.getInt(1);
//                String zString = rs.getString(2);
//                if(keywordNum.contains(id)) zString +="1";
//                else zString +="0";
//                updateZindex(id, zString);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    int getTupleNum(String table){
//        try{
//            String sql = "SELECT COUNT(*) FROM mydb."+table;
//            rs = stmt.executeQuery(sql);
//            rs.next();
//            System.out.println(rs.getInt(1));
//            return rs.getInt(1);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return 0;
//    }
//
//}
