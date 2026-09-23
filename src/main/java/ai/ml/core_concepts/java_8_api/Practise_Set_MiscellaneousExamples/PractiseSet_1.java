package ai.ml.core_concepts.java_8_api.Practise_Set_MiscellaneousExamples;

/** Print Even and Odds Numbers using even and odd threads **/

public class PractiseSet_1 {
    public static void main(String[] args) {
        Object object = new Object();
        DemoThread obj = new DemoThread(object);
        Thread evenT=new Thread(obj, "EvenThread");
        Thread oddT=new Thread(obj, "OddThread");
        evenT.start();
        oddT.start();
    }
}

class DemoThread implements Runnable {
    Object object;
    DemoThread(Object o){
        this.object=o;
    }
    int i=1;
    @Override
    public void run() {
        while(i <= 10){
            if(i % 2 ==0 && Thread.currentThread().getName().equals("EvenThread")){
                synchronized (object) {
                    System.out.println(i + "    " + Thread.currentThread().getName());
                    i++;
                    try {
                        object.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if(i % 2 !=0 && Thread.currentThread().getName().equals("OddThread")){
                synchronized (object){
                    System.out.println(i+"    "+Thread.currentThread().getName());
                    i++;
                    object.notify();
                }
            }

        }
    }
}
