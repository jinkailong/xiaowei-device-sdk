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

import com.tencent.xiaowei.util.JsonUtil;


/**
 * 上下文信息
 */
public class XWContextInfo implements Parcelable {

    /**
     * 上下文ID
     */
    public String ID;

    /**
     * 等待用户说话的超时时间(单位:ms)
     */
    public int speakTimeout;

    /**
     * 用户说话的断句时间(单位:ms)
     */
    public int silentTimeout;

    /**
     * 声音请求的首包标志，首包时必须为true
     */
    public boolean voiceRequestBegin;

    /**
     * 当使用外部VAD时，声音尾包置成true
     */
    public boolean voiceRequestEnd;

    /**
     * 识别引擎是用近场还是远场 {@link #PROFILE_TYPE_NEAR} or {@link #PROFILE_TYPE_FAR}
     */
    public int profileType;
    /**
     * 远场
     */
    public final static int PROFILE_TYPE_FAR = 0;
    /**
     * 近场
     */
    public final static int PROFILE_TYPE_NEAR = 1;

    /**
     * 云端校验类请求生效{@link com.tencent.xiaowei.def.XWCommonDef.RequestType#WAKEUP_CHECK}
     */
    public int voiceWakeupType;

    /**
     * 默认，纯本地唤醒或者按键唤醒
     */
    public final static int WAKEUP_TYPE_DEFAULT = 0;
    /**
     * 需要云端校验唤醒词的请求
     */
    public final static int WAKEUP_TYPE_CLOUD_CHECK = 1;

    /**
     * 纯本地唤醒但是需要云端过滤掉唤醒词{@link #voiceWakeupText}
     */
    public final static int WAKEUP_TYPE_LOCAL_WITH_TEXT = 2;
    /**
     * 纯本地唤醒但是需要云端过滤掉的唤醒词，语音识别类请求生效{@link com.tencent.xiaowei.def.XWCommonDef.RequestType#VOICE}
     */
    public String voiceWakeupText;

    public static final int REQUEST_PARAM_USE_LOCAL_VAD = 0x1;    //使用本地VAD
    public static final int REQUEST_PARAM_GPS = 0x2;    //使用GPS位置
    public static final int REQUEST_PARAM_USE_LOCAL_TTS = 0x4;    //使用本地TTS
    public static final int REQUEST_PARAM_DUMP_SILK = 0x8;    //保存发出去的silk文件
    public static final int REQUEST_PARAM_ONLY_VAD = 0x10;    //只做后台VAD

    /**
     * 请求的一些参数
     */
    public long requestParam;

    public XWContextInfo() {
        ID = null;
        speakTimeout = 5000;
        silentTimeout = 500;
        voiceRequestBegin = false;
        voiceRequestEnd = false;
    }


    protected XWContextInfo(Parcel in) {
        ID = in.readString();
        speakTimeout = in.readInt();
        silentTimeout = in.readInt();
        voiceRequestBegin = in.readByte() != 0;
        voiceRequestEnd = in.readByte() != 0;
        profileType = in.readInt();
        voiceWakeupType = in.readInt();
        voiceWakeupText = in.readString();
        requestParam = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ID);
        dest.writeInt(speakTimeout);
        dest.writeInt(silentTimeout);
        dest.writeByte((byte) (voiceRequestBegin ? 1 : 0));
        dest.writeByte((byte) (voiceRequestEnd ? 1 : 0));
        dest.writeInt(profileType);
        dest.writeInt(voiceWakeupType);
        dest.writeString(voiceWakeupText);
        dest.writeLong(requestParam);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<XWContextInfo> CREATOR = new Creator<XWContextInfo>() {
        @Override
        public XWContextInfo createFromParcel(Parcel in) {
            return new XWContextInfo(in);
        }

        @Override
        public XWContextInfo[] newArray(int size) {
            return new XWContextInfo[size];
        }
    };

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}