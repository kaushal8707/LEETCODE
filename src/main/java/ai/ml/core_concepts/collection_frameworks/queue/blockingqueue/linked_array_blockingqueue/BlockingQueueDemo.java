package ai.ml.core_concepts.collection_frameworks.queue.blockingqueue.linked_array_blockingqueue;

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

class Producer implements Runnable {
    private BlockingQueue<Integer> queue;
    private int value=0;
    public Producer(BlockingQueue<Integer> queue){
        this.queue=queue;
    }
    @Override
    public void run() {
        while(true){
            try{
                System.out.println("Producer produced: - "+value);
                queue.put(value++);
                Thread.sleep(500);

            }catch (Exception e){
                Thread.currentThread().interrupt();
                System.out.println("Producer Interrupted !!");
            }
        }
    }
}

class Consumer implements Runnable {
    private BlockingQueue<Integer> queue;
    public Consumer(BlockingQueue<Integer> queue){
        this.queue=queue;
    }
    @Override
    public void run() {
        while(true){
            try{
                Integer value = queue.take();
                System.out.println("Consumer consumed: - "+value);
                Thread.sleep(1000);

            }catch (Exception e){
                Thread.currentThread().interrupt();
                System.out.println("Consumer Interrupted !!");
            }
        }
    }
}
