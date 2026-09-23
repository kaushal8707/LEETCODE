package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.important;

import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.Employee1;
import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.EmployeeDatabase;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FindAverageSalaryEmployeeWhoseGradeA {
	public static void main(String[] args) {
		
		//find the Average Salary of those Employee Whose Grade is A
		double avgSalaryGradeAEmployee = EmployeeDatabase.getEmployees()
							.stream().filter(emp->emp.getGrade().equals("A"))
							 .map(emp->emp.getSalary()).mapToDouble(a->a).average().getAsDouble();
		System.out.println(avgSalaryGradeAEmployee);	
		
		//find the Sum Salary of those Employee Whose Grade is A
		double gradeATotalSal = EmployeeDatabase.getEmployees().stream()
									.filter(emp->emp.getGrade().equals("A"))
									    .map(emp->emp.getSalary()).mapToDouble(a->a)
									    		.sum();
		System.out.println(gradeATotalSal);	
		
		//find the second highest salary
		String empName = EmployeeDatabase.getEmployees().stream()
			.sorted(Comparator.comparing(Employee1::getSalary).reversed())
			     .collect(Collectors.toList()).get(1).getName();
		System.out.println(empName);
		
		//find the name of Employees whose salary is greater than 50000
		
		List<String> list = EmployeeDatabase.getEmployees().stream()
			.filter(emp->emp.getSalary() > 50000)
			    .map(emp->emp.getName()).collect(Collectors.toList());
		list.forEach(System.out::println);
	}
}
