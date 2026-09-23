package ai.ml.core_concepts.multithreading.interruption.Programs;

public class InterruptionExample {

    public static void main(String[] args) {
        // 1. Create a thread
        Thread t1 = new Thread(new InterruptibleTask());

        // 2. Start the thread
        t1.start();

        // 3. Main thread sleeps briefly to allow t1 to start its work
        try {
            Thread.sleep(1000); // Sleep in main thread
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 4. Interrupt t1 from the main thread
        System.out.println("Main thread is interrupting t1...");
        t1.interrupt();

        // 5. Main thread waits for t1 to finish (optional, for clean shutdown)
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Main thread exiting.");
    }
}

class InterruptibleTask implements Runnable {
    @Override
    public void run() {
        System.out.println("t1 started, going to sleep for 5 seconds.");
        try {
            // Thread goes to sleep and will be interrupted
            Thread.sleep(5000);
            System.out.println("t1 woke up without interruption (this message shouldn't appear).");
        } catch (InterruptedException e) {
            // This block is executed when the thread is interrupted during sleep
            System.out.println("t1 was interrupted during sleep! Exiting gracefully.");
            // When InterruptedException is caught, the interrupt flag is cleared.
            // If the task needs to propagate the interrupt status or do further work that
            // should also be interruptible, it should restore the flag:
            // Thread.currentThread().interrupt(); // Restore the interrupt status
            return; // Exit the thread
        }
        System.out.println("t1 finished execution (this message shouldn't appear).");
    }
}
