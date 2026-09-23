package ai.ml.core_concepts.multithreading.thread_communication.programs.THREAD_COMMUNICATION_BLOCKING_QUEUE.PROGRAMS;

import java.util.concurrent.BlockingQueue;

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