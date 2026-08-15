package com.java.hime.hash_map;

import java.util.HashMap;
import java.util.Map;

public class SubarraySumEquals_K {
    public static void main(String[] args) {
       int[] nums = {1,1,1}; // {1,1,1}  {1,2,3}  {1, 1, 2, 4, 5, 5, 9, 10,13}
       int k = 2; // 2   3
       int subarraySum = subarraySum(nums, k);
       System.out.println(subarraySum);
    }

    private static int subarraySum(int[] nums, int k) {
        Map<Integer, Integer> map = new HashMap();
        int cPrefix = 0;
        int result = 0;
        map.put(0, 1);   // In prefix sum it is not counting first index
        for(int i : nums){
            cPrefix += i;   //find prefix sum -> {1,1,1}->  {1,2,3}-> and suppose k=2
            map.put(cPrefix, map.getOrDefault(cPrefix, 0)+1);
            result += map.getOrDefault(cPrefix - k, 0);  // find count prefix_sum -k
        }
        return result;
    }
}
