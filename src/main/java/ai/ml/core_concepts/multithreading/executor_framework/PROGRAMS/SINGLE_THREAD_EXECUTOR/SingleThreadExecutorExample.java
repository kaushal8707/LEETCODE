package ai.ml.core_concepts.multithreading.executor_framework.PROGRAMS.SINGLE_THREAD_EXECUTOR;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SingleThreadExecutorExample {
    public static void main(String[] args) {
        // Create a single-threaded executor
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Submit tasks for execution
        for (int i = 0; i < 5; i++) {
            final int taskNumber = i;
            executor.submit(() -> {
                System.out.println("Executing task " + taskNumber + " on thread: " + Thread.currentThread().getName());
                try {
                    // Simulate some work
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Initiate an orderly shutdown
        executor.shutdown();
        try {
            // Wait for all tasks to complete
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Force shutdown if tasks don't finish in time
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
