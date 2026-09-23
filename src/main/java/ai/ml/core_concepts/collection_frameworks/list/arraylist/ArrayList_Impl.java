package ai.ml.core_concepts.collection_frameworks.list.arraylist;

import java.util.ArrayList;
import java.util.List;

public class ArrayList_Impl {
    public static void main(String[] args) {

        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(5);
        list.add(88);

       /** get(index) */
        System.out.println(list.get(2));

        /** size() */
        System.out.println(list.size());
        for(int i=0;i<list.size();i++){
            System.out.println(list.get(i));
        }
        for(int x:list){
            System.out.println(x);
        }

        /** contains(existing_element) */
        System.out.println(list.contains(5));
        System.out.println(list.contains(58));



        /**add(Index, element)
         *
         * If we want to Insert in Middle - Between 5 & 88
         * then 88 will shift right and element will insert at 2nd Index
         * so, It add-1 elements, so, list size get changed.
         * */
        list.add(2, 11);
        System.out.println(list);

        /** set(Index, element)
         *
         * If we want to replace any specific Index element
         * then 22 will replace 2nd index element which is 11
         * so, It does replace an element so list size doesn't change.
         * */
        list.set(2, 22);

        /** Array List extends AbstractList and
         * AbstractList is having toString() method
         * so directly we can use list in s.o.p so, it will print elements
         * */
        System.out.println(list);

        list.remove(2);
        System.out.println(list.size());

        list.trimToSize();

    }
}
