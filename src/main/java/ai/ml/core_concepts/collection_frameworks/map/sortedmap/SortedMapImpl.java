package ai.ml.core_concepts.collection_frameworks.map.sortedmap;

import java.util.SortedMap;
import java.util.TreeMap;

public class SortedMapImpl {

    public static void main(String[] args) {

        SortedMap<String, Integer> map = new TreeMap<>();
        map.put("Vivek", 91);
        map.put("Shubham", 99);
        map.put("Mohit", 78);

        System.out.println(map);

        System.out.println("----------------------");

        SortedMap<Integer, String> map1 = new TreeMap<>();
        map1.put( 91, "Vivek");
        map1.put( 99, "Shubham");
        map1.put(78, "Mohit");

        System.out.println(map1);

        System.out.println("----------------------");

        SortedMap<Integer, String> map3 = new TreeMap<>();
        map3.put( 91, "Vivek");
        map3.put(99, "Shubham");
        map3.put(78, "Mohit");
        map3.put(77, "Vipul");

        System.out.println(map3.firstKey());
        System.out.println(map3.lastKey());
        System.out.println(map3.headMap(91));   // exclude
        System.out.println(map3.tailMap(91));
        System.out.println(map3.subMap(78, 91));   // exclude
        System.out.println(map3);

        System.out.println("----------------------");

        SortedMap<Integer, String> map4 = new TreeMap<>((a, b)-> b-a);          // you want 99 91 -> 99-91 -> +ve so 99 will shift right ->
                                                                                // so for descending will do b-a -> 91-99 -> -ve
        map4.put( 91, "Vivek");
        map4.put(99, "Shubham");
        map4.put(78, "Mohit");
        map4.put(77, "Vipul");
        System.out.println(map4);

        System.out.println("--------Student - Comparable--------------");
        SortedMap<Student, Integer> map5 = new TreeMap<>();                    //want to print (charlie, Bob) -> a-b-> + so it will shift right
                                                                                // so we can do b-a
        map5.put(new Student("Charlie", 3.5), 3);
        map5.put(new Student("Bob", 3.7), 1);
        map5.put(new Student("Alice", 3.5), 2);
        map5.put(new Student("Akshit", 3.9), 4);

        System.out.println(map5);

        System.out.println("--------Person - Comparator--------------");
        SortedMap<Person, Integer> map6 = new TreeMap<>(
                (a, b)-> {
                                                                    //want to print (3.9, 3.7) -> a-b-> + so it will shift right,  so we can do b-a
                    if (b.getGpa() - a.getGpa() > 0)
                        return 1;
                    else if (b.getGpa() - a.getGpa() < 0)
                        return -1;
                    else
                        return a.getName().compareTo(b.getName());         // if grade equal sort name in ascending order
                                                                          // (Alice, Charlie)-> (a, b)-> a-b-> -ve -> so ascending order
                });

        map6.put(new Person("Charlie", 3.5), 3);
        map6.put(new Person("Bob", 3.7), 1);
        map6.put(new Person("Alice", 3.5), 2);
        map6.put(new Person("Akshit", 3.9), 4);

        System.out.println(map6);

        System.out.println("-------key having null----throw NullPointerException----");
        SortedMap<String, Integer> map7 = new TreeMap<>();
        map7.put("Vivek", 91);
        map7.put("Shubham", 99);
        map7.put("Mohit", 78);
        map7.put(null, 78);

        System.out.println(map7);

    }
}
