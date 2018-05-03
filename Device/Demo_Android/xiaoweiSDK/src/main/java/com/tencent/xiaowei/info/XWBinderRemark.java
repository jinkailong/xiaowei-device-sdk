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


public class XWBinderRemark implements Parcelable {
    public String remark;
    public long tinyid;

    public XWBinderRemark() {

    }

    protected XWBinderRemark(Parcel in) {
        remark = in.readString();
        tinyid = in.readLong();
    }

    public static final Creator<XWBinderRemark> CREATOR = new Creator<XWBinderRemark>() {
        @Override
        public XWBinderRemark createFromParcel(Parcel in) {
            return new XWBinderRemark(in);
        }

        @Override
        public XWBinderRemark[] newArray(int size) {
            return new XWBinderRemark[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(remark);
        dest.writeLong(tinyid);
    }

}
