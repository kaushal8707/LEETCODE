package ai.ml.leetcode.miscellaneous;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UniqueNumberOfOccurrences {
    public static boolean hasUniqueOccurrences(int[] arr) {
        // Step 1: Count occurrences of each number
        Map<Integer, Integer> countMap = new HashMap<>();
        for (int num : arr) {
            countMap.put(num, countMap.getOrDefault(num, 0) + 1);
        }

        // Step 2: Check if frequencies are unique
        Set<Integer> uniqueCounts = new HashSet<>(countMap.values());

        // If sizes match, all counts are unique
        return countMap.size() == uniqueCounts.size();
    }

    public static void main(String[] args) {
        int[] test1 = {1, 2, 2, 1, 1, 3}; // 1->3 times, 2->2 times, 3->1 time
        int[] test2 = {1, 2};             // 1->1 time, 2->1 time (Duplicate counts)

        System.out.println(hasUniqueOccurrences(test1)); // Output: true
        System.out.println(hasUniqueOccurrences(test2)); // Output: false
    }
}

