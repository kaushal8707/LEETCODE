package ai.ml.core_concepts.collection_frameworks.set.copyonwritearrayset;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * => If here shopping list is an array list then exception will occurred bcz while we are reading we modifying.
 *     but if we make shoppinglist to CopyOnWriteArrayList then ConcurrentModification Exception not occurred.
 *     But while we were doing loop we are looking into a stable copy/snapshots of shoppingList. it will be modified
 *     But It will modify on a new Copy. when we come after loop then only old list reference will be replaced with a
 *     newly copiedList.
 */
public class CopyOnWriteArraySetDemo {
    public static void main(String[] args) {

        // Thread - Safe
        // No Duplicate Elements
        // Copy-On-Write Mechanism
        // Iterators Do not Reflect Modifications

        CopyOnWriteArraySet<Integer> copyOnWriteArraySet = new CopyOnWriteArraySet<>();
        ConcurrentSkipListSet<Integer> concurrentSkipListSet = new ConcurrentSkipListSet<>();

        for(int i=1; i<=5; i++){
            copyOnWriteArraySet.add(i);
            concurrentSkipListSet.add(i);
        }

        System.out.println("Initial CopyOnWriteArraySet - "+copyOnWriteArraySet);
        System.out.println("Initial ConcurrentSkipListSet - "+concurrentSkipListSet);


        System.out.println("\n Iterating and Modifying CopyOnWriteArraySet\n");
        for(Integer num: copyOnWriteArraySet){
            System.out.println("Reading from CopyOnWriteArraySet  "+num);
            //Attempt to modify the set during Iteration
            copyOnWriteArraySet.add(6);
        }



        System.out.println("-------------------");

        System.out.println("\n Iterating and Modifying ConcurrentSkipListSet\n");
        for(Integer num: concurrentSkipListSet){
            System.out.println("Reading from CopyOnWriteArraySet  "+num);
            //Attempt to modify the set during Iteration
            if(num==5) {
                concurrentSkipListSet.add(6);
            }
        }

        System.out.println("-------------------");

        System.out.println("\n Iterating and Modifying ConcurrentSkipListSet\n");
        for(Integer num: concurrentSkipListSet){
            System.out.println("Reading from CopyOnWriteArraySet  "+num);
            //Attempt to modify the set during Iteration
            concurrentSkipListSet.add(6);
        }

    }
}

