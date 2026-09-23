package ai.ml.leetcode.miscellaneous;

import java.util.Stack;

public class MakeStringGreat {
    public static void main(String[] args) {
        String s="leEeetcode";
        String s1="abBAcC";
        String makeGood = makeGood(s);
        System.out.println(makeGood);
    }
    private static String makeGood(String s) {
        Stack<Character> stack = new Stack();
        for(Character ch : s.toCharArray()){
            if(!stack.isEmpty() && Math.abs(stack.peek()-ch)==32){
                stack.pop();
            }else{
                stack.push(ch);
            }
        }
       StringBuilder sb=new StringBuilder();
        for(char c:stack){
            sb.append(c);
        }
        return sb.toString();
    }
}
