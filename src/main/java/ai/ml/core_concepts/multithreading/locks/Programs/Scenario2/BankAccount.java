package ai.ml.core_concepts.multithreading.locks.Programs.Scenario2;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/** here Indefinitely wait is not happening */

public class BankAccount {
    private int balance = 100;
    private final Lock lock = new ReentrantLock();

    public void withdraw(int amount){
        System.out.println(Thread.currentThread().getName()+" attempting to withdraw "+amount);
        try{
            //lock.lock();        // this is same as a synchronized method like if any thread come so he will wait untill he acquire the lock
                                  // so there is no much use of lock() method bcz we should just have to check if lock avail execute otw perform some other task
                                  // otherwise just wait for a specified amount of time if get the lock do perform otw perform some other task in else block
                                  // why should simply wait to get the lock indefinitely...
            if(lock.tryLock(5000, TimeUnit.MILLISECONDS)){
                if(balance>=amount) {
                    try {
                        System.out.println(Thread.currentThread().getName() + " proceeding with withdraw ");
                        Thread.sleep(3000);    // simulate time taken to process the withdrawn
                        balance -= amount;
                        System.out.println(Thread.currentThread().getName() + " completed withdrawl. remaining balance: " + balance);
                    }catch (Exception e){
                        Thread.currentThread().interrupt();
                    } finally {
                        lock.unlock();     // always we release the resources in finally block.
                    }

                } else {
                    System.out.println("insufficient balance "+ balance);
                }
            }else{
                System.out.println(Thread.currentThread().getName()+" couldn't acquired the lock will try again later !!");
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        if(Thread.currentThread().isInterrupted()){
            System.out.println("-----------------------");
        }
    }
}
