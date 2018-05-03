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
 * C2C消息结构定义
 */
public class XWCCMsgInfo implements Parcelable {

    /**
     * 业务名称
     */
    public String businessName;

    /**
     * 消息体，以字节数组的形式存在
     */
    public byte[] msgBuf;

    public XWCCMsgInfo() {}

    protected XWCCMsgInfo(Parcel in) {
        businessName = in.readString();
        msgBuf = in.createByteArray();
    }

    public static final Creator<XWCCMsgInfo> CREATOR = new Creator<XWCCMsgInfo>() {
        @Override
        public XWCCMsgInfo createFromParcel(Parcel in) {
            return new XWCCMsgInfo(in);
        }

        @Override
        public XWCCMsgInfo[] newArray(int size) {
            return new XWCCMsgInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(businessName);
        dest.writeByteArray(msgBuf);
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
