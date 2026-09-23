package ai.ml.core_concepts.multithreading.additional;

class Odd implements Runnable
{
    Even even;
    public Odd(Even even) {
        this.even = even;
    }
    public void run()
   {
       synchronized (this.even)
       {
          for(int i=1;i<=10;i++)
          {
              if(i%2!=0)
              {
                  System.out.print(i+" ");
              }
          }
          this.even.notify();
       }
   }
}
class Even implements Runnable
{
     public void run()
        {
           synchronized (this)
        {
            try {
                this.wait();
            }catch (InterruptedException e){e.printStackTrace();}
            for(int i=1;i<=10;i++)
             {
                 if(i%2==0)
                 {
                     System.out.print(i+" ");
                 }
             }
        }
        }
}
public class printEvenAndOddUsingWaitNotify
{
    public static void main(String[] args) {
        Even ev=new Even();
        Odd od=new Odd(ev);
        Thread t1=new Thread(ev);
        Thread t2=new Thread(od);
        t1.start();t2.start();

    }
}
