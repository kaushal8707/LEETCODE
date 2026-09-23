package ai.ml.leetcode.miscellaneous;

public class NumberOfGoodPairs_BruteForceApproach {
    public static void main(String[] args) {
        int []nums = {1,2,3,1,1,3};
        int numIdenticalPairs = numIdenticalPairs(nums);
        System.out.println(numIdenticalPairs);
    }

    private static int numIdenticalPairs(int[] nums) {
        int goodPairCount = 0;
        for(int i=0;i<nums.length-1; i++){
            for(int j=i+1; j<nums.length; j++){
                if(nums[i]==nums[j]){
                    goodPairCount++;
                }
            }
        }
        return goodPairCount;
    }
}
