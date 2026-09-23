package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.handson.example;

import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.Person;
import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.StudentSapient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConvertListToMapAndSortBasedOnKeyOrValue {

	public static void main(String[] args) {
		
		 List<Person> list1 = new ArrayList<>();
	        list1.add(new Person(100, "Mohan"));
	        list1.add(new Person(200, "Sohan"));
	        list1.add(new Person(300, "Mahesh"));
	        Map<Integer, String> map = list1.stream().collect(
	        								Collectors.toMap(Person::getId, Person::getName));
	        map.forEach((x, y) -> System.out.println("Key: " + x +", value: "+ y));
	        
	        
	        List<StudentSapient> list= Arrays.asList(new StudentSapient(5,"zrtt",16),
	                new StudentSapient(1,"raj",56),
	                new StudentSapient(2,"hansh",12),
	                new StudentSapient(3,"komal",34),
	                new StudentSapient(4,"raghu",45)
	                );
			
			System.out.println("Sorting Based on Key");
			list.stream()
				.collect(Collectors.toMap(StudentSapient::getName, StudentSapient::getScore))
						.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toList())
								.forEach(System.out::println);
			
			System.out.println("Sorting Based on Value");
			list.stream()
			.collect(Collectors.toMap(StudentSapient::getName, StudentSapient::getScore))
					.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toList())
							.forEach(System.out::println);
			
			System.out.println("---------");
			list.stream().sorted(Comparator.comparing(StudentSapient::getScore)).collect(Collectors.toMap(StudentSapient::getName, StudentSapient::getScore)).entrySet().forEach(System.out::print);
		}					
	}
