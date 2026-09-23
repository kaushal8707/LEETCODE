package ai.ml.core_concepts.collection_frameworks.queue.priorityqueue;

import java.util.Comparator;
import java.util.PriorityQueue;

public class PriorityQueueDemp {
    public static void main(String[] args) {

        //part of the Queue Interface
        //If priority come then normal Linked List will not work
        //orders elements based on their natural ordering (for primitives lowest first)
        //custom comparator for customized ordering
        //the head element or front element should be on a high priority we don't care about other queues elements.
        //does not allow null elements

        PriorityQueue<Integer> priorityQueue = new PriorityQueue<>();
        priorityQueue.add(15);
        priorityQueue.add(10);
        priorityQueue.add(30);
        priorityQueue.add(5);

        /** we want first element*/
//        System.out.println(priorityQueue.peek());
//        priorityQueue.remove();
//        System.out.println(priorityQueue.peek());

        System.out.println(priorityQueue);          // not sorted

        /** we don't care about queue other element's in which order they are in queue
         * we just worry about the head on priority element
          */

        while(!priorityQueue.isEmpty()){
            System.out.println(priorityQueue.poll());
        }

        System.out.println("==============================");
        /** If we want to give our own order in a priority queue */
        PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.reverseOrder());
        pq.add(15);
        pq.add(10);
        pq.add(30);
        pq.add(5);
        while(!pq.isEmpty()){
            System.out.println(pq.poll());
        }

        System.out.println("=============================");
        /** If we want to give our custom order while custom object in a priority queue */
                                                                   //(3.9, 3.7) -> 3.9-3.7 -> +ve -> left move right and it will print in ascending order
                                                                   //so, 3.7-3.9 -> -ve  -> it will print in ascending order and no need to move element
        Comparator<Student> comparator = (s1, s2)-> {              //want to give priority for high gpa, if gpa equals it will
                if(s2.getGpa()-s1.getGpa()>0){
                    return 1;                                       //take name on alphabetical order
                }else if(s2.getGpa()-s1.getGpa()<0){
                    return -1;
                }else {
                    return s1.getName().compareTo(s2.getName());    //(alice,charlie) -> a-b-> -ve-> ascending order
                }
        };

        PriorityQueue<Student> priorityQueues = new PriorityQueue<>(comparator);

        priorityQueues.add(new Student("Charlie", 3.5));
        priorityQueues.add(new Student("Bob", 3.7));
        priorityQueues.add(new Student("Alice", 3.5));
        priorityQueues.add(new Student("Akshit", 3.9));
        while(!priorityQueues.isEmpty()){
            System.out.println(priorityQueues.poll());
        }
    }
}
