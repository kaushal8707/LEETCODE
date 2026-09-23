package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.streams;

import java.util.Arrays;
import java.util.List;

public class EmployeeDB {
    public static List<Employees> getEmployees(){
        return Arrays.asList(new Employees(12,"kaushal","Software",1234500),
                new Employees(28,"prakash","Mechanical",334651),
                new Employees(65,"sohan","Electronic",912736),
                new Employees(39,"vinay","Hardware",451271),
                new Employees(81,"dipesh","HR",874651)
        );
    }
}
