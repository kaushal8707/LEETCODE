package ai.ml.core_concepts.java_8_api.Practice_Set_Streams_Primitives;

import java.util.Arrays;
import java.util.OptionalInt;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PracticeSet_2 {
    public static void main(String[] args) {
       /** till now, we saw the examples on Wrapper class like Integer, Double, String...not in primitive data type like int, long... **/
       /** Important Points **/
       int [] numArr = {1,3,5,6};
       IntStream intStream = Arrays.stream(numArr); // While we use primitive int type to convert in stream, it will give IntStream instead of Stream

       Integer [] numsAr = {6,8,9,7,2};
       Stream<Integer> stream = Arrays.stream(numsAr); // While we use Wrapper class Integer type, it will give Stream

       Stream<Integer> streamData = intStream.boxed(); // Boxed will convert IntStream to Stream of Integer

       System.out.println(IntStream.range(1, 5).boxed().collect(Collectors.toList()));  // boxed() convert to Stream Wrapper class
       System.out.println(IntStream.rangeClosed(1, 5).boxed().collect(Collectors.toList()));
       System.out.println(IntStream.iterate(1, i-> i+1).boxed().limit(6).collect(Collectors.toList()));

        IntStream intStreamNums = IntStream.of(1, 2, 3, 4, 5);
        OptionalInt max = intStreamNums.max();
        System.out.println(max.getAsInt());

        DoubleStream doubles = new Random().doubles(5);
        System.out.println(doubles.sum());
        System.out.println(doubles.max());
        System.out.println(doubles.min());
        System.out.println(doubles.average());
        System.out.println(doubles.count());
        System.out.println(doubles.boxed().toList());

        IntStream ints = new Random().ints(5);
        System.out.println(ints.boxed().toList());



    }
}
