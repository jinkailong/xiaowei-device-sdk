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

import android.app.Application;
import android.text.TextUtils;

import com.tencent.xiaowei.info.XWResponseInfo;
import com.tencent.xiaowei.sdk.XWDeviceBaseManager;
import com.tencent.xiaowei.util.JsonUtil;
import com.tencent.xiaowei.util.QLog;

import java.util.List;

import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_ALARM;

/**
 * 闹钟相关管理：将后台下发的闹钟事件
 */
public class DeviceSkillAlarmManager implements IDeviceSkillAlarmManager {
    private static final String TAG = DeviceSkillAlarmManager.class.getSimpleName();

    /**
     * 添加一个闹钟项
     */
    private static final int CLOCK_OPT_ADD = 1;
    /**
     * 更新一个闹钟项
     */
    private static final int CLOCK_OPT_UPDATE = 2;
    /**
     * 删除一个闹钟项
     */
    private static final int CLOCK_OPT_DEL = 3;
    /**
     * 修改一个闹钟项类型
     */
    private static final int CLOCK_OPT_MODIFY_TYPE = 4;

    private volatile static IDeviceSkillAlarmManager instance;

    public static IDeviceSkillAlarmManager instance() {
        if (instance == null) {
            synchronized (DeviceSkillAlarmManager.class) {
                if (instance == null) {
                    instance = new DeviceSkillAlarmManager();
                }
            }
        }

        return instance;
    }

    private DeviceSkillAlarmManager() {
    }

    @Override
    public void init(Application application) {
        QLog.d(TAG, "init() => device skill alarm manager.");
        SkillAlarmManager.instance().init(application);
    }

    @Override
    public void startDeviceAllAlarm() {
        QLog.d(TAG, "startDeviceAllAlarm().");

        SkillAlarmManager.instance().startAllAlarm();
    }

    @Override
    public boolean isSetAlarmOperation(XWResponseInfo responseInfo) {
        return responseInfo != null
                && responseInfo.appInfo.ID.equals(SKILL_ID_ALARM)
                && responseInfo.responseData != null;
    }

    @Override
    public boolean isSnoozeAlarm(XWResponseInfo responseInfo) {
        ClockListBean array = JsonUtil.getObject(responseInfo.responseData, ClockListBean.class);

        if (array == null) {
            return false;
        }

        List<ClockListBean.ClockInfoBean> clockResources = array.getClock_info();

        return clockResources != null && clockResources.size() == 1 && clockResources.get(0).isSnooze();

    }

    @Override
    public boolean operationAlarmSkill(XWResponseInfo responseInfo) {
        ClockListBean array = JsonUtil.getObject(responseInfo.responseData, ClockListBean.class);

        if (array == null) {
            QLog.d(TAG, "operationAlarmSkill() array == null.");
            return false;
        }

        List<ClockListBean.ClockInfoBean> clockResources = array.getClock_info();
        if (clockResources == null || clockResources.size() == 0) {
            QLog.d(TAG, "clockResources == null || clockResources.size() == 0.");
            return false;
        }

        QLog.d(TAG, String.format("operationAlarmSkill size=%s", clockResources.size()));
        for (ClockListBean.ClockInfoBean clockInfo : clockResources) {
            QLog.d(TAG, String.format("clockInfo=%s", clockInfo.toString()));

            SkillAlarmBean alarmBean = new SkillAlarmBean();
            alarmBean.setKey(clockInfo.getClock_id());
            alarmBean.setEvent(clockInfo.getEvent());
            alarmBean.setAlarmTime(Long.valueOf(clockInfo.getTrig_time()) * 1000L);
            alarmBean.setServerTime(XWDeviceBaseManager.getServerTime() * 1000L);
            QLog.d(TAG, String.format("serverTime:%s, currentTime=%s",
                    alarmBean.getServerTime(), System.currentTimeMillis()));
            alarmBean.setServerTimeDifference(alarmBean.getServerTime() - System.currentTimeMillis());
            alarmBean.setServerType(clockInfo.getService_type());
            alarmBean.setType(clockInfo.getClock_type());
            alarmBean.setRepeatType(clockInfo.getRepeat_type());
            alarmBean.setRepeatInterval(clockInfo.getRepeat_interval());

            int operation = ISkillAlarmManager.SkillAlarmManagerOperation.CLOCK_OPT_NONE;
            if (clockInfo.getOpt() == CLOCK_OPT_ADD) {
                operation = ISkillAlarmManager.SkillAlarmManagerOperation.CLOCK_OPT_ADD;
            } else if (clockInfo.getOpt() == CLOCK_OPT_DEL) {
                operation = ISkillAlarmManager.SkillAlarmManagerOperation.CLOCK_OPT_DEL;
            } else if (clockInfo.getOpt() == CLOCK_OPT_UPDATE) {
                operation = ISkillAlarmManager.SkillAlarmManagerOperation.CLOCK_OPT_UPDATE;
            } else if (clockInfo.getOpt() == CLOCK_OPT_MODIFY_TYPE) {
                operation = ISkillAlarmManager.SkillAlarmManagerOperation.CLOCK_OPT_MODIFY_TYPE;
            }

            SkillAlarmManager.instance().executeAlarm(alarmBean, operation, new OnOperationFinishListener() {
                @Override
                public void onOperationFinish(SkillAlarmBean bean, String action) {
                    QLog.d(TAG, String.format("onOperationFinish() SkillAlarmBean=%s, action=%s", bean.toString(), action));

                    if (TextUtils.equals(action, AlarmDbManager.ALARM_ADD_ACTION)) {
                        SkillAlarmManager.instance().startAlarm(bean);
                    } else if (TextUtils.equals(action, AlarmDbManager.ALARM_UPDATE_ACTION)) {
                        SkillAlarmManager.instance().updateAlarm(bean);
                    } else if (TextUtils.equals(action, AlarmDbManager.ALARM_DEL_ACTION)) {
                        SkillAlarmManager.instance().deleteAlarm(bean);
                    }
                }
            });
        }
        return true;
    }

    @Override
    public void updateAlarmList() {
        QLog.d(TAG, "updateAlarmList() update alarm list with server alarm list");
        SkillAlarmManager.instance().updateAlarmList();
    }

    @Override
    public void triggerDelayAlarm() {
        SkillAlarmManager.instance().triggerDelayAlarm();
    }

    @Override
    public void eraseAllAlarms() {
        SkillAlarmManager.instance().eraseAllAlarms();
    }

    @Override
    public void release() {
        QLog.d(TAG, "release() => device skill alarm manager.");
        SkillAlarmManager.instance().release();
    }
}
