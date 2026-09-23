package ai.ml.core_concepts.multithreading.locks.Programs.Scenario1;

public class BankAccount {
    private int balance = 100;

    public synchronized void withdraw(int amount){
        System.out.println(Thread.currentThread().getName()+" attempting to withdraw "+amount);
        if(balance>=amount){
            System.out.println(Thread.currentThread().getName()+" proceeding with withdraw ");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            balance-=amount;
            System.out.println(Thread.currentThread().getName()+" completed withdrawl. remaining balance: "+balance);
        }else{
            System.out.println(Thread.currentThread().getName()+" insufficient balance ");
        }
    }
}
