package ai.ml.core_concepts.multithreading.locks.Programs.Scenario3;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantExample {
    private final Lock lock = new ReentrantLock();

    public void outerMethod(){
        lock.lock();
        try{
            System.out.println("outer Method");
            innerMethod();        //outer method is calling Inner method
        } finally {
            lock.unlock();
        }
    }

    public void innerMethod(){
        lock.lock();
        try{
            System.out.println("inner Method");
        } finally {
            lock.unlock();
//            lock.unlock();
        }
    }

    public static void main(String[] args) {
        ReentrantExample reentrantExample = new ReentrantExample();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                reentrantExample.outerMethod();
            }
        };
        Thread t1=new Thread(runnable, "Thread1");
        Thread t2=new Thread(runnable, "Thread2");
        Thread t3=new Thread(runnable, "Thread3");
        t1.start();
        t2.start();
        t3.start();

    }
}
