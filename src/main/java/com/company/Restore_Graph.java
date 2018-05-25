package main.java.com.company;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import org.apache.jena.rdf.model.Resource;

import java.util.HashMap;
import java.util.Vector;

public class Restore_Graph {
    /**对等压缩的还原*/
    public static Graph<String, String> restore_graph_equ(Graph<String, String>g, HashMap<Integer, Vector<Resource>>vertex, HashMap<Integer, Vector<String>>map){
        //System.out.println(vertex);
        //System.out.println(map);
        String subject, object = null;
        /**遍历所有的map,key：每一个map的键值*/
        for(Integer key : vertex.keySet()){
            Vector<Resource> v = vertex.get(key);
            for(int i = 0;i < v.size();i++) {
                String str = v.get(i).toString();
                g.addVertex(str);
                Vector<String> v1 = map.get(key);
                for (int j = 1; j < v1.size(); ) {
                    subject = v1.get(j);
                    object = v1.get(j + 2);
                    if (subject.equals("***")) {
                        subject = str;
                    }
                    if (object.equals("***")) {
                        object = str;
                    }
                    g.addEdge(subject + "-" + object, subject, object, EdgeType.DIRECTED);
                    j += 3;
                }
            }
        }
        return g;
    }
    /**依赖压缩的还原*/
    public static Graph<String, String> restore_graph_dep(Graph<String, String>g, HashMap<Integer, Vector<Resource>>vertex, HashMap<Integer, Vector<String>>map){
        String subject, object = null;
        for(Integer key : vertex.keySet()){
            /**添加所有的节点*/
            Vector<Resource> v = vertex.get(key);
            for(int i = 0;i < v.size()-1;i++) {
                String str = v.get(i).toString();
                g.addVertex(str);
            }
            /**添加所有的边*/
            Vector<String> v1 = map.get(key);
            for (int j = 0; j < v1.size()-2; j += 2) {
                subject = v1.get(j);
                object = v1.get(j + 2);
                g.addEdge(subject + "-" + object, subject, object, EdgeType.DIRECTED);
            }
        }
        return g;
    }

}
