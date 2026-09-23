package ai.ml.leetcode.stacks.stack_implementations.array;

// Stack Implementation Using Array

import java.util.NoSuchElementException;

public class MyStack {

    int[] arr;
    int top;
    int capacity;
    MyStack(int size){
        top=-1;
        capacity=size;
        arr = new int[capacity];
    }

    //push()
    private void push(int x){
        if(isFull()){
            throw new StackOverflowError("Stack is Full ! can't Push data !!");
        }
        arr[++top] = x;
        System.out.println("Inserted element: "+x);
    }
    //pop()
    private int pop(){
        if(isEmpty()){
            throw new NoSuchElementException("No Element Found !! Stack is EMPTY");
        }
        int popped = arr[top--];
        return popped;
    }
    //pop()
    private int peek(){
        if(isEmpty()){
            throw new NoSuchElementException("No Element Found !! Stack is EMPTY");
        }
        return arr[top];
    }
    //display
    private void display(){
        if(isEmpty()){
            throw new NoSuchElementException("No Element Found !! Stack is EMPTY");
        }
        for(int i=top;i>-1;i--){
            System.out.print(arr[i]+"  ");
        }
        System.out.println();
    }

    //size
    private int size(){
        return top+1;
    }
    //isFull()
    private boolean isFull(){
        return top == capacity-1;
    }
    //isEmpty()
    private boolean isEmpty(){
        return top == -1;
    }

    // main   - stack implementation using array
    public static void main(String[] args) {
        MyStack stack=new MyStack(4);
        stack.push(10);
        stack.push(20);
        stack.push(30);
        stack.push(40);
        stack.display();
        System.out.println(stack.pop());
        System.out.println(stack.peek());
        System.out.println(stack.pop());
        stack.display();
        System.out.println(stack.size());

    }
}
