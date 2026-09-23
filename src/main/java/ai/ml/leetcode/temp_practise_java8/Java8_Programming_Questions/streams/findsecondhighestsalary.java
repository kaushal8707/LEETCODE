package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.streams;

import java.util.Comparator;

public class findsecondhighestsalary {
    public static void main(String[] args) {
        Employee1 highestSal=EmployeeDatabase.getEmployees()
                .stream().sorted(Comparator.comparing(Employee1::getSalary).reversed())
                .skip(1)
                .findFirst().get();
        System.out.println("findsecondhighestsalary = "+highestSal);
    }
}
