package ai.ml.core_concepts.multithreading.locks.Programs.Scenario4;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FairnessOfLockExample {
    private final Lock lock = new ReentrantLock(true);

    public void accessResource(){
        lock.lock();
        try{
            System.out.println(Thread.currentThread().getName()+" acquired the lock");
            Thread.sleep(1000);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }finally {
            lock.unlock();
            System.out.println(Thread.currentThread().getName()+" release the lock");
        }
    }

    public static void main(String[] args) {
        FairnessOfLockExample example = new FairnessOfLockExample();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                example.accessResource();
            }
        };
        Thread thread1 = new Thread(task, "Thread 1");
        Thread thread2 = new Thread(task, "Thread 2");
        Thread thread3 = new Thread(task, "Thread 3");
        thread1.start();
        thread2.start();
        thread3.start();
    }
}