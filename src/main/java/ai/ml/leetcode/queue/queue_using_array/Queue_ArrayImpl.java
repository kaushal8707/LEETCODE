package ai.ml.leetcode.queue.queue_using_array;

/*
    But: this still cannot reuse the positions freed at the beginning. For a proper array-based queue,
    the next improvement would be a circular queue, where rear wraps back to index 0
 */
public class Queue_ArrayImpl {
    int front;
    int rear;
    int size;
    int arr[];
    Queue_ArrayImpl(){
        front=0;
        rear=-1;
        size=0;
        arr=new int[4];
    }
    private void enqueue(int x){
        if(isFull()){
            System.out.println("Queue is Full!!");
            return;
        }
        rear++;
        arr[rear] = x;
        size++;
    }
    private int dequeue(){
        if(isEmpty()){
            System.out.println("Queue is Empty");
            return -1;
        }
        int data = arr[front];
        front++;
        size--;
        return data;
    }
    private int peek(){
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
        for(int i=front; i<=rear; i++){
            System.out.print(arr[i]+" ");
        }
    }

    private boolean isEmpty(){
        return size==0;
    }
    private boolean isFull(){
        return size==arr.length;
    }
    public static void main(String[] args) {
        Queue_ArrayImpl queue=new Queue_ArrayImpl();
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
