package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.important;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FindDuplicateNumberInGivenList {
	public static void main(String[] args) {
        List<Integer> list= Arrays.asList(1,2,3,4,5,4,3,4,3,5,6);
        //way 1
        Set<Integer> reql1 = list.stream().filter(ele->Collections.frequency(list, ele)>1)
        								.collect(Collectors.toSet());
        System.out.println(reql1);
        
        //way 2
        Set<Integer> set=new HashSet<Integer>();
        list.stream().filter(p->!set.add(p)).collect(Collectors.toSet())
        			.forEach(System.out::println);
	}
}
