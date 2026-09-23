package ai.ml.core_concepts.multithreading.additional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Task implements Runnable
{
    int i;
    Task(int i)
    {
        this.i=i;
    }
    @Override
    public void run()
    {
      System.out.println(Thread.currentThread().getName()+"started");
      System.out.println(Thread.currentThread().getName()+"-----"+i);
      System.out.println(Thread.currentThread().getName()+"ended");

    }
}

public class ExecutorServiceApp
{
    public static void main(String[] args)
    {
        ExecutorService service=Executors.newFixedThreadPool(3);
        for(int i=1;i<=15;i++)
        {
            Task t=new Task(i);
            service.execute(t);
        }

    }
}
