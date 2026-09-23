package ai.ml.core_concepts.collection_frameworks.map.linkedhashmap;

import java.util.LinkedHashMap;
import java.util.Map;

public class LinkedHashMapDemo {
    public static void main(String[] args) {

        LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<>(11, 0.3f, false);
        linkedHashMap.put("Orange", 10);
        linkedHashMap.put("Apple", 20);
        linkedHashMap.put("Guava", 13);
        for(Map.Entry<String, Integer> entry:linkedHashMap.entrySet()){
            System.out.println(entry.getKey()+"  : "+entry.getValue());
        }


        /**
         * In LinkedHashMap constructor we have 3 parameters
         * 1. initial capacity -> what is the size of our Internal array.
         * 2. load factor -> when Internal array size need to be double.
         * 3. access order -> the ordering mode
         *                    true - for access order
         *                    false - for insertion order
         *
         */
        System.out.println("-------------------------------");
        LinkedHashMap<String, Integer> linkedHashMap1 = new LinkedHashMap<>(11);
        LinkedHashMap<String, Integer> linkedHashMap2 = new LinkedHashMap<>(11, 0.3f);
        LinkedHashMap<String, Integer> linkedHashMap3 = new LinkedHashMap<>(11, 0.3f, false);


        /** If we make access order is true
         *
         * if we do access linkedHashMap.get(element) then this accessed element will go at end of linkedHashMap
         * when this access order is false or by default also its false then element will store in insertion order in a linkedlist
         * accessorder -> true means which element will get access will go to end of linkedHashMap
         *
         *
         * */

        /**
         *
         * There is a Mechanism Least Recently Used (LRU)
         * In this LRU Algorithm we keep a track of that elements which is Lease Recently accessed.
         * which means a old data or the data which is not being used.
         * So, The elements is not used frequently we keep on top, so that easily we can remove.
         *
         *
         * Use - Case
         *
         * There are some entries or data need to Remove Least Recently Used data
         * actually so many data getting accessing so it's difficult to find out the data which is Least Recently Used.
         * so, In that Case we can make access order true and we can use Linked Hash Map.
         *
         *
         */
        LinkedHashMap<String, Integer> linkedHashMap4 = new LinkedHashMap<>(11, 0.3f, true);
        linkedHashMap4.put("Orange", 10);
        linkedHashMap4.put("Apple", 20);
        linkedHashMap4.put("Guava", 13);

        linkedHashMap4.get("Apple");
        linkedHashMap4.get("Orange");
        linkedHashMap4.get("Guava");
        linkedHashMap4.get("Apple");
        linkedHashMap4.get("Orange");
        linkedHashMap4.get("Apple");
        linkedHashMap4.get("Guava");
        linkedHashMap4.get("Guava");

        for(Map.Entry<String, Integer> entry:linkedHashMap4.entrySet()){
            System.out.println(entry.getKey()+"  : "+entry.getValue());
        }


        /** getOrDefault()
         *
         * If value is present for a particular key take that
         * If not present then get the default value what they assigned
         *
         * */
        LinkedHashMap<String, Integer> linkedHashMap6 = new LinkedHashMap<>(11, 0.3f, false);
        linkedHashMap6.put("Shubham", 10);
        linkedHashMap6.put("Vipul", 20);
        linkedHashMap6.put("Navin", 13);

        Integer value1= linkedHashMap6.getOrDefault("Shubham", 0);
        Integer value2 = linkedHashMap6.getOrDefault("Adani", 0);

        System.out.println(value1);
        System.out.println(value2);


        /**
         * putIfAbsent(key, value)
         *
         * If key is not present in HashMap then only store that element
         *
         * */

        LinkedHashMap<String, Integer> linkedHashMap7 = new LinkedHashMap<>(11, 0.3f, false);
        linkedHashMap7.put("Shubham", 10);
        linkedHashMap7.put("Vipul", 20);

        linkedHashMap7.putIfAbsent("Shubham", 99);
        linkedHashMap7.putIfAbsent("Shubha", 81);

        System.out.println(linkedHashMap7);
    }
}
