package ai.ml.core_concepts.collection_frameworks.map.identityhashmap;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class IdentityHashMapImpl {
    public static void main(String[] args) {

        /** HashMap */
        String key1=new String("key");
        String key2=new String("key");

        Map<String, Integer> map = new HashMap<>();
        map.put(key1, 1);
        map.put(key2, 2);

        System.out.println(map);
        System.out.println("-------------------");

        /** Identity Hash Map
         *
         * In case of Identity Hash Map
         * In your class Either hashcode or equals method will be there or not
         *
         *In Identity Hash Map Object class hashcode() method only applicable
         * bcz In Identity Hash Map it used Object class hashcode() which used object Memory Address.
         *so here both hashcode is different bcz of new keyword and 2 entry will get created.
         *
         * so here it will use IdentityHashCode and ==
         * IdentityHashCode-> the hashcode which is there inside an Object class.
         *
         * */
        String key3=new String("key");
        String key4=new String("key");

        Map<String, Integer> map1 = new IdentityHashMap<>();
        map1.put(key3, 1);
        map1.put(key4, 2);

        System.out.println(map1);

        System.out.println("-------------------");

        /**
        here address is same bcz of pointing to string literals
         so Object class hashcode will compute based on memory address
         so only one entry
         */
        String key5="key";
        String key6="key";

        Map<String, Integer> map2 = new IdentityHashMap<>();
        map2.put(key5, 1);
        map2.put(key6, 2);

        System.out.println(map2);
    }
}
