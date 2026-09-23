package ai.ml.core_concepts.java_8_api.Practise_Set_MiscellaneousExamples;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PracticeSet_3 {
    public static void main(String[] args) {

        /** find duplicate numbers in a given list **/

        List<Integer> list= Arrays.asList(1,2,3,4,5,4,3,4,3,5,6);
        List<Integer> collect = list.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 1)
                .map(entry-> entry.getKey())
                .collect(Collectors.toList());
        System.out.println(collect);


        /** find unique numbers in a given list **/
        List<Integer> uniqueNumbersInGivenList = list.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() == 1)
                .map(e-> e.getKey())
                .toList();
        System.out.println(uniqueNumbersInGivenList);

        // for each internal demo
        List<String> checkList = List.of("asd","jhu","mkj","pqo");
        Consumer<String> consumer = (t) -> System.out.println(t);
        for(String i : checkList){
            consumer.accept(i);
        }

        //------or----------
        checkList.forEach(System.out::println);

        // print only even key from map
        List<Integer> evenKeysFromMap = getMap().entrySet().stream().filter(entry -> entry.getKey() % 2 == 0).map(entry -> entry.getKey()).collect(Collectors.toList());
        System.out.println("even keys from map : "+evenKeysFromMap);

        //digit to word
        Map<Integer, String> map = new HashMap<>();
        map.put(1,"one"); map.put(2,"two");map.put(3,"three");map.put(4,"four");map.put(5,"five");
        map.put(6,"six"); map.put(7,"seven");map.put(8,"eight");map.put(9,"nine");map.put(0,"zero");
        Integer number = 7654;
        String[] charArr = String.valueOf(number).split("");
        for(String n : charArr ){
            System.out.print(map.get(Integer.parseInt(n))+" ");
        }

    }

    private static Map<Integer, String> getMap(){
        Map<Integer,String> map = new HashMap<Integer,String>();
        map.put(1, "one");
        map.put(2, "two");
        map.put(3, "three");
        map.put(4, "four");
        map.put(5, "five");
        return map;
    }
}
