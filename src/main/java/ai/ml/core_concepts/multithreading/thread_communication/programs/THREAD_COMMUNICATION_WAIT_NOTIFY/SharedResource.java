package ai.ml.core_concepts.multithreading.thread_communication.programs.THREAD_COMMUNICATION_WAIT_NOTIFY;

class SharedResource {
    private int data;
    private boolean hasData;

    public synchronized void produce(int value){
        while(hasData){
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        data = value;
        hasData = true;
        notify();
    }

    public synchronized int consume(){
        while(!hasData){
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        hasData = false;
        notify();
        return data;
    }
}