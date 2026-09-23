package ai.ml.leetcode.arrays_strings;

public class MinimumSizeSubarraySum {
    public static void main(String[] args) {
        int target = 7;  //4
        int nums[] = {2,3,1,2,4,3};  // [1,4,4]
        int minSubArrayLen = minSubArrayLen(target, nums);
        System.out.println(minSubArrayLen);
    }

    private static int minSubArrayLen(int target, int[] nums) {
        int left = 0;
        int window = 0;
        int min = nums.length+1;
        for (int right=0 ; right<nums.length; right++){
            window += nums[right];

            while(window >= target) {
                min = Math.min(min, right-left+1);
                window -= nums[left];
                left ++;
            }
        }
        return min;
    }
}
