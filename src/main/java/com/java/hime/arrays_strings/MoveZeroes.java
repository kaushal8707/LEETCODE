package com.java.hime.arrays_strings;

import java.util.Arrays;

public class MoveZeroes {
    public static void main(String[] args) {
        int nums[] = {1,6,0,0,0,0,4,5}; // {[0,1,0,3,12]}
        moveZeroes(nums);
    }

    private static void moveZeroes(int[] nums) {
        int j=0;
        for(int i=0;i<nums.length;i++){
            if(nums[i]!=0){
                int temp = nums[i];      // when nums position num is 0 we skip and continue otw we will swap iterating elements of i from jth index position elements which which will increase index whenever non zero elements founds
                nums[i]=nums[j];
                nums[j]=temp;

                j++;
            }
        }
        System.out.println(Arrays.toString(nums));
    }
}
