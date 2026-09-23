package ai.ml.core_concepts.multithreading.executor_framework.PROGRAMS.CACHE_THREAD_POOL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CachedPoolExample {
    public static void main(String[] args) {
        // Create the cached thread pool
        ExecutorService executor = Executors.newCachedThreadPool();

        for (int i = 0; i < 50; i++) {
            int taskId = i;
            executor.execute(() -> {
                System.out.println("Executing task " + taskId + " on " + Thread.currentThread().getName());
            });
        }

        // Always shut down the executor when finished
        executor.shutdown();
    }
}