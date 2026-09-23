package ai.ml.core_concepts.java_8_api.Practice_Set__Collectors;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PractiseSet_1 {
    public static void main(String[] args) {
                /* Supplier, Consumer, Predicate, Function */
                Supplier<Integer> supplier = () -> new Random().nextInt(10);
                Function<Integer, Integer> function = num-> num * num;
                Predicate<Integer> predicate = (num)-> num % 2 == 0;
                Consumer<Integer> consumer = num -> System.out.println("Result: "+num);

                Integer num = supplier.get();
                if(predicate.test(num)){
                    consumer.accept(function.apply(num));
                }else{
                    System.out.println("not proceed with given input !!");
                }
            }
        }
