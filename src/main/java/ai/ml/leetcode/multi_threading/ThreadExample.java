package ai.ml.leetcode.multi_threading;
class TaskA extends Thread
{
    @Override
    public void run()
    {
        System.out.println(Thread.currentThread().getName()+"  thread started");
        for(int i=1;i<=10;i++)
       {
           System.out.print(i+"  ");
       }
        System.out.println(Thread.currentThread().getName()+"  thread ended");

    }
}
public class ThreadExample extends Thread
{
    public static void main(String[] args) {
        System.out.println("Main thread started");
        TaskA t1=new TaskA();
        t1.start();
        System.out.println("Main thread ended");

    }
}
