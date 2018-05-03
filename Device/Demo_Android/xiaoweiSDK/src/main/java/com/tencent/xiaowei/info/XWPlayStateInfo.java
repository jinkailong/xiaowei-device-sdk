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
 * 状态信息
 */
public class XWPlayStateInfo implements Parcelable {

    /**
     * 场景信息
     */
    public XWAppInfo appInfo;

    /**
     * 播放状态
     */
    public int state;

    /**
     * playID，如果不是在播放url资源，则无需上报
     */
    public String playID;

    /**
     * 播放内容
     */
    public String playContent;

    /**
     * 播放偏移量
     */
    public long playOffset;

    /**
     * 播放模式
     */
    public int playMode;


    public XWPlayStateInfo() {
    }

    protected XWPlayStateInfo(Parcel in) {
        appInfo = in.readParcelable(XWAppInfo.class.getClassLoader());
        state = in.readInt();
        playID = in.readString();
        playContent = in.readString();
        playOffset = in.readLong();
        playMode = in.readInt();
    }

    public static final Creator<XWPlayStateInfo> CREATOR = new Creator<XWPlayStateInfo>() {
        @Override
        public XWPlayStateInfo createFromParcel(Parcel in) {
            return new XWPlayStateInfo(in);
        }

        @Override
        public XWPlayStateInfo[] newArray(int size) {
            return new XWPlayStateInfo[size];
        }
    };

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(appInfo, flags);
        dest.writeInt(state);
        dest.writeString(playID);
        dest.writeString(playContent);
        dest.writeLong(playOffset);
        dest.writeInt(playMode);
    }
}