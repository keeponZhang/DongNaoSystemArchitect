package com.zhang.threadpool.asynTask;

import android.os.AsyncTask;
import android.util.Log;

public  class DownloadTask2 extends AsyncTask<String, Integer, Boolean> {

	@Override
	protected Boolean doInBackground(String... strings) {
		return null;
	}

	@Override
	protected void onPreExecute() {
	}
 


	private int doDownload() {
			return 0;
	}

	private static final String TAG = "DownloadTask";
	@Override
	protected void onProgressUpdate(Integer... values) {
		Log.e(TAG, "onProgressUpdate: "+values );
	}
 
	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {
			Log.e(TAG, "onPostExecute 下载成功. ");
		} else {
			Log.e(TAG, "onPostExecute 下载失败. ");

		}
	}
}
