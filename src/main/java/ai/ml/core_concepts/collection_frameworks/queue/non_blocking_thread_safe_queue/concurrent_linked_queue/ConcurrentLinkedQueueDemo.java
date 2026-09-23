package ai.ml.core_concepts.collection_frameworks.queue.non_blocking_thread_safe_queue.concurrent_linked_queue;

import java.util.concurrent.ConcurrentLinkedQueue;

/** Non-Blocking Queue - ConcurrentLinkedQueue */

public class ConcurrentLinkedQueueDemo {

    private static ConcurrentLinkedQueue<String> taskQueue = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        Thread producer = new Thread(() -> {
            while (true) {
                try{
                    taskQueue.add("Task: "+System.currentTimeMillis());
                    System.out.println("Submitted Task : "+System.currentTimeMillis());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        Thread consumer = new Thread(() -> {
            while (true) {
                try{
                    String task = taskQueue.poll();
                    System.out.println("processing... : "+task);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        producer.start();
        consumer.start();
    }

}
