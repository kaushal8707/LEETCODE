package ai.ml.core_concepts.multithreading.executor_framework.PROGRAMS.SCHEDULED_THREAD_POOL;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerExample {
    public static void main(String[] args) {

        // 1. Create a pool with 2 threads
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        // 2. Task to run after a 5-second initial delay
        scheduler.schedule(() -> System.out.println("one-time task executed"), 5, TimeUnit.SECONDS);

        // 3. Task to run every 3 seconds (Fixed Rate)
        // Starts at 0, then 3, 6, 9...
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("fixed rate task " + System.currentTimeMillis() / 1000);
        }, 0, 5, TimeUnit.SECONDS);

        // 4. Task with 4 seconds delay between executions (Fixed Delay)
        // Finish + 4s -> Start next
        scheduler.scheduleWithFixedDelay(()->{
            System.out.println("fixed delay task start");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
            System.out.println("fixed delay end");
        }, 0, 5, TimeUnit.SECONDS);
    }
}