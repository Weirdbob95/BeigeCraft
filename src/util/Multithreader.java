package util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class Multithreader {

    private static final int NUM_THREADS = 12;
    private static final int TIMEOUT = 60;
    private static final ThreadPoolExecutor THREAD_POOL = new ThreadPoolExecutor(NUM_THREADS, NUM_THREADS, TIMEOUT, TimeUnit.SECONDS, new LinkedBlockingQueue()) {
        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            currentTasks--;
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            currentTasks++;
        }
    };

    private static int currentTasks;

    static {
        THREAD_POOL.allowCoreThreadTimeOut(true);
    }

    public static void run(Runnable r) {
        THREAD_POOL.execute(r);
    }

    public static void runIfConvenient(Runnable r) {
        if (currentTasks < NUM_THREADS) {
            THREAD_POOL.execute(r);
        }
    }
}
