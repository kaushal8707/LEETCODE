package ai.ml.core_concepts.java_8_api.Practice_Set_Parallel_Processing;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PractiseSet_1 {
    public static void main(String[] args) {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        /** Parallel Stream **/
        // A type of stream that enables parallel processing of elements, allowing multiple threads to process parts of the streams simultaneously
        // This can significantly improve performance for large data sets workload is distributed across multiple threads **/
        List<Long> factorialList = Arrays.asList(1, 2, 3, 4, 5).stream().map(PractiseSet_1::factorial).collect(Collectors.toList());
        System.out.println("list of factorial : "+factorialList);

        List<Integer> cumulativeSumList = Arrays.asList(1, 2, 3, 4, 5).stream().map(i -> atomicInteger.addAndGet(i)).collect(Collectors.toList());
        System.out.println("list of cumulative sum : "+cumulativeSumList);

        List<Integer> listOfSquaresNum = Arrays.asList(1, 2, 3, 4, 5).stream().map(i -> i * i).collect(Collectors.toList());
        System.out.println("list of square num : "+listOfSquaresNum);

        int sumOfSquareNums = Arrays.asList(1, 2, 3, 4, 5).stream().mapToInt(i -> i * i).sum();
        System.out.println("sum of square nums : "+sumOfSquareNums);
    }

    private static long factorial(int num){
        return IntStream.rangeClosed(1, num).reduce(1, (x, y)-> x * y);
    }
}
