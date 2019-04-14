package com.zhang.threadpool;

import android.util.Log;

class RunnableTask implements Runnable {
    private String name ="";
    public RunnableTask(String name) {
        this.name = name;  
    }
    public RunnableTask() {
    }
    @Override  
    public void run() {  
        try {
            Log.e("TAG", this.name+ " is running in: "+Thread.currentThread().getName() );
            Thread.sleep(100);
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
}  