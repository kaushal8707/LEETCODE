package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.functional.interfaces;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class PredicateDemo implements Predicate<Integer>{
	
	@Override
	public boolean test(Integer t) {
		if(t%2==0) {
			return true;
		}else {
			return false;
		}
	}


	public static void main(String[] args) {

	//Traditional approach
	Predicate<Integer> predicate = new PredicateDemo();
	  System.out.println(predicate.test(10));
	
	//Lambda Expression Approach
	Predicate<Integer> pred=(t)->t%2==0;
	System.out.println(pred.test(11));
	
	//using filter
	List<Integer> list = Arrays.asList(1,2,3,4,5,6,7,8,9);
	list.stream().filter(t->t%2==0).forEach(System.out::println); 
	
	
	}  
}