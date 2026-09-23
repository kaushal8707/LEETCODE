package ai.ml.leetcode.queue.queue_using_linkedlist;

public class Queue_Using_LinkedList {
    Node front = null;
    Node rear = null;

    private void add(int data) {
        Node newNode = new Node(data);
        if(isEmpty()){
            front = rear = newNode;
            return;
        }
        rear.next = newNode;
        rear = newNode;
    }

    private int remove() {
        if(isEmpty()){
            System.out.println("queue is empty !!");
            return -1;
        }
        if(front==rear){
            rear=null;
        }
        Node temp = front;
        front = front.next;
        return temp.data;
    }

    private int peek() {
        if(isEmpty()){
            System.out.println("queue is empty !!");
            return -1;
        }
        Node temp = front;
        return temp.data;
    }

    private boolean isEmpty(){
        return front == null &&
                rear == null;
    }

    public static void main(String[] args) {
        Queue_Using_LinkedList queue = new Queue_Using_LinkedList();
        queue.add(11);
        queue.add(22);
        queue.add(33);

        while(!queue.isEmpty()) {
            System.out.println(queue.peek());
            queue.remove();
        }
    }
}
