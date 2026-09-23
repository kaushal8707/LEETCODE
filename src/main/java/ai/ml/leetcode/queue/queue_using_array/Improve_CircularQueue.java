package ai.ml.leetcode.queue.queue_using_array;

public class Improve_CircularQueue {
    int front;
    int rear;
    int size;
    int arr[];
    Improve_CircularQueue(){
        front=0;
        rear=-1;
        size=0;
        arr=new int[4];
    }
    private boolean isEmpty(){
        return size==0;
    }
    private boolean isFull(){
        return size==arr.length;
    }
    private void enqueue(int x){
        if(isFull()){
            System.out.println("Queue is Full!!");
            return;
        }
        // Move rear circularly
        rear = rear + 1 % arr.length;
        arr[rear] = x;
        size++;
    }
    private int dequeue(){
        if (isEmpty()) {
            System.out.println("Queue is Empty");
            return -1;
        }
        int data = arr[front];
        // Move front circularly
        front = front + 1 % arr.length;
        size--;
        return data;
    }
    private int peek() {
        if (isEmpty()) {
            System.out.println("Queue is Empty");
            return -1;
        }
        return arr[front];
    }
    private void printQueue(){
        if (isEmpty()) {
            System.out.println("Queue is Empty");
            return;
        }
        int index = front;
        for(int i=0; i<size; i++){
            System.out.print(arr[index]+" ");
            index = index + 1 % arr.length;
        }
    }

    public static void main(String[] args) {
        Improve_CircularQueue queue=new Improve_CircularQueue();
        queue.enqueue(11);
        queue.enqueue(22);
        queue.enqueue(33);
        queue.enqueue(43);
        queue.enqueue(111);

        System.out.println("\nAvailable in Queue");
        queue.printQueue();

        int deQueueElement =queue.dequeue();
        System.out.print("\nDequeue Element - "+deQueueElement);

        int deQueueEle =queue.dequeue();
        System.out.print("\nDequeue Element - "+deQueueEle);

        System.out.println("\nAvailable in Queue");
        queue.printQueue();

        int peekedElement =queue.peek();
        System.out.print("\n Peek Element - "+peekedElement);

        System.out.println("\nAvailable in Queue");
        queue.printQueue();
    }
}
