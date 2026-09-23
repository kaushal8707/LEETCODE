package ai.ml.leetcode.multi_threading;

class Odd1 implements Runnable
{
    public void run()
    {
            for(int i=1;i<=10;i++)
            {
                if(i%2!=0)
                {
                    System.out.print(i+" ");
                }
            }
    }
}
class Even1 implements Runnable
{
    public void run()
    {
            for(int i=1;i<=10;i++)
            {
                if(i%2==0)
                {
                    System.out.print(i+" ");
                }
            }
    }
}
public class printEvenAndOddUsingJoin
{
    public static void main(String[] args) throws InterruptedException {
        Even1 ev=new Even1();
        Odd1 od=new Odd1();
        Thread t1=new Thread(ev);
        Thread t2=new Thread(od);
        t1.start();t1.join();t2.start();

    }
}
