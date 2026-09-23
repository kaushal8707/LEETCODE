package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.streams;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class highestPaidEmployeeFromDepartment {
    public static void main(String[] args) {
        List<Employees> list = Stream.of(
                        new Employees(1, "Basant", "DEV", 50000),
                        new Employees(8, "santhosh", "DEV", 80000),
                        new Employees(3, "pratik", "QA", 60000),
                        new Employees(9, "Dipak", "QA", 90000),
                        new Employees(4, "Bikash", "DEVOPS", 40000))
                .collect(Collectors.toList());
        list.stream()
                .collect(Collectors.groupingBy(Employees::getDept,
                              Collectors.maxBy(Comparator.comparing(Employees::getSalary))))
                .entrySet()
                .stream()
                .forEach(entry->{
                    System.out.println(entry.getKey()+"   --  "+entry.getValue().get().getName());
                });

    }
}
