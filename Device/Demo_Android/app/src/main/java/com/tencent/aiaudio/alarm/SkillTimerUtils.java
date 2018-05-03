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
package com.tencent.aiaudio.alarm;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SkillTimerUtils {
    private static final String TAG = "SkillTimerUtils";

    private static final long DAY_INTERVAL = 24 * 60 * 60 * 1000;
    private static final long WEEK_INTERVAL = 7 * DAY_INTERVAL;

    /**
     * 当前的间隔是按照每天来计算
     *
     * @param currentTime 最近一次触发的时间值
     */
    public static long nextTime(long currentTime) {
        long systemTime = System.currentTimeMillis();
        long nextTime = getNextDayTime(currentTime);

        // 如果当前系统时间大于下一次触发时间.
        while (systemTime >= nextTime) {
            Log.d(TAG, "systemTime(" + systemTime + "ms) >= nextTime(" + nextTime + ")");
            nextTime = getNextDayTime(nextTime);
        }

        return nextTime;
    }

    /**
     * 传递一个时间值，获取闹钟最近本周几的时间值
     */
    private static long getCurrentWeekTime(Calendar calendar, int week) {
        calendar.set(Calendar.DAY_OF_WEEK, week + 1);
        return calendar.getTimeInMillis();
    }

    /**
     * 当前的间隔是按照每周几来计算
     *
     * @param currentTime 最近一次触发的时间值
     * @param weekValue   当前每周几,格式如"1,3,5"
     */
    public static long nextTime(long currentTime, String weekValue) {
        final int[] weeks = parseDateWeeks(weekValue);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(currentTime));
        Log.d(TAG, "current:" + getAlarmTime(currentTime));

        if (null == weeks || weeks.length == 0) {
            Log.d(TAG, "null == checkedWeeks || checkedWeeks.length == 0.");
            return 0L;
        }

        long systemTime = System.currentTimeMillis();
        long weekStartTime = getCurrentWeekTime(calendar, weeks[0]);
        long nextStartTime = weekStartTime;

        if (nextStartTime >= systemTime) {
            return nextStartTime;
        }

        int weeksLength = weeks.length;
        for (int index = 1; index < weeksLength; index++) {
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            calendar.set(Calendar.DAY_OF_WEEK, weeks[index] + 1);
            nextStartTime = getCurrentWeekTime(calendar, weeks[index]);
            Log.d(TAG, String.format("systemTime:%s,nextStartTime:%s.",
                    getAlarmTime(systemTime), getAlarmTime(nextStartTime)));

            // 如果指定本周t1,t2,..,t7中最后一个周几的元素
            if (index == weeksLength - 1) {
                Log.d(TAG, String.format("nextStartTime=%s.systemTime=%s.", nextStartTime, systemTime));
                if (nextStartTime < systemTime) {
                    long nextWeekStartTime = getNextWeekTime(weekStartTime);
                    Log.d(TAG, "nextWeekStartTime:" + getAlarmTime(nextWeekStartTime));
                    nextTime(nextWeekStartTime, weekValue);
                } else {
                    return nextStartTime;
                }
            }
        }

        return nextStartTime;
    }

    private static long getNextWeekTime(long currentTime) {
        return currentTime + WEEK_INTERVAL;
    }

    private static long getNextDayTime(long currentTime) {
        return currentTime + DAY_INTERVAL;
    }

    public static String getAlarmTime(long alarmTime) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(new Date(alarmTime));
    }

    public static int[] parseDateWeeks(String weekValue) {
        int[] weeks = null;

        try {
            final String[] items = weekValue.split(",");
            weeks = new int[items.length];
            int i = 0;

            for (String s : items) {
                weeks[i++] = Integer.valueOf(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return weeks;
    }
}

