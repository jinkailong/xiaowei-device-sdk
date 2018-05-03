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

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;

import com.tencent.aiaudio.activity.Alarm2Activity;
import com.tencent.aiaudio.activity.AlarmActivity;
import com.tencent.aiaudio.chat.AVChatManager;
import com.tencent.xiaowei.control.XWeiControl;
import com.tencent.xiaowei.def.XWCommonDef;
import com.tencent.xiaowei.info.XWResponseInfo;
import com.tencent.xiaowei.sdk.XWDeviceBaseManager;
import com.tencent.xiaowei.sdk.XWSDK;
import com.tencent.xiaowei.util.JsonUtil;
import com.tencent.xiaowei.util.QLog;

import org.xutils.x;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static android.content.Context.ALARM_SERVICE;
import static com.tencent.aiaudio.alarm.ISkillAlarmManager.SkillAlarmManagerOperation.CLOCK_OPT_ADD;
import static com.tencent.aiaudio.alarm.ISkillAlarmManager.SkillAlarmManagerOperation.CLOCK_OPT_DEL;
import static com.tencent.aiaudio.alarm.ISkillAlarmManager.SkillAlarmManagerOperation.CLOCK_OPT_MODIFY_TYPE;
import static com.tencent.aiaudio.alarm.ISkillAlarmManager.SkillAlarmManagerOperation.CLOCK_OPT_UPDATE;
import static java.lang.System.currentTimeMillis;


public class SkillAlarmManager implements ISkillAlarmManager, OnQueryAllAlarmListener {
    private static final String TAG = SkillAlarmManager.class.getSimpleName();

    private volatile static ISkillAlarmManager INSTANCE;

    private Application mApplication;

    private Map<Integer, SkillAlarmHistory> mAlarmResourceMap;

    List<SkillAlarmBean> mDelayAlarmList;

    private ISkillAlarmConfig mSkillAlarmConfig;

    public static ISkillAlarmManager instance() {
        if (INSTANCE == null) {
            synchronized (SkillAlarmManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SkillAlarmManager();
                }
            }
        }

        return INSTANCE;
    }

    private SkillAlarmManager() {
        StartAlarmBroadcastReceiver.mOnObtainAlarmResource = new OnObtainAlarmResource() {
            @Override
            public SkillAlarmHistory onObtainAlarm(int key) {
                QLog.d(TAG, String.format("onObtainAlarm(%s)", key));

                if (mAlarmResourceMap == null) {
                    QLog.d(TAG, "mAlarmResourceMap == null.");
                    return null;
                }

                return mAlarmResourceMap.remove(key);
            }
        };
        mAlarmResourceMap = new ConcurrentHashMap<>();
        mDelayAlarmList = new CopyOnWriteArrayList<>();
    }

    @Override
    public void init(Application application) {
        this.mApplication = application;
        x.Ext.init(application);
    }

    @Override
    public void startAllAlarm() {
        QLog.d(TAG, "startAllAlarm()");
        AlarmDbManager.instance().queryAllAlarmItem(SkillAlarmManager.this);
    }

    @Override
    public void startAlarm(SkillAlarmBean bean) {
        if (bean != null && !isExistsAlarmTime(bean)) {
            if (bean.getType() == SkillAlarmBean.TYPE_ALARM_CLOCK) {
                startAlarmTime(bean);
            } else if (bean.getType() == SkillAlarmBean.TYPE_ALARM_PROMPT) {
                startAlarmTime(bean);
            } else if (bean.getType() == SkillAlarmBean.TYPE_ALARM_LOOP) {
                startRepeatAlarmTime(bean);
            }
        }
    }

    @Override
    public void updateAlarm(SkillAlarmBean bean) {
        cancelAlarmTime(getKeyValue(bean));
        startAlarm(bean);
    }

    @Override
    public void deleteAlarm(SkillAlarmBean bean) {
        cancelAlarmTime(getKeyValue(bean));
        AlarmDbManager.instance().deleteAlarmItem(bean, null);
    }

    @Override
    public void executeAlarm(SkillAlarmBean bean, int operation, OnOperationFinishListener listener) {
        if (bean == null) {
            QLog.d(TAG, "executeAlarm() bean == null.");
            return;
        }

        QLog.d(TAG, String.format("executeAlarm(bean=%s, operation=%s)", bean.toString(), obtainAlarmValue(operation)));

        if (operation == CLOCK_OPT_ADD) {
            AlarmDbManager.instance().addAlarmItem(bean, listener);
        } else if (operation == CLOCK_OPT_DEL) {
            AlarmDbManager.instance().deleteAlarmItem(bean, listener);
        } else if (operation == CLOCK_OPT_UPDATE) {
            AlarmDbManager.instance().updateAlarmItem(bean, listener);
        }
    }

    @Override
    public void setSkillAlarmConfig(ISkillAlarmConfig config) {
        this.mSkillAlarmConfig = config;
    }

    @Override
    public void delayAlarm(SkillAlarmBean bean) {
        QLog.d(TAG, "delayAlarm alarm: " + bean.toString());
        mDelayAlarmList.clear();
        mDelayAlarmList.add(bean);
    }

    @Override
    public void updateAlarmList() {
        QLog.d(TAG, "updateAlarmList() update alarm list with server alarm list");

        XWSDK.getInstance().getDeviceAlarmList(new XWSDK.GetAlarmListRspListener() {
            @Override
            public void onGetAlarmList(int errCode, String strVoiceID, String[] arrayAlarmList) {
                if (errCode == XWCommonDef.ErrorCode.ERROR_NULL_SUCC) {

                    if (arrayAlarmList == null) {
                        QLog.d(TAG, "getDeviceAlarmList() array == null.");
                        return;
                    }


                    QLog.d(TAG, String.format("operationAlarmSkill size=%s", arrayAlarmList.length));

                    List<SkillAlarmBean> deletedAlarmList = getAlarmList();  // 云端已经删除的闹钟列表：先获取本地的闹钟列表，再根据云端列表去比较

                    for (String strClockInfo : arrayAlarmList) {
                        ClockListBean.ClockInfoBean clockInfo = JsonUtil.getObject(strClockInfo, ClockListBean.ClockInfoBean.class);
                        if (clockInfo == null) {
                            continue;
                        }

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

                        if (isExistsAlarmTime(alarmBean)) {
                            cancelAlarmTime(getKeyValue(alarmBean));
                            SkillAlarmHistory history = mAlarmResourceMap.get(getKeyValue(alarmBean));
                            if (history != null)
                                deletedAlarmList.remove(history.getAlarmBean());
                        }

                        executeAlarm(alarmBean, CLOCK_OPT_ADD, new OnOperationFinishListener() {
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

                    // 删除本地数据中的闹钟数据
                    for (SkillAlarmBean bean : deletedAlarmList) {
                        SkillAlarmManager.instance().deleteAlarm(bean);
                    }
                }
            }
        });
    }

    @Override
    public void triggerDelayAlarm() {
        QLog.d(TAG, "triggerDelayAlarm delayAlarm size: " + mDelayAlarmList.size());
        if (mDelayAlarmList.size() > 0) {
            ArrayList<SkillAlarmBean> beans = new ArrayList<>(mDelayAlarmList);
            // 打开闹钟Activity
            Intent intent1 = new Intent(mApplication.getApplicationContext(), AlarmActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent1.putExtra("alarms", beans);
            mApplication.getApplicationContext().startActivity(intent1);

            mDelayAlarmList.clear();
        }
    }

    @Override
    public void eraseAllAlarms() {
        mDelayAlarmList.clear();
        for (SkillAlarmHistory bean : mAlarmResourceMap.values()) {
            deleteAlarm(bean.getAlarmBean());
        }
    }

    @Override
    public void release() {
        QLog.d(TAG, "release() => skill alarm manager.");
        AlarmDbManager.instance().release();
        if (mApplication != null) {
            mApplication = null;
        }
    }

    @Override
    public ArrayList<SkillAlarmBean> getAlarmList() {
        if (mAlarmResourceMap != null) {
            ArrayList<SkillAlarmBean> list = new ArrayList<>(mAlarmResourceMap.size());
            for (SkillAlarmHistory his : mAlarmResourceMap.values()) {
                list.add(his.getAlarmBean());
            }
            return list;
        }
        return null;
    }

    private String obtainAlarmValue(int operation) {
        if (operation == CLOCK_OPT_ADD) {
            return "CLOCK_OPT_ADD";
        } else if (operation == CLOCK_OPT_DEL) {
            return "CLOCK_OPT_DEL";
        } else if (operation == CLOCK_OPT_MODIFY_TYPE) {
            return "CLOCK_OPT_MODIFY_TYPE";
        } else if (operation == CLOCK_OPT_UPDATE) {
            return "CLOCK_OPT_UPDATE";
        } else {
            return "CLOCK_OPT_NONE";
        }
    }

    @Override
    public void onQueryAllAlarm(final List<SkillAlarmBean> alarmItems) {
        if (alarmItems == null || alarmItems.size() == 0) {
            QLog.d(TAG, "alarmItems == null || alarmItems.size() == 0.");
        } else {
            QLog.d(TAG, String.format("onQueryAllAlarm() alarmItems.size() = %s", alarmItems.size()));

            for (SkillAlarmBean bean : alarmItems) {
                QLog.d(TAG, String.format("onQueryAllAlarm() alarm time => %s, bean => %s",
                        SkillTimerUtils.getAlarmTime(bean.getAlarmTime()), bean.toString()));

                if (!isExistsAlarmTime(bean)) {
                    int type = bean.getType();
                    if (type == SkillAlarmBean.TYPE_ALARM_CLOCK) {
                        startAlarmTime(bean);
                    } else if (type == SkillAlarmBean.TYPE_ALARM_PROMPT) {
                        startAlarmTime(bean);
                    } else if (type == SkillAlarmBean.TYPE_ALARM_LOOP) {
                        startRepeatAlarmTime(bean);
                    }
                }
            }
        }
    }

    /**
     * 该闹钟是否已经设置
     *
     * @param bean 闹钟实体
     * @return 已设置返回 true，否则返回false
     */
    private boolean isExistsAlarmTime(SkillAlarmBean bean) {
        if (bean == null) {
            QLog.d(TAG, "isExistsAlarmTime bean == null.");
            return false;
        }

        int key = getKeyValue(bean);
        if (mAlarmResourceMap == null) {
            QLog.d(TAG, "isExistsAlarmTime() mAlarmResourceMap == null.");
            return false;
        }

        return mAlarmResourceMap.get(key) != null;
    }

    /**
     * 开始闹钟项
     */
    private void startAlarmTime(SkillAlarmBean bean) {
        if (isAlarmOverdue(bean)) {
            QLog.d(TAG, "delete overdue alarm item.");
            AlarmDbManager.instance().deleteAlarmItem(bean, null);
        } else {
            startAlarmTime(bean, StartAlarmBroadcastReceiver.START_ALARM_DELETE);
        }
    }

    /**
     * 开始循环闹钟项
     */
    private void startRepeatAlarmTime(SkillAlarmBean bean) {
        if (updateAlarmTime(bean)) {
            startAlarmTime(bean, StartAlarmBroadcastReceiver.START_ALARM_UPDATE_TIME);
        } else {
            QLog.w(TAG, "update alarm fail.");
        }
    }

    /**
     * 判断是否闹钟过期
     */
    private boolean isAlarmOverdue(SkillAlarmBean bean) {
        if (bean == null) {
            QLog.d(TAG, "bean == null.");
            return false;
        }

        return bean.getAlarmTime() < currentTimeMillis();
    }

    /**
     * 如果循环闹钟当前时间已经过期，则更新循环闹钟时间
     *
     * @return 返回true，表示该闹钟有效
     */
    private boolean updateAlarmTime(SkillAlarmBean bean) {
        if (bean == null) {
            QLog.d(TAG, "updateAlarmTime bean == null.");
            return false;
        }

        long oldAlarmTime = bean.getAlarmTime();
        long oldServerTime = bean.getServerTime();
        int repeatType = bean.getRepeatType();
        String repeatInterval = bean.getRepeatInterval();

        if (oldAlarmTime >= currentTimeMillis()) {
            QLog.d(TAG, "oldAlarmTime >= System.currentTimeMillis().");
            return true;
        }

        if (repeatType == SkillAlarmBean.CLOCK_REPEAT_TYPE.CLOCK_REPEAT_TYPE_WEEK) {
            bean.setAlarmTime(SkillTimerUtils.nextTime(oldAlarmTime, bean.getRepeatInterval()));
            bean.setServerTime(SkillTimerUtils.nextTime(oldServerTime, repeatInterval));
        } else if (repeatType == SkillAlarmBean.CLOCK_REPEAT_TYPE.CLOCK_REPEAT_TYPE_DAY) {
            bean.setAlarmTime(SkillTimerUtils.nextTime(oldAlarmTime));
            bean.setServerTime(SkillTimerUtils.nextTime(oldServerTime));
        }

        QLog.d(TAG, String.format("updateAlarmTime() bean=%s", bean.toString()));
        AlarmDbManager.instance().updateAlarmItem(bean, null);
        return true;
    }

    /**
     * 利用AlarmManager设置一个闹钟
     *
     * @param bean      闹钟实体
     * @param operation 非循环：StartAlarmBroadcastReceiver.START_ALARM_DELETE
     *                  循环：StartAlarmBroadcastReceiver.START_ALARM_UPDATE_TIME
     */
    private void startAlarmTime(SkillAlarmBean bean, int operation) {
        if (bean == null) {
            QLog.d(TAG, "startAlarmTime() bean == null.");
            return;
        }

        long alarmTime = bean.getAlarmTime();
        QLog.d(TAG, "startAlarmTime() value:" + bean.toString());

        long serverTimeMillis = bean.getServerTime();
        long localTimeMillis = System.currentTimeMillis();

        String localTimeValue = SkillTimerUtils.getAlarmTime(localTimeMillis);
        String serverTimeValue = SkillTimerUtils.getAlarmTime(serverTimeMillis);
        String alarmTimeValue = SkillTimerUtils.getAlarmTime(alarmTime);

        QLog.d(TAG, String.format("localTimeValue=%s,serverTimeValue=%s,alarmTimeValue=%s,difference=%s",
                localTimeValue, serverTimeValue, alarmTimeValue, bean.getServerTimeDifference()));

        if (TextUtils.isEmpty(bean.getKey())) {
            AlarmDbManager.instance().deleteAlarmItem(bean, null);
            QLog.d(TAG, "TextUtils.isEmpty(bean.getKey(), delete overdue alarm item.");
            return;
        }

        long startAlarmSurplusMillis = alarmTime - localTimeMillis;
        if (startAlarmSurplusMillis < 0 && bean.getType() != SkillAlarmBean.TYPE_ALARM_LOOP) {
            AlarmDbManager.instance().deleteAlarmItem(bean, null);
            QLog.d(TAG, "startAlarmSurplusSecond < 0,delete overdue alarm item.");
            return;
        }

        int key = getKeyValue(bean);
        PendingIntent intent = getPendingIntent(key);
        putBasePlayerBeanToResources(key, bean, operation);

        if (bean.ismIsOpen()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(alarmTime);
            calendar.add(Calendar.MILLISECOND, (int) bean.getServerTimeDifference());

            AlarmManager alarmManager = obtainAlarmManager();
            if (alarmManager == null) {
                QLog.d(TAG, "alarmManager == null.");
            } else {
                QLog.d(TAG, String.format("启动闹钟时间 ==> (timeInMillis=%s)",
                        SkillTimerUtils.getAlarmTime(calendar.getTimeInMillis())));

                if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent);
                }
            }
        }
    }

    private void putBasePlayerBeanToResources(int key, SkillAlarmBean resource, int operation) {
        if (mAlarmResourceMap == null) {
            mAlarmResourceMap = new ConcurrentHashMap<>();
        }

        SkillAlarmHistory history = new SkillAlarmHistory();
        history.setAlarmBean(resource);
        history.setOperation(operation);
        mAlarmResourceMap.put(key, history);
    }

    private PendingIntent getPendingIntent(int key) {
        Intent intent = new Intent(mApplication, StartAlarmBroadcastReceiver.class);
        intent.setAction(String.valueOf(key));
        return PendingIntent.getBroadcast(mApplication, key, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 取消已设置闹钟
     *
     * @param key 闹钟key
     */
    private void cancelAlarmTime(int key) {
        if (mAlarmResourceMap == null) {
            QLog.d(TAG, "cancelAlarmTime() mAlarmResourceMap == null.");
            return;
        }

        PendingIntent currentAlarmIntent = getPendingIntent(key);
        if (currentAlarmIntent == null) {
            QLog.d(TAG, "cancelAlarmTime() currentAlarmIntent == null.");
            return;
        }

        AlarmManager alarmManager = obtainAlarmManager();
        QLog.d(TAG, String.format("cancelAlarmTime(%s)", key));
        if (alarmManager == null) {
            QLog.d(TAG, "alarmManager == null.");
        } else {
            alarmManager.cancel(currentAlarmIntent);
        }

        mAlarmResourceMap.remove(key);
    }

    /**
     * 获取闹钟实体的key，并转为整型
     *
     * @param bean 闹钟实体
     * @return 闹钟实体的Key(整型)
     */
    private int getKeyValue(SkillAlarmBean bean) {
        int key = 0;

        if (TextUtils.isEmpty(bean.getKey())) {
            QLog.d(TAG, "TextUtils.isEmpty(bean.getKey()).");
            return key;
        }

        try {
            key = Integer.parseInt(bean.getKey());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return key;
    }

    /**
     * 获取AlarmManager服务
     *
     * @return AlarmManager服务
     */
    private AlarmManager obtainAlarmManager() {
        if (mApplication == null) {
            QLog.d(TAG, "obtainAlarmManager() mApplication == null.");
            return null;
        }

        return (AlarmManager) mApplication.getSystemService(ALARM_SERVICE);
    }

    /**
     * 本地闹钟触发监听广播：
     * 1. 向后台请求触发闹钟；
     * 2. 如果是循环闹钟，更新下一次触发时间，并设置该闹钟
     */
    public static class StartAlarmBroadcastReceiver extends BroadcastReceiver {
        /**
         * 删除闹钟项
         */
        public static final int START_ALARM_DELETE = 1;
        /**
         * 更新闹钟项
         */
        public static final int START_ALARM_UPDATE_TIME = 2;

        public static OnObtainAlarmResource mOnObtainAlarmResource;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            QLog.d(TAG, String.format("闹钟时间到了.action:%s", action));

            int key = -1;
            try {
                key = Integer.valueOf(action);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (key == -1) {
                QLog.d(TAG, "onReceive() key == -1");
                return;
            }

            SkillAlarmHistory history = mOnObtainAlarmResource.onObtainAlarm(key);
            if (history == null) {
                QLog.d(TAG, "onReceive() history == null.");
                return;
            }

            int operation = history.getOperation();
            SkillAlarmBean bean = history.getAlarmBean();
            if (bean == null) {
                QLog.d(TAG, "onReceive() bean == null.");
                return;
            }

            QLog.d(TAG, String.format("闹钟数据:%s", history.toString()));

            if (operation == START_ALARM_DELETE) {
                AlarmDbManager.instance().deleteAlarmItem(bean, null);
            } else if (operation == START_ALARM_UPDATE_TIME) {
                updateNextTime(bean);
            }

            if (bean.isTimingPlaySkill()) {
                XWSDK.getInstance().getTimingSkillResource(bean.getKey(), new XWSDK.RequestListener() {
                    @Override
                    public boolean onRequest(int event, XWResponseInfo rspData, byte[] extendData) {
                        // 由控制层处理播放资源
                        XWeiControl.getInstance().processResponse(rspData.voiceID, rspData, extendData);
                        return true;
                    }
                });
            } else {
                // 闹钟/提醒, 设备端本地处理，如果正在视频通话中，将延迟处理
                if (AVChatManager.getInstance().mState > 0) {
                    SkillAlarmManager.instance().delayAlarm(bean);
                } else {
                    // 打开闹钟Activity
                    ArrayList<SkillAlarmBean> beans = new ArrayList<>();
                    beans.add(bean);
                    Intent intent1 = new Intent(context, AlarmActivity.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent1.putExtra("alarms", beans);
                    context.startActivity(intent1);
                }
            }

        }

        /**
         * 更新循环闹钟下一次触发时间
         *
         * @param bean 闹钟实体
         */
        private void updateNextTime(SkillAlarmBean bean) {
            if (bean == null) {
                QLog.d(TAG, "updateNextTime() bean == null.");
                return;
            }

            int repeatType = bean.getRepeatType();
            if (repeatType == SkillAlarmBean.CLOCK_REPEAT_TYPE.CLOCK_REPEAT_TYPE_DAY) {
                bean.setAlarmTime(SkillTimerUtils.nextTime(bean.getAlarmTime()));
                bean.setServerTime(SkillTimerUtils.nextTime(bean.getServerTime()));
            } else if (repeatType == SkillAlarmBean.CLOCK_REPEAT_TYPE.CLOCK_REPEAT_TYPE_WEEK) {
                bean.setAlarmTime(SkillTimerUtils.nextTime(bean.getAlarmTime(), bean.getRepeatInterval()));
                bean.setServerTime(SkillTimerUtils.nextTime(bean.getServerTime(), bean.getRepeatInterval()));
            }

            QLog.d(TAG, String.format("next alarm time:%s, next server time:%s",
                    SkillTimerUtils.getAlarmTime(bean.getAlarmTime()),
                    SkillTimerUtils.getAlarmTime(bean.getServerTime())));
            AlarmDbManager.instance().updateAlarmItem(bean, new OnOperationFinishListener() {
                @Override
                public void onOperationFinish(SkillAlarmBean bean, String action) {
                    if (action.equals(AlarmDbManager.ALARM_UPDATE_ACTION)) {
                        SkillAlarmManager.instance().startAlarm(bean);
                    }
                }
            });
        }
    }

    /**
     * 已经设置的闹钟定义
     */
    private static class SkillAlarmHistory {
        private SkillAlarmBean mSkillAlarmBean;
        private int operation;

        public SkillAlarmBean getAlarmBean() {
            return mSkillAlarmBean;
        }

        public void setAlarmBean(SkillAlarmBean SkillAlarmBean) {
            mSkillAlarmBean = SkillAlarmBean;
        }

        public int getOperation() {
            return operation;
        }

        public void setOperation(int operation) {
            this.operation = operation;
        }

        @Override
        public String toString() {
            return "SkillAlarmHistory{" + "mSkillAlarmBean=" + mSkillAlarmBean.toString() +
                    ", operation=" + operation +
                    '}';
        }
    }

    /**
     * 获取闹钟数据接口
     */
    private interface OnObtainAlarmResource {
        /**
         * 需要获取闹钟时触发回调
         *
         * @param key 闹钟的key值
         * @return 返回闹钟实体
         */
        SkillAlarmHistory onObtainAlarm(int key);
    }
}
