package ai.ml.core_concepts.multithreading.MULTITHREADING.Process_Threads_Multitasking_MultiThreading.Program;

public class Hello {
    public static void main(String[] args) {
        /** extending Thread class */
        World world = new World();
        world.start();

        /** implementing Runnable interface */
        Java java = new Java();
        Thread thread = new Thread(java);
        thread.start();


        for( ; ; ){        //Infinite loop
            System.out.println("  Hello  "+ " using Thread - "+Thread.currentThread().getName());
        }
    }
}
