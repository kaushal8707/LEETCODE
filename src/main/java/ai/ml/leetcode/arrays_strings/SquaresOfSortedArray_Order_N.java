package ai.ml.leetcode.arrays_strings;

import java.util.Arrays;

public class SquaresOfSortedArray_Order_N {
    public static void main(String[] args) {
       int nums[] = {-4,-1,0,3,10};   // [-7,-3,2,3,11]
        int[] sortedSquares = sortedSquares(nums);
        System.out.println(Arrays.toString(sortedSquares));
    }

    public static int[] sortedSquares(int[] nums) {
        int k = nums.length-1;
        int l=0;
        int r=nums.length-1;
        int result_array[]=new int[nums.length];
        while(l<r){
            if(Math.abs(nums[l]) > Math.abs(nums[r])){
                result_array[k] = (nums[l] * nums[l]);
                l++;
            }else{
                result_array[k] = (nums[r] * nums[r]);
                r--;
            }
            k--;
        }
        return result_array;
    }
}
