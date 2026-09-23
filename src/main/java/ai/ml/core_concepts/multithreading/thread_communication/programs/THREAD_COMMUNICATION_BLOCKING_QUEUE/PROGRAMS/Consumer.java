package ai.ml.core_concepts.multithreading.thread_communication.programs.THREAD_COMMUNICATION_BLOCKING_QUEUE.PROGRAMS;

import java.util.concurrent.BlockingQueue;

class Consumer implements Runnable {
    private BlockingQueue<Integer> queue;
    public Consumer(BlockingQueue<Integer> queue){
        this.queue=queue;
    }
    @Override
    public void run() {
        while (true) {
            try {
                Integer value = queue.take();
                System.out.println("Consumer consumed: - " + value);
                Thread.sleep(1000);

            } catch (Exception e) {
                Thread.currentThread().interrupt();
                System.out.println("Consumer Interrupted !!");
            }
        }
    }
}