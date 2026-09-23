package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.important;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FrequencyOfCharacter {
	public static void main(String[] args) {
		String input = "kaushal kumar singh";
		input=input.replace(" ","");
		Stream<String> data=Arrays.stream(input.split(""));
		Map<String, Long> countMap=data.collect(Collectors.groupingBy(Function.identity(), 
											Collectors.counting()));
		System.out.println(countMap);
	}
}
