package ai.ml.core_concepts.multithreading.synchronization.Program.SYNCHRONIZATION_DEMO;

public class Counter {
    private int count = 0;

//    public synchronized void increment(){
    public void increment(){
        synchronized (this) {                    // here this means we are considering only one instance. so one instance
            count++;                             // on which increment method getting called. if multiple thread trying to access that
        }                                        // then at a time only one thread can access it.
    }                                            // If we have multiple object of counter then both will run independently

    public int getCount(){
        return count;
    }
}
