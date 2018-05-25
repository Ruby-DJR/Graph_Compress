package main.java.com.company;

import edu.uci.ics.jung.graph.Graph;
import org.apache.jena.rdf.model.*;

import java.util.*;

public class CompressAlgorithm {

    /**Equ_vertex：对等压缩中保存被压缩掉的节点（超节点）*/
    public static HashMap<Integer, ArrayList<Resource>> Equ_vertex = new HashMap<Integer, ArrayList<Resource>>();
    /**Equ_map:存储内部结构（被压缩掉的图结构）
     * Vector中下标的含义
     * 0:类型  1（4/7/10/13/16）、 2（5/8/11/14/17）、 3（6/9/12/15/18）：父结点、关系、子节点  */
    public static HashMap<Integer, ArrayList<String>> Equ_map = new HashMap<Integer, ArrayList<String>>();


    /**Dep_vertex：依赖压缩中保存被压缩掉的节点（超节点）*/
    public static HashMap<Integer, Vector<Resource>> Dep_vertex = new HashMap<Integer, Vector<Resource>>();
    /**Dep_map:存储内部结构（被压缩掉的图结构）
     * Vector中下标的含义
     *  结点、关系、节点、关系、 节点、....、节点、关系、节点、类型（最后一个节点） */
    public static HashMap<Integer, Vector<String>> Dep_map = new HashMap<Integer, Vector<String>>();


    /**等价压缩原理：类型一样、完全相同的邻居**/
    public static Graph<String, String> EquCompress(Set<Resource> resSet, Graph<String, String>g, Model model){
        /**familySet：聚集图（压缩后的图）*/
        Set<String> familySet = new HashSet<String>();
        /**Vector family:存储节点的类型及其邻居*/
        ArrayList<String> family = null;
        ArrayList<Resource> vertex = null;
        int map_num = 0;
        //Iterator迭代器是一个对象，它工作时遍历并选择序列中的对象
        Iterator<Resource> resIt = resSet.iterator();
        while (resIt.hasNext()){
            family = new ArrayList<String>();
            vertex = new ArrayList<Resource>();
            Resource resource = resIt.next();
            /** 类型 **/
            if (get_type.getType(model, resource) == null){
                family.add("null");
            }
            else {
                family.add(get_type.getType(model, resource));
            }
            /** 邻居 （subject、object）**/
            /** 当resource为subject时,找相应的predicate和object **/
            StmtIterator It_1 = model.listStatements(resource, null, (RDFNode) null);
            while(It_1.hasNext()){
                Statement stmt = It_1.next();
                Property predicate = stmt.getPredicate();
                RDFNode object = stmt.getObject();
                if (predicate.toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")){
                    continue;
                }
                if (!(object instanceof Resource)){
                    continue;
                }
                family.add("***");
                family.add(predicate.toString());
                family.add(object.toString());
            }

            /**当resource为object时,找相应的subject和predicate **/
            StmtIterator It_2 = model.listStatements(null, null, resource);
            while(It_2.hasNext()){
                Statement stmt = It_2.next();
                Resource subject = stmt.getSubject();
                Property predicate = stmt.getPredicate();
                RDFNode object = stmt.getObject();
                if (predicate.toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")){
                    continue;
                }
                if (!(object instanceof Resource)){
                    continue;
                }
                family.add(subject.toString());
                family.add(predicate.toString());
                family.add("***");
            }
            /** 判断：是否满足等价压缩的条件 **/
            if (familySet.contains(family.toString())){
                //判断之前是否已经保存此超节点
                if (Equ_map.containsValue(family)){
                    //取key值
                    Integer key = null;
                    //Map,HashMap并没有实现Iteratable接口.不能用于增强for循环.
                    for(Integer getKey: Equ_map.keySet()){
                        if(Equ_map.get(getKey).equals(family)){
                            key = getKey;
                        }
                    }
                    Equ_vertex.get(key).add(resource);
                }
                else {
                    vertex.add(resource);
                    Equ_vertex.put(map_num, vertex); //保存超实体
                    Equ_map.put(map_num, family); //保存内部结构图
                    map_num += 1;
                }
                g.removeVertex(resource.toString());
            }
            else{
                familySet.add(family.toString());
            }
        }
        return g;
    }

    /**依赖压缩：1）长辈关系为空；2）晚辈关系有且只有一个；
     * 正向依赖压缩策略：压缩过程中只能将长辈压缩至晚辈中，即永远只能将x->y的边压缩入y中,y变为超实体；
     * */
    public static Graph<String, String> DepCompress(Set<Resource> resSet, Graph<String, String>g, Model model){
        Iterator<Resource> resIt  = null;
        int map_num = 0;
        while (true){
            /**flag:判断整个图是否全部压缩完毕*/
            int flag = 0;
            resIt  = resSet.iterator();
            while (resIt.hasNext()){
                /**vertex保存各个D超实体中的所有节点*/
                Vector<Resource> vertex = null;
                vertex = new Vector<Resource>();
                /**family保存超实体的内部结构*/
                Vector<String> family = null;
                family = new Vector<String>();

                Resource resource = resIt.next();
                /**先判断图中有没有该节点*/
                if (!g.containsVertex(resource.toString())){
                    continue;
                }
                if (get_type.getType(model, resource) == null){
                    continue;
                }
                /**长辈关系为空*/
                StmtIterator It_2 = model.listStatements(null, null, resource);
                //subject_count统计长辈的个数，若有多个长辈，则跳过
                int subject_count = 0;
                while (It_2.hasNext()){
                    Statement stmt = It_2.next();
                    Resource subject = stmt.getSubject();
                    if ((subject instanceof  Resource)&& (get_type.getType(model, subject) != null)){
                        subject_count += 1;
                    }
                }
                if (subject_count > 0){
                    continue;
                }

                /**晚辈关系有且只有一个*/
                int object_count = 0;
                //child:晚辈节点
                Resource child = null;
                Property predicate = null;

                StmtIterator It_3 = model.listStatements(resource, null, (RDFNode) null);
                while(It_3.hasNext()){
                    Statement stmt = It_3.next();
                    RDFNode object = stmt.getObject();
                    if (stmt.getPredicate().toString().contains("owl") || stmt.getPredicate().toString().contains("rdf") || stmt.getPredicate().toString().contains("rdfs")){
                        continue;
                    }
                    if (!(object instanceof Resource) || get_type.getType(model, (Resource) object) == null){
                        continue;
                    }
                    object_count += 1;
                    child = (Resource) object;
                    predicate = stmt.getPredicate();
                }
                /**当此节点满足依赖压缩时：实现多个依赖实体合并为一个超实体，而不是两两合并，以减少超实体的个数
                 * 满足上述的条件：1）此节点的孩子节点有且只有一个；2）此节点的孩子节点的父结点只有一个（自己）*/
                if (object_count == 1){
                    vertex.add(resource);
                    family.add(resource.toString());
                    g.removeVertex(resource.toString());
                    while (true){
                        /**count_child：节点的孩子节点个数*/
                        int count_child = 0;
                        Resource child_child = null;
                        StmtIterator It_4 = model.listStatements(child, null, (RDFNode) null);
                        while(It_4.hasNext()){
                            Statement stmt = It_4.next();
                            RDFNode object = stmt.getObject();
                            if (stmt.getPredicate().toString().contains("owl") || stmt.getPredicate().toString().contains("rdf") || stmt.getPredicate().toString().contains("rdfs")){
                                continue;
                            }
                            if (!(object instanceof Resource) || get_type.getType(model, (Resource) object) == null){
                                continue;
                            }
                            child_child = (Resource)object;
                            count_child += 1;
                        }

                        /**count_father：节点的父节点个数*/
                        int count_father = 0;
                        StmtIterator It_5 = model.listStatements(null, null, child);
                        while (It_5.hasNext()){
                            Statement stmt = It_5.next();
                            Resource subject = stmt.getSubject();
                            if ((subject instanceof  Resource)&& (get_type.getType(model, subject) != null)){
                                count_father += 1;
                            }
                        }
                        /**单链表中可以依赖压缩的所有情况，归纳如下：*/
                        if (count_child == 0 && count_father == 1){
                            vertex.add(child);
                            family.add(predicate.toString());
                            family.add(child.toString());
                            family.add(get_type.getType(model, child));
                            g.removeVertex(child.toString());
                            break;
                        }
                        else if (count_child == 1 && count_father == 1){
                            vertex.add(child);
                            family.add(predicate.toString());
                            family.add(child.toString());
                            family.add(get_type.getType(model, child));
                            child = child_child;
                            g.removeVertex(child.toString());
                        }
                        else{
                            vertex.add(child);
                            family.add(predicate.toString());
                            family.add(child.toString());
                            family.add(get_type.getType(model, child));
                            break;
                        }
                    }
                    /**将压缩的超实体的所有节点的Resource写入Dep_vertex（MAP）中*/
                    Dep_vertex.put(map_num, vertex);
                    Dep_map.put(map_num, family);
                    map_num += 1;
                    flag = 1;
                }
                else {
                    continue;
                }
            }
            if (flag == 0){
                break;
            }
        }
        return g;
    }
}
