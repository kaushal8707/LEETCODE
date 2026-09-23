package ai.ml.leetcode.multi_threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIntegerExample
{
    static AtomicInteger sharedAtomicInteger=new AtomicInteger();
    public static void main(String[] args) throws InterruptedException
    {
     RunnableAtomic runnable=new RunnableAtomic();
        ExecutorService service=Executors.newFixedThreadPool(3);
        for(int i=0;i<10;i++)
        {
            service.execute(runnable);
        }


        Thread.sleep(5000);
     System.out.println("SharedAtomicInteger: - "+sharedAtomicInteger);
    }
}
