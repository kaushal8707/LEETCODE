package ai.ml.core_concepts.multithreading.cyclicbarrier;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class PopulationCalculator {

    // Shared variable to store the total population (thread-safe)
    private static final AtomicInteger totalPopulation = new AtomicInteger(0);
    private static final int NUM_REGIONS = 4;

    public static void main(String[] args) {
        // The barrier action runs after the last thread calls await(),
        // but before all threads are released.
        Runnable sumAction = () -> {
            System.out.println("\n✅ All regional data processed.");
            System.out.println("Final total population calculation: " + totalPopulation.get() + "\n");
            // The barrier automatically resets after this action runs, for cyclic use
        };

        // Initialize the CyclicBarrier with the number of parties (threads)
        // and the barrier action
        CyclicBarrier barrier = new CyclicBarrier(NUM_REGIONS, sumAction);

        ExecutorService executor = Executors.newFixedThreadPool(NUM_REGIONS);

        // Submit tasks for each region
        executor.submit(new RegionalTask(barrier, 50000000, "North"));
        executor.submit(new RegionalTask(barrier, 40000000, "South"));
        executor.submit(new RegionalTask(barrier, 35000000, "East"));
        executor.submit(new RegionalTask(barrier, 60000000, "West"));

        executor.shutdown();
    }

    static class RegionalTask implements Runnable {
        private final CyclicBarrier barrier;
        private final int regionalPopulation;
        private final String regionName;

        public RegionalTask(CyclicBarrier barrier, int population, String regionName) {
            this.barrier = barrier;
            this.regionalPopulation = population;
            this.regionName = regionName;
        }

        @Override
        public void run() {
            try {
                System.out.println(regionName + " region processing started...");
                // Simulate some work/calculation
                Thread.sleep(1000);

                // Add to the total population
                totalPopulation.addAndGet(regionalPopulation);
                System.out.println(regionName + " region completed. Waiting at the barrier.");

                // Wait for all other threads to reach the barrier
                barrier.await();

                System.out.println(Thread.currentThread().getName() + " proceeding after the barrier.");

            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }
}
