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
 * 位置信息
 */
public class XWLocationInfo implements Parcelable {

    public double           longitude;          // 经度
    public double           latitude;           // 纬度
    public String           description;        // 位置描述

    public XWLocationInfo() {}

    protected XWLocationInfo(Parcel in) {
        longitude = in.readDouble();
        latitude = in.readDouble();
        description = in.readString();
    }

    public static final Parcelable.Creator<XWLocationInfo> CREATOR = new Parcelable.Creator<XWLocationInfo>() {
        @Override
        public XWLocationInfo createFromParcel(Parcel in) {
            return new XWLocationInfo(in);
        }

        @Override
        public XWLocationInfo[] newArray(int size) {
            return new XWLocationInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeString(description);
    }

    @Override
    public String toString() {
        return "XWAIAudioLocation{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                ", description='" + description + '\'' +
                '}';
    }
}