package ai.ml.leetcode.hash_map;

import java.util.HashSet;
import java.util.Set;

/**
 * find the subarray with maximum sum that has unique elements
 * Better can solve using sliding window
 */
public class MaximumErasureValue {
    public static void main(String[] args) {
        int []nums = {4,2,4,5,6};   // [5,2,1,2,5,2,1,2,5]    // op -  17   8
        int maximumUniqueSubarray = maximumUniqueSubarray(nums);
        System.out.println(maximumUniqueSubarray);
    }
    private static int maximumUniqueSubarray(int[] nums) {
        Set<Integer> set=new HashSet<>();
        int max=0;
        int sum=0;
        int left=0;
        for(int right=0; right<nums.length; right++){
            while(set.contains(nums[right])){
               set.remove(nums[left]);
               sum-=nums[left];
               left++;
            }
            set.add(nums[right]);
            sum+=nums[right];
            max=Math.max(max, sum);
        }
        return max;
    }
}
