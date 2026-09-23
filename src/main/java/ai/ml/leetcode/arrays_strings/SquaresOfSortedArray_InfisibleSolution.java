package ai.ml.leetcode.arrays_strings;
import java.util.Arrays;


public class SquaresOfSortedArray_InfisibleSolution {
    public static void main(String[] args) {
        int nums[] = {-4, -1, 0, 3, 10};   // [-7,-3,2,3,11]
        int[] sortedSquares = sortedSquares(nums);
        System.out.println(Arrays.toString(sortedSquares));
    }

    public static int[] sortedSquares(int[] nums) {
        int[] array = Arrays.stream(nums)
                .map(i -> i * i)
                .toArray();
        Arrays.sort(array);
        return array;
    }
}