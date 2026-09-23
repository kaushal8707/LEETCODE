package ai.ml.core_concepts.collection_frameworks.queue.non_blocking_thread_safe_queue.concurrent_linked_dequeue;

import java.util.concurrent.ConcurrentLinkedDeque;

public class ConcurrentLinkedDequeueDemo {
    public static void main(String[] args) {

        // non-blocking, thread safe doubled-ended queue
        // CAS - use compare & swap technique

        ConcurrentLinkedDeque<String> deque = new ConcurrentLinkedDeque<>();
        deque.add("Element1");
        deque.addFirst("Element0");
        deque.addLast("Element2");
        System.out.println(deque);

        deque.removeFirst();
        deque.removeLast();

        System.out.println(deque);
    }
}
