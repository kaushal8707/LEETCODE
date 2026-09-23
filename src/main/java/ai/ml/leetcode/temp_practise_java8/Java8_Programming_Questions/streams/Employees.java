package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.streams;

import java.util.ArrayList;
import java.util.List;

public class Employees {
    private int id;
    private String name;
    private String dept;
    private double salary;

    public Employees(int id, String name, String dept, double salary) {
        this.id = id;
        this.name = name;
        this.dept = dept;
        this.salary = salary;
    }

    public static List<Employees> getEmpList(){
        List<Employees> list = new ArrayList<>();
        list.add(new Employees(1, "A", "Dep1",2000));
        list.add(new Employees(2, "B", "Dep2", 3000));
        list.add(new Employees(3, "C", "Dep3",4000));
        list.add(new Employees(4, "D", "Dep4",5000));
        return list;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }
}