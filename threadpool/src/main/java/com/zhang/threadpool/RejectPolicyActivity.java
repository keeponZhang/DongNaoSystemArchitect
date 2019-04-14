package com.zhang.threadpool;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @创建者 keepon
 * @创建时间 2019/4/13 0013 下午 11:42
 * @描述 ${TODO}
 * @版本 $$Rev$$
 * @更新者 $$Author$$
 * @更新时间 $$Date$$
 */
public class RejectPolicyActivity extends AppCompatActivity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_policy);
	}

	private static final int THREADS_SIZE = 1;
	private static final int CAPACITY = 1;
	public void discardPolicyDemo(View view) {
		// 创建线程池。线程池的"最大池大小"和"核心池大小"都为1(THREADS_SIZE)，"线程池"的阻塞队列容量为1(CAPACITY)。
		ThreadPoolExecutor pool = new ThreadPoolExecutor(THREADS_SIZE, THREADS_SIZE, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(CAPACITY));
		// 设置线程池的拒绝策略为"丢弃"
		pool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());

		// 新建10个任务，并将它们添加到线程池中。
		for (int i = 0; i < 10; i++) {
			Runnable myrun = new RunnableTask("task-"+i);
			pool.execute(myrun);
		}
		// 关闭线程池
		pool.shutdown();

		// task-0 is running.:
		// task-1 is running.:
//		线程池pool的阻塞队列是ArrayBlockingQueue，ArrayBlockingQueue是一个有界的阻塞队列，ArrayBlockingQueue的容量为1。这也意味着线程池的阻塞队列只能有一个线程池阻塞等待。
//　　根据”“中分析的execute()代码可知：线程池中共运行了2个任务。第1个任务直接放到Worker中，通过线程去执行；第2个任务放到阻塞队列中等待。其他的任务都被丢弃了！
	}





	public void discardOldestPolicyDemo(View view) {
		// 创建线程池。线程池的"最大池大小"和"核心池大小"都为1(THREADS_SIZE)，"线程池"的阻塞队列容量为1(CAPACITY)。
		ThreadPoolExecutor pool = new ThreadPoolExecutor(THREADS_SIZE, THREADS_SIZE, 0, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(CAPACITY));
		// 设置线程池的拒绝策略为"DiscardOldestPolicy"
		pool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());

		// 新建10个任务，并将它们添加到线程池中。
		for (int i = 0; i < 10; i++) {
			Runnable myrun = new RunnableTask("task-"+i);
			pool.execute(myrun);
		}
		// 关闭线程池
		pool.shutdown();
		//task-0 is running.
		//task-9 is running.
//		将”线程池的拒绝策略”由DiscardPolicy修改为DiscardOldestPolicy之后，当有任务添加到线程池被拒绝时，线程池会丢弃阻塞队列中末尾的任务，然后将被拒绝的任务添加到末尾。
	}
	public void abortPolicyDemo(View view) {
		// 创建线程池。线程池的"最大池大小"和"核心池大小"都为1(THREADS_SIZE)，"线程池"的阻塞队列容量为1(CAPACITY)。
		ThreadPoolExecutor pool = new ThreadPoolExecutor(THREADS_SIZE, THREADS_SIZE, 0, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(CAPACITY));
		// 设置线程池的拒绝策略为"抛出异常"
		pool.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

		try {

			// 新建10个任务，并将它们添加到线程池中。
			for (int i = 0; i < 10; i++) {
				Runnable myrun = new RunnableTask("task-"+i);
				pool.execute(myrun);
			}
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
			Log.e("TAG", "abortPolicyDemo:RejectedExecutionException " );
			// 关闭线程池
			pool.shutdown();
		}

//		java.util.concurrent.RejectedExecutionException
//		at java.util.concurrent.ThreadPoolExecutor$AbortPolicy.rejectedExecution(ThreadPoolExecutor.java:1774)
//		at java.util.concurrent.ThreadPoolExecutor.reject(ThreadPoolExecutor.java:768)
//		at java.util.concurrent.ThreadPoolExecutor.execute(ThreadPoolExecutor.java:656)
//		at AbortPolicyDemo.main(AbortPolicyDemo.java:27)
//		task-0 is running.
//		task-1 is running.
//		将”线程池的拒绝策略”由DiscardPolicy修改为AbortPolicy之后，当有任务添加到线程池被拒绝时，会抛出RejectedExecutionException。
	}
	public void callerRunsPolicyDemo(View view) {
		// 创建线程池。线程池的"最大池大小"和"核心池大小"都为1(THREADS_SIZE)，"线程池"的阻塞队列容量为1(CAPACITY)。
		final ThreadPoolExecutor pool = new ThreadPoolExecutor(THREADS_SIZE, THREADS_SIZE, 0, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(CAPACITY));
		// 设置线程池的拒绝策略为"CallerRunsPolicy"
		pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

		// 新建10个任务，并将它们添加到线程池中。
		for (int i = 0; i < 10; i++) {
			final int finalI = i;
			new Thread("线程："+i){
				@Override
				public void run() {
					Runnable myrun = new RunnableTask("task-"+ finalI);
							pool.execute(myrun);
							if(finalI==10){
								// 关闭线程池
								pool.shutdown();
							}

				}
			}.start();
//			Runnable myrun = new RunnableTask("task-"+i);
//			pool.execute(myrun);
			// 关闭线程池

		}

//		用于被拒绝任务的处理程序，它直接在 execute 方法的调用线程中运行被拒绝的任务；如果执行程序已关闭，则会丢弃该任务
		//			pool.shutdown();
	}
}
