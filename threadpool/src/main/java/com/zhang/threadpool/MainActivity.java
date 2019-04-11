package com.zhang.threadpool;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";
	private ThreadPoolExecutor mThreadPoolExecutor;
	private static final RejectedExecutionHandler defaultHandler =
			new ThreadPoolExecutor.AbortPolicy();

	/*AbortPolicy：抛出异常，这也是默认的策略
	CallerRunsPolicy：使用调用者所在线程来执行任务
	DiscardOldestPolicy：先丢弃队列中最末尾的任务，再重新通过execute方法执行该任务。
	DiscardPolicy：不做任何处理，直接丢弃*/

	private ExecutorService mSingleThreadExecutor;
	/*ThreadPoolExecutor执行execute()分4种情况
	1.若当前运行的线程少于corePoolSize,则创建新线程来执行任务(执行这一步需要获取全局锁)
	2.若运行的线程多于或等于corePoolSize,则将任务加入BlockingQueue
	3.若无法将任务加入BlockingQueue,则创建新的线程来处理任务(执行这一步需要获取全局锁)
	4.若创建新线程将使当前运行的线程超出maximumPoolSize,任务将被拒绝,并调用RejectedExecutionHandler.rejectedExecution()
	*/


//	LinkedBlockingQueue
	//添加
//	add方法在添加元素的时候，若超出了度列的长度会直接抛出异常：
//	对于put方法，若向队尾添加元素的时候发现队列已经满了会发生阻塞一直等待空间，以加入元素。
//	offer方法在添加元素时，如果发现队列已满无法添加的话，会直接返回false。
//	从队列中取出并移除头元素的方法有：poll，remove，take。
	/*poll: 若队列为空，返回null。
	remove:若队列为空，抛出NoSuchElementException异常。
	take:若队列为空，发生阻塞，等待有元素。*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public void createThreadPoolExecutor(View view) {
		//创建基本线程池,如果任务队列是100，任务只有30，队列未满，不会创建非核心线程。
//		mThreadPoolExecutor = new ThreadPoolExecutor(3,5,1, TimeUnit.SECONDS,
//				new LinkedBlockingQueue<Runnable>(100));
		//创建基本线程池,如果任务队列是25，任务有30，队列满了，会创建非核心线程。前面任务会先放在任务队列，直到任务队列满了，就会创建非核心线程执行任务。
		mThreadPoolExecutor = new ThreadPoolExecutor(3,5,1, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(25));
	}

	public void threadPoolExecute(View view) {
		for(int i = 0;i<30;i++){
			final int finali = i;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(2000);
						Log.e(TAG,"Thread run: "+finali);
						Log.e(TAG,"当前线程："+Thread.currentThread().getName() );
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
			if(mThreadPoolExecutor!=null){
				mThreadPoolExecutor.execute(runnable);
			}

			if(mSingleThreadExecutor!=null){
				mSingleThreadExecutor.execute(runnable);
			}

		}
	}

	public void getActiveCount(View view) {

		if(mThreadPoolExecutor==null){
			Log.e(TAG, "mThreadPoolExecutor==null " );
			return;
		}
		Log.e(TAG, "getCorePoolSize: "+mThreadPoolExecutor.getCorePoolSize() );
		//一开始没执行过任务时，getPoolSize是为0
		Log.e(TAG, "getPoolSize: "+mThreadPoolExecutor.getPoolSize() );
	}

	public void createFixedThreadPool(View view) {
		//创建fixed线程池 new ThreadPoolExecutor(nThreads, nThreads,0L,
		// TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>());
		//特点：参数为核心线程数，只有核心线程，无非核心线程，并且阻塞队列无界。
		//通俗：创建可容纳固定数量线程的池子，每隔线程的存活时间是无限的，当池子满了就不再添加线程了；
		// 如果池中的所有线程均在繁忙状态，对于新任务会进入阻塞队列中(无界的阻塞队列)
		//适用：执行长期的任务，性能好很多
		mThreadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
	}
	/*因为没有核心线程，其他全为非核心线程，SynchronousQueue是不存储元素的，每次插入操作必须伴随一个移除操作，一个移除操作也要伴随一个插入操作。
	当一个任务执行时，先用SynchronousQueue的offer提交任务，如果线程池中有线程空闲，则调用SynchronousQueue的poll方法来移除任务并交给线程处理；如果没有线程空闲，则开启一个新的非核心线程来处理任务。
	由于maximumPoolSize是无界的，所以如果线程处理任务速度小于提交任务的速度，则会不断地创建新的线程，这时需要注意不要过度创建，应采取措施调整双方速度，不然线程创建太多会影响性能。
	从其特点可以看出，CachedThreadPool适用于有大量需要立即执行的耗时少的任务的情况。*/
	public void createCachedThreadPool(View view) {
		//创建Cached线程池new ThreadPoolExecutor(0, Integer.MAX_VALUE,
		//         60L, TimeUnit.SECONDS,new SynchronousQueue<Runnable>());
		//特点：没有核心线程，只有非核心线程，并且每个非核心线程空闲等待的时间为60s，采用SynchronousQueue队列。
		//适用：执行很多短期异步的小程序或者负载较轻的服务器
		mThreadPoolExecutor= (ThreadPoolExecutor) Executors.newCachedThreadPool();
	}

	public void createSingleThreadPool(View view) {
		//创建Single线程池 (new ThreadPoolExecutor(1, 1,
		//          0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>()));
//		mSingleThreadExecutor = Executors.newSingleThreadExecutor();
		//通俗：创建只有一个线程的线程池，且线程的存活时间是无限的；当该线程正繁忙时，对于新任务会进入阻塞队列中(无界的阻塞队列)
		//适用：一个任务一个任务执行的场景
		mThreadPoolExecutor = new ThreadPoolExecutor(1, 1,
				0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
	}


	public void createScheduledThreadPool(View view) {
		//创建Scheduled线程池
		//底层：创建ScheduledThreadPoolExecutor实例，corePoolSize为传递来的参数，maximumPoolSize为Integer.MAX_VALUE；keepAliveTime为0；unit为：TimeUnit.NANOSECONDS；workQueue为：new DelayedWorkQueue() 一个按超时时间升序排序的队列
		//通俗：创建一个固定大小的线程池，线程池内线程存活时间无限制，线程池可以支持定时及周期性任务执行，如果所有线程均处于繁忙状态，对于新任务会进入DelayedWorkQueue队列中，这是一种按照超时时间排序的队列结构
		//适用：周期性执行任务的场景
		 ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(3);
		Runnable runnable = new Runnable() {
			@Override
			public void run() {

				Log.e(TAG, "This task is delayed to execute");
			}

		};
		//延迟启动任务
//		scheduledThreadPool.schedule(runnable,5,TimeUnit.SECONDS);
		//延迟5s后启动，每1s执行一次
//		scheduledThreadPool.scheduleAtFixedRate(runnable,3,1,TimeUnit.SECONDS);
		//启动后第一次延迟5s执行，后面延迟1s执行
//		scheduledThreadPool.scheduleWithFixedDelay(runnable,5,1,TimeUnit.SECONDS);


	}

	public void createPriorityThreadPool(View view) {
		//创建自定义线程池(优先级线程)
		 ExecutorService priorityThreadPool = new ThreadPoolExecutor(3,3,0, TimeUnit.SECONDS,new PriorityBlockingQueue<Runnable>());
		executeTaskPriority(priorityThreadPool);
	}

	private void executeTaskPriority(ExecutorService priorityThreadPool) {
		for(int i = 0;i<30;i++){
			final int priority = i;
			priorityThreadPool.execute(new PriorityRunnable(priority) {
				@Override
				protected void doSomeThing() {
					Log.e(TAG, "优先级为 "+priority+"  的任务被执行");
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});

		}
	}

	//常见的阻塞队列有下列7种：
	/*ArrayBlockingQueue ：一个由数组结构组成的有界阻塞队列。
	LinkedBlockingQueue ：一个由链表结构组成的有界阻塞队列。
	PriorityBlockingQueue ：一个支持优先级排序的无界阻塞队列。
	DelayQueue：一个使用优先级队列实现的无界阻塞队列。
	SynchronousQueue：一个不存储元素的阻塞队列。
	LinkedTransferQueue：一个由链表结构组成的无界阻塞队列。
	LinkedBlockingDeque：一个由链表结构组成的双向阻塞队列。*/


	/*1.shutDown()  关闭线程池，不影响已经提交的任务
	2.shutDownNow() 关闭线程池，并尝试去终止正在执行的线程
	3.allowCoreThreadTimeOut(boolean value) 允许核心线程闲置超时时被回收
	4.submit 一般情况下我们使用execute来提交任务，但是有时候可能也会用到submit，使用submit的好处是submit有返回值。
	5.beforeExecute() - 任务执行前执行的方法
	6.afterExecute() -任务执行结束后执行的方法
	7.terminated() -线程池关闭后执行的方法*/
}
