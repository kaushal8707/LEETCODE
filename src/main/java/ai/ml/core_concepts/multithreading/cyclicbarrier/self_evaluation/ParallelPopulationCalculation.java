package ai.ml.core_concepts.multithreading.cyclicbarrier.self_evaluation;

import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelPopulationCalculation {
    public static void main(String[] args) {
        int TOTAL_REGION=3;

        Runnable sumPopulation = ()->{
            System.out.println("All region data processed...");
            System.out.println("Final Calutation for All population - "+PopulationCalculationService.sumPopulation);
        };
        CyclicBarrier cyclicBarrier=new CyclicBarrier(TOTAL_REGION, sumPopulation);

        ExecutorService service= Executors.newFixedThreadPool(TOTAL_REGION);
        REGION[] regions = REGION.values();
        for(int i=0;i<TOTAL_REGION;i++){
           service.execute(new PopulationCalculationService(regions[i].name(), cyclicBarrier, new Random().nextInt(1500, 2000)));
        }

        service.shutdown();
    }
}
