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
 * 用来描述文件传输信息的对象
 */
public class XWFileTransferInfo implements Parcelable {

    /**
     * 图片文件
     */
    public static final int TYPE_TRANSFER_FILE_IMAGE = 1;
    /**
     * 视频文件
     */
    public static final int TYPE_TRANSFER_FILE_VIDEO = 2;
    /**
     * 语音文件
     */
    public static final int TYPE_TRANSFER_FILE_AUDIO = 3;
    /**
     * 其它文件
     */
    public static final int TYPE_TRANSFER_FILE_OTHER = 4;

    /**
     * FTN传输通道
     */
    public static final int TYPE_TRANSFER_CHANNEL_FTN = 1;
    /**
     * 小文件传输通道
     */
    public static final int TYPE_TRANSFER_CHANNEL_MINI = 2;

    /**
     * 文件Info的唯一标识
     */
    public long id;// cookie

    public long sender;

    /**
     * 文件本地路径
     */
    public String filePath;
    /**
     * 文件后台索引
     */
    public byte[] fileKey;
    /**
     * 传输类型：1 上传; 2 下载; 3 点对点发送; 4 点对点接收
     */
    public int transferType;
    /**
     * 额外参数或信息
     */
    public byte[] bufferExtra;
    /**
     * 业务名称：可以根据该字段的值，对接收到的文件做不同的处理
     */
    public String businessName;
    /**
     * 小文件的扩展key
     */
    public byte[] miniToken;
    /**
     * 文件大小
     */
    public long fileSize;
    /**
     * 传输通道类型
     */
    public int channelType;
    /**
     * 文件类型
     */
    public int fileType;

    public XWFileTransferInfo() {

    }


    protected XWFileTransferInfo(Parcel in) {
        id = in.readLong();
        sender = in.readLong();
        filePath = in.readString();
        fileKey = in.createByteArray();
        transferType = in.readInt();
        bufferExtra = in.createByteArray();
        businessName = in.readString();
        miniToken = in.createByteArray();
        fileSize = in.readLong();
        channelType = in.readInt();
        fileType = in.readInt();
    }

    public static final Creator<XWFileTransferInfo> CREATOR = new Creator<XWFileTransferInfo>() {
        @Override
        public XWFileTransferInfo createFromParcel(Parcel in) {
            return new XWFileTransferInfo(in);
        }

        @Override
        public XWFileTransferInfo[] newArray(int size) {
            return new XWFileTransferInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(sender);
        dest.writeString(filePath);
        dest.writeByteArray(fileKey);
        dest.writeInt(transferType);
        dest.writeByteArray(bufferExtra);
        dest.writeString(businessName);
        dest.writeByteArray(miniToken);
        dest.writeLong(fileSize);
        dest.writeInt(channelType);
        dest.writeInt(fileType);
    }


    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
