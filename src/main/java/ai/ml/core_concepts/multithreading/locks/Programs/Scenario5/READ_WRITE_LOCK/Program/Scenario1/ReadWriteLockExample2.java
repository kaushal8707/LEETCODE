package ai.ml.core_concepts.multithreading.locks.Programs.Scenario5.READ_WRITE_LOCK.Program.Scenario1;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockExample2 {
    public static void main(String[] args) {
        ReadWriteLock lock = new ReentrantReadWriteLock();
        SharedResource1 resource = new SharedResource1();

        // Runnable for readers: allows concurrent access
        Runnable readTask = () -> {
            lock.readLock().lock();
            try {
                System.out.println(Thread.currentThread().getName() + " read: " + resource.read());
            } finally {
                lock.readLock().unlock();
            }
        };

        // Runnable for writers: requires exclusive access
        Runnable writeTask = () -> {
            lock.writeLock().lock();
            try {
                int val = (int)(Math.random() * 100);
                resource.write(val);
                System.out.println(Thread.currentThread().getName() + " wrote: " + val);
            } finally {
                lock.writeLock().unlock();
            }
        };

        // Start threads
        for (int i = 0; i < 3; i++) new Thread(readTask, "Reader-" + i).start();
        for (int i = 0; i < 2; i++) new Thread(writeTask, "Writer-" + i).start();
    }
}

// Shared resource protected by the lock
class SharedResource1 {
    private int data = 0;
    public int read() { return data; }
    public void write(int value) { this.data = value; }
}