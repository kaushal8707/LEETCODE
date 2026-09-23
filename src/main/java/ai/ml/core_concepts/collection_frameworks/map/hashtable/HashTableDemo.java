package ai.ml.core_concepts.collection_frameworks.map.hashtable;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class HashTableDemo {
    public static void main(String[] args) {

        Hashtable<Integer, String> hashtable = new Hashtable<>();
        hashtable.put(2, "Banana");
        hashtable.put(1, "Apple");
        hashtable.put(3, "Cherry");

        /**
         *
         * HashTable is Synchronized
         * no null key and no null value
         * slower than hashmap
         * all methods are synchronized in HashTable
         * legacy class replaced with ConcurrentHashMap
         * only linked list in case of collision occurred
         * both put() and get() all methods inside HashTable is synchronized so complete map/hashtable get locked so the thread
         * who want to just read then also need to wait, to resolve ConcurrentHashMap came.
         */

        System.out.println(hashtable);

        System.out.println("Value for key  2 - "+hashtable.get(2));
        System.out.println("Does key 3 exists ? - "+hashtable.containsKey(3));
        hashtable.remove(1);
        System.out.println("After Removing key 1 - "+hashtable);

//        hashtable.put(null, "value");  // Throws Exception
//        hashtable.put(4, null);        // Throws Exception
        System.out.println();

        /**------------------- Concurrency Example---------------*/

        Map<Integer, String> map=new Hashtable<>();
        Thread thread1=new Thread(()->{
           for(int i=0; i<1000; i++){
               map.put(i, "Thread1");
           }
        });

        Thread thread2=new Thread(()->{
            for(int i=1000; i<2000; i++){
                map.put(i, "Thread2");
            }
        });
        thread1.start();
        thread2.start();
        try {
            thread1.join();
            thread2.join();
        }catch (InterruptedException exception){
            exception.printStackTrace();
        }
        System.out.println("FInal Size of HashMap - "+map.size());

    }
}
