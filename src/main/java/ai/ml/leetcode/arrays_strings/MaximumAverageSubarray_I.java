package ai.ml.leetcode.arrays_strings;

public class MaximumAverageSubarray_I {
    public static void main(String[] args) {
       int [] nums = {1,12,-5,-6,50,3};
       int k = 4;
        double maxAverage = findMaxAverage(nums, k);
        System.out.println("Maximum Average Sub Array I  "+maxAverage);
    }

    private static double findMaxAverage(int[] nums, int k) {
        int l = 0;
        double window = 0;
        double max = 0.0;
        for (int i=0; i<k; i++){
            window += nums[i];
        }
        max = window / k;
        for(int r=k; r<nums.length; r++){
            window += nums[r];
            window -= nums[r-k];

            double avg = window / k;
            max = Math.max(max, avg);
        }
        return max;
    }
}
