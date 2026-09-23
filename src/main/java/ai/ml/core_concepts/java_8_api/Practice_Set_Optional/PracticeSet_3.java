package ai.ml.core_concepts.java_8_api.Practice_Set_Optional;

import java.util.List;
import java.util.Optional;

/** To handle Null Pointer Exception even we can use in our pojo class for getter method
 *  instead of returning actual value we can give Optional **/

public class PracticeSet_3 {
    public static void main(String[] args) {
        Student student=new Student();
        student.setEmail(null);
        System.out.println(student.getEmail());
    }
}

 class Student {
    private int id;
    private String name;
    private String email;
    private List<String> phoneNumbers;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }
    public void setName(String name) {
        this.name = name;
    }
    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }
    public void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }
    @Override
    public String toString() {
        return "Customer [id=" + id + ", name=" + name + ", email=" + email + ", phoneNumbers=" + phoneNumbers + "]";
    }
}
