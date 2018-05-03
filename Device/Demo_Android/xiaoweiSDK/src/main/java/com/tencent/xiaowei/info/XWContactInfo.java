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
 * Created by xw on 2016/11/22.
 */
public class XWContactInfo implements Parcelable {

    public static int BINDER_TYPE_OWNER = 1;
    public static int BINDER_TYPE_SHARER = 2;

    public static final int TYPE_BINDER = 1;
    public static final int TYPE_FRIEND_DEVICE = 2;
    public static final int TYPE_FRIEND_QQ = 3;
    public static final int TYPE_OTHERS = 4;
    public static final int TYPE_AI_AUDIO = 5; // QQ代收好友

    /**
     * id
     */
    public long tinyID;
    /**
     * 头像
     */
    public String headUrl;

    /**
     * 定者类型，一般是{@link #BINDER_TYPE_OWNER}或者{@link #BINDER_TYPE_SHARER}
     */
    public int type;
    /**
     * 昵称或备注
     */
    public String remark;

    /**
     * 联系人类型，一般是{@link #TYPE_BINDER}
     */
    public int contactType;

    /**
     * 在线状态：0是离线，非0为在线
     */
    public int online;

    public XWContactInfo() {

    }

    public XWContactInfo(long tinyID) {
        this.tinyID = tinyID;
    }

    protected XWContactInfo(Parcel in) {
        tinyID = in.readLong();
        headUrl = in.readString();
        type = in.readInt();
        remark = in.readString();
        contactType = in.readInt();
    }

    public static final Creator<XWContactInfo> CREATOR = new Creator<XWContactInfo>() {
        @Override
        public XWContactInfo createFromParcel(Parcel in) {
            return new XWContactInfo(in);
        }

        @Override
        public XWContactInfo[] newArray(int size) {
            return new XWContactInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(tinyID);
        dest.writeString(headUrl);
        dest.writeInt(type);
        dest.writeString(remark);
        dest.writeInt(contactType);
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        XWContactInfo info = new XWContactInfo();
        info.tinyID = tinyID;
        info.headUrl = headUrl;
        info.type = type;
        info.remark = remark;
        info.contactType = contactType;
        info.online = online;
        return info;
    }
}
