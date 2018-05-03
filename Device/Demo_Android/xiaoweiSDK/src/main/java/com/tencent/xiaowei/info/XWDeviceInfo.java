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
 * 设备信息
 */
public class XWDeviceInfo implements Parcelable{



    /**
     * 设备支持能力(位域){@link com.tencent.xiaowei.def.XWCommonDef.DEVICE_INFO_PROP}
     */
    public long        properties;

    /**
     * 扩展参数
     */
    public byte[]      extendBuffer;

    public XWDeviceInfo() {}

    protected XWDeviceInfo(Parcel in) {
        properties = in.readLong();
        extendBuffer = in.createByteArray();
    }

    public static final Parcelable.Creator<XWDeviceInfo> CREATOR = new Parcelable.Creator<XWDeviceInfo>() {
        @Override
        public XWDeviceInfo createFromParcel(Parcel in) {
            return new XWDeviceInfo(in);
        }

        @Override
        public XWDeviceInfo[] newArray(int size) {
            return new XWDeviceInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(properties);
        dest.writeByteArray(extendBuffer);
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}