package ai.ml.core_concepts.multithreading.lifecycle;

/** States of Threads */
public class MyThread extends Thread{

    @Override
    public void run() {
        System.out.println("RUNNING");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) throws InterruptedException {

        MyThread t1 = new MyThread();
        System.out.println(t1.getState());   /**NEW*/
        t1.start();
        System.out.println(t1.getState());   /**RUNNABLE*/

        System.out.println(Thread.currentThread().getState()); // here Main Thread only executing/Running but state showing Runnable
                                                               // bcz in Enum no RUNNING state specified so either running or ready to run

        //here we want to give a chance to execute run method in t1 thread
        Thread.sleep(100);    //we are sleeping main thread for 100 ms so that
                                   // OS or JVM will get a time to execute thread MyThread
        System.out.println(t1.getState());    /**TIMED_WAITING*/// while MyThread t1 is paused for 2 sec

        t1.join();     //Waits for this thread to die.
                       //Main Thread is waiting to finish the execution of MyThread t1
                       //who is caller or who is running t1.join(), Main method so Main Method will wait
                       //for T1 to get finished. so once MyThread complete run() method executed then only Main method start execute
        System.out.println(t1.getState());   /**TERMINATED*/ //after t1.join() called this line will execute once t1 finish run method i.e after 2 sec
                                                            // bcz Main method was waiting for t1 to get finished



    }
}
