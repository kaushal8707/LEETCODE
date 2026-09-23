package ai.ml.core_concepts.multithreading.thread_communication.programs.THREAD_COMMUNICATION_WAIT_NOTIFY;

class Prooducer implements Runnable {
    private SharedResource resource;
    public Prooducer(SharedResource resource){
        this.resource = resource;
    }
    @Override
    public void run() {
        for(int i=0; i<10; i++ ){
            resource.produce(i);
            System.out.println(" produced : "+i);
        }
    }
}