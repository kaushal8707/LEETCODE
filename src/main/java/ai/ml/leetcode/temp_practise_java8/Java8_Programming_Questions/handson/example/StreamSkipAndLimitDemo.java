package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.handson.example;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
//skip() uses for how many elements we are going to skip after that element it will take
//limit() uses for first how many elements we are going to take
import java.util.stream.Stream;

public class StreamSkipAndLimitDemo {

	public static void main(String[] args) {
		Stream.of(3,7,9,1,2,5)
        .skip(3)
        .forEach(e->System.out.println(e));

		System.out.println("First we filter it and extract even number and then skip by two in final result");
		Stream.of(1,2,3,4,5,6,7,8,9)
        .filter(f->f%2==0)
        .skip(2)
        .forEach(System.out::println);

		System.out.println("we have a stream of alphabets. First we sort it and then skip by 2 elements");
		Stream.of("A","D","C","B")
        .sorted()
        .skip(2)
        .forEach(a->System.out.println(a));


		System.out.println("we have a list of integers. We will run limit and skip both on source stream");
		List<Integer> list=Arrays.asList(1,2,3,4,5,6,7);
		
		List<Integer> afterLimit=list.stream().limit(4).collect(Collectors.toList());
		List<Integer> afterSkip=list.stream().skip(2).collect(Collectors.toList());
		
		System.out.println("limit :" +afterLimit);
		System.out.println("skip :" +afterSkip);	
	}
}
