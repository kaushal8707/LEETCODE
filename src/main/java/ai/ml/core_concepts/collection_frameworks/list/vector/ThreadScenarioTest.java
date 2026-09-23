package ai.ml.core_concepts.collection_frameworks.list.vector;

import java.util.ArrayList;

public class ThreadScenarioTest {

    public static void main(String[] args) {

        ArrayList<Integer> list=new ArrayList<>();

        Thread t1=new Thread(()-> {
            for(int i=1;i<=10;i++){
                list.add(i);
            }
        });

        Thread t2=new Thread(()-> {
            for(int i=1;i<=10;i++){
                list.add(i);
            }
        });

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(list.size());
    }
}
