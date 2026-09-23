package ai.ml.core_concepts.java_8_api.Practice_Set__Collectors;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PractiseSet7 {
    public static void main(String[] args) {

        System.out.println("-------------------------Questions--------------------------");
        /**  1 - find all names whose length is greater than 4 and group them together **/
        List<String> nameList = Arrays.asList("Anna", "Bob", "Alexander", "Brian", "Alice");
        /**  2 - Counting word occurrences **/
        String sentence="Hello World Hello Java World";
        /**  3 - Partitioning Even & Odd Number **/
        List<Integer> integerList = Arrays.asList(1, 2, 3, 4, 5, 6);
        /**  4 - Summing values in map **/
        Map<String , Integer> items = new HashMap<>();
        items.put("Apple", 10);
        items.put("Banana", 20);
        items.put("Orange", 15);
        /**  5 - Creating a Map from Stream elements **/
        List<String> fruits = Arrays.asList("Apple", "Banana", "Cherry");
        /**  6 - find map key with count using to Map() **/
        List<String> words3 = Arrays.asList("Apple", "Banana", "Apple", "Orange", "Banana", "Apple");

        System.out.println("-------------------------Answers--------------------------");

        TreeMap<Integer, String> groupNameLengthGreaterThan4 = nameList.stream().filter(n -> n.length() > 4).collect(Collectors.groupingBy(String::length, TreeMap::new, Collectors.joining("___")));
        System.out.println("group the name based on length greater than value 4 "+groupNameLengthGreaterThan4);

        LinkedHashMap<Integer, Long> countingWordOccurrenceBasedOnWordLengthAndKeyInInsertionOrder = Arrays.asList(sentence.split(" ")).stream().collect(Collectors.groupingBy(String::length, LinkedHashMap::new, Collectors.counting()));
        System.out.println("counting word occurrences based on word length with their insertion order "+countingWordOccurrenceBasedOnWordLengthAndKeyInInsertionOrder);
        TreeMap<String, Long> countingWordOccurrenceAndKeyInSortedOrder = Arrays.asList(sentence.split(" ")).stream().collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting()));
        System.out.println("counting word occurrences with insertion order "+countingWordOccurrenceAndKeyInSortedOrder);

        Map<Boolean, List<Integer>> evenOddBasedPartitioned = integerList.stream().collect(Collectors.partitioningBy(num -> num % 2 == 0));
        System.out.println("partitioning based on even and odd numbers "+evenOddBasedPartitioned);

        Integer summingMapValues1 = items.values().stream().reduce((a, b) -> a + b).get();
        Integer summingMapValues2 = items.values().stream().reduce(Integer::sum).get();
        long sumMapValues = items.values().stream().collect(Collectors.summarizingInt(n -> n)).getSum();
        Integer sumMapValues2 = items.values().stream().collect(Collectors.summingInt(n -> n));
        System.out.println(summingMapValues1+"---"+sumMapValues2+"---"+summingMapValues2+"---"+sumMapValues);

        Map<String, Integer> mapKeyUpperCaseValueTheirLength = fruits.stream().collect(Collectors.toMap(f-> f.toUpperCase(), f-> f.length()));
        System.out.println("created map uppercase as a key and value as their length "+mapKeyUpperCaseValueTheirLength);

        Map<String, Integer> createdMapCountWordOccurrence = words3.stream().collect(Collectors.toMap(key -> key, value -> 1, (x, y) -> x + y));
        System.out.println("created a map count word occurrence :"+createdMapCountWordOccurrence);

    }
}
