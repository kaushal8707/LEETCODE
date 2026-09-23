package ai.ml.core_concepts.collection_frameworks.map.LRUCache;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    int capacity;
    public LRUCache(int capacity){
        super(capacity, 0.75f, true);
        this.capacity=capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size()>capacity;
    }

    public static void main(String[] args) {

            /** Example - 1
             *
             * Bob will get removed
             * the reason is This method is invoked by put and putAll after inserting a new entry into the map.
             * It provides the implementor with the opportunity to remove the eldest entry each time a new one is added.
             * This is useful if the map represents a cache:
             * it allows the map to reduce memory consumption by deleting stale entries.
             *
             * */

        LRUCache<String, Integer> studentMap = new LRUCache<>(3);
        studentMap.put("Bob", 44);
        studentMap.put("Alice", 37);
        studentMap.put("Vipul", 23);
        studentMap.put("pradip", 19);

        studentMap.get("Bob");

        System.out.println(studentMap);


        /** Example - 2
         * now it will remove Alice
         * so, here Bob is latest used entry
         * either you use put or get both treat like a
         * recently, get used entry
         *
         */

        LRUCache<String, Integer> studentMap1 = new LRUCache<>(3);
        studentMap1.put("Bob", 44);
        studentMap1.put("Alice", 37);
        studentMap1.put("Vipul", 23);
        studentMap1.get("Bob");
        studentMap1.put("pradip", 19);

        System.out.println(studentMap1);

    }
}
