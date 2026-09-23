package ai.ml.core_concepts.multithreading.executor_framework.PROGRAMS.FIXED_THREAD_POOL;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/** invokeAll() with TimeOut */

public class ExecutorServiceExample_5 {
    public static void main(String[] args) throws ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Callable<Integer> callable1 = () -> {
            Thread.sleep(1000);
            System.out.println("Task 1");
            return 1;
        };
        Callable<Integer> callable2 = () -> {
            Thread.sleep(1000);
            System.out.println("Task 2");
            return 2;
        };
        Callable<Integer> callable3 = () -> {
            Thread.sleep(1000);
            System.out.println("Task 3");
            return 3;
        };
        List<Callable<Integer>> list = Arrays.asList(callable1, callable2, callable3);
        List<Future<Integer>> futures = null;
        try {
            futures = executorService.invokeAll(list, 1, TimeUnit.SECONDS);
        }catch (InterruptedException e) {

        }
        for(Future<Integer> future : futures){
            try {
                System.out.println(future.get());
            } catch (CancellationException e) {

            } catch (InterruptedException e) {

            }
        }

        executorService.shutdown();
        System.out.println("Hello world");

    }
}
