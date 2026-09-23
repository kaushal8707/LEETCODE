package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.important;
import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.Employee;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HighestPaidEmployeeFromDepartment {

	public static void main(String[] args) {
		List<Employee> list = Stream.of(
				new Employee(1, "Basant", "DEV", 50000),
				new Employee(8, "santhosh", "DEV", 80000),
				new Employee(3, "pratik", "QA", 60000),
				new Employee(9, "Dipak", "QA", 90000),
				new Employee(4, "Bikash", "DEVOPS", 40000)).collect(Collectors.toList());
		Map<String, Object> map=list.stream().collect(Collectors.groupingBy(Employee::getDept, 
				                                     Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparing(Employee::getSalary)), Optional::get)));
		System.out.println(map);
	}
}
