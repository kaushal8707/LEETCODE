package ai.ml.core_concepts.multithreading.deadlocks.Programs;

// Represents a shared resource (a bank account)
public class Account {
    private String name;
    private double balance;

    public Account(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public void deposit(double amount) {
        this.balance +=amount;
    }

    public String getName() {
        return name;
    }

    public void withdraw(double amount) {
        this.balance -=amount;
    }
}
