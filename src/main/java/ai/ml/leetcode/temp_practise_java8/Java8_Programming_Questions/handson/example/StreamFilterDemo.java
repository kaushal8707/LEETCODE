package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.handson.example;

import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.Users;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StreamFilterDemo {

	public static void main(String[] args) {
		List<Users> list = Users.getUsers();
	    
        //Stream filter() with mapToInt() and sum()
        List<Users> list1 = Users.getUsers();
        System.out.println("--- Sum of age between the user id 2 and 4 ---");
        int sum = list1.stream().filter(u -> u.getId() >= 2 && u.getId() <= 4)
                .mapToInt(u -> u.getAge()).sum();
        System.out.println("Sum: " + sum);

        //list1.stream().filter(t->t.getId()>=2 && t.getId()<=4).mapToInt(t->t.getAge()).

        //Stream filter() with collect()
        List<Users> list2 = Users.getUsers();
        long count = list2.stream().filter(u -> u.getName().endsWith("sh"))
                .collect(Collectors.counting());
        System.out.println("Number of users ending name with 'sh': "+ count);
        
        System.out.println("Using findFirst() ---");
        Users user = list.stream().filter(u -> u.getName().endsWith("sh"))
                .findFirst().orElse(null);
        System.out.println(user.getName());
        System.out.println("Using forEach() ---");
        list.stream().filter(u -> u.getName().endsWith("sh"))
                .forEach(u -> System.out.println(u.getName()));

       List<Users> list3 = Users.getUsers();
       int sumOfAge= list3.stream().filter(u -> u.getName().endsWith("sh"))
                .mapToInt(u -> u.getAge()).sum();
       System.out.println(sumOfAge);
       
        //Filter with List
        List<Integer> list4  = Arrays.asList(3, 12, 23, 44, 20, 10, 17, 8);
        List<Integer> evenList=list4.stream().filter(i->i%2==0)
                .collect(Collectors.toList());
        evenList.forEach(a->System.out.print(a+"  "));

        List<Integer> oddList=list4.stream().filter(i->i%2!=0)
                .collect(Collectors.toList());
        oddList.forEach(a->System.out.print(a+"  "));
	}
}
