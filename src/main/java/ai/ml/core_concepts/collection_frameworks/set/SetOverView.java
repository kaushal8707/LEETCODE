package ai.ml.core_concepts.collection_frameworks.set;

import lombok.Synchronized;

import java.util.*;

public class SetOverView {
    public static void main(String[] args) {

        // Set is a collection that can not contain duplicate elements.

        // Because Set is Based on HashMap Principles so
              // Time Complexity ->   find()  ->  O(1)
              // Time Complexity ->   insert() -> O(1)

        // Faster Operations

        // Map ->  Implementations (HashMap, LinkedHashMap, TreeMap, EnumMap)
        // Set ->  Implementations (HashSet, LinkedHashSet, TreeSet, EnumSet)

        /**
         * HashSet - Uniqueness
         * LinkedHashSet - Uniqueness + insertion order maintain
         * TreeSet - Uniqueness + sorted order
         *
         */

        Set<Integer> set = new HashSet<>();
        set.add(12);
        set.add(1);
        set.add(1);
        set.add(67);
        System.out.println(set);

        System.out.println("----------------");

        Set<Integer> set2 = new LinkedHashSet<>();
        set2.add(12);
        set2.add(1);
        set2.add(1);
        set2.add(67);
        System.out.println(set2);

        System.out.println("----------------");

        Set<Integer> set3 = new TreeSet<>();
        set3.add(12);
        set3.add(1);
        set3.add(1);
        set3.add(67);
        System.out.println(set3);
        System.out.println(set3.contains(12));
        System.out.println(set3.remove(67));
       // set3.clear();
        System.out.println(set3.isEmpty());
        for(int i : set3){
            System.out.println(i);
        }

        /** Thread Safety - Collections.synchronizedMap(map) */

        Set<Integer> synchronizedSet = Collections.synchronizedSet(set3);

        System.out.println("===========================");

        Set<Integer> syncSet = Collections.synchronizedSet(set3);
        synchronized(syncSet) {
            for (int i : syncSet) {
                //safe iteration
                System.out.println(i + " ");
            }
        }


        /** Un-Modifiable Set */

        Set<Integer> integers = Set.of(4, 1, 2, 3, 6, 7, 9, 8,15,23,11,22,67,13);   //Recommended
        System.out.println(integers);

        Set<Integer> set5 = Collections.synchronizedSet(set3);   // Not Recommended
        System.out.println(set5);

    }
}
