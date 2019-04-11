package com.zhang.threadpool;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @创建者 keepon
 * @创建时间 2019/4/11 0011 下午 10:42
 * @描述 ${TODO}
 * @版本 $$Rev$$
 * @更新者 $$Author$$
 * @更新时间 $$Date$$
 */
public class FutureAndFutureTaskActivity extends AppCompatActivity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_future);
	}

//	线程池submit提交的参数最终会包装成futuretask,返回future.一个是系统帮你封装，
// 此时你要通过返回的future才能获取返回值。一个是自己封装futuretask,
// 这时直接通过futuretask就可以获取返回值。futuretask构造函数既接受一个参数callable，
// 也接受两个参数runnable和result,后者也会封装成callable.submit后最终会调到Execute的execute方法，
// 因为futuretask是是唯一实现runnabletask的，runnabletask实现了runnable和future接口，
// 所以后来是调的futuretask的run方法里，run方法又会调用callable的call方法。futuretask又是future,
// 所以又可以通过futuretask拿到返回值。
	private static final String TAG = "FutureAndFutureTaskActi";
	public void callableAndFuture(View view) {
		ExecutorService executor = Executors.newCachedThreadPool();
		Task task = new Task();
		Future<Integer> result = executor.submit(task);
		executor.shutdown();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		Log.e(TAG, "主线程在执行任务: " );



		try {
			Log.e(TAG, "task运行结果"+result.get() );
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		Log.e(TAG, "所有任务执行完毕: ");
	}

	public void callableAndFutureTask(View view) {
		//第一种方式
		ExecutorService executor = Executors.newCachedThreadPool();
		Task task = new Task();
		FutureTask<Integer> futureTask = new FutureTask<Integer>(task);
		executor.submit(futureTask);
		executor.shutdown();

		//第二种方式，注意这种方式和第一种方式效果是类似的，只不过一个使用的是ExecutorService，一个使用的是Thread
        /*Task task = new Task();
        FutureTask<Integer> futureTask = new FutureTask<Integer>(task);
        Thread thread = new Thread(futureTask);
        thread.start();*/

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		Log.e(TAG, "主线程在执行任务: " );


		try {
			Log.e(TAG, "task运行结果"+futureTask.get() );
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		Log.e(TAG, "所有任务执行完毕: ");

	}
}
