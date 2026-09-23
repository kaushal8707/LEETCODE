package ai.ml.core_concepts.collection_frameworks.list.copyonwritearraylist;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CopyOnWriteArrayList_MultipleThreadDemo {
    public static void main(String[] args) {

//        List<String> sharedList = new ArrayList<>();
        List<String> sharedList = new CopyOnWriteArrayList<>();
        sharedList.add("item1");
        sharedList.add("item2");
        sharedList.add("item3");


        Thread readerThread = new Thread(()-> {

            try{
                while(true){
                    //Iterate through the list
                    for(String item:sharedList){
                        System.out.println("Reading Item: "+item);
                        Thread.sleep(100);
                    }
                }
            }catch (Exception exception){
                System.out.println("Exception in reader Thread : "+exception);
            }
        });

        Thread writerThread = new Thread(()-> {

           try{
               Thread.sleep(500);  // delay to start reading thread first
               sharedList.add("item4");
               System.out.println("Added Item4 to List ");

               Thread.sleep(500);  // delay to start reading thread first
               sharedList.remove("item1");
               System.out.println("Removed Item1 from List ");

           }catch (InterruptedException e){
                e.printStackTrace();
           }
        });

        readerThread.start();
        writerThread.start();

    }
}
