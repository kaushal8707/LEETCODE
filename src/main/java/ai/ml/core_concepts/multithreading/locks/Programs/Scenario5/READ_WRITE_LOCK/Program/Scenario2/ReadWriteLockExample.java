package ai.ml.core_concepts.multithreading.locks.Programs.Scenario5.READ_WRITE_LOCK.Program.Scenario2;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

        class SharedResource {
            private int data = 0;
            private final ReadWriteLock lock = new ReentrantReadWriteLock();

            public int readData() {
                // Acquire the read lock
                lock.readLock().lock();
                try {
                    // Perform the read operation
                    System.out.println(Thread.currentThread().getName() + " is reading data: " + data);
                    return data;
                } finally {
                    // Release the read lock in a finally block to ensure it always happens
                    lock.readLock().unlock();
                }
            }

            public void writeData(int newData) {
                // Acquire the write lock
                lock.writeLock().lock();
                try {
                    // Perform the write operation (only one thread can be here at a time)
                    System.out.println(Thread.currentThread().getName() + " is writing data: " + newData);
                    data = newData;
                } finally {
                    // Release the write lock in a finally block
                    lock.writeLock().unlock();
                }
            }
        }

        public class ReadWriteLockExample {
            public static void main(String[] args) {
                SharedResource sharedResource = new SharedResource();

                // Create and start reader threads
                for (int i = 0; i < 3; i++) {
                    Thread readerThread = new Thread(sharedResource::readData, "Reader " + i);
                    readerThread.start();
                }

                // Create and start writer threads
                for (int i = 0; i < 2; i++) {
                    final int value = i;
                    Thread writerThread = new Thread(() -> sharedResource.writeData(value), "Writer " + i);
                    writerThread.start();
                }
            }
        }