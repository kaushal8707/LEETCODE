package com.java.hime.hash_map;

import java.util.HashMap;
import java.util.Map;

public class MaxSumOfPairWithEqualSumOfDigits {
    public static void main(String[] args) {
        int[] nums = {18, 72, 43, 36, 13, 7, 54};
        int result = maximumSum(nums);
        System.out.println("Max Sum Of Pair With Equal Sum Of Digits - "+result);
    }

    private static int maximumSum(int[] nums) {
        Map<Integer, Integer> map = new HashMap<>();
        int max = -1;
        for(int i=0;i<nums.length;i++){
            int sDigit = sumOfDigits(nums[i]);
            if(map.containsKey(sDigit)){
                max = Math.max(max, nums[i] + map.get(sDigit));
                if(nums[i] > map.get(sDigit)){
                    map.put(sDigit, nums[i]);
                }
            }else{
                map.put(sDigit, nums[i]);
            }
        }
        return max;
    }

    private static int sumOfDigits(int num){
        int sum=0;
        while(num>0){
            sum += num % 10;
            num /= 10;
        }
        return sum;
    }
}
