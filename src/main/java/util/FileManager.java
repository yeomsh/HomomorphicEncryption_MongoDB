package util;

import java.io.*;
import java.util.ArrayList;

public class FileManager {
   static String src = ".\\chain.txt";
   public static ArrayList<String> readChainFile() throws IOException {
      ArrayList<String> retStr = new ArrayList<>();
      BufferedReader in = null;
      try {
         in = new BufferedReader(new FileReader(src));
         String s;
         while ((s = in.readLine()) != null) {
            retStr.add(s);
         }
         in.close();
      }  catch (FileNotFoundException e){
         BufferedWriter out = new BufferedWriter(new FileWriter(src, true));
         out.flush();
         out.close();
      } catch (IOException e) {
         System.err.println(e);
      } finally {
         try {
            if (in != null) {
               in.close();
            }
         } catch (Exception ex) {
         }
      }
      return retStr;
   }

   public static void addTofile(String text) {
      try {
         System.out.println("addTofile: "+src);
         BufferedWriter out = new BufferedWriter(new FileWriter(src, true));
         out.write(text+"\r\n"); //\r\n: 개행
         out.flush();
         out.close();
      } catch (IOException e) {
      }
   }

   public static void reWriteFile(ArrayList<String> chainStr) {
      try {
         System.out.println("reWriteFile: "+src);
         BufferedWriter out = new BufferedWriter(new FileWriter(src, false));
         StringBuilder sb = new StringBuilder();
         for (String chain : chainStr){
            sb.append(chain+"\r\n");
         }
         out.write(sb.toString());
         out.flush();
         out.close();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

   }
}