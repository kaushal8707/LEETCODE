package ai.ml.leetcode.miscellaneous;

import java.util.Arrays;

public class MajorityElements_Solved_In_NlogN_Approach {
    public static void main(String[] args) {
        int nums[] = {2,2,1,1,1,2,2};  // 3,2,3
        int majorityElements = findmajorityElements(nums);
        System.out.println(majorityElements);
    }
    private static int findmajorityElements(int[] nums) {
        Arrays.sort(nums);
        int result = nums[nums.length / 2];
        return result;
    }
}

// Time Complexity - n log(n)