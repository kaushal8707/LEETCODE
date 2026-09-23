package ai.ml.core_concepts.multithreading.executor_framework.PROGRAMS.FIXED_THREAD_POOL;

import java.util.concurrent.*;

public class ExecutorServiceExample_7 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Integer> future = executorService.submit(() -> {
            try{
                Thread.sleep(2000);
            }catch (InterruptedException e){
                System.out.println("Exception occurred "+e);
            }
            return 42;
        });

        Integer i=null;
        try {
            i = future.get(1, TimeUnit.SECONDS);  // If result come within 1 sec then fine otw timeout exception will throw.
            System.out.println(future.isDone());
            System.out.println(i);
        }catch (TimeoutException | InterruptedException | ExecutionException e){
            System.out.println("Exception occurred "+e);
        }

    }
}
