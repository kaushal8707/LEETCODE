package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.handson.example;

import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.Customer;
import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.EKartDataBase;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StreamMapDemo {
	public static void main(String[] args) {
		
		List<Customer> customers = EKartDataBase.getAll();
		
		// List<Customer> convert List<String>        -->  Data Transformation
		// mapping :   customer  ->  customer.getEmail
		// One - to - One Mapping
		
		customers.stream()
				.map(Customer::getEmail)
					.collect(Collectors.toList()).forEach(System.out::println);
		
		//find the all employee name list in sorted order
		customers.stream().sorted(Comparator.comparing(Customer::getName))
					.map(Customer::getName).collect(Collectors.toList()).forEach(System.out::println);
				
		//find the highest salary paid Employee name
		String highestSalaryPaidEmployee = 
				customers.stream().max(Comparator.comparing(Customer::getSalary))
						.map(Customer::getName).get();
		System.out.println("Highest Salary Paid Employee = "+highestSalaryPaidEmployee);
		
		//find the highest salary Customer detail
		Customer highest_Salary = 
						customers.stream().max(Comparator.comparing(Customer::getSalary)).get();
				System.out.println("Highest Salary Paid Employee = "+highest_Salary);

		//find the lowest salary Customer Id
		int id = customers.stream().min(Comparator.comparing(Customer::getSalary))
							.map(Customer::getId).get();
		System.out.println("lowest salary Customer Id "+id);
		
		//find the average of salary
		double avg_salary=customers.stream().mapToLong(Customer::getSalary).average().getAsDouble();
		System.out.println("average salary "+avg_salary);
		
		//find sum of salary
		long sumOfSalary = customers.stream().mapToLong(Customer::getSalary).sum();
		System.out.println("Sum = "+sumOfSalary);
		
		//find highest salary
		long highestSalary = customers.stream().mapToLong(Customer::getSalary).max().getAsLong();
		System.out.println("Highest Salary = "+highestSalary);
	}
}
