package com.jancar.media.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池工具类
 *
 * @author: Tzq
 * @date 2018/12/13.
 */


public class ThreadUtils {

    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }


}
