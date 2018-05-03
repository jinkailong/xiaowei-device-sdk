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
package com.tencent.xiaowei.info;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 上报信息结构定义
 */
public class XWEventLogInfo implements Parcelable {

    public static final String EVENT_PLAYER_START = "client_OnStart";
    public static final String EVENT_PLAYER_PREPARE = "client_OnPrepareAsync";
    public static final String EVENT_TTS_BEGIN = "client_OnTTSBegin";
    public static final String EVENT_TTS_PREPARED = "client_OnTTSCallback";
    public static final String EVENT_TTS_END = "client_OnTTSEnd";

    public static final String EVENT_QQCALL_INVITE = "status_qqcall_invite";// 被叫
    public static final String EVENT_QQCALL_CALL_OUT = "status_qqcall_call_out";// 主叫
    public static final String EVENT_QQCALL_ING = "status_qqcall_ing";// 通话中
    public static final String EVENT_QQCALL_OUT = "status_qqcall_out";// 退出通话

    public static final int DEF_LOGTYPE_ULS = 1;  //uls log
    public static final int DEF_LOGTYPE_ALR = 2;  //全链路 log

    public int type = DEF_LOGTYPE_ULS | DEF_LOGTYPE_ALR;  //DEF_LOGTYPE_ULS or DEF_LOGTYPE_ALR

    //全链路 log only
    public String event;
    public int retCode;
    public long time;   //ms timestamp
    public String skillName;
    public String skillID;
    //通用字段
    public String sessionID;
    public String logData;
    //uls log only
    public int ulsSubCmd;

    public XWEventLogInfo() {
    }

    protected XWEventLogInfo(Parcel in) {
        type = in.readInt();
        event = in.readString();
        retCode = in.readInt();
        time = in.readLong();
        skillName = in.readString();
        skillID = in.readString();
        sessionID = in.readString();
        logData = in.readString();
        ulsSubCmd = in.readInt();
    }

    public static final Parcelable.Creator<XWEventLogInfo> CREATOR = new Parcelable.Creator<XWEventLogInfo>() {
        @Override
        public XWEventLogInfo createFromParcel(Parcel in) {
            return new XWEventLogInfo(in);
        }

        @Override
        public XWEventLogInfo[] newArray(int size) {
            return new XWEventLogInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(event);
        dest.writeInt(retCode);
        dest.writeLong(time);
        dest.writeString(skillName);
        dest.writeString(skillID);
        dest.writeString(sessionID);
        dest.writeString(logData);
        dest.writeInt(ulsSubCmd);
    }

    @Override
    public String toString() {
        return "XWAIAudioRes{" +
                "type=" + type +
                ", event='" + event + '\'' +
                ", retCode=" + retCode +
                ", time=" + time +
                ", skillName='" + skillName + '\'' +
                ", skillID='" + skillID + '\'' +
                ", sessionID='" + sessionID + '\'' +
                ", logData='" + logData + '\'' +
                ", ulsSubCmd=" + ulsSubCmd +
                '}';
    }
}