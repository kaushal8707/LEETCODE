package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.important;

import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.Employee;
import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.EmployeeDB;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SortingListCustomObject {
	public static void main(String[] args) {
	
		//sort employee object based on salary and get lowest salary employee object
		EmployeeDB.getEmployees().stream()
		       .sorted(Comparator.comparing(Employee::getSalary)).collect(Collectors.toList())
		       				.forEach(System.out::println);
		
		//sort employee object based on name in descending order
				EmployeeDB.getEmployees().stream()
				       .sorted(Comparator.comparing(Employee::getName).reversed()).collect(Collectors.toList())
				       				.forEach(System.out::println);
				
		//find second highest salary employee
		Employee emp=EmployeeDB.getEmployees().stream()
					.sorted(Comparator.comparing(Employee::getSalary).reversed())
							.collect(Collectors.toList()).get(1);
		System.out.println(emp);

		//sort based on lambda expression or using Comparator
		List<Employee> listObj=EmployeeDB.getEmployees();
		Collections.sort(listObj, (obj1, obj2)->obj1.getName().compareTo(obj2.getName())); 
		listObj.forEach(System.out::println);
	}
}
