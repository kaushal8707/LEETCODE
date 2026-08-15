package com.java.hime.hash_map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IntersectionOfMultipleArrays {
    public static void main(String[] args) {
        int[][] nums= {{3,1,2,4,5},{1,2,3,4},{3,4,5,6}};
        List<Integer> intersection = intersection(nums);
        System.out.println("Intersaction - "+intersection);
    }

    private static List<Integer> intersection(int[][] nums) {
        int arr_length = nums.length;
        Map<Integer, Integer> map = new HashMap<>();
        for(int[] arr : nums){
            for(int i : arr){
                if(map.containsKey(i)){
                    map.put(i, map.getOrDefault(i, 0) + 1);
                }else{
                    map.put(i, 1);
                }
            }
        }
        return map.entrySet()
                .stream()
                .filter(entry-> entry.getValue()==arr_length)
                .map(entry-> entry.getKey())
                .collect(Collectors.toList());
    }
}
