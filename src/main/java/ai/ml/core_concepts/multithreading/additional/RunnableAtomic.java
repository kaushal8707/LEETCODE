package ai.ml.core_concepts.multithreading.additional;

public class RunnableAtomic implements Runnable
{
    @Override
    public void run() {

        System.out.println(Thread.currentThread().getName() +"  ");


        System.out.println(AtomicIntegerExample.sharedAtomicInteger.getAndIncrement());
    }
}
