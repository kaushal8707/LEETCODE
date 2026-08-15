package com.java.hime.arrays_strings;

import java.util.HashMap;
import java.util.Map;

public class MaxSumPairWithEqualSumofDigits {
    public static void main(String[] args) {
        int[] nums = {18, 72, 43, 36, 13, 7, 54};
        int maximumSum = maximumSum(nums);
        System.out.println("Max Sum Pair With Equal Sum of Digits - "+maximumSum);
    }

    private static int maximumSum(int[] nums) {
        int i=0;
        int max = 0;
        Map<Integer, Integer> map = new HashMap();
        while(i<nums.length){
            int dsum = sumOfDigits(nums[i]);
            int data = map.getOrDefault(dsum, -1);
            if(data!=-1){
                int sum = data + nums[i];
                if(nums[i] > data){
                    map.put(dsum, nums[i]);
                }
                max = Math.max(max, sum);
            }else{
                map.put(dsum, nums[i]);
            }

            i++;
        }
        return max;
    }

    private static int sumOfDigits(int num){
        int sum = 0;
        while(num!=0){
            sum += num % 10;
            num /= 10;
        }
        return sum;
    }
}
