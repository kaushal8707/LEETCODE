package ai.ml.core_concepts.multithreading.threadlocal.RealTimeUsecase;

// Example demonstrating usage in a multithreaded scenario
class ThreadLocalDemo implements Runnable {

    @Override
    public void run() {
        // Simulate a web request processing flow
        String threadName = Thread.currentThread().getName();
        try {
            // Set user info at the beginning of the request handling
            UserSessionManager.setUserSessionInfo(threadName + "_User");

            // Access user info in different methods without passing parameters
            processRequestLayer1();
            processRequestLayer2();

        } finally {
            // Ensure cleanup in a finally block
            UserSessionManager.clearUserSessionInfo();
            System.out.println(threadName + " cleared session info.");
        }
    }

    private void processRequestLayer1() {
        UserContext ctx = UserSessionManager.getUserSessionInfo();
        System.out.println(Thread.currentThread().getName() + " Layer 1: " + ctx);
    }

    private void processRequestLayer2() {
        UserContext ctx = UserSessionManager.getUserSessionInfo();
        System.out.println(Thread.currentThread().getName() + " Layer 2: " + ctx);
    }
}