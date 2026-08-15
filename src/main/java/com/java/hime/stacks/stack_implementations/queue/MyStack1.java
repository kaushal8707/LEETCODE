package com.java.hime.stacks.stack_implementations.queue;

//Stack Implementation Using a Single Queue

import java.util.LinkedList;
import java.util.Queue;

public class MyStack1 {

    private Queue<Integer> queue;
    MyStack1(){
        queue = new LinkedList<>();
    }
    //push
    private void push(int x){
       int size =  queue.size();
       queue.add(x);
       while(size>0){
           queue.add(queue.poll());
           size--;
       }
    }
    //pop
    private int pop(){
        return queue.poll();
    }
    //peek
    private int peek(){
        return queue.peek();
    }
    //empty
    private boolean empty(){
        return queue.isEmpty();
    }
    //main - Implementing stack using a single queue
    public static void main(String[] args) {
        MyStack1 stack=new MyStack1();
        stack.push(11);
        stack.push(22);
        stack.push(44);
        stack.push(55);
        System.out.println(stack.peek());
        System.out.println(stack.pop());
        System.out.println(stack.peek());
        System.out.println(stack.empty());
        System.out.println("----------------------");
        while(!stack.empty()){
            System.out.println(stack.pop());
        }
    }
}
//TC - O(n) - atleast 1 time each element performing push/pop operations