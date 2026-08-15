package com.java.hime.stacks;

import java.util.Stack;

public class BackspaceStringCompare {
    public static void main(String[] args) {
        String s = "ab#c", t = "ad#c";  // "ab##", t = "c#d#"  // "a#c", t = "b"
        boolean backspaceCompare = backspaceCompare(s, t);
        String result = backspaceCompare == false ? "Not Equal" : "Both Pattern are Equals";
        System.out.println(result);
    }

    private static boolean backspaceCompare(String s, String t) {
        Stack<Character> sStack = new Stack<>();
        Stack<Character> tStack = new Stack<>();
        for(char ch : s.toCharArray()){
            if(ch=='#'){
                if(!sStack.isEmpty()){
                    sStack.pop();
                }
            }else{
                sStack.push(ch);
            }
        }
        for(char ch : t.toCharArray()) {
            if (ch == '#') {
                if (!tStack.isEmpty()) {
                    tStack.pop();
                }
            } else {
                tStack.push(ch);
            }
        }
        return sStack.equals(tStack);
    }
}
