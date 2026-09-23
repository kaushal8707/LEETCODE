package ai.ml.core_concepts.java_8_api.Practice_Set_Streams_Primitives;
/** Stream : a sequence of elements supporting functional & declarative programming. **/
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/** How to use Stream :  Source, Intermediate Operation & Terminal Operation **/


public class PracticeSet_1 {
    public static void main(String[] args) {

        /** different ways of creating a stream **/

        //1 from collection
        List<Integer> listNum = Arrays.asList(1,2,3,4);
        Stream<Integer> stream = listNum.stream();

        //2 from Arrays
        int arr[] = {4,5,6,7};
        IntStream intStream = Arrays.stream(arr);

        //3 from of
        Stream<Integer> streamNums = Stream.of(6, 8, 7, 9);

        //4 from generate & iterate ( infinite streams )
        Stream<Integer> generatedStream = Stream.generate(() -> 1); // will generate infinite time 1 , generate use supplier to produce
        //generatedStream.forEach(System.out::println);
        Stream<Integer> generatedLimitedStream = Stream.generate(() -> 1).limit(10); // will generate 10 times 1
        generatedLimitedStream.forEach(System.out::println);

        /** iterate() : <T> Stream<T> iterate(final T seed, final UnaryOperator<T> f) **/
        /** iterate() : <T> Stream<T> iterate(T seed, Predicate<? super T> hasNext, UnaryOperator<T> next) **/
        Stream<Integer> evenStreams = Stream.iterate(1, x -> x + 1).filter(x -> x % 2 == 0).limit(10);
        evenStreams.collect(Collectors.toList()).forEach(System.out::println);

        List<Integer> primeList = Stream.iterate(1, x -> x + 1)
                .peek(i-> System.out.println("x:"+i))
                .filter(PracticeSet_1::isPrime)
                .peek(i-> System.out.println("y:"+i))
                .limit(10).collect(Collectors.toList());
    }
    private static boolean isPrime(int num){
        return IntStream.rangeClosed(2, num/2).noneMatch(i-> num % i == 0);
    }

    private static boolean isEven(int num){
        return num % 2 == 0 ;
    }
}
