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

import com.tencent.xiaowei.control.XWMediaType;
import com.tencent.xiaowei.util.JsonUtil;

/**
 * 媒体播放资源信息
 */
public class XWeiMediaInfo implements Parcelable {
    /**
     * 资源ID
     */
    public String resId;
    /**
     * 资源内容
     */
    public String content;
    /**
     * 类型 具体参考{@link XWMediaType}中的说明
     */
    public int mediaType;
    /**
     * 偏移量
     */
    public int offset;
    /**
     * 描述信息
     */
    public String description;

    public XWeiMediaInfo() {
    }

    protected XWeiMediaInfo(Parcel in) {
        resId = in.readString();
        content = in.readString();
        mediaType = in.readInt();
        offset = in.readInt();
        description = in.readString();
    }

    public static final Creator<XWeiMediaInfo> CREATOR = new Creator<XWeiMediaInfo>() {
        @Override
        public XWeiMediaInfo createFromParcel(Parcel in) {
            return new XWeiMediaInfo(in);
        }

        @Override
        public XWeiMediaInfo[] newArray(int size) {
            return new XWeiMediaInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(resId);
        dest.writeString(content);
        dest.writeInt(mediaType);
        dest.writeInt(offset);
        dest.writeString(description);
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
