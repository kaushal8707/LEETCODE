package com.java.hime.hash_map;

import java.util.HashMap;
import java.util.Map;

public class CountNumberofPairsWithAbsoluteDifference_K {
    public static void main(String[] args) {
        int[] nums = {1,2,2,1};  // 3,2,1,5,4      1,3
        int k = 1;   // 2    3
        int countKDifference = countKDifference(nums, k);
        System.out.println(countKDifference);
    }
    private static int countKDifference(int[] nums, int k) {
        Map<Integer, Integer> map = new HashMap<>();
        int res = 0;
        for(int i=0; i<nums.length;i++){
            res += map.getOrDefault(nums[i]+k, 0);
            res += map.getOrDefault(nums[i]-k, 0);
            map.put(nums[i], map.getOrDefault(nums[i], 0) + 1);
        }
        return res;
    }
}
/**
 * The idea is to count the frequency of each number in a hash map or dictionary as we go through the array Iterate
 * over the array and for each element arr[i], we need another element say complement such
 * that abs(arr[i] - complement) = k. Now, we can have two cases:
 *
 *  (arr[i] - complement) is positive:
 * arr[i] - complement = k
 * So, complement = arr[i] - k
 * (arr[i] - complement) is negative:
 * (arr[i] - complement) = -k
 * So, complement = arr[i] + k
 * So for each element arr[i], we can check if complement (arr[i] + k) or (arr[i] - k) is present in the hash map.
 * If it is, increment the count variable by the occurrences of complement in map.
 */