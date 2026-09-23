package ai.ml.core_concepts.multithreading.cyclicbarrier.self_evaluation;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class PopulationCalculationService implements Runnable {

    public static final AtomicInteger sumPopulation = new AtomicInteger(0);
    String regionName;
    CyclicBarrier cyclicBarrier;
    int populationCount;

    public PopulationCalculationService(String regionName, CyclicBarrier cyclicBarrier, int populationCount) {
        this.regionName = regionName;
        this.cyclicBarrier = cyclicBarrier;
        this.populationCount = populationCount;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + "calculating population for a region " + regionName);
        System.out.println("population for a region: " + regionName + " is : " + populationCount);
        sumPopulation.addAndGet(populationCount);
        System.out.println(regionName + " region completed. Waiting at the barrier.");

        // Wait for all other threads to reach the barrier
        try {
            cyclicBarrier.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (BrokenBarrierException e) {
            throw new RuntimeException(e);
        }

        System.out.println(Thread.currentThread().getName() + " proceeding after the barrier.");
    }
}
