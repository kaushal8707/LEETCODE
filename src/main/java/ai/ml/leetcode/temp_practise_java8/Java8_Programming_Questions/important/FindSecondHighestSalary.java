package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.important;

import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.Employee1;
import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.EmployeeDatabase;

import java.util.Comparator;
import java.util.stream.Collectors;

public class FindSecondHighestSalary {

	public static void main(String[] args) {
		String empName = EmployeeDatabase.getEmployees().stream()
				.sorted(Comparator.comparing(Employee1::getSalary).reversed())
			     .collect(Collectors.toList()).get(1).getName();
		System.out.println(empName);
	}

}
