package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.streams;
public class User
{
    private String userName;
    private int age;
    private float salary;

    public User(String userName, int age, float salary) {
        this.userName = userName;
        this.age = age;
        this.salary = salary;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public float getSalary() {
        return salary;
    }

    public void setSalary(float salary) {
        this.salary = salary;
    }

    public void showData()
    {
        System.out.println(userName+"  "+age);
    }
    public static void viewRecord()
    {
        System.out.println("inside static ref");
    }
}
