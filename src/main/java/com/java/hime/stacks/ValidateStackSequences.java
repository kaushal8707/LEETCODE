package com.java.hime.stacks;
import java.util.Stack;

public class ValidateStackSequences {
    public static void main(String[] args) {
        int[] pushed = {1,2,3,4,5};  //1,2,3,4,5]
        int[] popped = {4,5,3,2,1};  //[4,3,5,1,2]
        boolean validateStackSequences = validateStackSequences(pushed, popped);
        System.out.println(validateStackSequences);
    }
    private static boolean validateStackSequences(int[] pushed, int[] popped) {
        Stack<Integer> stack = new Stack<>();
        int j=0;
        for(int i=0;i<pushed.length;i++){
            stack.push(pushed[i]);
            while(!stack.isEmpty() &&
                    stack.peek()==popped[j]){
                stack.pop();
                j++;
            }
        }
        return stack.isEmpty();
    }
}
//TC - O(N)
