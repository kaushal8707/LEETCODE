package ai.ml.core_concepts.collection_frameworks.iterable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IterableDemo {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList();
        for(int i : list){
            System.out.println(i);
        }

        //Java compiler converts above code to below code
        //Internally below code runs in for each loop.

        Iterator<Integer> iterator = list.iterator();
        while(iterator.hasNext()){
            System.out.println(iterator.next());
        }
    }
}
