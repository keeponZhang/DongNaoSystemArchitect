package com.zhang.threadpool.asynTask;

import android.os.AsyncTask;
import android.util.Log;

public  class DownloadTask extends AsyncTask<Void, Integer, Boolean> {
 
	@Override
	protected void onPreExecute() {
		Log.e(TAG, "onPreExecute: " );
	}
 
	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			while (true) {
				int downloadPercent = doDownload();
				publishProgress(downloadPercent);
				if (downloadPercent >= 100) {
					break;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	int i;
	private int doDownload() {
		for (int j = 0; j < 2000; j++) {

		}
		i++;
			return i;
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

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}
}
