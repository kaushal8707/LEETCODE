package com.java.hime.stacks.monotonic_stack;

import java.util.Arrays;
import java.util.Stack;

public class MonotonicIncreasingStack {
    public static void main(String[] args) {
        int nums[]={1, 7, 9, 5};
        int[] monotonicIncreasingStack = monotonicIncreasingStack(nums);
        System.out.println(Arrays.toString(monotonicIncreasingStack));
    }

    private static int[] monotonicIncreasingStack(int[] nums) {
        Stack<Integer> stack = new Stack();
        for(int i=0;i<nums.length;i++){
            while(!stack.isEmpty() && nums[i] < stack.peek()){
                stack.pop();
            }
            stack.push(nums[i]);
        }
        return stack.stream().mapToInt(Integer::intValue).toArray();
    }
}
