package ai.ml.core_concepts.java_8_api.Practice_Set_Intermediate_Terminal_Ops;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PracticeSet_2 {
    public static void main(String[] args) {

        /** Terminal Operations **/
        /**  collect, forEach, reduce, count, max, min**/
        /** short-circuit-operations: anyMatch, allMatch, noneMatch, findFirst, findAny **/

        List<Integer> listNums = Arrays.asList(1, 2, 3, 4, 5, 6);
        List<String> listNames = Arrays.asList("kaushal", "Punit", "Amar", "Pawan", "Vinod", "Akshit");

        List<Integer> collectDemo1 = listNums.stream().skip(2).collect(Collectors.toList()); System.out.println("collectDemo1 :"+collectDemo1);  //it takes an element and create a new list and return that list
        List<Integer> collectListDemo2 = listNums.stream().toList(); System.out.println("collectListDemo2 :"+collectListDemo2); //same as collect(Collectors.toList()) as per new java version

        //reduce: combine elements to produce a single result, reduce take a BinaryOperator means BiFunction with 2 same type input with same return type, take BiFunction, we accumulate result in a single value like sum of all elements
        Integer viewRes = listNums.stream().reduce((a, b) -> {
            System.out.println(a + "      " + b);
            return a + b;
        }).get();
        System.out.println(viewRes);
        Integer reduceSumResult = listNums.stream().reduce((n1, n2) -> n1 + n2).get(); /** sum **/
        System.out.println("sum operation using reduce operation : "+reduceSumResult);
        Integer sumOfEvenNumbers = listNums.stream().filter(n -> n % 2 == 0).reduce((n1, n2) -> n1 + n2).get();    /** find sum of only even numbers */
        System.out.println("sum of even numbers using reduce operation : "+sumOfEvenNumbers);
        Integer maxNum = listNums.stream().reduce(Integer::max).get();  /** max **/
        System.out.println("max from given numbers using reduce operation : "+maxNum);
        Integer minNum = listNums.stream().reduce(Integer::min).get();  /** min **/
        System.out.println("min from given numbers using reduce operation : "+minNum);
        Integer sumNums = listNums.stream().reduce(Integer::sum).get();  /** sum **/
        System.out.println("sum of given numbers using reduce operation : "+sumNums);
        Integer result = listNums.stream().filter(n -> n % 2 == 0).reduce((a, b) -> (a) + (b*b)).get(); System.out.println("result : "+result);  // sum of square- wrong
        int sumOfSquareOfNums = listNums.stream().filter(n-> n%2==0).mapToInt(i -> i * i).sum(); System.out.println("sumOfSquareOfNums : "+sumOfSquareOfNums);
        int sumOfEven = IntStream.rangeClosed(1, 10).filter(n -> n % 2 == 0).sum(); System.out.println("sumOfEven : "+sumOfEven);
        IntStream.generate(()-> 10).skip(2).limit(10).forEach(System.out::println);
        int sum = IntStream.iterate(1, n -> n + 1).limit(5).sum(); System.out.println("sum : "+sum);

        /** short-circuit-operations: anyMatch, allMatch, noneMatch, findFirst, findAny **/
        boolean anyMatchResult = listNames.stream().anyMatch(p -> p.startsWith("A")); System.out.println("any Match Result : "+ anyMatchResult);
        boolean b = listNums.stream().anyMatch(x -> x % 2 == 0); System.out.println(b); // anyMatch return true when any single elements from a list
        boolean b1 = listNums.stream().allMatch(x -> x > 0); System.out.println(b1);   //allMatch() -  once found any element not match return false no need to check all elements */
        boolean b2 = listNums.stream().noneMatch(x -> x < 0);  System.out.println(b2);  // noneMatch return true when none of elements in a list
        String findFirstResult = listNames.stream().findFirst().get(); System.out.println("find first result : "+findFirstResult);
        String findAnyResult = listNames.stream().findAny().get(); System.out.println("find any result : "+findAnyResult);

        // 8. min/max
        System.out.println("Max if ascending order element there - "+ Stream.of(2, 64,44).max(Comparator.naturalOrder()).get());
        System.out.println("Max if descending order element there - "+Stream.of(2, 64,44).max(Comparator.reverseOrder()).get());
        System.out.println("Max if descending order element there - "+Stream.of(2, 64,44).max((m, n)-> n-m).get());
        System.out.println("Min - "+Stream.of(2, 64,44).min(Comparator.naturalOrder()).get());

        // forEachOrdered : when stream is parallel then forEach() run randomly or in an arbitrarily, but if you want to print in an order in parallel stream you can use forEachOrdered */
        Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9).parallelStream().forEach(System.out::print);  System.out.println("============");  //using foreach with parallel stream
        Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9).parallelStream().forEachOrdered(System.out::print);   /**using forEachOrdered with parallel **/ System.out.println("");

        /** Examples **/

        // 1. Counting occurrence of character
        String sentence = "Hello World";
        long countChar = sentence.chars().filter(x -> x == 'l').count(); System.out.println("count char : "+countChar);

        // 2. Squaring and then sorting given numbers
        List<Integer> numbers = Arrays.asList(5, 2, 9, 1, 6);
        numbers.stream().map(i-> i*i).sorted(Comparator.reverseOrder()).toList().forEach(System.out::println);

        /** Streams cannot be reused after a terminal operation has been called */
        Stream<String> stream = listNames.stream();
        stream.forEach(System.out::println);
        // List<String> list3 = stream.map(String::toUpperCase).toList();   //exception





    }
}
