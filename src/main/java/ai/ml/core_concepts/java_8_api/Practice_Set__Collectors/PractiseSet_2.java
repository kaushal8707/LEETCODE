package ai.ml.core_concepts.java_8_api.Practice_Set__Collectors;


import java.util.function.Function;

public class PractiseSet_2 {
    public static void main(String[] args) {
        Function<Integer, Integer> doubled = (a)-> a * 2;
        Function<Integer, Integer> trippled = (a)-> a * 3 -1;

        System.out.println(doubled.apply(4));
        System.out.println(trippled.apply(4));
        System.out.println(doubled.andThen(trippled).apply(4));
        System.out.println(doubled.compose(trippled).apply(4));  //compose perform in reverse order of andThen
    }
}
