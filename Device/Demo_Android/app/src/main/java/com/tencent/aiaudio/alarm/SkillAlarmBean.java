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

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 闹钟信息类
 */
@Table(name = "order")
public class SkillAlarmBean implements Parcelable {
    /**
     * 提醒
     */
    public static int TYPE_ALARM_PROMPT = 0;
    /**
     * 闹钟
     */
    public static int TYPE_ALARM_CLOCK = 1;
    /**
     * 循环闹钟
     */
    public static int TYPE_ALARM_LOOP = 2;

    protected SkillAlarmBean(Parcel in) {
        mKey = in.readString();
        mType = in.readInt();
        mEvent = in.readString();
        mAlarmTime = in.readLong();
        mRepeatType = in.readInt();
        mRepeatInterval = in.readString();
        mServerType = in.readInt();
        mServerTimeDifference = in.readLong();
        mServerTime = in.readLong();
        mIsOpen = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mKey);
        dest.writeInt(mType);
        dest.writeString(mEvent);
        dest.writeLong(mAlarmTime);
        dest.writeInt(mRepeatType);
        dest.writeString(mRepeatInterval);
        dest.writeInt(mServerType);
        dest.writeLong(mServerTimeDifference);
        dest.writeLong(mServerTime);
        dest.writeByte((byte) (mIsOpen ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SkillAlarmBean> CREATOR = new Creator<SkillAlarmBean>() {
        @Override
        public SkillAlarmBean createFromParcel(Parcel in) {
            return new SkillAlarmBean(in);
        }

        @Override
        public SkillAlarmBean[] newArray(int size) {
            return new SkillAlarmBean[size];
        }
    };

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SkillAlarmBean{");
        sb.append("mKey='").append(mKey).append('\'');
        sb.append(", mType=").append(mType);
        sb.append(", mEvent='").append(mEvent).append('\'');
        sb.append(", mAlarmTime=").append(SkillTimerUtils.getAlarmTime(mAlarmTime));
        sb.append(", mRepeatType=").append(mRepeatType);
        sb.append(", mRepeatInterval='").append(mRepeatInterval).append('\'');
        sb.append(", mServerType=").append(mServerType);
        sb.append(", mServerTimeDifference=").append(mServerTimeDifference);
        sb.append(", mServerTime=").append(SkillTimerUtils.getAlarmTime(mServerTime));
        sb.append(", mIsOpen=").append(mIsOpen);
        sb.append('}');
        return sb.toString();
    }

    /**
     * 生成Json格式的闹钟项
     *
     * 请注意JSON串中的各个字段的key，trig_time字段值的单位为秒:
     *
     * {"clock_type":0, "event":"提醒我喝水","repeat_interval":"2","repeat_type":2,"service_data":"","service_type":0,"trig_time":"1512119648"}
     *
     * @return 闹钟项的JSON串
     */
    public String toJsonString() {
        JSONObject jsonObject = new JSONObject();

        try {
            if (!TextUtils.isEmpty(mKey)) {
                jsonObject.put("clock_id", mKey);
            }

            jsonObject.put("clock_type", mType);
            jsonObject.put("event", mEvent);
            jsonObject.put("repeat_interval", getRepeatInterval());
            jsonObject.put("repeat_type", getRepeatType());
            jsonObject.put("service_type", getServerType());
            jsonObject.put("trig_time", String.valueOf(getAlarmTime() / 1000));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }

    /**
     * 当前请求闹钟播放的key
     */
    @Column(name = "key", isId = true)
    private String mKey = "";
    /**
     * 闹钟类型
     */
    @Column(name = "type")
    private int mType = TYPE_ALARM_PROMPT;
    /**
     * 闹钟内容(离线时将内容播放出来)
     */
    @Column(name = "event")
    private String mEvent = "";
    /**
     * 启动闹钟时间
     */
    @Column(name = "alarmTime")
    private long mAlarmTime = 0L;
    /**
     * 循环是时间类型
     */
    @Column(name = "repeatType")
    private int mRepeatType = 1;
    /**
     * 循环是时间数据
     */
    @Column(name = "repeatInterval")
    private String mRepeatInterval = "";
    /**
     * 服务类型
     */
    @Column(name = "serverType")
    private int mServerType;
    /**
     * 与服务器相差时间
     */
    @Column(name = "serverTimeDifference")
    private long mServerTimeDifference;
    /**
     * 服务器时间
     */
    @Column(name = "serverTime")
    private long mServerTime = 0L;

    /**
     * 闹钟是否关闭
     */
    @Column(name = "open")
    private boolean mIsOpen = true;
    /**
     * 循环闹钟类型
     */
    public static class CLOCK_REPEAT_TYPE {
        /**
         * 按天循环,间隔为天数
         */
        public static int CLOCK_REPEAT_TYPE_DAY = 1;
        /**
         * 按周提醒,间隔为具体周几
         */
        public static int CLOCK_REPEAT_TYPE_WEEK = 2;
    }

    public long getServerTimeDifference() {
        return mServerTimeDifference;
    }

    public void setServerTimeDifference(long serverTimeDifference) {
        mServerTimeDifference = serverTimeDifference;
    }

    public int getServerType() {
        return mServerType;
    }

    /**
     * 是否为定时播放Skill
     *
     * @return true表示是定时播放Skill
     */
    public boolean isTimingPlaySkill() {
        return mServerType >= 1;
    }

    public void setServerType(int serverType) {
        mServerType = serverType;
    }

    public long getServerTime() {
        return mServerTime;
    }

    public void setServerTime(long serverTime) {
        mServerTime = serverTime;
    }

    public SkillAlarmBean() {

    }

    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        mKey = key;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public String getEvent() {
        return mEvent;
    }

    public void setEvent(String event) {
        mEvent = event;
    }

    public long getAlarmTime() {
        return mAlarmTime;
    }

    public void setAlarmTime(long alarmTime) {
        mAlarmTime = alarmTime;
    }

    public int getRepeatType() {
        return mRepeatType;
    }

    public void setRepeatType(int repeatType) {
        mRepeatType = repeatType;
    }

    public String getRepeatInterval() {
        return mRepeatInterval;
    }

    public void setRepeatInterval(String repeatInterval) {
        mRepeatInterval = repeatInterval;
    }

    public boolean ismIsOpen() {
        return mIsOpen;
    }

    public void setmIsOpen(boolean mIsOpen) {
        this.mIsOpen = mIsOpen;
    }

}
