package ai.ml.core_concepts.collection_frameworks.map.enummap;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * If all the keys in a Map are values from a single enum, it is recommended to use an EnumMap as the specific
 * implementation. An EnumMap, which has the advantage of knowing all possible keys in advance, is more efficient
 * compared to other implementations, as it can use a simple array as its underlying data structure.
 *
 * ordinal -> Within Enum there is an Internal Array so their Index called ordinal.
 *
 */
public class EnumMapDemo {
    public static void main(String[] args) {

        /**
         *     Internally contain Array of size same as enum
         *     No Hashing
         *     Ordinal/Index used
         *     Faster than HashMap bcz of no Hashing and Indexing
         *     Memory Efficient
         */
        Map<Day, String> map=new EnumMap<>(Day.class);   /**->Enum Map Order also maintaining it will print in a same order how you define within an Enum.*/
        map.put(Day.TUESDAY, "GYM");                     /**->while we do put() we didn't find hashcode,
                                                            ->it directly mapped to a Enum Index.
                                                            ->so here hashing not required directly we get Index and element will store. */
        map.put(Day.MONDAY, "Walk");
        System.out.println(Day.TUESDAY.ordinal());
        System.out.println(map);
        String s = map.get(Day.TUESDAY);
        System.out.println(s);
    }
    enum Day {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }
}
