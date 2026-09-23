package ai.ml.core_concepts.java_8_api.Practice_Set_Intermediate_Terminal_Ops;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Intermediate operations transform a stream into another stream && They are lazy which means they won't execute until a terminal operation invoked. **/
/** filter, map, sorted, distinct, limit, skip, peek, flatmap **/

public class PracticeSet_1 {
    public static void main(String[] args) {
        List<String> list = Arrays.asList("Akash", "Ronak", "Saurav", "Girmu", "Akash");

        long count = list.stream().filter(n -> n.startsWith("A")).count();   System.out.println("count name starts with A :"+count);

        ArrayList<String> nameMappedInUpperCase = list.stream().map(name -> name.toUpperCase()).collect(Collectors.toCollection(() -> new ArrayList<>())); System.out.println("mapped in upper case name collection : "+nameMappedInUpperCase);

        List<String> sortedNameBasedOnLengthInDescendingOrder = list.stream().sorted((a, b) -> b.length() - a.length()).collect(Collectors.toList()); System.out.println("sort name based on length in descending order : "+sortedNameBasedOnLengthInDescendingOrder);
        /** sort name based on length in descending order if name length is same sort on ascending order **/
        Comparator<String> comparator = (a, b)->{
            if(b.length() - a.length() < 0){
                return -1;
            }else if(b.length() - a.length() > 0){
                return 1;
            }else{
                return a.compareTo(b);
            }
        };
        list.stream().sorted(comparator).collect(Collectors.toList()).forEach(System.out::println);

        long countDistinctStartWithA = list.stream().distinct().filter(str -> str.startsWith("A")).count(); System.out.println("count distinct starts with A "+countDistinctStartWithA);

        Stream.iterate(1, num-> num + 1).map(i-> "Number "+i).limit(30).collect(Collectors.toList()).forEach(System.out::println);
        long countAfterSkip5NumberCount = Stream.iterate(1, num -> num + 1).map(i -> "Number " + i).limit(30).skip(5).count();         System.out.println("count after skip 5 number total count : "+countAfterSkip5NumberCount);

        /** peek **/ /** foreach() is our terminal operation where we consume something, same work peek will also do but peek is an Intermediate operation, peek() will perform one action on each element as it is consumed **/
        Stream.iterate(1, n-> n+1).skip(5).limit(40).peek(System.out::println).count();

        /** flatMap : Handle streams of collections, lists or arrays where each element is itself a collection, Flatten nested structure(eg. lists within lists) so that they can be processed as a single sequence of elements.
        //** Transform and Flatten elements at same time, map will transform data and flat will convert nested into a single stream and then we can perform any operations on it**/
        /** If you are working on a List<String> or List<List<String>> to perform any operations better to use flat Map instead of Map **/
        List<List<String>> listOfLists = Arrays.asList(Arrays.asList("apple", "banana"), Arrays.asList("orange", "kiwi"), Arrays.asList("pear", "grape"));
        listOfLists.stream().flatMap(Collection::stream).filter(w-> w.length() >=5)
                .map(String::toUpperCase).collect(Collectors.toList()).forEach(System.out::println);

        /** we want multiple strings into a single string */
        List<String> sentences = Arrays.asList("Hello world", "Java streams are powerful", "flat map is useful");
        List<String> modList = sentences.stream()
                .flatMap(f -> Arrays.stream(f.split(" "))).filter(f-> f.length() > 5).map(String::toUpperCase).toList();
        System.out.println(modList);

        List<String> words = Arrays.asList("apple", "banana", "cherry");     // Output: [a, p, p, l, e, b, a, n, a, n, a, c, h, e, r, r, y]
        List<Character> characters = words.stream().flatMap(word -> word.chars().mapToObj(c -> (char) c)).collect(Collectors.toList());
        System.out.println(characters);
        String resultantString = words.stream().flatMap(e -> Arrays.stream(e.split(""))).collect(Collectors.joining(","));
        System.out.println(resultantString);
    }
}
