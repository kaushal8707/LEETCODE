package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.handson.example;

import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.User;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class StreamMinMaxDemoPrimitiveAndObject {
	public static void main(String[] args) {
		  List<Integer> list= Arrays.asList(3,5,1,2,9);
		  int min=list.stream().min(Comparator.comparing(Integer::intValue)).get();
		  int max=list.stream().max(Comparator.comparing(Integer::intValue)).get();
		  System.out.println(min+"----"+max);

		  List<String> strList=Arrays.asList("vhj","mkj","kio","fgt");
		  String minStr=strList.stream().min(Comparator.comparing(String::valueOf)).get();
		  String maxStr=strList.stream().max(Comparator.comparing(String::valueOf)).get();
		  System.out.println(minStr+"----"+maxStr);

		  List<User> listOfObjects=Arrays.asList(
	                new User("kaushal",83,1233),
	                new User("nagraju",77,11221),
	                new User("columbus",52,55433)
	                );
		  User u1=listOfObjects.stream().max(Comparator.comparing(User::getAge)).get();
		  String uName=listOfObjects.stream().min(Comparator.comparing(User::getSalary)).map(User::getUserName).get();
		  System.out.println(u1+"----"+uName);
	}
}
