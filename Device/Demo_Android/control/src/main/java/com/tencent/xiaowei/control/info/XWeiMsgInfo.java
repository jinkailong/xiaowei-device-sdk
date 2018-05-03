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
package com.tencent.xiaowei.control.info;

import android.os.Parcel;
import android.os.Parcelable;

public class XWeiMsgInfo implements Parcelable {

    /**
     * 消息发送者的id
     */
    public long tinyId;

    /**
     * 消息类型
     */
    public int type;

    /**
     * 消息的内容
     */
    public String content;

    /**
     * 音视频消息的时长
     */
    public int duration;

    /**
     * 消息的时间戳
     */
    public int timestamp;

    /**
     * 是接收的消息还是发送的消息
     * true: 接收
     * false: 发送
     */
    public boolean isRecv;

    public XWeiMsgInfo() {
        tinyId = 0;
        type = 0;
        duration = 0;
        timestamp = 0;
        isRecv = true;
    }

    protected XWeiMsgInfo(Parcel in) {
        tinyId = in.readLong();
        type = in.readInt();
        content = in.readString();
        duration = in.readInt();
        timestamp = in.readInt();
        isRecv = in.readByte() != 0;
    }

    public static final Creator<XWeiMsgInfo> CREATOR = new Creator<XWeiMsgInfo>() {
        @Override
        public XWeiMsgInfo createFromParcel(Parcel in) {
            return new XWeiMsgInfo(in);
        }

        @Override
        public XWeiMsgInfo[] newArray(int size) {
            return new XWeiMsgInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(tinyId);
        dest.writeInt(type);
        dest.writeString(content);
        dest.writeInt(duration);
        dest.writeInt(timestamp);
        dest.writeByte((byte)(isRecv ? 1 : 0));
    }
}
