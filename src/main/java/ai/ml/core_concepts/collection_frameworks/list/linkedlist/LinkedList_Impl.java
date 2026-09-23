package ai.ml.core_concepts.collection_frameworks.list.linkedlist;

import java.util.Arrays;
import java.util.LinkedList;

public class LinkedList_Impl {
    public static void main(String[] args) {

        LinkedList<Integer> linkedList = new LinkedList<>();
        linkedList.add(1);
        linkedList.add(2);
        linkedList.add(3);

        linkedList.get(2);  /**  O(n) bcz to access each node has to traverse from starting of head node
                                    and need to reach desired location and find equal element and access it*/
        linkedList.addFirst(0);  /** O(1)  but in Array List O(n) bcz you need to shift all element right side*/
        linkedList.addLast(4);   /** O(1)  bcz It's a Double ended Linked List because we have a
                                    Pointer on Last element so that only
                                    we can do getLast(), removeLast(), addLast().... */

        linkedList.getFirst();
        linkedList.getLast();      /** O(1)  bcz It's a Double ended Linked List because we have a
                                     Pointer on Last element so that only
                                     we can do getLast(), removeLast(), addLast()....
                                     /** If Java Linked list would be a single linked list we can't perform this operation simple
                                     then we can't perform opertaion on O(1) it take the O(n) */

        System.out.println(linkedList);

        linkedList.removeIf(x-> x%2==0);
        System.out.println(linkedList);

        LinkedList<String> animals = new LinkedList<>(Arrays.asList("Dog", "Elemphent", "Cat"));
        LinkedList<String> animalsToRemove = new LinkedList<>(Arrays.asList("Lion", "Cat"));
        animals.removeAll(animalsToRemove);
        System.out.println(animals);


       /** If Java Linked list would be a single linked list we can't perform this operation simple*/







    }
}
