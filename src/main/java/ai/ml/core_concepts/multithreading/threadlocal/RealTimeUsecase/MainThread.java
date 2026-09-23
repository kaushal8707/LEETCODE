package ai.ml.core_concepts.multithreading.threadlocal.RealTimeUsecase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainThread {
    public static void main(String[] args) {
        ThreadLocalDemo task = new ThreadLocalDemo();
        Thread t1 = new Thread(task, "Thread-1");
        Thread t2 = new Thread(task, "Thread-2");
        t1.start(); t2.start();

//        ExecutorService executorService = Executors.newFixedThreadPool(3);
//        for(int i=0;i<3;i++){
//            executorService.execute(new ThreadLocalDemo());
//        }
//        executorService.shutdown();
    }
}
