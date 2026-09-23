package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.streams;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class sortinglistcustomobject {
    public static void main(String[] args) {
        //sort employee object based on salary and get lowest salary employee object
        Employees emp=EmployeeDB.getEmployees()
                .stream().sorted(Comparator.comparing(Employees::getSalary))
                .findFirst()
                .get();
        System.out.println("lowest salary employee = "+emp);

        //sort employee object based on name
        EmployeeDB.getEmployees()
                .stream()
                .sorted(Comparator.comparing(Employees::getName))
                .collect(Collectors.toList())
                .forEach(System.out::println);

        //find second highest salary employee
        String secondHighestSalary=EmployeeDB.getEmployees()
                .stream()
                .sorted(Comparator.comparing(Employees::getSalary).reversed())
                .skip(1)
                .findFirst()
                .map(Employees::getName)
                .get();
        System.out.println("second highest salary employee "+secondHighestSalary);


        //sort based on lambda expression or using Comparator
        List<Employees> list=EmployeeDB.getEmployees();
        Collections.sort(list, (emp1, emp2)->emp1.getDept().compareTo(emp2.getDept()));
        for(Employees employee:list){
            System.out.println("Sort Based on Name Using Lambda Expression"+employee);
        }

    }
}
