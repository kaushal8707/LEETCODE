package ai.ml.core_concepts.java_8_api.Practice_Set__Collectors;

import java.util.*;
import java.util.stream.Collectors;

// Collectors is a Utility class, provides a set of methods to create common collectors
public class PractiseSet_5_Collectors {
    public static void main(String[] args) {
        List<Product> products = Arrays.asList(new Product("Prd1", 123),
                new Product("Prd2", 514),
                new Product("Prd3", 881),
                new Product("Prd4", 11));

        List<String> list = Arrays.asList("Akash", "Ronak", "Saurav", "girmu", "Akash");
        long count = list.stream().filter(x -> x.startsWith("A")).count();
        System.out.println(count);

        /** 1. Collecting to a List */
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
        List<String> filteredNameList = names.stream()
                .filter(name -> name.startsWith("A"))
                .collect(Collectors.toList());
        System.out.println("filtered names started with A : "+filteredNameList);

        /** 2. Collecting to a Set */    //Set: remeoving duplicates
        List<Integer> numsList = Arrays.asList(1, 2, 2, 3, 4, 5, 5, 7, 8);
        Set<Integer> numsSet = numsList.stream()
                .collect(Collectors.toSet());
        System.out.println("num list converted to nums set : "+numsSet);

        /** 3. Collecting to a specific Collection : Collectors.toCollection() will give you any collection and use supplier to provide empty collection */
        ArrayDeque<Integer> deque = numsList.stream()
                .collect(Collectors.toCollection(() ->
                        new ArrayDeque<>()));
        System.out.println("integer nums in deque : "+deque);

        /** 4. Joining Strings : concatenate stream elements into a single stream **/
        List<String> nmList = Arrays.asList("yaut","pap","qtwrw","vabye","atyu");
        String concatenatedNamesString = nmList.stream()
                .collect(Collectors.joining("__"));
        System.out.println("concatenated names string : "+concatenatedNamesString);

        /** 5. Summarizing data : generates statistical data (count, sum, min, average, max) **/
        List<Integer> numbers = Arrays.asList(2, 3, 5, 7, 11);
        IntSummaryStatistics intSummaryStatistics = numbers.stream()
                .collect(Collectors.summarizingInt(n -> n));
        System.out.println("Max of given integer list :" +intSummaryStatistics.getMax());
        System.out.println("Sum of given integer list :"+intSummaryStatistics.getSum());
        System.out.println("Min of given list :"+intSummaryStatistics.getMin());
        System.out.println("average of given int data: "+intSummaryStatistics.getAverage());
        System.out.println("count no. of integres: "+intSummaryStatistics.getCount());
        Long countedElements = numbers.stream().collect(Collectors.counting()); System.out.println("counted elements: "+countedElements);
        Double averageNum = numbers.stream().collect(Collectors.averagingInt(x -> x)); System.out.println("average nums :"+averageNum);
        Integer sumNums = numbers.stream().collect(Collectors.summingInt(x -> x)); System.out.println("sum of nums: "+sumNums);
        Optional<Integer> minby = numbers.stream().collect(Collectors.minBy(Comparator.comparing(a -> a))); System.out.println("min by:"+minby.get());
        // min and max of custom object
        OptionalInt min = products.stream().mapToInt(product -> product.productValue).min(); System.out.println("min value from product :"+min.getAsInt());
        OptionalInt max = products.stream().mapToInt(product -> product.productValue).max(); System.out.println("max value from product :"+max.getAsInt());
        int sumValueProduct = products.stream().mapToInt(product -> product.productValue).sum(); System.out.println("sum of value from product :"+sumValueProduct);
        OptionalDouble averageValueProduct = products.stream().mapToInt(product -> product.productValue).average(); System.out.println("average of value from product :"+averageValueProduct.getAsDouble());
    }
}
