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

import com.tencent.xiaowei.def.XWCommonDef;
import com.tencent.xiaowei.util.JsonUtil;


/**
 * 资源信息
 */
public class XWResourceInfo implements Parcelable {

    public static final int FormatTTS               = 0;    //TTS，ID=resID
    public static final int FormatUrl               = 1;    //url，ID=playID；content=url
    public static final int FormatNotify            = 2;    //通知类
    public static final int FormatText              = 3;    //文本，content=文本字串
    public static final int FormatCommand           = 4;    //指令，ID=指令ID；content=指令内容
    public static final int FormatIntent            = 5;    //语义，content=语义json

    /**
     * 资源类型{@link XWCommonDef.ResourceFormat}
     */
    public int         format;

    /**
     * 资源ID
     */
    public String      ID;

    /**
     * 资源内容
     */
    public String      content;

    /**
     * 扩展信息，json格式
     */
    public String      extendInfo;

    /**
     * 播放偏移量
     */
    public int offset;

    /**
     * 播放次数，默认为-1，表示无限制次数
     */
    public int playCount;

    public XWResourceInfo() {}

    protected XWResourceInfo(Parcel in) {
        format = in.readInt();
        ID = in.readString();
        content = in.readString();
        extendInfo = in.readString();
        offset = in.readInt();
        playCount = in.readInt();
    }

    public static final Parcelable.Creator<XWResourceInfo> CREATOR = new Parcelable.Creator<XWResourceInfo>() {
        @Override
        public XWResourceInfo createFromParcel(Parcel in) {
            return new XWResourceInfo(in);
        }

        @Override
        public XWResourceInfo[] newArray(int size) {
            return new XWResourceInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(format);
        dest.writeString(ID);
        dest.writeString(content);
        dest.writeString(extendInfo);
        dest.writeInt(offset);
        dest.writeInt(playCount);
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}