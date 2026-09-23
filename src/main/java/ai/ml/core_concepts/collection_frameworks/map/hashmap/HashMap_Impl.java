package ai.ml.core_concepts.collection_frameworks.map.hashmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HashMap_Impl {

    public static void main(String[] args) {

        HashMap<Integer, String> map=new HashMap<>();
        map.put(31, "Shubham");
        map.put(12, "Akshit");
        map.put(2, "Neha");
        System.out.println(map);

        String student = map.get(31);
        System.out.println(student);
        String s = map.get(69);
        System.out.println(s);

        System.out.println(map.containsKey(2));
        System.out.println(map.containsValue("Shubham"));

        for(int i:map.keySet()){
            System.out.println(map.get(i));
        }

        /** print value in Uppercase */
        Set<Map.Entry<Integer, String>> entries = map.entrySet();
        for(Map.Entry<Integer, String> entry : entries){
            System.out.println(entry.getKey()+" : "+entry.getValue().toUpperCase());
        }

        map.remove(31);
        System.out.println(map);
        boolean isRemove = map.remove(31, "Ajay");
        System.out.println(isRemove);




    }
}
