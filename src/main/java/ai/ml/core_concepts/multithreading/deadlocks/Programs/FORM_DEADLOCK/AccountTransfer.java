package ai.ml.core_concepts.multithreading.deadlocks.Programs.FORM_DEADLOCK;
import com.zero.to.hero.multi.threading.deadlocks.Programs.Account;

public class AccountTransfer {

    // The transfer method that causes deadlock
    public static void transfer(Account from, Account to, double amount) {
        // Lock the 'from' account
        synchronized (from) {
            System.out.println(Thread.currentThread().getName() + " locked " + from.getName());
            try {
                // Introduce a slight delay to help force the deadlock scenario
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Try to lock the 'to' account
            synchronized (to) {
                System.out.println(Thread.currentThread().getName() + " locked " + to.getName());
                from.withdraw(amount);
                to.deposit(amount);
                System.out.println(Thread.currentThread().getName() + " transferred " + amount);
            }
        }
    }

    public static void main(String[] args) {
        final Account alice = new Account("Alice", 1000.0);
        final Account bob = new Account("Bob", 1000.0);

        // Thread 1: Alice transfers to Bob (locks Alice then Bob)
        new Thread(() -> {
            transfer(alice, bob, 100.0);
        }, "Thread-Alice").start();

        // Thread 2: Bob transfers to Alice (locks Bob then Alice)
        new Thread(() -> {
            transfer(bob, alice, 200.0);
        }, "Thread-Bob").start();
    }
}

