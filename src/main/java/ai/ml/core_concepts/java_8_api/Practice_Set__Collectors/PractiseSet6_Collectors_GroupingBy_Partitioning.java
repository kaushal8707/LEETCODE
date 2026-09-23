package ai.ml.core_concepts.java_8_api.Practice_Set__Collectors;

import java.util.*;
import java.util.stream.Collectors;

/** Grouping && Partitioning : Grouping take input as function Partitioning take input as predicate**/
public class PractiseSet6_Collectors_GroupingBy_Partitioning {
    public static void main(String[] args) {
        List<String> words = Arrays.asList("Hello",
                "World",
                "Java",
                "Streams",
                "Collecting",
                "Api");

        /** 1 groupingBy(Function<? super T, ? extends K> classifier) */    /** based on length of string it will group/divide the given elements*/
        Map<Integer, List<String>> groupByLengthOfString=words.stream()
                .collect(Collectors.groupingBy(String::length));
        System.out.println("grouping the elements based on their length: "+groupByLengthOfString);

        /** 2 groupingBy(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream) */ /*after grouping elements do you want to perform any operations on grouped elements*/
        Map<Integer, Long> groupByLengthCounting = words.stream().collect(Collectors.groupingBy(String::length, Collectors.counting()));
        System.out.println("grouping the elements based on their length and then counting: "+groupByLengthCounting);

        Map<Integer, String> groupByLengthConcatenating = words.stream().collect(Collectors.groupingBy(String::length, Collectors.joining(" , ")));
        System.out.println("grouping the elements based on their length and then joining: "+groupByLengthConcatenating);

        /** 3 groupingBy(Function<? super T, ? extends K> classifier, Supplier<M> mapFactory, Collector<? super T, A, D> downstream
         * classifier - on which basis/criteria want to create a group, ->Supplier - It is like a map factory. which will give you a specific map implementation like LinkedHashMap, TreeMap ...etc, like if you want to print key in sorted order use TreeMap, in insertion order use LinkedHashMap, -> Collector downstream - if you want performed any operations on grouped elements
         */
        LinkedHashMap<Integer, Long> groupByLengthKeyInInsertionOrder = words.stream().collect(Collectors.groupingBy(String::length, LinkedHashMap::new, Collectors.counting()));
        System.out.println("grouping the elements based on their length and key printing in insertion order: "+groupByLengthKeyInInsertionOrder);

        TreeMap<Integer, Long> groupByLengthKeyInSortedOrder = words.stream().collect(Collectors.groupingBy(String::length, TreeMap::new, Collectors.counting()));
        System.out.println("grouping the elements based on their length and key printing in sorted order(ascending): "+groupByLengthKeyInSortedOrder);

        NavigableMap<Integer, Long> groupByLengthKeyInSortedOrderDescending = words.stream().collect(Collectors.groupingBy(String::length, TreeMap::new, Collectors.counting())).descendingMap(); /** TreeMap Implements NavigableMap,  so we can apply more additional features on it. */
        System.out.println("grouping the elements based on their length and key printing in sorted order(descending): "+groupByLengthKeyInSortedOrderDescending);

        SortedMap<Integer, Long> findAllEntryWhichKeyIsLesserThanvalue6 = words.stream().collect(Collectors.groupingBy(String::length, TreeMap::new, Collectors.counting())).headMap(6); /** TreeMap Implements NavigableMap and  NavigableMap extends SortedMap**/
        System.out.println("grouping the elements based on their length and get All entry whose key is lesser than 6 "+findAllEntryWhichKeyIsLesserThanvalue6);

        Map.Entry<Integer, Long> findTheLowerEntrythenvalue6 = words.stream().collect(Collectors.groupingBy(String::length, TreeMap::new, Collectors.counting())).lowerEntry(6);
        System.out.println("grouping the elements based on their length and get the lower Entry which is lesser than 6 "+findTheLowerEntrythenvalue6);

        /** 3. Partitioning elements: partitions elements into two groups(true or false) based on predicate, scenario - will create 2 group whose length less than 5 in one group and whose length greater than 5 in another group **/

        /** 3.1  partitioningBy(Predicate<? super T> predicate) **/
        Map<Boolean, List<String>> lengthBasedPartitioned = words.stream().collect(Collectors.partitioningBy(name -> name.length() > 5));
        System.out.println("partitioned the list based on their string length : "+lengthBasedPartitioned);

        /** 3.2 partitioningBy(Predicate<? super T> predicate, Collector<? super T, A, D> downstream) **/
        Map<Boolean, String> lengthBasedPartitionedAndJoiningGroupedElements = words.stream().collect(Collectors.partitioningBy(name -> name.length() < 4, Collectors.joining("__")));
        System.out.println("partitioned the list based on their string length and then joining the grouped results : "+lengthBasedPartitionedAndJoiningGroupedElements);


    }
}
