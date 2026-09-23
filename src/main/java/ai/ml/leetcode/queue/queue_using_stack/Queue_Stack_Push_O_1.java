package ai.ml.leetcode.queue.queue_using_stack;
import java.util.Stack;

public class Queue_Stack_Push_O_1 {
    Stack<Integer> inputStack;
    Stack<Integer> outputStack;
    Queue_Stack_Push_O_1(){
        inputStack=new Stack<>();
        outputStack=new Stack<>();
    }
    private void push(int x){
        inputStack.push(x);
    }
    private int pop(){
        if(outputStack.empty()){
            while(!inputStack.isEmpty()) {
                outputStack.push(inputStack.pop());
            }
        }
        return outputStack.pop();
    }
    private int peek(){
        if(outputStack.empty()){
            while(!inputStack.isEmpty()) {
                outputStack.push(inputStack.pop());
            }
        }
        return outputStack.peek();
    }

    private boolean empty(){
        return inputStack.isEmpty()
                &&
               outputStack.isEmpty();
    }
    public static void main(String[] args) {
        Queue_Stack_Push_O_1 q = new Queue_Stack_Push_O_1();
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
