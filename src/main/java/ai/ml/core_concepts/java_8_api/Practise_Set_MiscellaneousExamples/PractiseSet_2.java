package ai.ml.core_concepts.java_8_api.Practise_Set_MiscellaneousExamples;

import com.zero.to.hero.java_8_api.Practise_Set_MiscellaneousExamples.models.CurrentEmployee;
import com.zero.to.hero.java_8_api.Practise_Set_MiscellaneousExamples.models.EmployeeDatabase;

import java.util.*;
import java.util.stream.Collectors;

public class PractiseSet_2 {
    public static void main(String[] args) {

        //Q1 - find the Average Salary of those Employee Whose Grade is A
        //Q2 - find the Sum Salary of those Employee Whose Grade is A
        //Q3 - find the second-highest salary
        //Q4 - find the name of Employees whose salary is greater than 50000

        double averageSalaryGardeAEmployee = EmployeeDatabase.getEmployees().stream()
                .filter(emp -> emp.getGrade().equals("A"))
                .mapToDouble(emp -> emp.getSalary())
                .average()
                .getAsDouble();
        System.out.println("Average Salary Grade A Employee : "+averageSalaryGardeAEmployee);

        double totalGradeAEmployeeSalaried = EmployeeDatabase.getEmployees().stream()
                .filter(emp -> emp.getGrade().equals("A"))
                .mapToDouble(emp -> emp.getSalary())
                .sum();
        System.out.println("Total Grade A Employee Salaried :" +totalGradeAEmployeeSalaried);

        CurrentEmployee secondHighestSalariedEmployee = EmployeeDatabase.getEmployees().stream()
                .sorted(Comparator.comparing(CurrentEmployee::getSalary).reversed())
                .skip(1)
                .findFirst()
                .get();
        System.out.println("Second Highest Salaried Employee :"+secondHighestSalariedEmployee);

        List<String> employeeNameSalaryMoreThan50K = EmployeeDatabase.getEmployees().stream()
                .filter(emp -> emp.getSalary() > 50000)
                .map(CurrentEmployee::getName)
                .collect(Collectors.toSet())
                .stream()
                .sorted(Comparator.comparing(String::valueOf).reversed())
                .toList();
        System.out.println("Employee Whose Salaries More Than 50k :"+employeeNameSalaryMoreThan50K);

    }
}
