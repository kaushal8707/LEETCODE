package com.java.hime.hash_map;

import java.util.HashMap;
import java.util.Map;

public class BinarySubarraysWithSum {
    public static void main(String[] args) {
        int nums[] = {1,0,1,0,1};   // approach prefix sum
        int goal = 2;
        int numSubarraysWithSum = numSubarraysWithSum(nums, goal);
        System.out.println(numSubarraysWithSum);
    }

    private static int numSubarraysWithSum(int[] nums, int goal) {
        Map<Integer, Integer> map = new HashMap<>();
        int curr=0;
        int result = 0;
        map.put(0,1);
        for(int i : nums){
            curr += i;
            map.put(curr, map.getOrDefault(curr, 0) + 1);
            result += map.getOrDefault(curr-goal, 0);
        }
        return result;
    }
}
