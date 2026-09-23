package ai.ml.core_concepts.collection_frameworks.queue.dequeue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;

public class DeQueueDemoImpl {
    public static void main(String[] args) {
        // double ended queue
        // allows insertion and deletion of elements from both ends.
        // versatile than regular queues and stacks because they support all the operations of both

        /**
         * INSERTIONS METHODS
         *
         * addFirst(E e) - Inserts the specified element at the front.
         * addLast(E e) - Inserts the specified element at the end.
         * offerFirst(E e) - Inserts the specified element at the front if possible.
         * offerLast(E e) - Inserts the specified element at the end If possible.
         *
         */

        /**
         * REMOVAL METHODS
         *
         * removeFirst() - Retrieves and removes the first element, or throw exception If empty.
         * removeLast() - Retrieves and removes the last element, or throw exception If empty.
         * pollFirst() - Retrieves and removes the first element, or return null If empty.
         * pollLast() - Retrieves and removes the last element, or return null If empty.
         *
         */

        /**
         * EXAMINATION METHODS
         *
         * getFirst() - Retrieves, but does not remove the first element.
         * getLast() - Retrieves, but does not remove the first element.
         * peekFirst() - Retrieves, but does not remove, the first element. or return null If empty.
         * peekLast() - Retrieves, but does not remove, the last element. or return null If empty.
         *
         */


        // Array Deque : This class is likely to be faster than Stack when used as a stack,
                         //and faster than LinkedList when used as a queue.

        //ArrayDeque & LinkedList both are a double-ended Queue.

        /** Deque Implementation - ArrayDeque */
        Deque<Integer> deque1 = new ArrayDeque<>();    // ArrayDeque- Faster Access bcz of contiguous memory allocation,
                                                       // low memory bcz no store pointer, no null allowed ...recommended to use ArrayDeque instead of Linked List
                                                        // circular head and tail
                                                        // no need to shift element just shift head and tail...

        deque1.addFirst(10);
        deque1.addLast(20);
        deque1.offerFirst(5);
        deque1.offerLast(25);

        System.out.println(deque1);
        System.out.println("First Element - "+deque1.getFirst());
        System.out.println("Last Element - "+deque1.getLast());
        deque1.removeFirst();
        deque1.pollLast();
        for(int i:deque1){
            System.out.println(i);
        }

        System.out.println("--------------------------");
        /** Deque Implementation - LinkedList */
        Deque<Integer> deque2 = new LinkedList<>();

        deque2.addFirst(10);
        deque2.addLast(20);
        deque2.offerFirst(5);
        deque2.offerLast(25);

        System.out.println(deque2);
        System.out.println("First Element - "+deque2.getFirst());
        System.out.println("Last Element - "+deque2.getLast());
        deque2.removeFirst();
        deque2.pollLast();
        for(int i:deque2){
            System.out.println(i);
        }




    }
}
