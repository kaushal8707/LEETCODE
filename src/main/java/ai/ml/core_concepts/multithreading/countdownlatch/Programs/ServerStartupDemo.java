package ai.ml.core_concepts.multithreading.countdownlatch.Programs;

import java.util.concurrent.CountDownLatch;

public class ServerStartupDemo {
    public static void main(String[] args) {
        // Initialize latch with the number of services to wait for
        CountDownLatch latch = new CountDownLatch(3);

        // Start services in parallel
        new Thread(new Service("DatabaseService", 2000, latch)).start();
        new Thread(new Service("CacheService", 1000, latch)).start();
        new Thread(new Service("MessagingService", 3000, latch)).start();

        try {
            System.out.println("Main Application is waiting for services to initialize...");

            // Main thread blocks here until count reaching zero
            latch.await();

            System.out.println("All services are up. Main Application starting now!");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
