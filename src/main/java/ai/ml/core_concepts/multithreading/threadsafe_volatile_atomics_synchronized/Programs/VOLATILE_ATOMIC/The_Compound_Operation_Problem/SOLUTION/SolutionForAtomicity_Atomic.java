package ai.ml.core_concepts.multithreading.threadsafe_volatile_atomics_synchronized.Programs.VOLATILE_ATOMIC.The_Compound_Operation_Problem.SOLUTION;

import java.util.concurrent.atomic.AtomicInteger;

public class SolutionForAtomicity_Atomic {

    private AtomicInteger count = new AtomicInteger(0);

    public void increment() {
        count.getAndIncrement();
    }

    public int getCount() {
        return count.get();
    }

    public static void main(String[] args) throws InterruptedException {
        SolutionForAtomicity_Atomic obj=new SolutionForAtomicity_Atomic();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                for(int i=1;i<=1000;i++){
                    obj.increment();                  // 1000 tasks are processing by threads
                }
            }
        };
        Thread t1=new Thread(runnable);
        Thread t2=new Thread(runnable);

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("counter result : "+obj.getCount());

    }
}
