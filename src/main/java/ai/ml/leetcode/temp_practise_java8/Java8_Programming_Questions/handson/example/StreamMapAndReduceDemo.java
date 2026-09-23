package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.handson.example;
import java.util.Arrays;  
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamMapAndReduceDemo {

	//Map	    -> 		Transforming Data
	//Reduce	->		Aggregating Data
	
	//combine elements of a stream and produce a single value

	//Map() ->	Transform Stream<Object> to Stream of int and 
	//Reduce() ->	combine stream of int and produce the sum of result
	public static void main(String[] args) {

		//sum of results
		Integer sumResult = Stream.of(2,4,6,9,1,3,7).reduce(0, (a,b)->a+b);
		System.out.println(sumResult); 
		
		//multiply of results
		Integer multiplyResult = Stream.of(2,4,6,9,1,3,7).reduce(1, (a,b)->a*b);
		System.out.println(multiplyResult); 
		
		//Reduce with Method Reference
		List<Integer> numbers = Arrays.asList(5,2,9,6,1,3,8);
		List<String> words=Arrays.asList("corejava","spring","hibernate");
		
		int sum=numbers.stream().reduce(Integer::sum).get();
		int max=numbers.stream().reduce(Integer::max).get();
		int min=numbers.stream().reduce(Integer::min).get();
		int greatestNumber = numbers.stream().reduce(0, (a,b)->a>b?a:b);
		String longestString = words.stream().reduce((word1,word2)->word1.length()>word2.length()?word1:word2).get();
		words.stream().map(String::toUpperCase).forEach(System.out::print);
		System.out.println();
		words.stream().map(String::length).forEach(System.out::print);
		System.out.println();
		words.stream().filter(a->a.contains("ja")).collect(Collectors.toList()).forEach(System.out::println);
		System.out.println("Min = "+min+"====="+" Max="+max+"======"+"sum="+sum);
		System.out.println("Greatest Number  "+greatestNumber);
		System.out.println("Longest String  "+longestString);
	}
}
