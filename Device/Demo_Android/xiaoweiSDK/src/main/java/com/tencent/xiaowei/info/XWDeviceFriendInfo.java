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

import com.tencent.xiaowei.info.XWContactInfo;

/**
 * 设备好友
 */
public class XWDeviceFriendInfo extends XWContactInfo {

    public XWDeviceFriendInfo() {
        contactType = TYPE_FRIEND_DEVICE;
    }

    protected XWDeviceFriendInfo(Parcel in) {
        super(in);
    }

    public static final Creator<XWDeviceFriendInfo> CREATOR = new Creator<XWDeviceFriendInfo>() {
        @Override
        public XWDeviceFriendInfo createFromParcel(Parcel in) {
            return new XWDeviceFriendInfo(in);
        }

        @Override
        public XWDeviceFriendInfo[] newArray(int size) {
            return new XWDeviceFriendInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }
}
