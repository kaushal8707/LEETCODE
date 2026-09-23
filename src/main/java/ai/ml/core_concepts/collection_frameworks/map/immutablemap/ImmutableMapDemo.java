package ai.ml.core_concepts.collection_frameworks.map.immutablemap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ImmutableMapDemo {
    public static void main(String[] args) {

        Map<String, Integer> map1 = new HashMap<>();
        map1.put("A", 1);
        map1.put("B", 2);
        map1.put("D", 4);

        Map<String, Integer> map2 = Collections.unmodifiableMap(map1);
        System.out.println(map2);
        //map2.put("C", 2);        // throw UnsupportedOperationException

        /** Introduced in Java 9  - way to create Unmodifiable/Immutable Map*/
        Map<String, Integer> map3 = Map.of("Shubham", 72, "Mani", 83);
        System.out.println(map3);
//        map3.put("Pinku", 44);     //throw .UnsupportedOperationException


        /** Map.of() Limitation - allow only 10 entries */
        Map<String, Integer> map4 = Map.ofEntries(Map.entry("Shubham_1", 72), Map.entry("Mani_1", 83));
        System.out.println(map4);
        map4.put("Prateek", 66);
        System.out.println(map4);


    }
}
