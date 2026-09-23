package ai.ml.core_concepts.java_8_api.Practice_Set_Intermediate_Terminal_Ops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**we can see Filtering not printed bcz there were no terminal operation once terminal operation (collect()) invoke then only stream started executing.**/

public class PracticeSet_3 {
    public static void main(String[] args) {
        List<String> nameList = Arrays.asList("kaushal", "vipul", "pravin", "mamta");
        Stream<String> stream = nameList.stream().filter(name -> {
            System.out.println("filtering : " + name);
            return name.length() > 2;
        });
        System.out.println("Before terminal operation");
        stream.collect(Collectors.toCollection(()-> new ArrayList<>()))
                .forEach(System.out::println);
        System.out.println("After terminal operation");
    }
}
