package ai.ml.core_concepts.multithreading.thread_communication.programs.THREAD_COMMUNICATION_WAIT_NOTIFY;

class Consumer implements Runnable {
    private SharedResource resource;
    public Consumer(SharedResource resource){
        this.resource = resource;
    }
    @Override
    public void run() {
        for(int i=0; i<10; i++ ){
            int value = resource.consume();
            System.out.println(" consumed - : "+value);
        }
    }
}