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

import java.util.Arrays;


/**
 * 响应信息
 */
public class XWResponseInfo implements Parcelable {

    public static final int EventIdle = 0;    //恢复空闲
    public static final int EventRequestStart = 1;    //请求开始
    public static final int EventOnSpeak = 2;    //检测到用户说话
    public static final int EventOnSilent = 3;    //检测到用户静音
    public static final int EventRecognize = 4;    //收到实时语音识别结果
    public static final int EventResponse = 5;    //收到响应

    public static final int WAKEUP_CHECK_RET_NOT = 0;// 不是云端校验唤醒的结果
    public static final int WAKEUP_CHECK_RET_FAIL = 1;// 唤醒校验失败
    public static final int WAKEUP_CHECK_RET_SUC = 2;// 成功唤醒，只说了唤醒词没有连续说话
    public static final int WAKEUP_CHECK_RET_SUC_RSP = 3;// 成功唤醒并且收到了最终响应
    public static final int WAKEUP_CHECK_RET_SUC_CONTINUE = 4;// 成功唤醒并且还需要继续传声音，还不知道会不会连续说话

    /**
     * 场景信息
     */
    public XWAppInfo appInfo;

    /**
     * 上一次的场景信息
     */
    public XWAppInfo lastAppInfo;

    /**
     * 结果 {@link com.tencent.xiaowei.def.XWCommonDef.XWeiErrorCode}
     */
    public int resultCode;

    /**
     * voice ID
     */
    public String voiceID;

    /**
     * 上下文信息
     */
    public XWContextInfo context;

    /**
     * 请求文本
     */
    public String requestText;

    /**
     * 响应扩展数据类型
     */
    public int responseType;

    /**
     * 响应扩展数据，json格式
     */
    public String responseData;

    /**
     * 资源集合list
     */
    public XWResGroupInfo[] resources;

    /**
     * 是否有更多资源
     */
    public boolean hasMorePlaylist;

    /**
     * 资源是否可以暂停恢复
     */
    public boolean recoveryAble;

    /**
     * 资源列表拼接类型{@link com.tencent.xiaowei.def.XWCommonDef.PlayBehavior}
     */
    public int playBehavior;

    /**
     * 这个响应的资源是通知或者提示，不应该影响当前该场景的列表变化，只是插播一下。例如音乐场景中询问"现在在放什么歌"，"周杰伦 稻香"这个TTS就是这种资源。
     */
    public boolean isNotify;

    /**
     * 云端唤醒校验结果，0表示非该类结果，1表示唤醒校验失败，2表示唤醒成功并且没连续说话，3表示说了指令唤醒词，4可能为中间结果，表示唤醒成功了，还在继续检测连续说话或者已经在连续说话了
     */
    public int wakeupFlag;

    /**
     * 自动化测试扩展数据，无需关注，一般为空值
     */
    @Deprecated
    public String autoTestData;

    public XWResponseInfo() {
    }


    protected XWResponseInfo(Parcel in) {
        appInfo = in.readParcelable(XWAppInfo.class.getClassLoader());
        lastAppInfo = in.readParcelable(XWAppInfo.class.getClassLoader());
        resultCode = in.readInt();
        voiceID = in.readString();
        context = in.readParcelable(XWContextInfo.class.getClassLoader());
        requestText = in.readString();
        responseType = in.readInt();
        responseData = in.readString();
        resources = in.createTypedArray(XWResGroupInfo.CREATOR);
        hasMorePlaylist = in.readByte() != 0;
        recoveryAble = in.readByte() != 0;
        playBehavior = in.readInt();
        isNotify = in.readByte() != 0;
        wakeupFlag = in.readInt();
        autoTestData = in.readString();
    }

    public static final Creator<XWResponseInfo> CREATOR = new Creator<XWResponseInfo>() {
        @Override
        public XWResponseInfo createFromParcel(Parcel in) {
            return new XWResponseInfo(in);
        }

        @Override
        public XWResponseInfo[] newArray(int size) {
            return new XWResponseInfo[size];
        }
    };

    @Override
    public String toString() {
        return "XWResponseInfo{" +
                "appInfo=" + appInfo +
                ", lastAppInfo=" + lastAppInfo +
                ", resultCode=" + resultCode +
                ", voiceID='" + voiceID + '\'' +
                ", context=" + context +
                ", autoTestData=" + autoTestData +
                ", requestText='" + requestText + '\'' +
                ", responseType=" + responseType +
                ", responseData='" + responseData + '\'' +
                ", resources=" + Arrays.toString(resources) +
                ", hasMorePlaylist=" + hasMorePlaylist +
                ", recoveryAble=" + recoveryAble +
                ", playBehavior=" + playBehavior +
                ", isNotify=" + isNotify +
                ", wakeupFlag=" + wakeupFlag +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(appInfo, flags);
        dest.writeParcelable(lastAppInfo, flags);
        dest.writeInt(resultCode);
        dest.writeString(voiceID);
        dest.writeParcelable(context, flags);
        dest.writeString(requestText);
        dest.writeInt(responseType);
        dest.writeString(responseData);
        dest.writeTypedArray(resources, flags);
        dest.writeByte((byte) (hasMorePlaylist ? 1 : 0));
        dest.writeByte((byte) (recoveryAble ? 1 : 0));
        dest.writeInt(playBehavior);
        dest.writeByte((byte) (isNotify ? 1 : 0));
        dest.writeInt(wakeupFlag);
        dest.writeString(autoTestData);
    }
}