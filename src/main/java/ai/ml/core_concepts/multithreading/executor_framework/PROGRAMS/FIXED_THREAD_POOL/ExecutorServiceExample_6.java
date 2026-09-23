package ai.ml.core_concepts.multithreading.executor_framework.PROGRAMS.FIXED_THREAD_POOL;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** invokeAny()*/

public class ExecutorServiceExample_6 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
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
        Integer i = executorService.invokeAny(list);// Executes the given tasks, returning the result of one that has completed successfully (i. e., without throwing an exception), if any do. Upon normal or exceptional return, tasks that have not completed are cancelled.
                                                    // The results of this method are undefined if the given collection is modified while this operation is in progress.
                                                    // It will give directly Integer not Future
        System.out.println(i);
        executorService.shutdown();
        System.out.println("Hello world");

    }
}
