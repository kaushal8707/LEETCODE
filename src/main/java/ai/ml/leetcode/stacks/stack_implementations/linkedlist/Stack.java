package ai.ml.leetcode.stacks.stack_implementations.linkedlist;

public class Stack {

    Node top=null;
    int size=0;

    class Node{
        int data;
        Node next;
        Node(int data){
            this.data=data;
            this.next=null;
        }
    }

    //push()
    private void push(int data){
        Node node=new Node(data);
        node.next=top;
        top=node;
        size++;
    }

    //pop()
    private int pop(){
        if(isEmpty()){
            System.out.println("List is EMPTY !!!");
            return -1;
        }
        int popped = top.data;
        top=top.next;
        size--;
        return popped;
    }

    //peek()
    private int peek(){
        if(isEmpty()){
            System.out.println("List is EMPTY !!!");
        }
        int peeked = top.data;
        return peeked;
    }

    //display()
    private void display(){
        if(isEmpty()){
            System.out.println("List is EMPTY !!!");
        }
        Node current = top;
        while(current!=null){
            int data = current.data;
            current=current.next;
            System.out.print(data+"  ");
        }
        System.out.println();
    }

    //empty
    private boolean isEmpty(){
        return top==null;
    }

    //size()
    private int size(){
        return size;
    }

    //Stack Implementation Using Linked List
    public static void main(String[] args) {
        Stack stack=new Stack();
        stack.push(10);
        stack.push(20);
        stack.push(30);
        stack.display();
        System.out.println("Peek - "+stack.peek());
        System.out.println("Size - "+stack.size());
        System.out.println("Popped out element - "+stack.pop());
        stack.display();
        System.out.println("Peek - "+stack.peek());
        System.out.println("Size - "+stack.size());
        System.out.println("Is empty :"+stack.isEmpty());

        System.out.println("Popped out element - "+stack.pop());
        System.out.println("Popped out element - "+stack.pop());

        System.out.println("Is empty :"+stack.isEmpty());
        System.out.println("Popped out element - "+stack.pop());
        System.out.println("Size - "+stack.size());

    }
}
