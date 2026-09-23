package ai.ml.leetcode.arrays_strings;

public class MaxConsecutiveOnes {
    public static void main(String[] args) {
       int []nums = {1,1,0,1,1,1}; //[1,0,1,1,0,1]  // op:  3  <- max consecutive one's
        int maxConsecutiveOnes = findMaxConsecutiveOnes(nums);
        System.out.println("Max Consecutive Ones I : "+maxConsecutiveOnes);
    }

    public static int findMaxConsecutiveOnes(int[] nums) {

        int l=0;
        int window = 0;
        int max=0;

        for (int r = 0; r<nums.length; r++) {
            window += nums[r];
            while(window != r-l+1){
                window -= nums[l];
                l++;
            }
            max = Math.max(max, r-l+1);
        }
        return max;
    }
}
