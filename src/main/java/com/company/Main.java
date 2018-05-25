package main.java.com.company;

import edu.uci.ics.jung.graph.util.EdgeType;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import edu.uci.ics.jung.graph.*;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static String filename = "./data/peel.rdf";
    //public static String filename = "./data/jamendo.rdf";


    public static void main(String[] args) {

        /**定义图的相关数据*/
        int triple_num = 0;  //所有三元组的个数
        double link_num = 0.0; //去除掉不符合TOG定义后的边，剩余的边的个数(double 为了计算avg_degree)
        int relation_num; //边的种类数量（关系种类数量）
        int vertex_num; //图中主语和宾语的个数（节点数）
        double radius = 0; //图的半径
        String type_sub = null;
        String type_obj = null;

        // 创建一个空模型
        Model model = ModelFactory.createDefaultModel();
        // 打开文件
        InputStream in = FileManager.get().open(filename);
        if (in == null){
            throw new IllegalArgumentException("File:" + filename + "not found");
        }
        //读取RDF文件
        model.read(in, "");
        StmtIterator iter = model.listStatements();

        //记录资源信息
        Set<Resource> resSet = new HashSet<Resource>();
        Set<String> preSet = new HashSet<String>();
        /**g:将peel.rdf文件转化成g图
         * g1、g2：分别用于对等压缩和依赖压缩的结果图*/
        DirectedSparseGraph<String, String> g = new DirectedSparseGraph<String, String>();
        DirectedSparseGraph<String, String> g1 = new DirectedSparseGraph<String, String>();
        DirectedSparseGraph<String, String> g2 = new DirectedSparseGraph<String, String>();
        DirectedSparseGraph<String, String> g3 = new DirectedSparseGraph<String, String>();
        DirectedSparseGraph<String, String> g_equ_restore = new DirectedSparseGraph<String, String>();
        DirectedSparseGraph<String, String> g_dep_restore = new DirectedSparseGraph<String, String>();

        /**resSet中加入所有的subject和object的uri
           将peel.rdf文件转化成g图*/
        while(iter.hasNext()){
            //接口Statement提供了访问陈述中主体, 谓词和客体的方法
            Statement stmt = iter.nextStatement();
            triple_num += 1; //每一个statement代表一个triple
            Resource subject = stmt.getSubject(); //主语

            // instanceof 判断指定对象是否是特定类的一个实例
            if (subject != null){
                //获取主语的类型
                type_sub = get_type.getType(model, subject);
            }
            else {
                //主语不是Resource
                type_sub = null;
                continue;
            }
            if (type_sub != null){
                //关系中含有rdf, rdfs, owl的一般不是实例之间的关系
                if (stmt.getPredicate().toString().contains("rdf") || stmt.getPredicate().toString().contains("rdfs") || stmt.getPredicate().toString().contains("owl")){
                    type_sub = null;
                    continue;
                }
            }
            else{
                continue;
            }

            //宾语
            RDFNode object = stmt.getObject();
            if (object instanceof Resource){
                //获取object的类型
                type_obj = get_type.getType(model, (Resource) object);
            }
            else{
                type_obj = null;
                continue;
            }
            if (type_obj != null){
                preSet.add(stmt.getPredicate().toString());

                //containsVertex: Returns true if this graph's vertex collection contains vertex.
                if (!g.containsVertex(subject.toString())){
                    //顶点中不包含subject，向resSet中添加subject
                    g.addVertex(subject.toString());
                    resSet.add(subject);
                }
                if (!g.containsVertex(object.toString())){
                    //顶点中不包含object，向resSet中添加object,g中添加边
                    g.addVertex(object.toString());
                    resSet.add((Resource) object);
                    g.addEdge(subject.toString() + "-" + object.toString(), subject.toString(), object.toString(), EdgeType.DIRECTED);
                }
                else{
                    //顶点中已包含subject,object，向g中添加边
                    g.addEdge(subject.toString() + "-" + object.toString(), subject.toString(), object.toString(), EdgeType.DIRECTED);
                }
            }
        }

        /**实验结果显示：
         * 输出原始图的统计信息*/
        link_num = (double) g.getEdgeCount();
        relation_num  = preSet.size();
        vertex_num = g.getVertexCount();

        System.out.println("------------primal graph------------");
        System.out.println("triple_num:" + triple_num);
        System.out.println("vertex_num:" + vertex_num);
        System.out.println("link_num:" + link_num);
        System.out.println("relation_num:" + relation_num);
        System.out.println("avg_degree:" + (link_num * 2 / vertex_num));
        System.gc(); //用于调用垃圾收集器，在调用时，垃圾收集器将运行以回收未使用的内存空间

//        /**对等压缩*/
//        g1 = (DirectedSparseGraph<String, String>) CompressAlgorithm.EquCompress(resSet, g, model);
//        System.out.println("------------afterEquCompress------------");
//        System.out.println("vertex_num:" + g1.getVertexCount());
//        System.out.println("link_num:" + g1.getEdgeCount());
//        System.gc(); //用于调用垃圾收集器，在调用时，垃圾收集器将运行以回收未使用的内存空间
//        /**对等压缩的还原*/
//        g_equ_restore = (DirectedSparseGraph<String, String>) Restore_Graph.restore_graph_equ(g1, CompressAlgorithm.Equ_vertex, CompressAlgorithm.Equ_map);
//        System.out.println("------------EquCompress restore_graph------------");
//        System.out.println("vertex_num:" + g_equ_restore.getVertexCount());
//        System.out.println("link_num:" + g_equ_restore.getEdgeCount());
//        System.gc(); //用于调用垃圾收集器，在调用时，垃圾收集器将运行以回收未使用的内存空间
//        /**依赖压缩*/
//        g2 = (DirectedSparseGraph<String, String>) CompressAlgorithm.DepCompress(resSet, g, model);
//        System.out.println("------------afterDepCompress------------");
//        System.out.println("vertex_num:" + g2.getVertexCount());
//        System.out.println("link_num:" + g2.getEdgeCount());
//        System.gc();
//        /**依赖压缩的还原*/
//        g_dep_restore = (DirectedSparseGraph<String, String>) Restore_Graph.restore_graph_dep(g2, CompressAlgorithm.Dep_vertex, CompressAlgorithm.Dep_map);
//        System.out.println("------------DepCompress restore_graph------------");
//        System.out.println("vertex_num:" + g_dep_restore.getVertexCount());
//        System.out.println("link_num:" + g_dep_restore.getEdgeCount());
//        System.gc(); //用于调用垃圾收集器，在调用时，垃圾收集器将运行以回收未使用的内存空间
//        /**子图压缩*/

    }
}


