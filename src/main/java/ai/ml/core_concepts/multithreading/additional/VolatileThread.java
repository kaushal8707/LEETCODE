package ai.ml.core_concepts.multithreading.additional;

public class VolatileThread implements Runnable
{
    VolatileObject volatileObject;

    public VolatileThread(VolatileObject volatileObject) {
        this.volatileObject = volatileObject;
    }

    @Override
    public void run()
    {
//        System.out.println(Thread.currentThread().getName()+" old NV: "+volatileObject.getCounter());
//        volatileObject.increaseCounter();
//        System.out.println(Thread.currentThread().getName()+" New NV:  "+volatileObject.getCounter());
        System.out.println(Thread.currentThread().getName()+" old V: "+volatileObject.getVcounter());
        volatileObject.increaseVCounter();;
        System.out.println(Thread.currentThread().getName()+" New V:  "+volatileObject.getVcounter());
    }
}
