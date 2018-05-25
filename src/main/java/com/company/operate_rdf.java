package main.java.com.company;

import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VCARD;
import org.apache.jena.vocabulary.VCARD4;

import java.io.InputStream;
import java.io.InputStreamReader;

public class operate_rdf {
    /**建立RDF图并打印*/
    public static void rdf_write(){
        //一些定义
        String personURI = "http://somewhere/JohnSmith";
        String givenName = "John";
        String familyName = "Smith";
        String fullName = givenName + " " + familyName;
        String email_value = "John@somewhere.com";
        String type = "internet";

        //创建model
        Model model = ModelFactory.createDefaultModel();//addProperty(RDF.type, model.createResource().
        //创建resource
//        Resource johnsmith = model.createResource(personURI).addProperty(VCARD.FN, fullName).addProperty(VCARD.N,
//               model.createResource().addProperty(VCARD.Given, givenName).addProperty(VCARD.Family, familyName));
        Resource johnsmith = model.createResource(personURI).addProperty(VCARD.FN, fullName).addProperty(VCARD.EMAIL,
                model.createResource().addProperty(RDF.value, email_value).addProperty(RDF.type, model.createResource("vcard:internet")));
         //list the statements in the graph
        StmtIterator iter = model.listStatements();
        //print out the predicate,subject,object of each statement
        while (iter.hasNext()){
            Statement stmt = iter.nextStatement();
            Resource subject = stmt.getSubject();
            Property predicate = stmt.getPredicate();
            RDFNode object = stmt.getObject();

            System.out.print(subject.toString() + " " + predicate.toString() + " ");
            if (object instanceof Resource){
                System.out.print(object.toString());
            }
            else {
                //object is a literal
                System.out.print("\"" + object.toString() + "\"");
            }
            System.out.println(".");

        }
         model.write(System.out);
        /**Jena有一个扩展接口允许将RDF写出到不同的格式中。
         * 上面使用的是一个标准的输出，Jena可是通过在write()里面添加参数来做其他RDF格式的输出。*/
        model.write(System.out, "RDF/XML-ABBREV");
        /**结果非常简约，甚至可以表现空白节点，但是由于他不适合写大型的模型，因为效率不能够被接受。
         * 所以写大型的文件并且保存空白节点，最好用N-Triples格式*/
        //model.write(System.out, "N-TRIPLES");

    }
    /**读取RDF*/
    public static void rdf_read(){
        String inputFileName = "data/vc-db-1.rdf";
        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(inputFileName);
        if (in == null){
            throw new IllegalArgumentException("File:" + inputFileName + "not found");
        }
        // 读取RDF XML内容到模型中
        model.read(in, null);
        // 标准打印出来
        model.write(System.out);
    }
    /**浏览模型*/
    public static void get_model(){
        String inputFileName = "data/vc-db-1.rdf";
        String johnSmithURI = "http://somewhere/JohnSmith/";
        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(inputFileName);
        if (in == null){
            throw new IllegalArgumentException("File:" + inputFileName + "not found!");
        }

        model.read(new InputStreamReader(in), "");
        Resource vcard = model.getResource(johnSmithURI);
        // 返回vcard:N 属性的值，强制转换返回类型为 Resource
        Resource name = (Resource) vcard.getProperty(VCARD.N).getObject();
        // 返回 name 的值为一个文本
        String fullName = vcard.getRequiredProperty(VCARD.FN).getString();
        // add two nick name properties to vcard
        // 获取johnSmithURI资源后，对其添加两个nickname属性
        vcard.addProperty(VCARD.NICKNAME, "Smith")
                .addProperty(VCARD.NICKNAME, "Adman");
        //设置输出结果
        System.out.println("The nicknames of \"" + fullName + "\" are:");
        //列出所有的昵称
        StmtIterator iter = vcard.listProperties(VCARD.NICKNAME);
        while (iter.hasNext()){
            System.out.println(" " + iter.nextStatement().getObject().toString());
        }
    }
    /**查询模型*/
    public static void inquire_model(){
        String inputFileName = "data/vc-db-1.rdf";
        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(inputFileName);
        if (in == null){
            throw new IllegalArgumentException("File:" + inputFileName + "not found!");
        }
        model.read(in, "");
        // select all the resources with a VCARD.FN property
        ResIterator iter = model.listResourcesWithProperty(VCARD.FN);
        if (iter.hasNext()){
            System.out.println("The database contains vcards for:");
            while (iter.hasNext()){
                System.out.println(" " + iter.nextResource().getRequiredProperty(VCARD.FN).getString());
            }
        }
        else {
            System.out.println("No vcards were found in the database.");
        }
        /**这个示例中的代码使用Java内嵌机制重构了该类。
         *  这里的方法 selects() 检查并确保了全名必须结束于“Smith”
         *  需要指出这里的匹配是基于陈述进行过滤的。*/
        StmtIterator iters = model.listStatements(new SimpleSelector(null, VCARD.FN, (RDFNode) null){
            public boolean selects(Statement s){
                return s.getString().endsWith("Smith");
            }
        });
        if (iters.hasNext()){
            System.out.println("The database contains vcards for:");
            while (iters.hasNext()){
                System.out.println(" " + iters.nextStatement().getObject().toString());
            }
        }
        else {
            System.out.println("No vcards were found in the database.");
        }


    }
    /**操作模型*/
    public static void operate_model(){
        String inputFileName1 = "data/vc-db-2.rdf";
        String inputFileName2 = "data/vc-db-3.rdf";
        Model model1 = ModelFactory.createDefaultModel();
        Model model2 = ModelFactory.createDefaultModel();
        InputStream in1 = FileManager.get().open(inputFileName1);
        if (in1 == null){
            throw new IllegalArgumentException("File:" + inputFileName1 + "not found!");
        }
        InputStream in2 = FileManager.get().open(inputFileName2);
        if (in2 == null){
            throw new IllegalArgumentException("File:" + inputFileName2 + "not found!");
        }
        model1.read(in1, "");
        model2.read(in2, "");
        /**并集：union(Model)
         * 交集和补集的操作也相似的，使用方法.intersection(Model)和.difference(Model)*/
        Model model = model1.union(model2);
        model.write(System.out, "RDF/XML-ABBREV");
        System.out.println();
    }
    /**容器*/
    public static void bag_model(){
        String inputFileName = "data/vc-db-1.rdf";
        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open( inputFileName );
        if (in == null) {
            throw new IllegalArgumentException( "File: " + inputFileName + " not found");
        }
        model.read(new InputStreamReader(in), "");
        Bag smiths = model.createBag();
        StmtIterator iter = model.listStatements(
                new
                        SimpleSelector(null, VCARD.FN, (RDFNode) null){
                             public boolean selects(Statement s){
                                 return s.getString().endsWith("Smith");
                             }
                        }
        );
        while (iter.hasNext()){
            smiths.add(iter.nextStatement().getSubject());
        }
        model.write(System.out);
        System.out.println();
        // print out the members of the bag
        NodeIterator iter2 = smiths.iterator();
        if (iter2.hasNext()) {
            System.out.println("The bag contains:");
            while (iter2.hasNext()) {
                System.out.println("  " +
                        ((Resource) iter2.next())
                                .getRequiredProperty(VCARD.FN)
                                .getString());
            }
        } else {
            System.out.println("The bag is empty");
        }
    }
    public static void main(String[] args) {
        rdf_write();
        rdf_read();
        get_model();
        inquire_model();
        operate_model();
        bag_model();

    }
}
