package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.handson.example;

import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.NewJoinee;
import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.Student;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class CloneFromOneObjToAnotherObj {

	public static void main(String[] args) {

		List<Student> students = Arrays.asList(new Student(111, "kaushal", "Bangalore", 66786.98),
										new Student(234, "raj", "pune", 1234.98),
										new Student(567, "mohan", "kanpur", 6789.98));
		List<NewJoinee> newJoinees = students.stream().map(student-> NewJoinee.builder()
										.name(student.getName())
										.address(student.getAddress())
										.fee(student.getFee())
										.build())
									.collect(Collectors.toList());
		newJoinees.forEach(System.out::println); 
	}

}
