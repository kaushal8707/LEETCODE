package ai.ml.core_concepts.multithreading.synchronization.Program.WITHOUT_SYNCHRONIZATION_DATA_INCONSISTENCY;

public class Counter {
    private int count =0;

    public void increment(){
        count++;
    }

    public int getCount(){
        return count;
    }
}
