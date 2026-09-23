package ai.ml.core_concepts.collection_frameworks.list.arraylist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WaysOfCreatingArrayList {
    public static void main(String[] args) {

        List<String> list = new ArrayList<>();
        System.out.println(list.getClass().getName());

        /** On the fly create a list - Arrays.asList() *
         *
         * Instead of List can we return ArrayList which is not possible
         * bcz It return Parent(List) and we are trying to give Child(ArrayList) which is not possible
         *
         * asList() always returns a fixed size List if you are trying to add / remove will throw
         * java.lang.UnsupportedOperationException bcz it return java.util.Arrays$ArrayList not ArrayList
         */
        List<String> list1 = Arrays.asList("Monday", "Tuesday");
        System.out.println(list1.getClass().getName());
        //list1.add("asd");
        System.out.println(list1);

        /**
         * asList() always returns a fixed size List if you are trying to add / remove will throw
         * java.lang.UnsupportedOperationException but you can replace the element using set()
         */

        String[] array = {"Apple", "Banana", "Cherry"};
        List<String> list2 = Arrays.asList(array);
        System.out.println(list2.getClass().getName());

        list2.set(2, "Cadbury");
        //list2.add("asd");
        System.out.println(list2);


        /**  List.of()
         *
         * It returns UnModifiable List
         *
         * difference between asList() & of(0 is at least in asList() returned List you can replace the elements
         * but of() returned List you can't even replace/add/remove so no operation can perform after create
         * if try will get UnsupportedOperationException
         *
         * */
        List<Integer> list3 = List.of(1, 2, 3, 4);
        //list3.set(2, 7);


        /**
         * If you want to make it Modifiable of() or asList() you can pass the collection object in arraylist
         * constructor.
         *
         * In constructor either we can pass initial capacity or collection(list, set...)
         */
        List<String> list4=new ArrayList<>(list2);
        list4.add("Mango");
        System.out.println(list4);


        /**
         *
         * while remove element from Array List it takes input either Index or element
         * but if we try to remove element 1 it's removing element 2
         * the reason is Intellij removing the element which is on index 1 which is element 2
         * so If you want to remove you have to create a Wrapper class then pass it
         */
        List<Integer> list5=new ArrayList<>();
        list5.add(1);
        list5.add(2);
        list5.add(3);

        list5.remove(Integer.valueOf(1));
        System.out.println(list5);

        /**
         * How we can convert List to Array using toArray() method
         * By default it return Object array so to convert in Specific type you can use
         * new String[0]/new Integer[0] just to inform either array is type string or integer
         *
         */
        List<Integer> list6=new ArrayList<>();
        list6.add(1);
        list6.add(2);
        list6.add(3);
        Object[] array1 = list6.toArray();
        Integer[] array2 = list6.toArray(new Integer[0]);


        /**
         * To sort List can use Collections.sort(list) which not return anything just void
         * but Instead of giving Collections.sort() we can use
         * list.sort(null) as well where null is a Comparator Input
         *
         * Comparator is an Interface with help of that we can do Custom Ordering.
         */
        List<Integer> list7=new ArrayList<>();
        list7.add(2);
        list7.add(4);
        list7.add(3);

        //Collections.sort(list7);
        list7.sort(null);
        System.out.println(list7);



    }
}
