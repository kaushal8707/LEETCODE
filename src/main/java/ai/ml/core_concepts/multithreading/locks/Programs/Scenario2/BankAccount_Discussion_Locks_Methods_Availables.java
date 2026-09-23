package ai.ml.core_concepts.multithreading.locks.Programs.Scenario2;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BankAccount_Discussion_Locks_Methods_Availables {
    private int balance = 100;
    private final Lock lock = new ReentrantLock();

    public void withdraw(int amount){
        System.out.println(Thread.currentThread().getName()+" attempting to withdraw "+amount);
        /** tryLock()*/
        if(lock.tryLock()){             // just assume t1 is having a lock and currently t2 is executing this then it will
                                        // return false and code won't run. so something we can show like server is busy something
                                        // earlier what was happening it was waiting for t2 but now it will she is lock free
                                        // if not then in else part we can perform something or we can print some msg like don't wait..
        }

        /** tryLock(long time, TimeUnit unit)*/
        try {
            if (lock.tryLock(1000, TimeUnit.MILLISECONDS)) {  // thread will wait for 1 second if within a second
                                                                   // he is getting a lock then fine otherwise he will perform some
                                                                   // other task in else block.
                                                                   // If another threads not interrupted then it will wait for a
                                                                   // given time.

                                                                   // when tryLock without time then it was immediately trying to
                                                                   // acquiring the lock immediately he won't wait.

                                                                   // when tryLock without time then immediately trying to acquired the lock
                                                                   // at same time if lock avail return true otherwise return false.

                                                                   // you are giving time means he will wait for a specified amount of time
                                                                   // if at that time lock avail or acquire then return true if lock not avail at given time as well it will return false

                                                                   // obviously he is waiting if the thread get interrupted will throw InterruptedException
                                                                   // that's why we have written try & catch for this

                                                                   // so in earlier tryLock() there was no waiting so no need to write catch block to handle interrupted exception
                                                                   // but here we are waiting so InterruptException may occurred bcz another thread may interrupt so we handle in catch block.
            }
        }catch(Exception e){

        }


    }
}
