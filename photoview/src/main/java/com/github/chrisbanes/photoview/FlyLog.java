package com.github.chrisbanes.photoview;
import android.util.Log;

/**
 * Author: FlyZebra
 * Created by FlyZebra on 2018/3/28-下午3:28.
 */
public class FlyLog {
    public static final String TAG = "com.jancar.plays";
    public static String[] filter = {
    };

    public static void d() {
        String s = buildLogString("");
        Log.d(TAG, s);
    }

    public static void d(String logString, Object... args) {
        String s = buildLogString(logString, args);
        for (String aFilter : filter) {
            if (s.contains(aFilter)) {
                return;
            }
        }
        Log.d(TAG, s);
    }

    public static void i() {
        String s = buildLogString("");
        Log.i(TAG, s);
    }

    public static void i(String logString, Object... args) {
        String s = buildLogString(logString, args);
        for (String aFilter : filter) {
            if (s.contains(aFilter)) {
                return;
            }
        }
        Log.i(TAG, s);
    }


    public static void v() {
        String s = buildLogString("");
        Log.v(TAG, s);
    }

    public static void v(String logString, Object... args) {
        String s = buildLogString(logString, args);
        for (String aFilter : filter) {
            if (s.contains(aFilter)) {
                return;
            }
        }
        Log.v(TAG, s);
    }


    public static void e() {
        String s = buildLogString("");
        Log.e(TAG, s);
    }

    public static void e(String logString, Object... args) {
        String s = buildLogString(logString, args);
        for (String aFilter : filter) {
            if (s.contains(aFilter)) {
                return;
            }
        }
        Log.e(TAG, s);
    }

    public static void w() {
        String s = buildLogString("");
        Log.w(TAG, s);
    }

    public static void w(String logString, Object... args) {
        for (String aFilter : filter) {
            if (logString.indexOf(aFilter) == 0) {
                return;
            }
        }
        String s = buildLogString(logString, args);
        Log.w(TAG, s);
    }


    private static String buildLogString(String str, Object... args) {
        if (args.length > 0) {
            str = String.format(str, args);
        }
        //进程消息
        Thread thread = Thread.currentThread();

        //打印位置
        StackTraceElement caller = new Throwable().fillInStackTrace().getStackTrace()[2];
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
                .append("[")
                .append(thread.getName())
                .append("][")
                .append(thread.getId())
                .append("](")
                .append(caller.getFileName())
                .append(":")
                .append(caller.getLineNumber())
                .append(")")
                .append(caller.getMethodName())
                .append("()")
                .append(">>>>")
                .append(str);
        return stringBuilder.toString();
    }
}