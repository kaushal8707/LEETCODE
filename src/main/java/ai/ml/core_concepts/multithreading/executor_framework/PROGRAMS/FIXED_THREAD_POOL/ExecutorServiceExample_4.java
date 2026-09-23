package ai.ml.core_concepts.multithreading.executor_framework.PROGRAMS.FIXED_THREAD_POOL;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class ExecutorServiceExample_4 {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Callable<Integer> callable1 = () -> {
            System.out.println("Task 1");
            return 1;
        };
        Callable<Integer> callable2 = () -> {
            System.out.println("Task 2");
            return 2;
        };
        Callable<Integer> callable3 = () -> {
            System.out.println("Task 3");
            return 3;
        };
        List<Callable<Integer>> list = Arrays.asList(callable1, callable2, callable3);
        List<Future<Integer>> futures = executorService.invokeAll(list);
        for(Future<Integer> future : futures){
            System.out.println(future.get());
        }

        executorService.shutdown();
        System.out.println("Hello world");

       /**Executes the given tasks, returning a list of Futures holding their status and results when all complete.
          Future. isDone is true for each element of the returned list. Note that a completed task could have terminated
          either normally or by throwing an exception. The results of this method are undefined if the given collection
          is modified while this operation is in progress. */

       /** invokeAll() will block main thread and once all task done bcz future then only main thread will resume and print */


    }
}
