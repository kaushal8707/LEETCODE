package ai.ml.core_concepts.multithreading.locks.Programs.Scenario1;

public class Main {
    public static void main(String[] args) {
        BankAccount sbi=new BankAccount();
        Runnable task=new Runnable() {
            @Override
            public void run() {
                sbi.withdraw(50);
            }
        };
        Thread t1 = new Thread(task, "Thread-1");
        Thread t2 = new Thread(task, "Thread-2");
        t1.start();
        t2.start();
    }
}

//usecase-
// we have one bank account and 2 threads trying to withdraw 50 rupees from same bank account.
// In a bank account 100 rupees available and 2 times you are trying to withdraw 50 rupees from there.


/**
 * when we use synchronized keyword when t1 access then t2 can't be access doesn't matter either you are making it sleep for some time
 * till t1 won't finish the complete task including sleep when t1 finish then t2 will start executing. here sleep means doing some task
 *
 * Here we put sleep to simulate longer operation
 *
 */