package ai.ml.core_concepts.collection_frameworks.queue.array_linked_priority_synchronous_delay_blockingqueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.*;

public class PriorityBlockingQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue queue = new PriorityBlockingQueue();
        queue.put(4);
        queue.put(6);
        queue.put(11);
        queue.put(5);
        queue.put(2);
        while(!queue.isEmpty()){
            System.out.println(queue.take());
        }
        System.out.println("-------------------");
        BlockingQueue queue1 = new PriorityBlockingQueue(2, Comparator.reverseOrder());
        queue1.put(4);
        queue1.put(6);
        queue1.put(11);
        queue1.put(5);
        queue1.put(2);
        while(!queue1.isEmpty()){
            System.out.println(queue1.take());
        }

        System.out.println("-----------------------");
        BlockingQueue queue2 = new PriorityBlockingQueue();
        ArrayList polledELements = new ArrayList();
        queue2.put("hare");
        queue2.put("asdf");
        queue2.put("oijhgf");
        queue2.put("zxcvb");
        queue2.put("tafsy");
        queue2.drainTo(polledELements);
        System.out.println(polledELements);

        System.out.println("------------------");
        BlockingQueue queue3 = new PriorityBlockingQueue();
        Thread t = new Thread(()->{
            try {
                System.out.println("polling from queue now");
                Object polledElement = queue3.take();
                System.out.println("polled from queue :" +polledElement);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t.start();
        System.out.println("adding to queue now");
        queue3.put("Hello");


        System.out.println("---------------------");
        BlockingQueue queue4 = new PriorityBlockingQueue();
        Thread pThread = new Thread(()->{
            System.out.println("Polling.");
            while(true){
                try {
                    Object polledElements = queue4.take();
                    System.out.println("Polled "+polledElements);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        pThread.start();
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        queue4.addAll(Arrays.asList(1,5,6,1,2,6,7));
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
    }
}
