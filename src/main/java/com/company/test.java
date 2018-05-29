package main.java.com.company;
import org.apache.jena.base.Sys;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.FileManager;

import java.io.*;
import java.sql.*;


 /**将DBpedia数据写入test.db数据库中的DBpedia表中*/
public class test {

    public static void main(String[] args) throws IOException {
        Connection c = null;
        java.sql.Statement stmt = null;
        String str = null;
        InputStreamReader isr = null;
        String filename = "./data/DBpedia/instance_types_en.ttl";

        // 打开文件
        InputStream in = FileManager.get().open(filename);
        BufferedReader br = null; //用于包装InputStreamReader,提高处理性能。因为BufferedReader有缓冲的，而InputStreamReader没有。
        if (in == null) {
            throw new IllegalArgumentException("File:" + filename + "not found");
        }

        isr = new InputStreamReader(in);  // InputStreamReader 是字节流通向字符流的桥梁,
        br = new BufferedReader(isr);
        int count = 0;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:test.db");
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");
            stmt = c.createStatement();

            while ((str = br.readLine()) != null) {
                InputStream is = new ByteArrayInputStream(str.getBytes());
                // 创建一个空模型
                Model model = ModelFactory.createDefaultModel();
                model.read(is, "", "TTL");
                StmtIterator iter = model.listStatements();
                while (iter.hasNext()) {
                    Statement stmts = iter.nextStatement();
                    String subject = stmts.getSubject().toString();
                    RDFNode object = stmts.getObject();
                    if (object instanceof Resource) {
                        String sql = "INSERT INTO DBpedia (ID,subject,type) " +
                                "VALUES (" + count + ",'" + subject.replace("'", "''") +"','" + object  + "');";
                        stmt.executeUpdate(sql);
                    } else {// object is a literal
                        String sql = "INSERT INTO DBpedia (ID,subject,type) " +
                                "VALUES (" + count + ",'" + subject.replace("'", "''") +"','" + object.toString().replace("'", "''")  + "');";
                        stmt.executeUpdate(sql);
                    }
                    count += 1;
                }
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Records created successfully");

    }
}
