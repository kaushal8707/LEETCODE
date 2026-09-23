package ai.ml.leetcode.miscellaneous;


// because we are not counting the number we are counting the number of pairs

import java.util.Arrays;

public class NumberOfGoodPairs {
    public static void main(String[] args) {
        int []nums = {1,2,3,1,1,3};
        int numIdenticalPairs = numIdenticalPairs(nums);
        System.out.println("numIdenticalPairs = "+numIdenticalPairs);
    }
    private static int numIdenticalPairs(int[] nums) {
        int goodPairCount = 0;
        int[] frequencyMap = new int[101];

        for (int i = 0; i < nums.length; i++) {
            goodPairCount = goodPairCount + frequencyMap[nums[i]];
            frequencyMap[nums[i]]++;
        }
        return goodPairCount;
    }
}
