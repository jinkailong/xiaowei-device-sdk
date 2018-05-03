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

import com.tencent.xiaowei.util.JsonUtil;


/**
 * 播放器相关信息
 */
public class XWeiPlayerInfo implements Parcelable {
    public int status;     //  com.tencent.aiaudio.Constants.XWeiInnerPlayerStatus
    public int repeatMode; //  com.tencent.aiaudio.Constants.XWeiRepeatMode

    public int playlistId;    // current playlist

    public int volume;
    public int quality;

    public XWeiPlayerInfo() {
    }

    protected XWeiPlayerInfo(Parcel in) {
        status = in.readInt();
        repeatMode = in.readInt();

        playlistId = in.readInt();

        volume = in.readInt();
        quality = in.readInt();
    }

    public static final Creator<XWeiPlayerInfo> CREATOR = new Creator<XWeiPlayerInfo>() {
        @Override
        public XWeiPlayerInfo createFromParcel(Parcel in) {
            return new XWeiPlayerInfo(in);
        }

        @Override
        public XWeiPlayerInfo[] newArray(int size) {
            return new XWeiPlayerInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(status);
        dest.writeInt(repeatMode);

        dest.writeInt(playlistId);

        dest.writeInt(volume);
        dest.writeInt(quality);
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
