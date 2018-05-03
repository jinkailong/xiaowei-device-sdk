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

import java.util.ArrayList;

public class XWeiMessageInfo implements Parcelable {

    /**
     * 文本消息
     */
    public static final int TYPE_TEXT = 0;

    /**
     * 图片消息
     */
    public static final int TYPE_IMAGE = 1;

    /**
     * 视频消息
     */
    public static final int TYPE_VIDEO = 2;

    /**
     * 语音消息
     */
    public static final int TYPE_AUDIO = 3;


    /**
     * 消息的类型
     */
    public int type = TYPE_TEXT;

    /**
     * 消息的真实内容
     * type为TYPE_AUDIO、TYPE_VIDEO、TYPE_PICTURE时，原文件的路径
     */
    public String content;

    /**
     * 消息的发送者uin或者din
     */
    public String sender;

    /**
     * 消息的接收者uin（qq）或者din（device）,只有din可以有多个
     */
    public ArrayList<String> receiver;

    /**
     * 语音类消息的时长
     */
    public int duration;

    public XWeiMessageInfo(){

    }

    protected XWeiMessageInfo(Parcel in) {
        type = in.readInt();
        content = in.readString();
        sender = in.readString();
        receiver = in.createStringArrayList();
        duration = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(content);
        dest.writeString(sender);
        dest.writeStringList(receiver);
        dest.writeInt(duration);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<XWeiMessageInfo> CREATOR = new Creator<XWeiMessageInfo>() {
        @Override
        public XWeiMessageInfo createFromParcel(Parcel in) {
            return new XWeiMessageInfo(in);
        }

        @Override
        public XWeiMessageInfo[] newArray(int size) {
            return new XWeiMessageInfo[size];
        }
    };
}
