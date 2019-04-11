package com.zhang.threadpool;

import android.support.annotation.NonNull;
public abstract class PriorityRunnable implements Runnable,Comparable<PriorityRunnable> {
    private int priority;

    public  PriorityRunnable(int priority){
        if(priority <0) {
            throw new IllegalArgumentException();
        }
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public int compareTo(@NonNull PriorityRunnable another) {
        int me = this.priority;
        int anotherPri=another.getPriority();
        return me == anotherPri ? 0 : me < anotherPri ? 1 : -1;
    }

    @Override
    public void run() {
            doSomeThing();
    }

    protected abstract void doSomeThing();
}