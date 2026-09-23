package ai.ml.core_concepts.multithreading.countdownlatch.Programs;

import java.util.concurrent.CountDownLatch;

// Service class that initializes on a separate thread
class Service implements Runnable {
    private final String serviceName;
    private final int initTime;
    private final CountDownLatch latch;

    public Service(String serviceName, int initTime, CountDownLatch latch) {
        this.serviceName = serviceName;
        this.initTime = initTime;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            // Simulate initialization work
            Thread.sleep(initTime);
            System.out.println(serviceName + " is up and running.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Crucial: Decrement the latch count when done
            latch.countDown();
        }
    }
}
