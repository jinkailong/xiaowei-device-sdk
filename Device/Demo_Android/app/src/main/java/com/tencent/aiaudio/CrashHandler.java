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
package com.tencent.aiaudio;

import android.os.Process;
import android.os.SystemClock;

import com.tencent.xiaowei.util.QLog;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 捕捉crash，保存到QLog
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static CrashHandler ourInstance = null;
    private Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;

    public static CrashHandler getInstance() {
        synchronized (CrashHandler.class) {
            if (ourInstance == null) {
                ourInstance = new CrashHandler();
                ourInstance.defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
            }
        }
        return ourInstance;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && null != defaultUncaughtExceptionHandler) {
            defaultUncaughtExceptionHandler.uncaughtException(thread, ex);
        } else {
            SystemClock.sleep(1000);
            Process.killProcess(Process.myPid());
            System.exit(1);
        }
    }

    private boolean handleException(Throwable ex) {
        if (null == ex) {
            return false;
        } else {
            saveCrashToLocal(ex);
        }
        return true;
    }

    /**
     * save crash to local
     */
    private void saveCrashToLocal(Throwable throwable) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        try {
            Throwable cause = throwable.getCause();
            cause.printStackTrace(printWriter);
        } catch (NullPointerException e) {
        } finally {
            printWriter.close();
        }
        final String result = writer.toString();
        final String message = throwable.getMessage();
        QLog.e("Crash", result + " " + message);
        QLog.flushLog();
    }

}
