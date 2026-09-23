package ai.ml.core_concepts.java_8_api.Practise_Set_MiscellaneousExamples;
import com.zero.to.hero.java_8_api.Practise_Set_MiscellaneousExamples.models.Employee;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PracticeSet_5 {
    public static void main(String[] args) {

        /** Frequency of characters **/
        String input = "kaushal kumar singh";
        long count = input.chars().filter(c -> c == 'a').count();  System.out.println("a count :" +count);

        List<Map.Entry<String, Long>> collect = Arrays.stream(input.split("")).filter(i -> !i.equals(" "))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet().stream().collect(Collectors.toList());
        System.out.println(collect);

        // Highest Paid Employee From Department
        Map<String, Optional<Employee>> highestPaidEmployeeFromDepartment = getEmployeeRecord().stream()
                .collect(Collectors.groupingBy(Employee::getDept, Collectors.maxBy(Comparator.comparing(Employee::getSalary))));
        System.out.println("highest paid employee from department :"+highestPaidEmployeeFromDepartment);

        //product of 2 min and max numbers
        int arr[] = {2, 4, 3, 7, 5, 8, 6, 1};
        Arrays.sort(arr);
        int minProd= arr[0]*arr[1];
        int maxProd=arr[arr.length-1] * arr[arr.length-2];
        System.out.println(minProd+"   "+maxProd );


    }
    private static List<Employee> getEmployeeRecord() {
        return Stream.of(
                new Employee(1, "Basant", "DEV", 50000),
                new Employee(8, "santhosh", "DEV", 80000),
                new Employee(3, "pratik", "QA", 60000),
                new Employee(9, "Dipak", "QA", 90000),
                new Employee(4, "Bikash", "DEVOPS", 40000)).collect(Collectors.toList());
    }
}
