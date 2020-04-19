package com.deo.flapd.utils;

public class CancellableThread implements Runnable {

    protected boolean isCanceled = false;

    public void run() {

    }

    public void cancel(){
        isCanceled = true;
    }
}
