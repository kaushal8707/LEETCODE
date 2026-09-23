package ai.ml.core_concepts.multithreading.countdownlatch.Programs.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ServerStartupTimeoutDemo {
    public static void main(String[] args) throws InterruptedException {
        // Initialize latch with the number of services to wait for
        CountDownLatch latch = new CountDownLatch(3);

        // Start services in parallel
        new Thread(new TestService("DatabaseService", 5000, latch)).start();
        new Thread(new TestService("CacheService", 1000, latch)).start();
        new Thread(new TestService("MessagingService", 3000, latch)).start();

        System.out.println("Main Application is waiting for services to initialize...");

        // Main thread blocks here until count reaching zero
        boolean completed = latch.await(3000, TimeUnit.MILLISECONDS);
        if(completed){
            System.out.println("all services initialized and started up successfully");
        }else{
            // This block executes if the 5-second limit is reached
            System.err.println("Startup timed out! Some services failed to initialize.");

            // Recovery logic: shutdown the app, alert admin, or start in "degraded" mode
            System.out.println("Remaining service count: " + latch.getCount());
        }
    }
}
