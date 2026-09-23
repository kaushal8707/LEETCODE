package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.important;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ForEachInternalDemo {
	public static void main(String[] args) {
		
		//So, whatever we are passing to accept method its simply print the result, same thing forEach()
		//method also do it just iterate the list and call consumer.accept() method to print all the results…
		List<String> list = new ArrayList<String>();
		list.add("perk");
		list.add("absco");
		list.add("defto");
		list.add("vesko");
		
		Consumer<String> consumer = (t)-> System.out.println(t);
		for(String s : list) {
			consumer.accept(s);
		}
		System.out.println("======================");
		list.forEach(System.out::println);
	}
}
