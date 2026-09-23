package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.handson.example;

import ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model.Person;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConvertMapToListAndListOfUserObject {

	public static void main(String[] args) {
		
		 //Convert Map to List of User Object Example and sort based on key 
        Map<Integer, String> map1 = new HashMap<>();
        map1.put(23, "Mahesh");
        map1.put(10, "Suresh");
        map1.put(26, "Dinesh");
        map1.put(11, "Kamlesh");

        List<Person> list = map1.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(e -> new Person(e.getKey(), e.getValue())).collect(Collectors.toList());

        list.forEach(System.out::println);

        
		Map<Integer, String> map = new HashMap<>();
        map.put(23, "Mahesh");
        map.put(10, "Suresh");
        map.put(26, "Dinesh");
        map.put(11, "Kamlesh");
        
        System.out.println("--Convert Map Values to List--");
        List<String> valueList = map.values().stream().collect(Collectors.toList());
        valueList.forEach(n -> System.out.println(n));

        System.out.println("--Convert Map Values to List using sort--");
        List<String> sortedValueList = map.values().stream()
                .sorted().collect(Collectors.toList());
        sortedValueList.forEach(n -> System.out.println(n));

        System.out.println("--Convert Map keys to List--");
        List<Integer> keyList = map.keySet().stream().collect(Collectors.toList());
        keyList.forEach(n -> System.out.println(n));

        System.out.println("--Convert Map keys to List using sort--");
        List<Integer> sortedKeyList = map.keySet().stream()
                .sorted().collect(Collectors.toList());
        sortedKeyList.forEach(n -> System.out.println(n));
	}
}
