package ai.ml.core_concepts.collection_frameworks.list.copyonwritearraylist;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CopyOnWriteArrayListDemo {
    public static void main(String[] args) {

    /**    "Copy on Write" means that whenever a write operation
        like adding or removing an element
        Instead of directly modifying the existing list
        a new copy of the list is created, and the modification is applied to that copy
        this ensures that other threads reading the list while it's being modified are un-effected.

        Read Operations: Fast and Direct, since they happen on a stable list without interferance from modifications.

        Write Operations: A new Copy of the list is created for every modifications.
        The reference to the list is then updated so that subsequent reads
        use this new list.

                Example- while we have a notebook multiple students reading through note book but if
        anyone want to change the content so instead of changing in original notepad
        we will create a copy of notepad and ask them to modify on copies notepad
        and once he done i can use teh reference of list instaed of our list ref.

        usecase ( when less Write and more Read)

       When Read Operation is more compare to Write Operation then we should use CopyOnWriteArrayList

        because on every write a new copy of list is creating so more memory consumed.  */

        CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>();

        List<String> shoppingList1 = new CopyOnWriteArrayList<>();
        shoppingList1.add("Milk");
        shoppingList1.add("Breads");
        shoppingList1.add("Eggs");

        System.out.println("Initial Shopping List - "+shoppingList1);

        for(String item:shoppingList1){
            System.out.println(item);
            /** try to modify list while reading */
            if(item.equals("Eggs")){
                shoppingList1.add("Butter");
                System.out.println("Added Butter While Reading...");
            }
        }
        System.out.println("Updated Shopping List - "+shoppingList1);

        System.out.println("--------------------------------------------");

        /** Read Write together on Array List
         * while reading trying to modify list
         * */
        List<String> shoppingList = new ArrayList<>();
        shoppingList.add("Milk");
        shoppingList.add("Breads");
        shoppingList.add("Eggs");

        System.out.println("Initial Shopping List - "+shoppingList);

        for(String item:shoppingList){
            System.out.println(item);
            /** try to modify list while reading */
            if(item.equals("Eggs")){
                shoppingList.add("Butter");
                System.out.println("Added Butter While Reading...");
            }
        }
        System.out.println("Updated Shopping List - "+shoppingList);
   }
}
