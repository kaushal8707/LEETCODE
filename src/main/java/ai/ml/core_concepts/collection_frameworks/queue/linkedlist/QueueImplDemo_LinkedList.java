package ai.ml.core_concepts.collection_frameworks.queue.linkedlist;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class QueueImplDemo_LinkedList {
    public static void main(String[] args) {

        /** Linked List act as a Stack*/
        LinkedList<Integer> linkedList=new LinkedList<>();
        linkedList.addFirst(12);
        linkedList.addFirst(43);
        linkedList.addFirst(33);
        System.out.println(linkedList.removeFirst());
        System.out.println(linkedList);

        System.out.println("------------------------");

        /** Linked List act as a Queue also*/
        LinkedList<Integer> list=new LinkedList<>();
        list.addLast(9);   //enqueue
        list.addLast(12);  //enqueue
        list.addLast(8);   //enqueue
        Integer removedFirst = list.removeFirst();   //dequeue
        System.out.println(removedFirst);
        System.out.println(list);
        System.out.println(list.getFirst());  //peek

        System.out.println("---------------------");

        /** Queue Implementation Class :  LinkedList */

        Queue<Integer> queue = new LinkedList<>();
        queue.add(1);
        System.out.println(queue.size());

        System.out.println(queue.remove());     // throw exception if empty
        System.out.println(queue.poll());       // better - return null if empty
        System.out.println(queue);

        System.out.println(queue.peek());       // better - return null if empty
        //System.out.println(queue.element());  // throw exception if empty
        System.out.println(queue);


        Queue<Integer> queue2 = new ArrayBlockingQueue<>(2);
        System.out.println(queue2.add(1));        // true
        System.out.println(queue2.offer(2));   // true

        System.out.println(queue2.offer(3));   //false
        System.out.println(queue2.add(3));        // throw an exception bcz blockingqueue capacity : 2
    }
}
