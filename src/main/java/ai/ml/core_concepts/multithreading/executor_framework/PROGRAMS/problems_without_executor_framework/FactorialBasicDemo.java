package ai.ml.core_concepts.multithreading.executor_framework.PROGRAMS.problems_without_executor_framework;


public class FactorialBasicDemo {
    public static void main(String[] args) {

        /** Things are happening Synchronously so taking 9 sec to perm */

//        long startTime=System.currentTimeMillis();
//        for(int i=1;i<10;i++){
//            System.out.println(factorial(i));
//        }
//        System.out.println("Total Time : " + (System.currentTimeMillis()-startTime));

        /** Let's do In Multithreading environments
         * we are creating 10 threads
         *
         *         since we have a main thread and other threads will run. so here first main thread will print the total time.
         *    we have not wait for all threads to get finished. this time actually showing for creating a threads and all not for execution time.
         *    so, we have to wait for all threads to get finished.
         *
         * => so scenarios may come If you want to perform some task and to perform task you need the results or response of all the threads
         *    might be 9 times you are fetching data from database and you want to use at end all the fetched data then let's see how we can perform.
         * */

//        long startTime=System.currentTimeMillis();
//        for(int i=1;i<10;i++){
//            int finalI = i;
//            Thread t=new Thread(()->{
//                long result = factorial(finalI);
//                System.out.println(result);
//            });
//            t.start();
//        }
//        System.out.println("Total Time : " + (System.currentTimeMillis()-startTime));


        /**
         * Perform task depends on Resulting of all Threads results
         * so first all threads should complete and we will use the results from all threads at end to perform something.
         *
         * 1. first of all create an array of threads
         * 2. it will start from 0
         * 3. then you have to wait for all threads to complete
         * 4. now response time will print at end after all threads completes
         */
        long startTime=System.currentTimeMillis();
        Thread[] threads = new Thread[9];
        for(int i=1;i<10;i++){
            int finalI = i;
            threads[i-1] = new Thread(()->{
                long result = factorial(finalI);
                System.out.println(result);
            });
            threads[i-1].start();
        }

        for(Thread thread : threads){
            try {
                thread.join();
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Total Time : " + (System.currentTimeMillis()-startTime));
    }

    private static long factorial(int n){
        try{
            Thread.sleep(1000);                      // we though factorial computation taking 1 sec
        }catch (InterruptedException e){
            throw  new RuntimeException(e);
        }
        long result=1;
        for(int i=1;i<=n;i++){
            result *= i;
        }
        return result;
    }
}

