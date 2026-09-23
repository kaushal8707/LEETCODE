package ai.ml.core_concepts.collection_frameworks.list.arraylist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ComparatorDemo_Impl {
    public static void main(String[] args) {

        List<Integer> list = new ArrayList<>();
        list.add(2);
        list.add(1);
        list.add(3);

        //It will sort in natural order
        //list.sort(null);

        //custom sorting
        list.sort(new MyComparator());
        System.out.println(list);


        List<String> words = Arrays.asList("banana", "apple", "date");
        words.sort(null);
        System.out.println(words);


        /** sort based on length of String */
        List<String> words1 = Arrays.asList("banana", "apple", "date");
        //words1.sort(new StringLengthComparator());

        /** using java 8 comparator lambda expression */
        words1.sort((o1, o2)-> o2.length()-o1.length());
        System.out.println(words1);

        /** sort based on Custom object fields */
        List<Student> students = new ArrayList<>();
        students.add(new Student("Charlie", 3.5));
        students.add(new Student("Bob", 3.7));
        students.add(new Student("Alice", 3.5));
        students.add(new Student("Akshit", 3.9));

        /** we want sort in based on grade in descending order
         *    3.9    3.7
         *    o1     o2
         *                 o1-o2  ->  3.9-3.7 -> +ve  so 3.7 come before and 3.9 come after (ascending)
         *                 o2-o1  ->  3.7-3.9 -> -ve   so, 3.9 come before and 3.7 come after descending)
         * */
        students.sort((o1, o2)-> {
            if(o2.getGpa()-o1.getGpa() > 0)
                return 1;
            else if(o2.getGpa()-o1.getGpa() < 0)
                return -1;
            else
                return 0;
        });
        System.out.println(students);


        /** java 8 comparing method */
        Comparator<Student> studentComparator = Comparator.comparing(Student::getGpa);
        students.sort(studentComparator);
        System.out.println(students);


        Comparator<Student> student1Comparator = Comparator.comparing(Student::getGpa).reversed();
        students.sort(student1Comparator);
        System.out.println(students);


        /** first descending order on grade then ascending order on name if grade are equal*/
        Comparator<Student> student2Comparator = Comparator.comparing(Student::getGpa).reversed().thenComparing(Student::getName);
        students.sort(student2Comparator);
        System.out.println(students);

        /** same we can do in if else sort on grade descending order while
         * 2 grade equals sort name based on ascending order
         **/
        students.sort((o1, o2)-> {     // want grade in descending
                                       // 3.9   3.7   -> 3.9-3.7 -> +ve  ->3.9 go right 3.7 come left
                                       // -> ascending order 3.7  3.9  -> so need o2-o1 here
              if(o2.getGpa() - o1.getGpa() > 0)
                  return 1;
              else if(o2.getGpa() - o1.getGpa() < 0)
                  return -1;
              else
                  //return 0;
                   /** name - ascending */
                  //return o1.getName().compareTo(o2.getName());                            //we want alice charlie

                /** name - descending */
                return o2.getName().compareTo(o1.getName());
        });
        System.out.println(students);


    }

    static class StringLengthComparator implements Comparator<String>{
        //we just have 2 think abt 2 obj rest all comparator can do
        // I want in ascending order so first date(4), apple(5), banana(6)
        //if we take date(4)   apple(5)
        //             o1        o2  ->   o1-o2 ->  4-5  -> -1(-ve) so it will be in ascending order
        //                           ->   o2-o1 ->  5-4  -> +1(+ve) so date go right apple come first so descending order
        @Override
        public int compare(String o1, String o2) {
            /**ascending order*/
            //return o1.length()-o2.length();

            /** descending order*/
            return o2.length()-o1.length();
        }
    }

    static class MyComparator implements Comparator<Integer>{
        // we just have to think about 2 obj rest all comparator can do
        // I want in ascending order so first 1, 2, 3
        // if we take    1        2
        //             o1        o2  ->   o1-o2 ->  1-2  -> -1(-ve) so it will be in ascending order
        //                           ->   o2-o1 ->  2-1  -> +1(+ve) so 1 go right 2 come first so descending order
        @Override
        public int compare(Integer o1, Integer o2) {
            /**ascending order*/
            //return o1-o2;

            /** descending order*/
            return o2-o1;
        }
    }
}
