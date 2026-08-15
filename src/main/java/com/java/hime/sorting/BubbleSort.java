package com.java.hime.sorting;

/**
 *  1st loop = Iteration
 *   2nd loop = compare and swapping ( multiple swapping in bubble sort )
 */


public class BubbleSort {

    public static void main(String[] args) {

        int nums[] = {5,1,2,8,3,7,4,9,6};
        System.out.println("---Before sorting---");

        for(int num : nums) {
            System.out.print(num+" ");
        }
        System.out.println("\n\nsorting starts->\n");

        bubbleSortOperation(nums);

        System.out.println("\n\n------After sorting------\n");
        for(int num : nums) {
            System.out.print(num+" ");
        }

    }

    private static void bubbleSortOperation(int[] nums) {

        for(int i=0; i<nums.length; i++) {               //Iteration loop

            for(int j=0; j< nums.length-1-i; j++) {      // swapping loop
                // iterating till (length-1-i) -> each iteration last elements get sorted so no need to check
                if(nums[j]>nums[j+1]) {

                    int temp = nums[j];
                    nums[j]=nums[j+1];
                    nums[j+1]=temp;
                }
            }
            /** after each Iteration */
            for(int num : nums) {
                System.out.print(num+" ");
            }
            System.out.println();
        }

    }
}
