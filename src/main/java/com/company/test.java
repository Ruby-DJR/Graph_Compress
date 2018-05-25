package main.java.com.company;

import java.util.ArrayList;
import java.util.HashMap;

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
