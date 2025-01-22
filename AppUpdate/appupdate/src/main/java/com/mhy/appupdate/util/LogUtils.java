/*
 Copyright © 2015, 2016 Jenly Yu <a href="mailto:jenly1314@gmail.com">Jenly</a>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 	http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
package com.mhy.appupdate.util;

import android.util.Log;

import java.util.Locale;


public class LogUtils {

    public static final String TAG = "AppUpdater";

    public static final String VERTICAL = "|";

    /**
     * 是否显示Log日志
     */
    private static boolean isShowLog = false;

    /**
     * Priority constant for the println method;use System.out.println
     */
    public static final int PRINTLN = 1;
    /**
     * Log日志优先权
     */
    private static int priority = PRINTLN;
    public static final String TAG_FORMAT = "%s.%s(%s:%d)";

    private LogUtils() {
        throw new AssertionError();
    }

    public static void setShowLog(boolean isShowLog) {

        LogUtils.isShowLog = isShowLog;
    }

    public static boolean isShowLog() {

        return isShowLog;
    }

    public static int getPriority() {

        return priority;
    }

    public static void setPriority(int priority) {

        LogUtils.priority = priority;
    }

    /**
     * 根据堆栈生成TAG
     *
     * @return TAG|className.methodName(fileName:lineNumber)
     */
    private static String generateTag(StackTraceElement caller) {
        String tag = TAG_FORMAT;
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(Locale.getDefault(), tag, callerClazzName, caller.getMethodName(), caller.getFileName(), caller.getLineNumber());
        return new StringBuilder().append(tag).toString();
    }

    /**
     * 获取堆栈
     *
     * @param n n=0		VMStack
     *          n=1		Thread
     *          n=3		CurrentStack
     *          n=4		CallerStack
     *          ...
     * @return
     */
    public static StackTraceElement getStackTraceElement(int n) {
        return Thread.currentThread().getStackTrace()[n];
    }

    /**
     * 获取调用方的堆栈TAG
     *
     * @return
     */
    private static String getCallerStackLogTag() {
        return TAG;
    }

    private static String getCallerStackLogMsg(String msg) {
        return generateTag(getStackTraceElement(5)) + ":" + msg;
    }

    /**
     * @param t
     * @return
     */
    private static String getStackTraceString(Throwable t) {
        return Log.getStackTraceString(t);
    }

    // -----------------------------------Log.v

    /**
     * Log.v
     *
     * @param msg
     */
    public static void v(String msg) {
        if (isShowLog && priority <= Log.VERBOSE)
            Log.v(getCallerStackLogTag(), getCallerStackLogMsg(String.valueOf(msg)));

    }

    public static void v(Throwable t) {
        if (isShowLog && priority <= Log.VERBOSE)
            Log.v(getCallerStackLogTag(), getCallerStackLogMsg(getStackTraceString(t)));
    }

    public static void v(String msg, Throwable t) {
        if (isShowLog && priority <= Log.VERBOSE)
            Log.v(getCallerStackLogTag(), getCallerStackLogMsg(String.valueOf(msg)), t);
    }

    // -----------------------------------Log.d

    /**
     * Log.d
     *
     * @param msg
     */
    public static void d(String msg) {
        if (isShowLog && priority <= Log.DEBUG)
            Log.d(getCallerStackLogTag(), getCallerStackLogMsg(String.valueOf(msg)));
    }

    public static void d(Throwable t) {
        if (isShowLog && priority <= Log.DEBUG)
            Log.d(getCallerStackLogTag(), getStackTraceString(t));
    }

    public static void d(String msg, Throwable t) {
        if (isShowLog && priority <= Log.DEBUG)
            Log.d(getCallerStackLogTag(), getCallerStackLogMsg(String.valueOf(msg)), t);
    }

    // -----------------------------------Log.i

    /**
     * Log.i
     *
     * @param msg
     */
    public static void i(String msg) {
        if (isShowLog && priority <= Log.INFO)
            Log.i(getCallerStackLogTag(), getCallerStackLogMsg(String.valueOf(msg)));
    }

    public static void i(Throwable t) {
        if (isShowLog && priority <= Log.INFO)
            Log.i(getCallerStackLogTag(), getStackTraceString(t));
    }

    public static void i(String msg, Throwable t) {
        if (isShowLog && priority <= Log.INFO)
            Log.i(getCallerStackLogTag(), getCallerStackLogMsg(String.valueOf(msg)), t);
    }

    // -----------------------------------Log.w

    /**
     * Log.w
     *
     * @param msg
     */
    public static void w(String msg) {
        if (isShowLog && priority <= Log.WARN)
            Log.w(getCallerStackLogTag(), getCallerStackLogMsg(String.valueOf(msg)));
    }

    public static void w(Throwable t) {
        if (isShowLog && priority <= Log.WARN)
            Log.w(getCallerStackLogTag(), getStackTraceString(t));
    }

    public static void w(String msg, Throwable t) {
        if (isShowLog && priority <= Log.WARN)
            Log.w(getCallerStackLogTag(), getCallerStackLogMsg(String.valueOf(msg)), t);
    }

    // -----------------------------------Log.e

    /**
     * Log.e
     *
     * @param msg
     */
    public static void e(String msg) {
        if (isShowLog && priority <= Log.ERROR)
            Log.e(getCallerStackLogTag(), getCallerStackLogMsg(String.valueOf(msg)));
    }

    public static void e(Throwable t) {
        if (isShowLog && priority <= Log.ERROR)
            Log.e(getCallerStackLogTag(), getStackTraceString(t));
    }

    public static void e(String msg, Throwable t) {
        if (isShowLog && priority <= Log.ERROR)
            Log.e(getCallerStackLogTag(), getCallerStackLogMsg(String.valueOf(msg)), t);
    }

    // -----------------------------------Log.wtf

    /**
     * Log.wtf
     *
     * @param msg
     */
    public static void wtf(String msg) {
        if (isShowLog && priority <= Log.ASSERT)
            Log.wtf(getCallerStackLogTag(), getCallerStackLogMsg(String.valueOf(msg)));
    }

    public static void wtf(Throwable t) {
        if (isShowLog && priority <= Log.ASSERT)
            Log.wtf(getCallerStackLogTag(), getStackTraceString(t));
    }

    public static void wtf(String msg, Throwable t) {
        if (isShowLog && priority <= Log.ASSERT)
            Log.wtf(getCallerStackLogTag(), getCallerStackLogMsg(String.valueOf(msg)), t);
    }

    // -----------------------------------System.out.print

    /**
     * System.out.print
     *
     * @param msg
     */
    public static void print(String msg) {
        if (isShowLog && priority <= PRINTLN)
            System.out.print(msg);
    }

    public static void print(Object obj) {
        if (isShowLog && priority <= PRINTLN)
            System.out.print(obj);
    }

    // -----------------------------------System.out.printf

    /**
     * System.out.printf
     *
     * @param msg
     */
    public static void printf(String msg) {
        if (isShowLog && priority <= PRINTLN)
            System.out.printf(msg);
    }

    // -----------------------------------System.out.println

    /**
     * System.out.println
     *
     * @param msg
     */
    public static void println(String msg) {
        if (isShowLog && priority <= PRINTLN)
            System.out.println(msg);
    }

    public static void println(Object obj) {
        if (isShowLog && priority <= PRINTLN)
            System.out.println(obj);
    }

}
