package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.model;
import java.util.ArrayList;
import java.util.List;

public class Users
{
    private int id;
    private String name;
    private int age;
    public Users(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public int getAge() {
        return age;
    }
    public static List<Users> getUsers() {
        List<Users> list = new ArrayList<Users>();
        list.add(new Users(1, "Dinesh", 20));
        list.add(new Users(2, "Kamal", 15));
        list.add(new Users(3, "Vijay", 25));
        list.add(new Users(4, "Ramesh", 30));
        list.add(new Users(5, "Mahesh", 18));
        return list;
    }
}
