package ai.ml.core_concepts.multithreading.locks.Programs.Scenario5.READ_WRITE_LOCK.Program.Scenario3;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteCounter {
    private int count=0;
    private final ReadWriteLock readWriteLock=new ReentrantReadWriteLock();
//    private final Lock readLock = readWriteLock.readLock();
//    private final Lock writeLock = readWriteLock.writeLock();

    public void increment(){
        readWriteLock.writeLock().lock();
        try{
            count++;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public int getCount(){
        readWriteLock.readLock().lock();
        try{
            return count;
        }finally {
            readWriteLock.readLock().unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException{
        ReadWriteCounter counter=new ReadWriteCounter();

        Runnable readTask = new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<10;i++){
                    System.out.println(Thread.currentThread().getName()+" read "+counter.getCount());
                }
            }
        };

        Runnable writeTask = new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<10;i++){
                    counter.increment();
                    System.out.println(Thread.currentThread().getName()+" incremented ");
                }
            }
        };

        Thread writerThread = new Thread(writeTask);
        Thread readerThread1 = new Thread(readTask);
        Thread readerThread2 = new Thread(readTask);

        writerThread.start();
        readerThread1.start();
        readerThread2.start();

        writerThread.join();
        readerThread1.join();
        readerThread2.join();

        System.out.println("Final count : "+counter.getCount());
    }
}
