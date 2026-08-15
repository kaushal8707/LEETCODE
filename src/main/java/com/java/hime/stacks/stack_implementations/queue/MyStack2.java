package com.java.hime.stacks.stack_implementations.queue;
import java.util.LinkedList;
import java.util.Queue;

//Stack Implementation Using a Double Queue
// queue1 - supporting queue, queue2 - task performing queue

public class MyStack2 {
    Queue<Integer> queue1;
    Queue<Integer> queue2;
    MyStack2(){
        queue1=new LinkedList();
        queue2=new LinkedList();
    }
    //push
    private void push(int x){
        while(!queue2.isEmpty()){
            queue1.add(queue2.poll());
        }
        queue2.add(x);
        while(!queue1.isEmpty()){
            queue2.add(queue1.poll());
        }
    }
    //pop
    private int pop(){
        return queue2.poll();
    }
    //peek
    private int peek(){
        return queue2.peek();
    }
    //empty
    private boolean empty(){
        return queue2.isEmpty();
    }
    //size
    private int size(){
        return queue2.size();
    }
    public static void main(String[] args) {
        MyStack2 stack=new MyStack2();
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