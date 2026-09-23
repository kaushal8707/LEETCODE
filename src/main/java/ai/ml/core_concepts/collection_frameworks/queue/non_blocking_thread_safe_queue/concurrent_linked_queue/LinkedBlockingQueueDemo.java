package ai.ml.core_concepts.collection_frameworks.queue.non_blocking_thread_safe_queue.concurrent_linked_queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/** Blocking Queue - LinkedBlockingQueue */

public class LinkedBlockingQueueDemo {

    private static BlockingQueue<String> taskQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        Thread producer = new Thread(() -> {
            while (true) {
                try{
                    taskQueue.put("Task: "+System.currentTimeMillis());  /** LinkedBlockingQueue - add tasks(will use lock internally)*/
                    System.out.println("Submitted Task : "+System.currentTimeMillis());
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        });
        Thread consumer = new Thread(() -> {
            while (true) {
                try{
                    String task = taskQueue.take();  /** LinkedBlockingQueue - take tasks(will use lock internally)*/
                    System.out.println("processing... : "+task);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        });
        producer.start();
        consumer.start();
    }
}
