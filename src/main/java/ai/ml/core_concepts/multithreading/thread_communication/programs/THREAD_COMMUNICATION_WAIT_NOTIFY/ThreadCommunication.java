package ai.ml.core_concepts.multithreading.thread_communication.programs.THREAD_COMMUNICATION_WAIT_NOTIFY;

public class ThreadCommunication {
    public static void main(String[] args) {
        SharedResource resource = new SharedResource();
        Thread producerThread = new Thread(new Prooducer(resource));
        Thread consumerThread = new Thread(new Consumer(resource));
        producerThread.start();
        consumerThread.start();
    }
}
