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

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class QLogUtil {

    private static final String TAG = "QLogUtil";
    private static HandlerThread mZipHandlerThread;
    private static Handler mZipHandler;

    public static long maxLogSize = 500 * 1024 * 1024l;
    public static long minFreeSpace = 200 * 1024 * 1024l;

    static {
        mZipHandlerThread = new HandlerThread("ZipFileLog");
        mZipHandlerThread.start();
        mZipHandler = new Handler(mZipHandlerThread.getLooper());
    }

    static String getLastLogFileName(File[] files) {
        if (files != null && files.length > 0) {
            // 排序
            File file = null;
            long t = 0;
            for (File f : files) {
                long temp = getSerializeTime(f.getName());
                if (temp > t) {
                    t = temp;
                    file = f;
                }
            }
            if (file != null) {
                return file.getName();
            }
        }
        return null;
    }

    static String getFirstLogFileName(File[] files) {
        if (files != null && files.length > 0) {
            // 排序
            File file = null;
            long t = Long.MAX_VALUE;
            for (File f : files) {
                long temp = getSerializeTime(f.getName());
                if (temp < t) {
                    t = temp;
                    file = f;
                }
            }
            if (file != null) {
                return file.getName();
            }
        }
        return null;
    }

    static void zipLogFiles(String zipPath, File files[], boolean delete) {
        try {
            File zipFile = new File(zipPath);
            zipFile.getParentFile().mkdirs();
            InputStream input;
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(
                    zipFile));
            zipOut.setComment("xiaowei sdk log");
            for (int i = 0; i < files.length; ++i) {
                input = new FileInputStream(files[i]);
                zipOut.putNextEntry(new ZipEntry(files[i].getName()));
                int count;
                byte[] buffer = new byte[2048];
                while ((count = input.read(buffer)) > 0) {
                    zipOut.write(buffer, 0, count);
                }
                input.close();
            }
            zipOut.close();
            if (delete) {
                for (File file : files) {
                    file.delete();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void zipLogFiles(final String logPath) {
        mZipHandler.post(new Runnable() {
            @Override
            public void run() {
                // 将之前的log文件压缩
                File menu = new File(logPath);
                File files[] = menu.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.endsWith(".log") && !filename.contains(getFileTimeStr(System.currentTimeMillis()));
                    }
                });
                if (files == null || files.length == 0) {
                    return;
                }

                String lastFile = getLastLogFileName(files);
                if (lastFile != null) {
                    zipLogFiles(menu + "/" + lastFile.substring(0, lastFile.length() - 3) + "zip", files, true);
                }
            }
        });
    }

    /**
     * 最大仅保存1GB日志，如果发现剩余内存小于200MB，保存之前先删除旧的日志
     */
    static void clearLogFiles(String logPath) {
        String path = needClearFilePath(logPath);
        while (path != null) {
            boolean success = new File(path).delete();
            Log.d("QLog", "delete " + path);
            if (!success) {
                break;
            }
            path = needClearFilePath(logPath);
        }
    }

    static String getFilePath(String logPath, long currentTime, String processName) {
        String thisHourLogName = getFileTimeStr(currentTime);
        return logPath + getLogFileName(thisHourLogName, processName);
    }

    static String getLogFileName(String hourTime, String processName) {
        return processName.replace(":", "_") + "." + hourTime + ".log";
    }

    static String getFileTimeStr(long currentTime) {
        SimpleDateFormat logFileFormatter = new SimpleDateFormat("yyyy.MM.dd.HH");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        return logFileFormatter.format(calendar.getTime());
    }

    private static String needClearFilePath(String logPath) {
        File menu = new File(logPath);
        File files[] = menu.listFiles();
        if (files != null && files.length > 0) {
            long fileCountLength = 0;
            for (File f : files) {
                fileCountLength += f.length();
            }
            if (fileCountLength > maxLogSize || getSDCardAvailableSize() < minFreeSpace) {
                return files[0].getAbsolutePath();
            }
        }
        return null;
    }

    private static long getSDCardAvailableSize() {
        long available;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File path = Environment.getExternalStorageDirectory();
            StatFs statfs = new StatFs(path.getPath());
            long blocSize = statfs.getBlockSize();
            long availaBlock = statfs.getAvailableBlocks();

            available = availaBlock * blocSize;
        } else {
            available = 0;
        }
        return available;
    }

    static String createLogFile(String menuPath, long time1, long time2, boolean delete) {
        if (time1 < 0) {
            time1 = 0;
        }
        if (time1 > System.currentTimeMillis()) {
            time1 = System.currentTimeMillis();
        }
        if (time2 < 0) {
            time2 = 0;
        }
        if (time2 > System.currentTimeMillis()) {
            time2 = System.currentTimeMillis();
        }
        long t1 = getSerializeTime(time1);
        long t2 = getSerializeTime(time2);
        return createLogFileImpl(menuPath, t1, t2, delete);
    }

    static String createLogFile(String menuPath, String serializeTime1, String serializeTime2, boolean delete) {
        long t1 = getSerializeTime(serializeTime1);
        long t2 = getSerializeTime(serializeTime2);
        return createLogFileImpl(menuPath, t1, t2, delete);
    }


    /**
     * 创建两个时间点中的日志压缩文件，便于上传
     *
     * @return
     */
    static String createLogFileImpl(String menuPath, long t1, long t2, boolean delete) {
        File menu = new File(menuPath);
        File files[] = menu.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith("log") || filename.endsWith("zip");
            }
        });

        File menuUpload = new File(menuPath + "/upload");
        File filesUpload[] = menuUpload.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith("failed");
            }
        });
        ArrayList<File> list = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                long tF = getSerializeTime(file.getName());
                if (tF >= t1 && tF <= t2) {
                    list.add(file);
                }
            }
        }

        if (filesUpload != null) {
            for (File file : filesUpload) {
                long tF = getSerializeTime(file.getName());
                if (tF >= t1 && tF <= t2) {
                    list.add(file);
                }
            }
        }

        if (list.size() == 0) {
            return null;
        }
        File logs[] = new File[list.size()];
        for (int i = 0; i < list.size(); i++) {
            logs[i] = list.get(i);
        }
        String filePath = menuPath + "/upload/" + getFileTimeStr(System.currentTimeMillis()) + ".zip";
        zipLogFiles(filePath, logs, delete);
        return filePath;
    }

    public static long getSerializeTime(long t) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");
        try {
            return Long.valueOf(format.format(t));
        } catch (Exception e) {
        }
        return -1;
    }

    static long getSerializeTime(String logFileName) {
        String time = logFileName.replaceAll("[^\\d]", "");
        try {
            return Long.valueOf(time);
        } catch (Exception e) {
        }
        return -1;
    }
}
