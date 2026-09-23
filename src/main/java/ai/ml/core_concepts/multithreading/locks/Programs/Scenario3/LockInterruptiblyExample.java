package ai.ml.core_concepts.multithreading.locks.Programs.Scenario3;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockInterruptiblyExample
{
    private final Lock lock = new ReentrantLock();

    public void outerMethod()
    {
        try{
            lock.lockInterruptibly();
                        // In lock.lock() indefinitely waiting but here because thread is in blocked waiting state
                        // already we saw timed waiting now we see block waiting. assume there is one thread running...
                        // now main thread entered into innerMethod() in that duration another thread came and he is waiting for thread 1
                        // to get finished and indefinitely waiting because we have not given time this is called block waiting state
                        // now if you are using lock() you can't interrupt but if you want to interrupt suppose you have written in main thread
                        // if work not done in 10 sec then we have to interrupt so to interrupt it should be interruptable
                        // so here interrupt exception will throw so need to handle in catch block InterruptException.
            System.out.println("outer Method");
            innerMethod();
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }finally {
            lock.unlock();
        }
    }
    public void innerMethod()
    {
        lock.lock();
        try {
            System.out.println("inner Method");
        }finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        LockInterruptiblyExample reentrantExample = new LockInterruptiblyExample();
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
