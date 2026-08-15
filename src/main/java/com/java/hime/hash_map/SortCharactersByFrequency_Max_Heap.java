package com.java.hime.hash_map;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

//Max Heap Using PriorityQueue
public class SortCharactersByFrequency_Max_Heap {
    public static void main(String[] args) {
        String s = "tree";  // cccaaa   Aabb
        String frequencySort = frequencySort(s);
        System.out.println(frequencySort);
    }

    private static String frequencySort(String s) {
        Map<Character, Integer> map = new HashMap<>();
        for(char ch:s.toCharArray()){
            map.put(ch, map.getOrDefault(ch, 0)+1);
        }

        // Step 2: Build a Max-Heap based on map values (frequencies)
        PriorityQueue<Character> maxHeap = new PriorityQueue<>(
                (a,b)-> map.get(b) -map.get(a)
        );
        maxHeap.addAll(map.keySet());

        // Step 3: Rebuild the string from the heap
        StringBuilder result = new StringBuilder();
        while(!maxHeap.isEmpty()){
            char c = maxHeap.poll();
            int count = map.get(c);
            for(int i=0; i<count;i++){
                result.append(c);
            }
        }
        return result.toString();
    }
}
//        Yes, a Priority Queue internally uses a Heap data structure (specifically, a binary heap),
//        but it does not use the full "Heap Sort" algorithm to sort the entire list at once.Here is a quick breakdown
//        of how they are related:Heap Data Structure: This is the underlying storage model. It organizes data in a
//        tree-like structure so that the element with the highest (or lowest) priority is always instantly accessible at
//        the top (the root).Priority Queue: This is the concept/abstract data type. It simply means "process the most important item first."
//        A heap is the most efficient way to build a priority queue