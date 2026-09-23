package ai.ml.core_concepts.multithreading.additional;

class Thread2 implements Runnable
{
    @Override
    public void run()
    {
        for(int i=1;i<=10;i++)
        {
            System.out.println(i);
        }
    }
}
public class ThreadWay2
{
    public static void main(String[] args)
    {
        Thread thread1=new Thread(new Thread2());
        thread1.start();
    }
}

