package util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class Multithreader {

    private static final int NUM_THREADS = 4;
    private static final int TIMEOUT = 60;
    private static final ThreadPoolExecutor THREAD_POOL = new ThreadPoolExecutor(NUM_THREADS, NUM_THREADS, TIMEOUT, TimeUnit.SECONDS, new LinkedBlockingQueue());

    static {
        THREAD_POOL.allowCoreThreadTimeOut(true);
    }

    public static boolean isFree() {
        return queuedTasks() == 0;
    }

    public static void run(Runnable r) {
        THREAD_POOL.execute(r);
    }

    public static int queuedTasks() {
        return THREAD_POOL.getQueue().size();
    }
}
