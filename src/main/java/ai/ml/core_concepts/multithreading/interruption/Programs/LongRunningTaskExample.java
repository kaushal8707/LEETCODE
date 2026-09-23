package ai.ml.core_concepts.multithreading.interruption.Programs;

public class LongRunningTaskExample {

    public static void main(String[] args) throws InterruptedException {
        Thread t2 = new Thread(new LongRunningTask());
        t2.start();

        Thread.sleep(100); // Let it run for a bit
        System.out.println("Main thread interrupting t2...");
        t2.interrupt();

        t2.join();
        System.out.println("Main thread exiting.");
    }
}

class LongRunningTask implements Runnable {
    @Override
    public void run() {
        int count = 0;
        // Check the interrupted status periodically within the loop
        while (!Thread.currentThread().isInterrupted()) {
            // Simulate some heavy work
            for (int i = 0; i < 1000000; i++) {
                count++;
            }
            System.out.println("t2 is running, count: " + count);

            // A method could also throw InterruptedException if interrupted
            // if (Thread.interrupted()) { throw new InterruptedException(); }
        }
        System.out.println("t2 detected an interrupt signal and is stopping.");
    }
}
