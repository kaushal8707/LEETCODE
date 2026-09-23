package ai.ml.core_concepts.multithreading.deadlocks.Programs.RESOLVE_DEADLOCK;

import com.zero.to.hero.multi.threading.deadlocks.Programs.Account;

public class AccountTransferDetail {

    public static void transferSafe(Account account1, Account account2, double amount) {
        // Determine a consistent order for locking (e.g., by name)
        Account firstLock = account1.getName().compareTo(account2.getName()) < 0 ? account1 : account2;
        Account secondLock = firstLock == account1 ? account2 : account1;

        // Lock in the defined order
        synchronized (firstLock) {
            System.out.println(Thread.currentThread().getName() + " locked " + firstLock.getName());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (secondLock) {
                System.out.println(Thread.currentThread().getName() + " locked " + secondLock.getName());
                // Perform the actual transaction once both locks are held
                account1.withdraw(amount);
                account2.deposit(amount);
                System.out.println(Thread.currentThread().getName() + " transferred " + amount);
            }
        }
    }

    public static void main(String[] args) {
        final Account alice = new Account("Alice", 1000.0);
        final Account bob = new Account("Bob", 1000.0);

        // Thread 1: Alice transfers to Bob (locks Alice then Bob)
        new Thread(() -> {
            transferSafe(alice, bob, 100.0);
        }, "Thread-Alice").start();

        // Thread 2: Bob transfers to Alice (locks Bob then Alice)
        new Thread(() -> {
            transferSafe(bob, alice, 200.0);
        }, "Thread-Bob").start();
    }
}
