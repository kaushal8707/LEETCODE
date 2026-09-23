package ai.ml.leetcode.arrays_strings;

import java.util.Arrays;

public class TwoSum_II_InputArrayIsSorted {
    public static void main(String[] args) {
        int numbers [] = {2,7,11,15};  //2,3,4]
        int target = 9;   //6
        int[] twoSum = twoSum(numbers, target);
        System.out.println(Arrays.toString(twoSum));
    }

    private static int[] twoSum(int[] numbers, int target) {
        int l = 0;
        int r = numbers.length -1;
        while( l < r){
            if(numbers[l] + numbers[r] < target){
                l++;
            }else if (numbers[l] + numbers[r] > target){
                r--;
            }else{
                return new int[] {l+1, r+1};
            }
        }
        return null;

    }
}
