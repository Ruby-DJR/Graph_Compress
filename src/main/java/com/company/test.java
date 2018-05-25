package main.java.com.company;

import java.util.ArrayList;
import java.util.HashMap;
 /**此文件是用来测试部分代码语法是否正确*/
public class test {
    public static void main(String[] args) {
        HashMap<Integer, ArrayList<String>> vertex = new HashMap<Integer, ArrayList<String>>();
        ArrayList<String> a = new ArrayList<String>();
        a.add("1");
        a.add("2");
        vertex.put(1, a);
        System.out.println(vertex);
    }
}
