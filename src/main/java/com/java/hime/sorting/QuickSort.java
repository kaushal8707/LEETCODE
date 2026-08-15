package com.java.hime.sorting;

public class QuickSort {

    public static void main(String[] args) {

        int nums[] = {5, 62, 2, 3, 11, 81, 4};
        int low = 0;
        int high = nums.length-1;
        System.out.println("\nBefore Sorting\n");
        for (int i:nums){
            System.out.print(i+" ");
        }
        System.out.println();

        quickSortOperation(nums, low, high);

        System.out.println("\nAfter Sorting\n");
        for (int i:nums){
            System.out.print(i+" ");
        }
        System.out.println();
    }

    private static void quickSortOperation(int[] nums, int low, int high) {

        if(low<high){
            int pi  = doPartition(nums, low, high);
            System.out.println(pi);
            quickSortOperation(nums, low, pi-1);
            quickSortOperation(nums, pi+1, high);
        }
    }

    private static int doPartition(int[] nums, int low, int high) {
        int pivot = nums[high];

        int i = low-1;

        for(int j=low; j<high; j++) {  // first in an iteration it will find all shortest element than pivot and move it to left and then move pivot so all left side smaller elements
            if(nums[j]<=pivot){
                i++;
                swapTwoValues(nums, i, j);
            }
        }
        swapTwoValues(nums, i+1, high);

        for(int k:nums){
            System.out.print(k+" ");
        }

        return i+1;
    }

    private static void swapTwoValues(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i]=nums[j];
        nums[j]=temp;
    }
}


// Time Complexity -
// Average: O(n log n)
// Best: O(n log n)
// Worst: O(n²)
// Extra space: O(log n) on average from recursion
// In-place: Yes
// Stable: No