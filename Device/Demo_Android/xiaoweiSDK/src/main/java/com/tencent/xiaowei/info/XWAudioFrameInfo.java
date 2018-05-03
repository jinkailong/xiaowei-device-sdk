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
 * Created by lingyuhuang on 2016/11/22.
 */
public class XWAudioFrameInfo implements Parcelable {

    public byte[] data;
    public int length;
    public long time;

    public XWAudioFrameInfo() {

    }

    protected XWAudioFrameInfo(Parcel in) {
        data = in.createByteArray();
        length = in.readInt();
        time = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(data);
        dest.writeInt(length);
        dest.writeLong(time);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<XWAudioFrameInfo> CREATOR = new Creator<XWAudioFrameInfo>() {
        @Override
        public XWAudioFrameInfo createFromParcel(Parcel in) {
            return new XWAudioFrameInfo(in);
        }

        @Override
        public XWAudioFrameInfo[] newArray(int size) {
            return new XWAudioFrameInfo[size];
        }
    };
}
