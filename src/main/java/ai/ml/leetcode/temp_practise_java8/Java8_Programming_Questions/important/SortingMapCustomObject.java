package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.important;
import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.Employee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SortingMapCustomObject {

	public static void main(String[] args) {
		Map<Employee, Integer> map = new HashMap<Employee, Integer>();
		map.put(new Employee(28,"prakash","Mechanical",334651) , 2);
		map.put(new Employee(65,"sohan","Electronic",912736), 4);
		map.put(new Employee(39,"vinay","HArdware",451271), 6);
		map.put(new Employee(81,"dipesh","HR",874651), 8);
		
		//sort map based on name of Employee
		
		map.entrySet().stream()
				.sorted(Map.Entry.comparingByKey(Comparator.comparing(Employee::getName)))
						.collect(Collectors.toList()).forEach(System.out::println); 
		
		//sort based on name of Employee In Reversed Order
		
		map.entrySet().stream()
		       .sorted(Map.Entry.comparingByKey(Comparator.comparing(Employee::getName).reversed()))
				       .collect(Collectors.toList()).forEach(System.out::println); 

		//sort map based on value in reveresd order
		
		map.entrySet().stream()
		       .sorted(Map.Entry.comparingByValue(Comparator.comparing(Integer::intValue).reversed()))
				       .collect(Collectors.toList()).forEach(System.out::println);
		
		//find the name of employee whose salary is highest  -->> this is an alternate way ...otw u can see stream().max().....map()...
		
		String name = map.entrySet().stream()
			.sorted(Map.Entry.comparingByKey(Comparator.comparing(Employee::getSalary).reversed()))
			      .findFirst().get().getKey().getName();
		System.out.println(name);
		
		//find Name of the Person who is in HR Department
		String personInHRDept = map.entrySet().stream()
			.filter(entry->entry.getKey().getDept().equals("HR"))
							.map(entry->entry.getKey().getName()).findAny().get();
		System.out.println(personInHRDept); 
		
		//Using Lambda Expression
		List<Map.Entry<Employee, Integer>> list = new ArrayList(map.entrySet());
		Collections.sort(list, (entry1, entry2)->entry1.getKey().getDept()
									.compareTo(entry2.getKey().getDept()));
		list.forEach(System.out::println); 
	}
}
