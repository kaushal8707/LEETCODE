package ai.ml.leetcode.multi_threading;

class Thread1 extends Thread
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
public class ThreadWay1
{
    public static void main(String[] args)
    {
        Thread1 thread1=new Thread1();
        thread1.start();
    }
}

