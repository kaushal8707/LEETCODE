package com.java.hime.stacks;

import java.util.Stack;

public class ValidParentheses {
    public static void main(String[] args) {
        String s = "()[]{}";  // ([)]  ()  (]  ([])
        boolean isValid = isValid(s);
        System.out.println("Is Pattern Valid ? : "+isValid);
    }

    private static boolean isValid(String s) {
        Stack<Character> stack = new Stack<>();
        for(char ch : s.toCharArray()){
            if(isLeftParenthesis(ch)){
                stack.push(ch);
            }
            Stack<Character> cStack = validateParenthesis(stack, ch);
            if(cStack.isEmpty()){
                return true;
            }
        }
        return false;
    }

    private static Stack<Character> validateParenthesis(Stack<Character> stack, char ch) {
        if(ch == ')' && stack.peek()=='('){
            stack.pop();
        }else if(ch == '}' && stack.peek()=='{'){
            stack.pop();
        }else if(ch == ']' && stack.peek()=='['){
            stack.pop();
        }
        return stack;
    }

    private static boolean isLeftParenthesis(char ch) {
        switch(ch){
            case '(':
            case '{':
            case '[':
                return true;
            default:
                return false;
        }
    }
}
