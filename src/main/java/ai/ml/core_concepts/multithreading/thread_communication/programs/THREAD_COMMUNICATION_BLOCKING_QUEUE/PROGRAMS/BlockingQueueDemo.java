package ai.ml.core_concepts.multithreading.thread_communication.programs.THREAD_COMMUNICATION_BLOCKING_QUEUE.PROGRAMS;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueDemo {
    public static void main(String[] args) {

        /**
         *        1. thread - safe Queue
         *        2. wait for queue to become non-empty/wait for space
         *                         //(Blocking queue waits for 2 things Either in a queue
         *                          //there is no space so that he can put element into and need to wait to pick element from queue so that
         *                          //if any element will come he will pick.
         *        3. simplify concurrency problems like producer-consumer
         *        4. standard queue -- Immediately
         *              =>empty -- remove() -- no waiting
         *              =>full  -- add() -- no waiting
         *        5. Blocking Queue
         *              =>put -- Block If the queue is full un-till space becomes available
         *              =>take -- Block If the queue is empty unless an element become available
         *              =>offer -- waits for space to become available, upto the specified timeout
         */


        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);  //A bounded, blocking queue backed by an Array
        Thread producer = new Thread(new Producer(queue));
        Thread consumer = new Thread(new Consumer(queue));
        producer.start();
        consumer.start();
    }
}