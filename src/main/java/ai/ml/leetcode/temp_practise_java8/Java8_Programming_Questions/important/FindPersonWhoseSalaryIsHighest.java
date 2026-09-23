package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.important;

import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.User;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindPersonWhoseSalaryIsHighest {

	public static void main(String[] args) {
		
	       List<User> list=Arrays.asList(
	                new User("kaushal",33,11423.22f),
	                new User("nagraju",77, 657113.22f),
	                new User("columbus",82,44171.11f),
	                new User("Monto",52,445615.112f)
	                );
	       //find the person name who is smallest in age
	       String youngestPersonName = list.stream().min(Comparator.comparing(User::getAge)).map(User::getUserName).get();
	       System.out.println(youngestPersonName);
	        
	       //find the person whose salary is highest
	       String highestSalaryPersonName = list.stream().max(Comparator.comparing(User::getSalary)).map(User::getUserName).get();
	       System.out.println(highestSalaryPersonName);

	       Map<User, Integer> map=new HashMap<User, Integer>();
	        map.put(new User("kaushal",33,11423.22f), 55);
	        map.put(new User("nagraju",77, 657113.22f), 33);
	        map.put(new User("columbus",82,44171.11f), 44);
	        map.put(new User("Monto",52,445615.112f), 11);
	        
	        //find the person name who is highest in age
	        String oldestPersonName = map.entrySet().stream().max(Map.Entry.comparingByKey(Comparator.comparing(User::getAge)))
	        									.map(entry->entry.getKey().getUserName()).get();
		    System.out.println(oldestPersonName);

	        //find the person name whose salary is lowest
		    String lowestSalaryPersonName = map.entrySet().stream().min(Map.Entry.comparingByKey(Comparator.comparing(User::getSalary)))
		    								.map(entry->entry.getKey().getUserName()).get();
		    System.out.println(lowestSalaryPersonName);


	}

}
