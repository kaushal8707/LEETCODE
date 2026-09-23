package ai.ml.core_concepts.multithreading.synchronization.Program.SYNCHRONIZATION_DEMO;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                for(int i=1;i<=1000;i++){
                    counter.increment();
                }
            }
        };
        Thread t1 = new Thread(runnable);
        Thread t2 = new Thread(runnable);
        t1.start();
        t2.start();

        t1.join();
        t2.join();


        System.out.println(counter.getCount());
        System.out.println("-------------------------");

        /** Note -  when we have not used synchronized then unpredictable result was coming
         *  because outcome was depends on thread relative timings bcz threads was running concurrently
         *  then result would be unpredictable this condition while no synchronize called "RACE_CONDITION"
         *  we can solve race condition problem by using synchronize \
         *  At a same time only one thread can access critical section that is called "MUTUAL_EXCLUSION"
         *  MUTUAL_EXCLUSION assures that multiple thread should not access critical section simultaniously
         *  mutual - RELATIONSHIP BETWEEN MULTIPLE THREADS WHICH WANT TO ACCESS SAME SHARED RESOURCES
         *  EXCLUSION - STOPPING OR PREVENT
         *
         */

    }
}
