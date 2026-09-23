package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.functional.interfaces;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class SupplierDemo implements Supplier<String>{

	@Override
	public String get() {
		return "Hello Java Programmer";
	}
	
	public static void main(String[] args) throws Exception {
	//Traditional Approach
	Supplier<String> supplier = new SupplierDemo();
	System.out.println(supplier.get());

	//LambdaExpression
	Supplier<String> supply=()->{
		return "Hi All Programmer";
	};
	System.out.println(supply.get()); 
	

	//In Real time Scenario In java 8 if we are doing filtering process and we are not getting any result,
	//but you want some dummy data or some meaningful response in such case you can go for Supplier

	Supplier<String> supp=()->"I am present if theres no values";
	List<String> list=Arrays.asList();
	System.out.println(list.stream().findAny().orElseGet(supp));
	
	System.out.println(list.stream().findAny().orElseThrow(()->new Exception("No data in our list")));
	
	System.out.println(list.stream().findFirst().orElse("No Values there"));
	
 }
}
