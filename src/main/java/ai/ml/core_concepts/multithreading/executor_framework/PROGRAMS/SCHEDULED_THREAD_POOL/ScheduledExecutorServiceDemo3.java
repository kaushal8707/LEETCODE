package ai.ml.core_concepts.multithreading.executor_framework.PROGRAMS.SCHEDULED_THREAD_POOL;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutorServiceDemo3 {
    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        scheduler.scheduleAtFixedRate(()->{
            System.out.println("Taxk running....");
        }, 0, 2, TimeUnit.SECONDS);
        try {
            if(!scheduler.awaitTermination(3, TimeUnit.SECONDS)){
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}
