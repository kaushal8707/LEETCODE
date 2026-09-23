package ai.ml.core_concepts.multithreading.MULTITHREADING.Process_Threads_Multitasking_MultiThreading.Program;

public class Java implements Runnable {
    @Override
    public void run() {
        for( ; ; ){
            System.out.println("Java   "+ " using Thread - "+Thread.currentThread().getName());
        }
    }
}