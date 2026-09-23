package ai.ml.core_concepts.multithreading.executor_framework.PROGRAMS.FIXED_THREAD_POOL;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
/** cancel(), isCancel(), isDone()  from Future methods */
public class ExecutorServiceExample_8 {
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

         future.cancel(true);
        //future.cancel(false);
        System.out.println(future.isCancelled());
        System.out.println(future.isDone()); /** Returns true if this task completed.
                                                Completion may be due to normal termination, an exception, or cancellation --
                                                in all of these cases, this method will return true.*/
        executorService.shutdown();
    }
}
