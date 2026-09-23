package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.functional.interfaces;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ConsumerDemo implements Consumer<Integer>{ 

	@Override
	public void accept(Integer value) {
		System.out.println("printing..."+value); 
	}
	
	public static void main(String[] args) {
		
		//Traditional approach
		Consumer consumer=new ConsumerDemo();
		consumer.accept(10); 
		
		//Lambda Expression Approach
		Consumer cons=(t)->{
			System.out.println("printed ..."+t);
		};
		cons.accept(11);
		
		//forEach takes input as a Consumer
		List<Integer> list = Arrays.asList(1,2,3,4,5);
		list.stream().forEach((t)->System.out.println(t)); 

	}

}
