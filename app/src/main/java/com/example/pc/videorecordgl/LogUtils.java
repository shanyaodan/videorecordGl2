package com.example.pc.videorecordgl;

import android.util.Log;

/**
 * Log日志输出类
 */
public class LogUtils {
    /**
     * 是否开启debug
     */
    public static boolean isDebug = true;

    /**
     * 错误
     * Write By LILIN
     * 2014-5-8
     *
     * @param clazz
     * @param msg
     */
    public static void e(Class<?> clazz, String msg) {
        if (isDebug) {
            Log.e(clazz.getSimpleName(), msg + "");
        }
    }

    /**
     * 信息
     * Write By LILIN
     * 2014-5-8
     *
     * @param clazz
     * @param msg
     */
    public static void i(Class<?> clazz, String msg) {
        if (isDebug) {
            Log.i(clazz.getSimpleName(), msg + "");
        }
    }

    /**
     * 警告
     * Write By LILIN
     * 2014-5-8
     *
     * @param clazz
     * @param msg
     */
    public static void w(Class<?> clazz, String msg) {
        if (isDebug) {
            Log.w(clazz.getSimpleName(), msg + "");
        }
    }


}
