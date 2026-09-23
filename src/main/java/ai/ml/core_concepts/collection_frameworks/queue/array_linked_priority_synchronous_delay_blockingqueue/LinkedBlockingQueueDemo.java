package ai.ml.core_concepts.collection_frameworks.queue.array_linked_priority_synchronous_delay_blockingqueue;

import java.util.Comparator;
import java.util.concurrent.*;

public class LinkedBlockingQueueDemo {
    public static void main(String[] args) {

        /** ArrayBlockingQueue
         *
         * A bounded, blocking queue backed by circular array so no shifting element
         * low memory overhead bcz simple array
         * uses a single lock for both enqueue and dequeue operations
         * more threads -> problems          ->need to wait more
         * use ArrayBlockingQueue when we have less threads
         */
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);  //Always keep bounded in case of un-bounded out of memory may come


        /** LinkedBlockingQueue
         *
         * Optionally bounded, backed by Linked List
         * uses two separate locks for both enqueue and dequeue operations
         * Higher Concurrency between producer and consumer
         * use LinkedBlockingQueue when we have more threads
         */
        BlockingQueue<Integer> queue1 = new LinkedBlockingQueue<>(); // if not define capacity then Creates a
                                                                    // LinkedBlockingQueue with a capacity of Integer.MAX_VALUE.

        /** PriorityBlockingQueue
         * Un-Bounded
         * It is same as Priority Queue
         * Internally It use Binary Heap as Array and can grow dynamically
         * Elements are not in ordered inside a queue but head element on natural ordering like a Priority queue
         * or a Provided Comparator -> In Priority Queue elements was not in order
         * we were getting prioritized element at head but here elements are in natural order
         * In producer-Consumer If anyone have a priority then also we can take in account, which mean on priority also we can consume elements
         * put(), won't block.Since Its unbounded so PUT method woun't block because always space will be available.
         * If you want to give Comparator then you must have to define initial capacity with comparator defined.
         *
         */

        BlockingQueue<String> queue2 = new PriorityBlockingQueue<>(11, Comparator.reverseOrder()); //Creates a PriorityBlockingQueue with the default
                                                                      // initial capacity (11) that orders its elements according
                                                                      // to their natural ordering.
        queue2.add("apple");
        queue2.add("banana");
        queue2.add("cherry");
        System.out.println(queue2);


        /** SynchronousQueue
         * Creates a SynchronousQueue with non-fair access policy, synchronous means access will be in order
         * each insert operation must wait for a corresponding remove operation by another thread and vice-versa
         * so one insert happen then next insert won't allow till the first element get removed
         * which means it can't store 2nd elements till first get remove, capacity of at most one element
         * it can't store element, capacity of at most one element.
         *
         */

        BlockingQueue<Integer> queue3 = new SynchronousQueue<>();



    }
}
