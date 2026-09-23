
![img.png](img.png)

All these Queue are not a Thread Safe.

Let's start a Thread Safe Queue - BlockingQueue.

        /**
         *        1. thread - safe Queue
         *        2. wait for queue to become non-empty/wait for space
         *                         //(Blocking queue waits for 2 things Either in a queue
         *                          //there is no space so that he can put element into and need to wait to pick element from queue so that
         *                          //if any element will come he will pick.
         *        3. simplify concurrency problems like producer-consumer
         *        4. standard queue -- Immediately
         *              =>empty -- remove() -- no waiting
         *              =>full  -- add() -- no waiting
         *        5. Blocking Queue
         *              =>put -- Block If the queue is full un-till space becomes available
         *              =>take -- Block If the queue is empty unless an element become available
         *              =>offer -- waits for space to become available, upto the specified timeout
         */


