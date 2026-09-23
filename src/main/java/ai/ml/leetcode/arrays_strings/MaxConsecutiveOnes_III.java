package ai.ml.leetcode.arrays_strings;

public class MaxConsecutiveOnes_III {
    public static void main(String[] args) {
        int nums[] = {1,1,1,0,0,0,1,1,1,1,0};  // {0,0,1,1,0,0,1,1,1,0,1,1,0,0,0,1,1,1,1}
        int k = 2;                             // k=3
        int longestOnes = longestOnes(nums, k);
        System.out.println("Max Consecutive Ones III "+longestOnes);
    }

    private static int longestOnes(int[] nums, int k) {
        int l=0;
        int r=0;
        int window = 0;
        int max=-1;
        for( ;r< nums.length; r++){
            window += nums[r];
            while(window + k < r-l+1){
                window -= nums[l];
                l++;
            }
            max = Math.max(max, r-l+1);
        }
        return max;
    }
}
