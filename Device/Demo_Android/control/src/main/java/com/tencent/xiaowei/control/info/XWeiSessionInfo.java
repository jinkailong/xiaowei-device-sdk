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
 * 场景Session信息
 */
public class XWeiSessionInfo implements Parcelable {
    public String skillName;
    public String skillId;

    public XWeiSessionInfo() {
    }

    protected XWeiSessionInfo(Parcel in) {
        skillName = in.readString();
        skillId = in.readString();
    }

    public static final Creator<XWeiSessionInfo> CREATOR = new Creator<XWeiSessionInfo>() {
        @Override
        public XWeiSessionInfo createFromParcel(Parcel in) {
            return new XWeiSessionInfo(in);
        }

        @Override
        public XWeiSessionInfo[] newArray(int size) {
            return new XWeiSessionInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(skillName);
        dest.writeString(skillId);
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
