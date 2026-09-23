package ai.ml.core_concepts.multithreading.synchronization.Program.WITHOUT_SYNCHRONIZATION_DATA_INCONSISTENCY;

public class Test {
    public static void main(String[] args) throws InterruptedException {

        Counter counter = new Counter();
        CounterThread t1 = new CounterThread(counter);        //both threads shared a common resources
        CounterThread t2 = new CounterThread(counter);        //both threads shared a common resources
        t1.start();
        t2.start();

        t1.join();    // if we not use join count will be 0 because main thread will run before completing these 2 threads
        t2.join();    // so giving join means first both threads will finish processing then only main thread will start running

        System.out.println(counter.getCount());      // it should come 2000, but coming 1743 or 1861
                                                     // because both threads running simultaneously just assume now count = 101 then
                                                     // both thread read 101 and increment to 102  which mean at same time both thread run counter.increment()
                                                     // it is happening because we are sharing one resource with multiple thread
                                                     // if we want to make sure at a same time only one thread should access it if one thread access another should wait
                                                     // so let's add synchronized keyword in method
    }
}
