package ai.ml.core_concepts.multithreading.threadsafe_volatile_atomics_synchronized.Programs.VOLATILE_ATOMIC.The_Compound_Operation_Problem;

public class VolatileCounterExample {

    private volatile int count = 0;     // Visibility okay, not atomic   // Lost Updates with volatile

    public void increment()
    {
        count++;
    }

    // Compound operation (read-modify-write)
    public int getCount()
    {
        return count;
    }

    public static void main(String[] args) throws InterruptedException {
        VolatileCounterExample demo = new VolatileCounterExample();

        Runnable task = () -> {
            for (int i = 0; i < 1000; i++){
                demo.increment();
            }
        };
        Thread t1 = new Thread(task),
                  t2 = new Thread(task);

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        // Result is often < 2000 due to race conditions

        System.out.println("Final count: " + demo.getCount());
    }
}
