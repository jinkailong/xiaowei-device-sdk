/*
 * Tencent is pleased to support the open source community by making  XiaoweiSDK Demo Codes available.
 *
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.tencent.xiaowei.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * 所有的日志输出都可以在Console中看到<br>
 * QLog定义了日志级别<br>
 * -  !isCLR 普通用户<br> 会保存w级别以及以上的日志
 * -  isCLR 染色用户<br> 会保存v级别以及以上的日志
 * 定义成CLR级别，后台配置染色了的用户才会输出到日志文件中，否则只在consloe中输出<br>
 * 日志文件的存储在/sdcard/tencent/xiaowei目录中<br>
 * 日志文件命名规则，一个小时生成一个文件，文件的签字是进程名+日期<br>
 * 日志文件系统采用cahce机制，LOG_ITEM_MAX_CACHE_SIZE是默认cahce大小 超过这个大小继续加入日志时触发写文件。<br>
 *
 * 测试阶段可以打开日志保存便于定位问题，发布时候可以关闭
 */
public class QLog {

    /**
     * 日志级别  染色用户日志级别
     * 如果是染色用户，会打印并保存文件，否则只会打印
     */
    public final static int CLR = 2;


    /**
     * normal 普通
     * 会打印并保存文件
     */
    public final static int NORMAL = 1;

    /**
     * 文件日志系统，缓存机制的缓存日志最大条数
     */
    private final static int LOG_ITEM_MAX_CACHE_SIZE = 50;
    private static boolean isCLR = true;// 是染色用户

    private static HandlerThread mHandlerThread;
    private static Handler mHandler;

    private static String logPath = "";
    private static String sBuildNumber = "";
    private static String processName = "";
    private static String packageName = "";
    private static BufferedWriter writer;
    private static ConcurrentLinkedQueue<LogItem> list = new ConcurrentLinkedQueue<>();
    private static long lastPrintMemoryTime;
    private static Context mContext;
    private static boolean mLogAble = true;

    static {
        logPath = Environment.getExternalStorageDirectory().getPath() + "/tencent/xiaowei/logs/";
    }

    /**
     * 初始化
     *
     * @param context
     * @param process
     */
    public static void init(Context context, String process) {
        if (mHandlerThread != null) {
            mHandlerThread.quit();
            mHandler.removeCallbacksAndMessages(null);
        }
        mContext = context.getApplicationContext();
        packageName = context.getPackageName();
        processName = process;
        logPath = Environment.getExternalStorageDirectory().getPath() + "/tencent/xiaowei/logs/" + packageName.replace(".", "/") + "/";

        mHandlerThread = new HandlerThread("FileLog");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    /**
     * 设置日志控制台打印开关
     *
     * @param enable
     */
    public static void setLogAble(boolean enable) {
        mLogAble = enable;
    }

    /**
     * 设置保存日志的大小
     *
     * @param maxLogSize  日志最大大小，大于1024才会保存。默认500MB
     * @param minFreeSize 剩余空间预留大小，至少留这么大。默认200MB
     */
    public static void setLogSaveSize(long maxLogSize, long minFreeSize) {
        QLogUtil.maxLogSize = maxLogSize;
        QLogUtil.minFreeSpace = minFreeSize;
    }

    /**
     * 设置sdk版本号
     *
     * @param buildNumber
     */
    public static void setBuildNumber(String buildNumber) {
        sBuildNumber = buildNumber;
        try {
            writeBuild();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void v(String tag, String msg) {
        v(tag, NORMAL, msg);
    }

    public static void v(String tag, int level, String msg) {
        v(tag, level, msg, null);
    }

    public static void v(String tag, String msg, Throwable ex) {
        v(tag, NORMAL, msg, ex);
    }

    public static void v(String tag, int level, String msg, Throwable ex) {
        if (mLogAble)
            Log.i(tag, msg + (ex == null ? "" : " " + ex.toString()));
        if (isCLR) {
            // 染色用户才上报
            addLogItem(tag, "V", msg + (ex == null ? "" : " " + ex.toString()));
        }
    }

    public static void d(String tag, String msg) {
        d(tag, NORMAL, msg);
    }

    public static void d(String tag, int level, String msg) {
        d(tag, level, msg, null);
    }

    public static void d(String tag, String msg, Throwable ex) {
        d(tag, NORMAL, msg, ex);
    }

    public static void d(String tag, int level, String msg, Throwable ex) {
        if (mLogAble) Log.i(tag, msg + (ex == null ? "" : " " + ex.toString()));
        if (isCLR) {
            // 染色用户才上报
            addLogItem(tag, "D", msg + (ex == null ? "" : " " + ex.toString()));
        }
    }

    public static void i(String tag, String msg) {
        i(tag, NORMAL, msg);
    }

    public static void i(String tag, int level, String msg) {
        i(tag, level, msg, null);
    }

    public static void i(String tag, String msg, Throwable ex) {
        i(tag, NORMAL, msg, ex);
    }

    public static void i(String tag, int level, String msg, Throwable ex) {
        if (mLogAble) Log.i(tag, msg + (ex == null ? "" : " " + ex.toString()));
        if (isCLR) {
            // 染色用户才上报
            addLogItem(tag, "I", msg + (ex == null ? "" : " " + ex.toString()));
        }
    }

    public static void w(String tag, String msg) {
        w(tag, NORMAL, msg);
    }

    public static void w(String tag, int level, String msg) {
        w(tag, level, msg, null);
    }

    public static void w(String tag, String msg, Throwable ex) {
        w(tag, NORMAL, msg, ex);
    }

    public static void w(String tag, int level, String msg, Throwable ex) {
        if (mLogAble) Log.w(tag, msg + (ex == null ? "" : " " + ex.toString()));
        if (level == NORMAL || isCLR) {
            // 保存文件
            addLogItem(tag, "W", msg + (ex == null ? "" : " " + ex.toString()));
        }
    }

    public static void e(String tag, String msg) {
        e(tag, NORMAL, msg);
    }

    public static void e(String tag, int level, String msg) {
        e(tag, level, msg, null);
    }

    public static void e(String tag, String msg, Throwable ex) {
        e(tag, NORMAL, msg, ex);
    }

    public static void e(String tag, int level, String msg, Throwable ex) {
        if (mLogAble) Log.e(tag, msg + (ex == null ? "" : " " + ex.toString()));
        if (level == NORMAL || isCLR) {
            // 保存文件
            addLogItem(tag, "E", msg + (ex == null ? "" : " " + ex.toString()));
        }
    }

    /**
     * 设置染色标记，染色后将保存v级别日志，否则保存w和e
     *
     * @param clr
     */
    public static void setIsCLR(boolean clr) {
        isCLR = clr;
    }

    public static boolean isColorLevel() {
        return isCLR;
    }


    private static void addLogItem(String tag, String level, String msg) {
        LogItem log = new LogItem();
        log.tag = tag;
        log.msg = msg;
        log.level = level;
        log.time = System.currentTimeMillis();
        log.threadId = Thread.currentThread().getId();
        list.add(log);
        if (list.size() >= LOG_ITEM_MAX_CACHE_SIZE) {
            flushLog();
        }
    }

    private static void dumpMemoryNecessary() {
        if (System.currentTimeMillis() - lastPrintMemoryTime > 180000L) {
            ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            d("MSF.D.QLog", QLog.NORMAL, ("availMem:" + memoryInfo.availMem / 1024L / 1024L + "M" + " lowThreshold:" + memoryInfo.threshold / 1024L / 1024L + "M"));
            lastPrintMemoryTime = System.currentTimeMillis();
        }
    }


    private static void initWriter(String path) throws IOException {
        File menu = new File(logPath);
        File file;

        try {
            if (!menu.exists()) {
                menu.mkdirs();
            }
            file = new File(path);
            if (!file.exists()) {
                QLogUtil.zipLogFiles(logPath);
                file.createNewFile();

                try {
                    if (writer != null) {
                        writer.flush();
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                writer = new BufferedWriter(new FileWriter(path, true), 8192);
                if (!TextUtils.isEmpty(sBuildNumber)) {
                    writeBuild();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (writer == null) {
            writer = new BufferedWriter(new FileWriter(path, true), 8192);
            if (!TextUtils.isEmpty(sBuildNumber)) {
                writeBuild();
            }
        }
    }

    private static void writeBuild() throws IOException {
        if (writer != null) {
            writer.write(QLogUtil.getFileTimeStr(System.currentTimeMillis()) + "|" + processName + "|D|" + "|SDK_Version: " + sBuildNumber + "\r\n");
            writer.flush();
        }
    }

    private static class LogItem {
        String tag;
        String msg;
        String level;
        long time;
        long threadId;

        @Override
        public String toString() {
            SimpleDateFormat timeFormatter = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
            return timeFormatter.format(time) + " " + threadId + "/" + level + "/" + tag + ": " + msg;
        }
    }

    /**
     * 将缓冲区Log刷到文件
     */
    public static void flushLog() {
        flushLog(null);
    }

    public interface QLogFlushListener {
        void onCompletion();
    }

    /**
     * 将缓冲区Log刷到文件
     */
    public static void flushLog(final QLogFlushListener listener) {
        if (QLogUtil.maxLogSize <= 1024) {
            listener.onCompletion();
            return;
        }
        synchronized (list) {
            if (mHandler == null) {
                if (mLogAble) Log.e("QLog", "You should called init at first.");
                if (listener != null) {
                    listener.onCompletion();
                }
                return;
            }
            if (list.size() == 0) {
                if (listener != null) {
                    listener.onCompletion();
                }
                return;
            }
            final ArrayList<LogItem> data = new ArrayList<>(list.size());
            data.addAll(list);
            list.clear();
            dumpMemoryNecessary();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // 根据 data 的第一个元素的时间 获得文件

                    String filePath = QLogUtil.getFilePath(logPath, data.get(0).time, processName);
                    try {
                        initWriter(filePath);

                        QLogUtil.clearLogFiles(logPath);
                        // 将data保存到文件
                        for (LogItem item : data) {
                            if (item != null) {
                                writer.write(item.toString());
                                writer.newLine();
                            } else {
                                if (mLogAble) Log.e("QLog", "item is null");
                            }
                        }
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (listener != null) {
                            listener.onCompletion();
                        }
                    }
                }
            });
        }
    }

    /**
     * 打包一个日志文件，时间点为time1到time2
     *
     * @param time1  ms
     * @param time2  ms
     * @param delete 打包后是否删除原文件
     * @return 打包的文件路径，未找到相关时间返回null
     */
    public static String createLogFile(long time1, long time2, boolean delete) {
        return QLogUtil.createLogFile(logPath, time1, time2, delete);
    }

    /**
     * 打包一个日志文件，时间点为serializeTime1到serializeTime1
     *
     * @param serializeTime1 字符串时间 例如: "2017061410"  年月日时
     * @param serializeTime1 字符串时间 例如: "2017061420"  年月日时
     * @param delete         打包后是否删除原文件
     * @return 打包的文件路径，未找到相关时间返回null
     */
    public static String createLogFile(String serializeTime1, String serializeTime2, boolean delete) {
        return QLogUtil.createLogFile(logPath, serializeTime1, serializeTime2, delete);
    }


}
