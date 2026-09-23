package ai.ml.leetcode.sorting_algos;

public class BubbleSortOperation {

    public static void main(String[] args) {
        int nums[] = {5,1,2,8,3,7,4,9,6};
        bubbleSortOperation(nums);
        for(int num : nums) {
            System.out.print(num+" ");
        }
    }
    private static void bubbleSortOperation(int[] nums) {
        for (int i = 0; i < nums.length - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < nums.length - 1 - i; j++) {
                if (nums[j] > nums[j + 1]) {
                    int temp = nums[j];
                    nums[j] = nums[j + 1];
                    nums[j + 1] = temp;
                    swapped = true;
                }
            }

            if (!swapped) {
                break;
            }
        }
    }
}

