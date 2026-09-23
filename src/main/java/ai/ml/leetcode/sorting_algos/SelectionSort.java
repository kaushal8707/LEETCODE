package ai.ml.leetcode.sorting_algos;

/**
 Selection Sort is a straightforward, in-place comparison sorting algorithm that repeatedly
 finds the minimum element from the unsorted section of an array and places it at the beginning.
 It partitions the array into two logical segments: a sorted section on the left and an unsorted section on the right

 */

public class SelectionSort {
    public static void main(String[] args) {
        int nums[]= {5,1,2,8,3,7,4,9,6};
        selectionSortDemo(nums);
        for(int i : nums){
            System.out.print(i+"  , ");
        }
    }

    //once find min element swap with outer-loop iterating position start with oth index

    private static void selectionSortDemo(int[] nums) {
        for(int i=0; i<nums.length-1; i++){
            int minIndex = i;

            for(int j=i+1; j<nums.length; j++){
                if(nums[j]<nums[minIndex]){
                    minIndex = j;
                }
            }
            int temp = nums[i];
            nums[i] = nums[minIndex];
            nums[minIndex] = temp;
        }
    }
}


/*
Step-by-Step ExampleLet's sort the array: [29, 10, 14, 37, 13]
Pass 1: The minimum from index 0 to 4 is 10. Swap 10 with 29.[10 | 29, 14, 37, 13]
Pass 2: The minimum from index 1 to 4 is 13. Swap 13 with 29.[10, 13 | 14, 37, 29]
Pass 3: The minimum from index 2 to 4 is 14. It is already in place. No swap is needed.[10, 13, 14 | 37, 29]
Pass 4: The minimum from index 3 to 4 is 29. Swap 29 with 37.[10, 13, 14, 29 | 37]
Result: The last element is inherently sorted. Final sorted list: [10, 13, 14, 29, 37].
 */