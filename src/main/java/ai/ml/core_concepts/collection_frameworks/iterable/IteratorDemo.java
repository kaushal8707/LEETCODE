package ai.ml.core_concepts.collection_frameworks.iterable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class IteratorDemo {
    public static void main(String[] args) {


        System.out.println("--------------------------");
        List<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(2);
        numbers.add(3);
        numbers.add(4);
        numbers.add(5);

        for(Integer number : numbers){
            if(number%2==0){
              //  numbers.remove(number);                 // will throw ConcurrentModificationException
            }
        }

        System.out.println("---------------CopyOnWriteArrayList----------------");

        List<Integer> numbersList = new CopyOnWriteArrayList<>();
        numbersList.add(1);
        numbersList.add(2);
        numbersList.add(3);
        numbersList.add(4);
        numbersList.add(5);

        for(Integer number : numbersList){
            if(number%2==0){
                numbersList.remove(number);
            }
        }
        System.out.println(numbersList);

        System.out.println("-------------Iterator remove() method----------------");

        List<Integer> listNums = new ArrayList<>();
        listNums.add(1);
        listNums.add(2);
        listNums.add(3);
        listNums.add(4);
        listNums.add(5);

        Iterator<Integer> itr = listNums.iterator();
        while (itr.hasNext()){
            Integer number = itr.next();
            if(number%2==0){
                itr.remove();
            }
        }
        System.out.println(listNums);


        System.out.println("-------------------list iterator-------------------");

        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);

        ListIterator<Integer> listIterator = list.listIterator(list.size());
        while (listIterator.hasPrevious()){
            Integer number = listIterator.previous();
            System.out.println(number);
        }

//        listIterator.hasPrevious();
//        listIterator.next();
//        listIterator.add(1);
//        listIterator.previous();
//        listIterator.remove();;
//        listIterator.nextIndex();
//        listIterator.previousIndex();
//        listIterator.hasNext();
    }
}
