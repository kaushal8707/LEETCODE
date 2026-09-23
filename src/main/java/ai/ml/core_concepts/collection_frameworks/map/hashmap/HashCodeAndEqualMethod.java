package ai.ml.core_concepts.collection_frameworks.map.hashmap;

import java.util.HashMap;
import java.util.Objects;

public class HashCodeAndEqualMethod {
    public static void main(String[] args) {

        HashMap<Student, String> map=new HashMap<>();
        Student s1=new Student("Alice", 1);
        Student s2=new Student("Bob", 2);
        Student s3=new Student("Alice", 1);  // here s1 is same student as s3

        /** now we have 3 objects but actually we have only 2 student */

        map.put(s1, "Engineer");  // hashcode1 --> index1
        map.put(s2, "Designer");  // hashcode2 --> index2
        map.put(s3, "Manager");   // hashcode3 --> index3    --> equals   --> replace

        System.out.println("Hash Map size "+map.size());
        System.out.println("Value for s1 - "+map.get(s1));
        System.out.println("Value for s3 - "+map.get(s3));


        /** Now we want s3 which is Alice only his designation replace with manager
         * But will it work
         *
         * as we know hashcode will get generate from that we can find Index for a key, here key is student
         *
         * Because hashcode definition we have not given. Student extend Object class then inside Object class hashcode.
         *
         * so, with the help of Memory Address hashcode generated. bcz we have used here new keyword so hashcode will always be different.
         * so, both object will be place on a different Index(Bucket)
         * so, Inside Map there will be a 3 entries.
         *
         *
         * */

        HashMap<String, Integer> map1=new HashMap<>();
        map1.put("Shubham", 90);  // hashcode1 --> index1
        map1.put("Neha", 80);     // hashcode2 --> index2
        map1.put("Shubham", 99);  // hashcode2 --> index2   --> equals()  --> replace


    }
}

class Student {

    private String name;
    private int id;

    public Student(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public boolean equals(Object obj) {

        if(obj==null) {
            return false;   /** If object which you sent is null return false
                                current object you are checking with null */
        }
        if(this==obj) {
            return true;     /** If checking same object return true */
        }
        if(this.getClass()!=obj.getClass()){
            return false;   /** you are checking a 2 instances of different class */
        }

        Student other = (Student) obj;

        return this.id==other.getId() && Objects.equals(this.name, other.getName());
    }

    @Override
    public String toString() {
        return "id: "+ id +", name: "+ name;
    }
}
