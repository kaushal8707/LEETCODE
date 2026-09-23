package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.stream.vs.parallel.stream;

import java.util.stream.IntStream;

public class StreamVSParallelStreamExample {

	public static void main(String[] args) {
		
	  IntStream.range(1, 10).forEach(x->{
   		  
   		  System.out.println("Stream -> Thread - "+Thread.currentThread().getName()+"   "+x);
   	  });
	  
   	  IntStream.range(1, 10).parallel().forEach(x->{
   		  
   		  System.out.println("Parallel Stream -> Thread - "+Thread.currentThread().getName()+"   "+x);
   	  });
	}

}
