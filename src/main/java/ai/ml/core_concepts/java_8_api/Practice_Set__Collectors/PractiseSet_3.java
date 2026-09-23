package ai.ml.core_concepts.java_8_api.Practice_Set__Collectors;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PractiseSet_3 {
    public static void main(String[] args) {
        Function<Integer, Integer> identityFunction = Function.identity();
        System.out.println(identityFunction.apply(111));

        //Collecting to a specific collection
        List<Integer> nums = Arrays.asList(5, 2, 2, 3, 4, 1, 5);
        ArrayDeque<Integer> arrayDeque = nums.stream()
                .collect(Collectors.toCollection(() -> new ArrayDeque<>()));
        System.out.println(arrayDeque);
    }
}
