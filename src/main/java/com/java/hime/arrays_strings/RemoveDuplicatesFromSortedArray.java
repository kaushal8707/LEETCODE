package com.java.hime.arrays_strings;

import java.util.Arrays;

public class RemoveDuplicatesFromSortedArray {
    public static void main(String[] args) {
        int nums[]={0,0,1,1,1,2,2,3,3,4};
        int removedDuplicatesArr = removeDuplicates(nums);
        System.out.println("Size of Unique Elements Array : "+removedDuplicatesArr);
    }

    private static int removeDuplicates(int[] nums) {
        int j=0;
        int i=1;
        while(i < nums.length){
            if(nums[i]!=nums[i-1]){
                j++;
                nums[j] = nums[i];}
            i++;
        }

        // let's print unique elements from an array
        int k=0;
        while(k<=j){
            System.out.println(nums[k]+"  ");
            k++;
        }
        return j+1;
    }
}
