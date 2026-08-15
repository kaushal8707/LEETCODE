package com.java.hime.stacks;

import java.util.Stack;

class MinStack {
    Stack<Integer> mainStack;
    Stack<Integer> minStack;
    public MinStack() {
        mainStack = new Stack<>();
        minStack = new Stack<>();
    }

    public void push(int value) {
        mainStack.push(value);
        if(minStack.isEmpty() || value <= minStack.peek()){
            minStack.push(value);
        }
        minStack.push(minStack.peek());
    }

    public void pop() {
        if(mainStack.isEmpty()){
            throw new IllegalStateException("Stack Is Empty !!");
        }
        mainStack.pop();
        minStack.pop();
    }

    public int top() {
        if(!mainStack.isEmpty()){
            return mainStack.peek();
        }
        throw new IllegalStateException("stack is empty");
    }

    public int getMin() {
        return minStack.peek();
    }

    public static void main(String[] args) {
        MinStack stack=new MinStack();
        stack.push(5);
        stack.push(3);
        stack.push(7);
        System.out.println("Current Min - "+stack.getMin());
        stack.push(7);
        System.out.println("Current Min - "+stack.getMin());
        stack.pop();
        System.out.println("Top Element - "+stack.top());
        System.out.println("Current Min After pop- "+stack.getMin());


    }
}