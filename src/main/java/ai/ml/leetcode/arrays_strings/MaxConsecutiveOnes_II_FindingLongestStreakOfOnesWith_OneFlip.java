package ai.ml.leetcode.arrays_strings;

public class MaxConsecutiveOnes_II_FindingLongestStreakOfOnesWith_OneFlip {
    public static void main(String[] args) {
       int []nums = {1,0,1,1,0};   //{1,0,1,1,0,1,1} // op:  4  <- max consecutive one's with at most one flip with Zero
        int maxConsecutiveOnes = findMaxConsecutiveOnes(nums);
        System.out.println("Max Consecutive Ones I : "+maxConsecutiveOnes);
    }

    public static int findMaxConsecutiveOnes(int[] nums) {
        int l=0;
        int window = 0;
        int r = 0;
        int max = -1;
        for(; r < nums.length; r++){

            window += nums[r];

            while(!(window == r-l || window == r-l+1)) {    // -> one flip means (r-l+1) -1 => r-l bcz we are counting at max one zero in our windows
              window -= nums[l];
              l++;
            }
            max = Math.max(max, r-l+1);
        }
        return max;
    }
}
