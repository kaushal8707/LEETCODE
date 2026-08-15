package com.java.hime.sorting;

/**
 *  is a simple, comparison-based sorting algorithm that builds a final sorted array one item at a time.
 *  It works similarly to the way you might organize a hand of playing cards: you pick up an unsorted card,
 *  compare it to the cards already in your hand, and insert it into its correct position.
 */
public class InsertionSort {
    public static void main(String[] args) {
        int nums[] = {5,1,2,8,3,7,4,9,6};
        insertionSort(nums);
        for(int i : nums){
            System.out.print(i+" ");
        }
    }

    private static void insertionSort(int[] nums) {

        for(int i=1; i<nums.length; i++){
            int key = nums[i];
            int j = i-1;

            while(j>=0 && nums[j] > key){
                nums[j+1] = nums[j];
                j--;
            }
            nums[j+1] = key;
        }
    }
}
