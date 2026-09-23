package ai.ml.core_concepts.multithreading.additional;
import java.util.concurrent.*;

class SumOfNumber implements Callable<Integer>
{
    int a;
    int b;
    public SumOfNumber(int a,int b)
    {
        this.a=a;
        this.b=b;
    }
    @Override
    public Integer call() throws Exception
    {
        return (a+b);
    }
}
public class CallableAndFutureExample
{
    public static void main(String[] args)throws  InterruptedException, ExecutionException
    {
        SumOfNumber sumOfNumber=new SumOfNumber(7,2);
        ExecutorService service=Executors.newFixedThreadPool(2);
        Future<Integer> future=service.submit(sumOfNumber);
        int sum=future.get();
        System.out.println(sum);
    }
}
