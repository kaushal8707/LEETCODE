package ai.ml.core_concepts.collection_frameworks.map.comparable;

import java.util.ArrayList;
import java.util.List;

public class ComparableDemo {
    public static void main(String[] args) {

        List<Student> list=new ArrayList<>();
        list.add(new Student("Charlie", 3.5));
        list.add(new Student("Bob", 3.7));
        list.add(new Student("Alice", 3.5));
        list.add(new Student("Akshit", 3.9));

        list.sort(null);
        System.out.println(list);


        List<Integer> list1=new ArrayList<>();
        list1.add(11);
        list1.add(6);
        list1.add(14);
        list1.add(2);

//        list1.sort(null);
//        System.out.println(list1);

    }
}
