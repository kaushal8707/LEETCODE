package ai.ml.core_concepts.multithreading.executor_framework.PROGRAMS.SCHEDULED_THREAD_POOL;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutorServiceDemo2 {
    public static void main(String[] args) {

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(                     // scheduler has task to print every 5 seconds
                ()-> System.out.println("Task executed after Every 5 seconds !"),
                5,
                5,
                TimeUnit.SECONDS);
        scheduler.schedule(()->{
            System.out.println("Initiating Shutdown");
            scheduler.shutdown();
        }, 20, TimeUnit.SECONDS);                    // another task after 20 seconds he has to shutdown
    }
}
