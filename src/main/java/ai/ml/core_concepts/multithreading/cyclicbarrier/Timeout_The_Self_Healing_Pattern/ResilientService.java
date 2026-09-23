package ai.ml.core_concepts.multithreading.cyclicbarrier.Timeout_The_Self_Healing_Pattern;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ResilientService {
    private static final int THREAD_COUNT = 3;
    private static CyclicBarrier barrier;
    private static final AtomicInteger cycleCount = new AtomicInteger(1);

    public static void main(String[] args) {
        initBarrier();

        // Start 3 worker threads
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int id = i;
            new Thread(() -> runTask(id)).start();
        }
    }

    private static void initBarrier() {
        barrier = new CyclicBarrier(THREAD_COUNT, () -> {
            System.out.println(">>> Phase " + cycleCount.getAndIncrement() + " complete! <<<");
        });
    }

    private static void runTask(int id) {
        while (true) {
            try {
                // Simulate work
                System.out.println("Thread-" + id + " is working...");

                // Intentionally make Thread-2 lag during Phase 1 to trigger a timeout
                if (id == 2 && cycleCount.get() == 1) {
                    Thread.sleep(10000);
                } else {
                    Thread.sleep(1000);
                }

                System.out.println("Thread-" + id + " reached barrier.");

                // 1. TIMED WAIT: Prevent infinite blocking
                barrier.await(3, TimeUnit.SECONDS);

            } catch (TimeoutException e) {
                System.err.println("Thread-" + id + ": Timeout! Breaking barrier.");
                handleRecovery();
            } catch (BrokenBarrierException e) {
                System.err.println("Thread-" + id + ": Barrier broken by another thread. Retrying...");
                handleRecovery();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // 2. SYNCHRONIZED RECOVERY: Ensures only one thread resets the barrier
    private static synchronized void handleRecovery() {
        if (barrier.isBroken()) {
            System.out.println("--- System Recovery: Resetting barrier for retry ---");
            barrier.reset();
        }
    }
}
