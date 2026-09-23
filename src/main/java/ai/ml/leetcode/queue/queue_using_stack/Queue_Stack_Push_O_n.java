package ai.ml.leetcode.queue.queue_using_stack;

import java.util.Stack;

public class Queue_Stack_Push_O_n {
    Stack<Integer> stack1;
    Stack<Integer> stack2;
    Queue_Stack_Push_O_n(){
        stack1 = new Stack<>();
        stack2 = new Stack<>();
    }
    private void push(int x){
        while(!stack2.isEmpty()){
            stack1.push(stack2.pop());
        }
        stack2.push(x);
        while(!stack1.isEmpty()){
            stack2.push(stack1.pop());
        }
    }
    private int pop(){
        return stack2.pop();
    }
    private int peek(){
        return stack2.peek();
    }
    private boolean empty(){
        return stack2.isEmpty();
    }


    public static void main(String[] args) {
        Queue_Stack_Push_O_n q = new Queue_Stack_Push_O_n();
        q.push(1);
        q.push(2);
        q.push(3);

        System.out.println("Front element is: " + q.peek()); // Output: 1
        System.out.println("Removed element is: " + q.pop()); // Output: 1
        System.out.println("Removed element is: " + q.pop()); // Output: 2
        System.out.println("Is queue empty? " + q.empty()); // Output: false

        q.push(4);
        System.out.println("Removed element is: " + q.pop()); // Output: 3
        System.out.println("Removed element is: " + q.pop()); // Output: 4
        System.out.println("Is queue empty? " + q.empty()); // Output: true
    }
}
