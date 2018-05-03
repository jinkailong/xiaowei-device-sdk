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
 * 绑定者和分享者
 */
public class XWBinderInfo extends XWContactInfo {

    /**
     * 未知的帐号类似
     */
    public static int BINDER_TINYID_TYPE_UNKNOWN = 0;

    /**
     * QQ绑定
     */
    public static int BINDER_TINYID_TYPE_QQ_NOM = 1;

    /**
     * QQ绑定，扩展方式，未使用
     */
    public static int BINDER_TINYID_TYPE_QQ = 2;

    /**
     * 微信绑定
     */
    public static int BINDER_TINYID_TYPE_WX = 3;

    public XWBinderInfo() {
        contactType = TYPE_BINDER;
    }

    protected XWBinderInfo(Parcel in) {
        super(in);
    }

    public static final Creator<XWBinderInfo> CREATOR = new Creator<XWBinderInfo>() {
        @Override
        public XWBinderInfo createFromParcel(Parcel in) {
            return new XWBinderInfo(in);
        }

        @Override
        public XWBinderInfo[] newArray(int size) {
            return new XWBinderInfo[size];
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

    @Override
    public Object clone() throws CloneNotSupportedException {
        XWBinderInfo info = new XWBinderInfo();
        info.tinyID = tinyID;
        info.headUrl = headUrl;
        info.type = type;
        info.remark = remark;
        info.contactType = contactType;
        info.online = online;
        return info;
    }
}
