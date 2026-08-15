package com.java.hime.hash_map;

import java.util.HashMap;
import java.util.Map;

public class CountNumberOfNiceSubarrays {
    public static void main(String[] args) {
        int nums[] = {1,1,2,1,1};  // 2,4,6    1,2,3
        int k = 3;  // k=1    k=3
        int numberOfSubarrays = numberOfSubarrays(nums, k);
        System.out.println("Number of Nice Sub Arrays - "+numberOfSubarrays);
    }

    private static int numberOfSubarrays(int[] nums, int k) {
        Map<Integer, Integer> map = new HashMap();
        int res = 0;
        int preSumOddNum = 0;
        map.put(0,1);
        for (int i : nums) {
            preSumOddNum += i % 2;
            map.put(preSumOddNum, map.getOrDefault(preSumOddNum, 0) + 1);
            res += map.getOrDefault(preSumOddNum - k, 0);
        }
        return res;
    }
}
