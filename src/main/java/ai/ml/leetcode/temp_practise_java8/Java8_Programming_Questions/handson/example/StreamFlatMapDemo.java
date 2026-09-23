package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.handson.example;

import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.Customer;
import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.EKartDataBase;

import java.util.List;
import java.util.stream.Collectors;

//flatMap()-> from Customers we need phoneNumbers….

public class StreamFlatMapDemo {

	public static void main(String[] args) {

		List<Customer> customers = EKartDataBase.getAll();

		// List<Customer> convert List<String>        -->  Data Transformation
		// mapping :   customer  ->  customer.getEmail
		// One - to - One Mapping
		
		customers.stream().map(Customer::getName)
				.collect(Collectors.toList()).forEach(System.out::println);
		
		customers.stream().map(Customer::getPhoneNumbers)
					.collect(Collectors.toList()).forEach(System.out::print);
		
		System.out.println();
		
		// List<Customer> convert List<String>        -->  Data Transformation
		// mapping :   customer  ->  customer.getPhoneNumbers
		// One - to - Many Mapping
		
		List<String> phoneNumbers = customers.stream().
										flatMap(cust->cust.getPhoneNumbers().stream())
												.collect(Collectors.toList());
		System.out.println(phoneNumbers);
		
	}
}
