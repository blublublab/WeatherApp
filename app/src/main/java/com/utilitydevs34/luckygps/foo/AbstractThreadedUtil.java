package com.utilitydevs34.luckygps.foo;


import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CountDownLatch;

public abstract class AbstractThreadedUtil {


    /**
     * Payload code
     */
    protected abstract void runPayload();

    /**
     * Run payload in a background thread and countdown latch then
     *
     * @param cdl latch to countdown
     */
    public void runWithCountdown(CountDownLatch cdl) {
        Thread t = new Thread(() -> {
            runPayload();
            cdl.countDown();
        });
        t.start();
    }

    /**
     * Run payload in a background thread
     */
    public void runNonBlocking() {
        Thread t = new Thread(this::runPayload);
        t.start();
    }

    /**
     * Run payload in current thread
     */
    public void run() {
        runPayload();
    }

    /**
     * Run payload in a background thread and run callback in UI thread then
     *
     * @param callback callback runnable
     */
    public void runWithBlockingCallback(Runnable callback) {
        Thread t = new Thread(() -> {
            runPayload();
            new Handler(Looper.getMainLooper()).post(() -> callback.run());
        });
        t.start();
    }

    /**
     * Run payload in a background thread, then run callback also in a background thread
     *
     * @param callback callback runnable
     */
    public void runNonBlocking(Runnable callback) {
        Thread t = new Thread(() -> {
            runPayload();
            callback.run();
        });
        t.start();
    }
}