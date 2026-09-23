package ai.ml.core_concepts.collection_frameworks.list.vector;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Vector;

public class Vector_Impl {
    public static void main(String[] args) {

        Vector<Integer> vector = new Vector<>();
        /** default-Initial capacity */
        System.out.println(vector.capacity());


        Vector<Integer> vector1 = new Vector<>(12);
        /** defined-Initial capacity */
        System.out.println(vector1.capacity());

        /** once Internal array full with it's initial capacity it doubled but in array 1.5 */
        Vector<Integer> vector2 = new Vector<>(5);
        vector2.add(1);
        vector2.add(1);
        vector2.add(1);
        vector2.add(1);
        vector2.add(1);
        vector2.add(1);
        System.out.println(vector2.capacity());

        /** If you define ur own increment capacity then also you can do
         * so instead of 10 it will be 8 then once full it would be 11 always +3
         * */
        Vector<Integer> vector3 = new Vector<>(5, 3);
        vector3.add(1);
        vector3.add(1);
        vector3.add(1);
        vector3.add(1);
        vector3.add(1);
        vector3.add(1);
        System.out.println(vector3.capacity());
        vector3.add(1);
        vector3.add(1);
        vector3.add(1);
        System.out.println(vector3.capacity());


        /** In, Vector we can pass Collection object in constructor */
        Vector<Integer> vector5=new Vector<>(Arrays.asList(1,2,3,4,5));
        System.out.println(vector5);

        LinkedList<Integer> list=new LinkedList<>();
        list.add(1);
        list.add(3);
        Vector<Integer> vector6=new Vector<>(list);
        System.out.println(vector6);








    }
}
