package ai.ml.core_concepts.collection_frameworks.list.copyonwritearraylist.selftry;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CopyOnWriteArrayListDemo {
    public static void main(String[] args) {

        List<Integer> readWriteOp = new CopyOnWriteArrayList<>();
        readWriteOp.add(111);
        readWriteOp.add(222);
        readWriteOp.add(333);
        readWriteOp.add(444);

        Thread reader = new Thread(()->{
            for(Integer data : readWriteOp){
                System.out.println("Read Data:  "+data);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        Thread writer = new Thread(()->{
            readWriteOp.add(1234);
        });

        writer.start();
        reader.start();;
    }
}
