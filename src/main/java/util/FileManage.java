package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;

public class FileManage {
   String src = "";
   ArrayList<String> retStr = new ArrayList<String>();

   public FileManage(String src) {
      this.src = src;
   }
   public void makeContractFolder() {
      File folder = new File(src + "\\contract");
      if (!folder.exists()) {
         folder.mkdir();
      }
   }

   public ArrayList<String> fileLineRead(String name) throws IOException {
      retStr.clear();
      BufferedReader in = null;
      try {
         in = new BufferedReader(new FileReader(name));
         String s;

         while ((s = in.readLine()) != null) {
            retStr.add(s);
         }
         in.close();
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

   public void makeFile(String name) throws IOException {
      File f = new File(src + "\\" + name + ".txt");
      if (!f.exists()) {
         f.createNewFile();
      }
      this.src = f.getAbsolutePath();
   }

   public void addTofile(String text) {
      try {
         BufferedWriter out = new BufferedWriter(new FileWriter(src, true));
         out.write(text);
         out.newLine();
         out.flush();
         out.close();
      } catch (IOException e) {
      }
   }

   public void setSrc(String src) {
      this.src = src;
   }

   public String getSrc() {
      return this.src;
   }

   public String getLineString(int i) throws IOException {
      fileLineRead(src);
      return retStr.get(i);
   }

   public void reWriteFile(ArrayList<String> chainStr) {
      try {
         BufferedWriter out = new BufferedWriter(new FileWriter(src, false));
         for (int i = 0; i < chainStr.size(); i++) {
            out.write(chainStr.get(i));
            out.newLine();
            out.flush();
         }
         out.close();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

   }

}