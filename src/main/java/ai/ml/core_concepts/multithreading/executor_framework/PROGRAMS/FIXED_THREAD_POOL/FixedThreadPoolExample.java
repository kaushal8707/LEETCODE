package ai.ml.core_concepts.multithreading.executor_framework.PROGRAMS.FIXED_THREAD_POOL;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FixedThreadPoolExample {
    public static void main(String[] args) {

        // Create a fixed thread pool with 4 threads
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        // Submit tasks to the executor
        for(int i=0; i<10; i++){
            final int taskNumber = i;
            executorService.execute(()->{
                System.out.println("Executing task " + taskNumber + " on thread: " + Thread.currentThread().getName());
            });
        }
        // Initiate an orderly shutdown
        executorService.shutdown();
        try{
            // Wait for all tasks to complete for up to 3 seconds
            if(!executorService.awaitTermination(3, TimeUnit.SECONDS)){
                executorService.shutdownNow();   // Force shutdown if tasks don't complete in time
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}
