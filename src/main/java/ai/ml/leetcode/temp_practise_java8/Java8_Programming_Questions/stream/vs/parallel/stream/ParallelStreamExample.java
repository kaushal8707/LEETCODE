package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.stream.vs.parallel.stream;
import java.util.stream.IntStream;

public class ParallelStreamExample {
      public static void main(String[] args) {
		
    		//print 1 to 10 numbers
//  		IntStream.range(1, 10).forEach(System.out::print);
  		
  		//System.out.println(); 
  		
  		//print only even number 1 to 100
//  		IntStream.range(1, 100).filter(a->a%2==0).forEach(System.out::print);
  		
  	//I will try to iterate the 100 records using both the Streams and see the Performance
    	  long start=0;
    	  long end=0;
    	  
    	  start=System.currentTimeMillis();
    	  IntStream.range(1, 10).forEach(System.out::println);
    	  end=System.currentTimeMillis();
    	  System.out.println("Plain Stream took time :" +(end-start));
    	  
    	  System.out.println("*********************************");
    	  
    	  start=System.currentTimeMillis();
    	  IntStream.range(1, 10).parallel().forEach(System.out::println);
    	  end=System.currentTimeMillis();
    	  System.out.println("Parallel Stream took time :" +(end-start));
  		
	}

}
