package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.handson.example;

import java.util.Random;
import java.util.stream.Stream;

public class StreamGeneratedDemo {
	public static void main(String[] args) {
	 // generate 7 Random Integer and skip first 3
     Stream.generate(()->new Random().nextInt(10)).limit(7).skip(3).forEach(System.out::println); 
     // generate 3 Random Boolean
     Stream.generate(()->new Random().nextBoolean()).limit(3).forEach(System.out::println); 
     // example to generate constant stream
     Stream.generate(()->"Hello Java Ji").limit(3).forEach(System.out::println); 
     
	}
}
