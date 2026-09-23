package ai.ml.core_concepts.multithreading.executor_framework.PROGRAMS.SCHEDULED_THREAD_POOL;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** perform task at regular Interval - scheduleAtFixedRate()
 *
 * @NotNull
 * public abstract java. util. concurrent. ScheduledFuture<?> scheduleAtFixedRate(     @NotNull  Runnable command,
 *     long initialDelay,
 *     long period,
 *     @NotNull java. util. concurrent. TimeUnit unit )
 *
 *     */

public class ScheduledExecutorServiceDemo {
    public static void main(String[] args) {

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(
                ()-> System.out.println("Task executed after Every 5 seconds !"),
                5,
                5,
                TimeUnit.SECONDS);
        scheduler.shutdown();
    }
}
