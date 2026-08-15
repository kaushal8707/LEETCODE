package com.java.hime.stacks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class NextGreaterElement_I {
    public static void main(String[] args) {
        int[] nums1 = {4,1,2};   // num1 is subset of num2 so all elements in elements must contain in nums2 array
        int[] nums2 = {1,3,4,2};
        int[] nextGreaterElement = nextGreaterElement(nums1, nums2);
        System.out.println(Arrays.toString(nextGreaterElement));
    }
    private static int[] nextGreaterElement(int[] nums1, int[] nums2) {
        Stack<Integer> stack = new Stack<Integer>();
        Map<Integer, Integer> map = new HashMap();
        int resultArr[]=new int[nums1.length];
        for(int i=0; i<nums2.length;i++){
            if(!stack.isEmpty() && nums2[i] > stack.peek()){
                int popped = stack.pop();
                map.put(popped, nums2[i]);
            }
            stack.push(nums2[i]);  // after pop, we have to store the greater element
        }                          // Monotonic Decreasing Stack at end In stack will be 4 1nd 2
        for(int i : stack){
            map.put(i, -1);
        }
        for(int i=0;i<nums1.length;i++){
            resultArr[i] = map.get(nums1[i]);
        }
        return resultArr;
    }
}
