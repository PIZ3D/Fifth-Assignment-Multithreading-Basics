## Question 1

- The output will be :  
  Calling run()  
  Running in: main  
  Calling start()  
  Running in: Thread-2   
 

  When you call t1.run(), it executes the run() method in the current thread (main thread) like a normal method.

   But when you call t2.start(), it creates a new thread (Thread-2) and executes the run() method in that new thread.
  

- start() creates a new thread and executes the run() method in that new thread.

  run()  executes the method in the current thread (doesn't create a new thread).


## Question 2

- The output will be :  
  Main thread ends.  
  Daemon thread running...  
  Daemon thread running...   
  (possibly a few more Daemon thread running...)   

  Daemon threads are terminated when all non-daemon threads (like the main thread) finish.

  The program may print a few lines from the daemon thread before the JVM exits, but not all 20 iterations.
   

- The thread becomes a (non-daemon) thread.

  The program will run all 20 iterations (about 10 seconds) before exiting, even though the main thread ends quickly.



- Garbage collectors / Autosave ...


## Question 3

- The output is :   
Thread is running using a ...!

- A lambda expression

- It's the shortest way and it doesn't require creating separate classes   

   




