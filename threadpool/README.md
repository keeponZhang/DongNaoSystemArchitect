# ThreadPoolExecutor
![](../images/threadpool1.png)
### 1.ThreadPoolExecutor对于任务的处理流程，其中有几点需要说明：
##### 1.如果当前运行的线程小于 corePoolSize，则创建新线程来执行任务。
##### 2.如果运行的线程等于或多于 corePoolSize，则将任务加入到等待队列中
##### 3.如果无法将任务加入到等待队列，则继续创建新的线程来执行任务。
##### 4.如果创建新线程使得当前运行的线程超过maximumPoolSize，任务将被拒绝。
### 2. code
```java
  public void execute(Runnable command) {
         if (command == null)
             throw new NullPointerException();
         /* * Proceed in 3 steps: * * 1\. If fewer than corePoolSize threads are running, try to * start a new thread with the given command as its first * task. The call to addWorker atomically checks runState and * workerCount, and so prevents false alarms that would add * threads when it shouldn't, by returning false. * * 2\. If a task can be successfully queued, then we still need * to double-check whether we should have added a thread * (because existing ones died since last checking) or that * the pool shut down since entry into this method. So we * recheck state and if necessary roll back the enqueuing if * stopped, or start a new thread if there are none. * * 3\. If we cannot queue task, then we try to add a new * thread. If it fails, we know we are shut down or saturated * and so reject the task. */
         int c = ctl.get();
         // 1如果线程数小于基本线程数，则创建线程并执行当前任务
         if (workerCountOf(c) < corePoolSize) {
             if (addWorker(command, true))
                 return;
             c = ctl.get();
         }
         // 2.如线程数大于等于基本线程数或线程创建失败，则将当前任务放到工作队列中。
         if (isRunning(c) && workQueue.offer(command)) {
         	//添加成功则进行二次检查，当发现了下面这两种情况之一，那么还需要进行额外的处理：
             int recheck = ctl.get();
             //如果发现线程池变为了非running状态，那么会将该任务从等待队列中移除；
             if (! isRunning(recheck) && remove(command))
                 reject(command);
              //如果当前线程池已经没有存活的线程，那么为了让等待队列中的任务可以运行，我们需要通过addWorker方法启动一个新线程，
              // 与第一步不同的是，该线程的第一个任务为空。
             else if (workerCountOf(recheck) == 0)
                 addWorker(null, false);
         }
//         3.如果无法将任务加入到等待队列，则继续创建新的非核心线程来执行任务。
         else if (!addWorker(command, false))
             // 抛出RejectedExecutionException异常
//             4.如果创建新线程使得当前运行的线程超过maximumPoolSize，任务将被拒绝。
             reject(command);
     }
```   
#### addWorker 
##### 整个addWorker进行了以下几步操作：
  *  根据当前线程池的状态，判断是否允许新建线程
  *  根据当前线程池的工作线程数，判断是否允许新建线程
  *  创建一个Worker对象，这个Worker类中包含了一个线程
  *  将新建的Worker对象加入到线程池中
  *  启动Worker中的线程
```java
private boolean addWorker(Runnable firstTask, boolean core) {
        retry:
        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);

            //第一部分：当前线程池的状态是否满足加入的条件
            if (rs >= SHUTDOWN &&
                ! (rs == SHUTDOWN &&
                   firstTask == null &&
                   ! workQueue.isEmpty()))
                return false;
            //第二部分：当前线程池的容量是否满足加入的条件
            for (;;) {
                int wc = workerCountOf(c);
                if (wc >= CAPACITY ||
                    wc >= (core ? corePoolSize : maximumPoolSize))
                    return false;
                if (compareAndIncrementWorkerCount(c))
                    break retry;
                c = ctl.get();  // Re-read ctl
                if (runStateOf(c) != rs)
                    continue retry;
                // else CAS failed due to workerCount change; retry inner loop
            }
        }

        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {
            //第三部分：创建工作类Worker
            w = new Worker(firstTask);
            final Thread t = w.thread;
            if (t != null) {
                final ReentrantLock mainLock = this.mainLock;
                mainLock.lock();
                try {
                    // Recheck while holding lock.
                    // Back out on ThreadFactory failure or if
                    // shut down before lock acquired.
                    int rs = runStateOf(ctl.get());

                    if (rs < SHUTDOWN ||
                        (rs == SHUTDOWN && firstTask == null)) {
                        if (t.isAlive()) 
                            throw new IllegalThreadStateException();
                        //第四部分：将Worker加入到线程池中
                        workers.add(w);
                        int s = workers.size();
                        if (s > largestPoolSize)
                            largestPoolSize = s;
                        workerAdded = true;
                    }
                } finally {
                    mainLock.unlock();
                }
                if (workerAdded) {
                    //第五部分：启动线程
                    t.start();
                    workerStarted = true;
                }
            }
        } finally {
            if (! workerStarted)
                addWorkerFailed(w);
        }
        return workerStarted;
    }
``` 
```java
final void runWorker(Worker w) {
	//这里面的task就是我们通过execute方法传入的Runnable，如果Worker的
	// 第一个任务不为空，那么会首先执行该任务，如果第一个任务执行完毕，
	// 那么会调用getTask()方法来尝试去获取下一个任务，
	// 当getTask()方法不返回（等待队列为空）时，会一直阻塞在这里，
	// 而当这个while循环退出的时候，那么Worker所对应的线程就会被销毁。
        Thread wt = Thread.currentThread();
        Runnable task = w.firstTask;
        w.firstTask = null;
        w.unlock(); 
        boolean completedAbruptly = true;
        try {
            while (task != null || (task = getTask()) != null) {
                w.lock();
                if ((runStateAtLeast(ctl.get(), STOP) ||
                     (Thread.interrupted() &&
                      runStateAtLeast(ctl.get(), STOP))) &&
                    !wt.isInterrupted())
                    wt.interrupt();
                try {
                    beforeExecute(wt, task);
                    Throwable thrown = null;
                    try {
                        task.run();
                    } catch (RuntimeException x) {
                        thrown = x; throw x;
                    } catch (Error x) {
                        thrown = x; throw x;
                    } catch (Throwable x) {
                        thrown = x; throw new Error(x);
                    } finally {
                        afterExecute(task, thrown);
                    }
                } finally {
                    task = null;
                    w.completedTasks++;
                    w.unlock();
                }
            }
            completedAbruptly = false;
        } finally {
            processWorkerExit(w, completedAbruptly);
        }
    }
 ``` 
 ```java
 private Runnable getTask() {
// getTask()方法会去在第(1)步中的等待队列workerQueue取任务，在获取任务的
// 时候会考虑超时时间keepAliveTime，如果超时时间到了仍然没有获取到任务，
// 那么getTask()方法就会返回null，从而runWorker()中的while循环就会结束，
// 之后在finally代码块中通过processWorkerExit(w, completedAbruptly)销毁
// 该线程。
         boolean timedOut = false; // Did the last poll() time out?
 
         for (;;) {
             int c = ctl.get();
             int rs = runStateOf(c);
 
             // Check if queue empty only if necessary.
             if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
                 decrementWorkerCount();
                 return null;
             }
 
             int wc = workerCountOf(c);
 
             // Are workers subject to culling?
             boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;
 
             if ((wc > maximumPoolSize || (timed && timedOut))
                 && (wc > 1 || workQueue.isEmpty())) {
                 if (compareAndDecrementWorkerCount(c))
                     return null;
                 continue;
             }
 
             try {
                 Runnable r = timed ?
                     workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                     workQueue.take();
                 if (r != null)
                     return r;
                 timedOut = true;
             } catch (InterruptedException retry) {
                 timedOut = false;
             }
         }
     }
  ``` 