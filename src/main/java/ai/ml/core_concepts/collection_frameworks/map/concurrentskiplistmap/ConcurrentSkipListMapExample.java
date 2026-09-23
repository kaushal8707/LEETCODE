package ai.ml.core_concepts.collection_frameworks.map.concurrentskiplistmap;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.Map;

public class ConcurrentSkipListMapExample {
    public static void main(String[] args) {
        // Create a ConcurrentSkipListMap with natural ordering (ascending keys)
        ConcurrentSkipListMap<String, String> map = new ConcurrentSkipListMap<>();

        // Add mappings
        map.put("3", "Apple");
        map.put("1", "Banana");
        map.put("2", "Orange");
        map.put("4", "Grapes");

        // Print the map (keys are automatically sorted)
        System.out.println("ConcurrentSkipListMap: " + map);

        // Use NavigableMap methods
        System.out.println("First Entry: " + map.firstEntry());
        System.out.println("Last Key: " + map.lastKey());
        System.out.println("Ceiling Key for '2': " + map.ceilingKey("2"));

        // Iterate over the map (iterators are weakly consistent)
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }

         ConcurrentSkipListMap<Integer, String> map1 = new ConcurrentSkipListMap<>();

            // Add elements (insertion is thread-safe)
            map1.put(3, "Three");
            map1.put(1, "One");
            map1.put(2, "Two");
            map1.put(5, "Five");
            map1.put(4, "Four");

            // The map is automatically sorted by keys
            System.out.println("Sorted Map: " + map1); // Output: Sorted Map: {1=One, 2=Two, 3=Three, 4=Four, 5=Five}

            // Use navigational methods
            System.out.println("First Key: " + map1.firstKey()); // Output: First Key: 1
            System.out.println("Last Key: " + map1.lastKey()); // Output: Last Key: 5
            System.out.println("Ceiling Key for 2: " + map1.ceilingKey(2)); // Output: Ceiling Key for 2: 2
            System.out.println("Higher Key for 2: " + map1.higherKey(2)); // Output: Higher Key for 2: 3

            // Atomic put if absent operation
            map1.putIfAbsent(1, "One New"); // Will not replace "One"
            System.out.println("Map after putIfAbsent(1, \"One New\"): " + map1.get(1)); // Output: Map after putIfAbsent(1, "One New"): One
    }
}
