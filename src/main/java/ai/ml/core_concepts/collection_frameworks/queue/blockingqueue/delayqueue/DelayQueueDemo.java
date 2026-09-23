package ai.ml.core_concepts.collection_frameworks.queue.blockingqueue.delayqueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayQueueDemo {

    public static void main(String[] args) throws InterruptedException {

        /**
         * Thread safe unbounded blocking queue
         * elements can only be taken from the queue when there delay has expired ...as example you insert 3 with some delay 4 with some delay
         * when their delay expired then only you can take it out from the queue
         * useful for scheduling tasks to be executed after a certain delay
         * internally uses priority queue
         */

        BlockingQueue<DelayTask> delayQueue = new DelayQueue<DelayTask>();  /** In delay queue we need to pass parameter type 'java.lang.String'
                                                                                is not within its bound */
        delayQueue.put(new DelayTask("Task 1", 5, TimeUnit.SECONDS));
        delayQueue.put(new DelayTask("Task 2", 3, TimeUnit.SECONDS));
        delayQueue.put(new DelayTask("Task 3", 10, TimeUnit.SECONDS));
        while(!delayQueue.isEmpty()){
            DelayTask delayTask = delayQueue.take();
            System.out.println("executed - "+delayTask.getTaskName()+" at "+System.currentTimeMillis());
        }
    }
}
class DelayTask implements Delayed {

    private final String taskName;
    private final long startTime;
    public DelayTask(String taskName, long delay, TimeUnit unit) {
        this.taskName = taskName;
        this.startTime = System.currentTimeMillis()+unit.toMillis(delay);
    }
    @Override
    public long getDelay(TimeUnit unit) {                     /** returns the remaining delay associated with this object in the given time unit*/
        long remaining = startTime - System.currentTimeMillis();
        return unit.convert(remaining, TimeUnit.MILLISECONDS);
    }
    @Override
    public int compareTo(Delayed o) {                         /** compares this object with the specified object for order return a negative integer
                                                                  zero or a positive integer as this object is less than , equal to or greater than the specified object
                                                                  */
        if(this.startTime < ((DelayTask)o).startTime) {
            return -1;
        }
        if(this.startTime > ((DelayTask)o).startTime) {
            return 1;
        }
        return 0;
    }

    public String getTaskName() {
        return taskName;
    }
}