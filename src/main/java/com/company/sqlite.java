package main.java.com.company;

import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class sqlite {
    public static String[] read_file() throws IOException {
        String[] results = new String[2];
        String str = null;
        InputStreamReader isr = null;
        String filename = "./data/DBpedia/instance_types_en.ttl";
        // 创建一个空模型
        Model model = ModelFactory.createDefaultModel();
        // 打开文件
        InputStream in = FileManager.get().open(filename);
        BufferedReader br = null; //用于包装InputStreamReader,提高处理性能。因为BufferedReader有缓冲的，而InputStreamReader没有。
        if (in == null) {
            throw new IllegalArgumentException("File:" + filename + "not found");
        }
        isr = new InputStreamReader(in);  // InputStreamReader 是字节流通向字符流的桥梁,
        br = new BufferedReader(isr);
        while ((str = br.readLine()) != null) {
            InputStream is = new ByteArrayInputStream(str.getBytes());
            model.read(is, "", "TTL");
            StmtIterator iter = model.listStatements();
            while (iter.hasNext()) {
                org.apache.jena.rdf.model.Statement stmt = iter.nextStatement();
                String subject = stmt.getSubject().toString();
                RDFNode object = stmt.getObject();
                results[0] = subject;
                System.out.print("主语 " + results[0] + "\t");
                if (object instanceof Resource) {
                    results[1] = object.toString();
                    System.out.print(" 宾语 " + results[1]);
                } else {// object is a literal
                    System.out.print("宾语 33" + object.toString() + "\"");
                }
            }
            break;
        }
        return results;
    }
    //创建数据库
    public static void creat_database(){
        Connection c = null;
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:test.db");  //创建test.db数据库
        }catch (Exception e){
            System.err.println(e.getClass().getName() + ":" + e.getMessage());
            System.exit(0);
        }
        System.out.println("opened database successfully");
    }
    //建表
    public static void creat_table(){
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:test.db");
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            String sql = "CREATE TABLE DBpedia " +
                    "(ID INT PRIMARY KEY NOT NULL," +
                    " subject char(150) not null, " +
                    " type char(150) );";
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Table created successfully");
    }
    //insert
    public static void insert(String subject, String type){
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:test.db");
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");
            stmt = c.createStatement();
            String sql = "INSERT INTO DBpedia (ID,subject,type,type_value) " +
                    "VALUES (1, 'Paul', '" + type  + "', 'California');";
            stmt.executeUpdate(sql);

            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Records created successfully");
    }

    public static void main(String[] args) {
        creat_table();
        //insert();
    }

}
