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
 * 场景(App)信息
 */
public class XWAppInfo implements Parcelable {

    /**
     * App名称，用于表示场景名称
     */
    public String name;

    /**
     * App ID，当前App(Skill)的唯一ID
     */
    public String ID;

    /**
     * App 类型，App(Skill)类型，用于进行场景分类，决定了部分扩展字段的解析方法
     */
    public int type;

    public XWAppInfo() {
    }

    protected XWAppInfo(Parcel in) {
        name = in.readString();
        ID = in.readString();
        type = in.readInt();
    }

    public static final Parcelable.Creator<XWAppInfo> CREATOR = new Parcelable.Creator<XWAppInfo>() {
        @Override
        public XWAppInfo createFromParcel(Parcel in) {
            return new XWAppInfo(in);
        }

        @Override
        public XWAppInfo[] newArray(int size) {
            return new XWAppInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(ID);
        dest.writeInt(type);
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}