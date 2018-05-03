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

public class XWLoginInfo implements Parcelable {

    /**
     * 设备的网络环境为WIFI以及除了移动、联通、电信、香港之外的网络
     */
    public static final int TYPE_NETWORK_WIFI = 1;

    /**
     * 设备的网络环境为中国移动
     */
    public static final int TYPE_NETWORK_MOBILE = 2;
    /**
     * 设备的网络环境为中国联通
     */
    public static final int TYPE_NETWORK_UNICOM = 3;
    /**
     * 设备的网络环境为中国电信
     */
    public static final int TYPE_NETWORK_TELECOM = 4;
    /**
     * 设备的网络环境为中国香港
     */
    public static final int TYPE_NETWORK_HONGKONG = 5;

    /**
     * SDK正常运行模式
     */
    public static final int SDK_RUN_MODE_DEFAULT = 0;
    /**
     * SDK低功耗运行模式
     */
    public static final int SDK_RUN_MODE_LOW_POWER = 1;

    /**
     * 设备名称
     */
    public String deviceName;
    /**
     * 设备授权码
     */
    public String license;
    /**
     * 设备序列号
     */
    public String serialNumber;
    /**
     * IOT后台分配的公钥
     */
    public String srvPubKey;
    /**
     * 产品ID
     */
    public long productId;
    /**
     * 产品版本
     */
    public int productVersion;
    /**
     * 网络类型 ，取值必须是NETWORK_TYPE_WIFI、NETWORK_TYPE_MOBILE、NETWORK_TYPE_UNICOM、NETWORK_TYPE_TELECOM、NETWORK_TYPE_HONGKONG
     */
    public int networkType;
    /**
     * 运行模式 ，取值必须是SDK_RUN_MODE_DEFAULT、SDK_RUN_MODE_LOW_POWER
     */
    public int runMode;
    /**
     * 系统路径 ，SDK会在该目录下写入保证正常运行必需的配置信息
     */
    public String sysPath;
    /**
     * 系统路径下存储空间大小 ，SDK对该目录的存储空间要求小（最小大小：10K，建议大小：100K），SDK写入次数较少，读取次数较多
     */
    public long sysCapacity;
    /**
     * 应用路径 ，SDK会在该目录下写入运行过程中的异常错误信息
     */
    public String appPath;
    /**
     * 应用路径下存储空间大小 ，SDK对该目录的存储空间要求较大（最小大小：500K，建议大小：1M），SDK写入次数较多，读取次数较少
     */
    public long appCapacity;
    /**
     * 临时路径 ，SDK会在该目录下写入接收到的文件
     */
    public String tmpPath;
    /**
     * 临时路径下存储空间大小 ，SDK对该目录的存储空间要求很大，建议尽可能大一些
     */
    public long tmpCapacity;

    public XWLoginInfo() {

    }

    protected XWLoginInfo(Parcel in) {
        deviceName = in.readString();
        license = in.readString();
        serialNumber = in.readString();
        srvPubKey = in.readString();
        productId = in.readLong();
        productVersion = in.readInt();
        networkType = in.readInt();
        runMode = in.readInt();
        sysPath = in.readString();
        sysCapacity = in.readLong();
        appPath = in.readString();
        appCapacity = in.readLong();
        tmpPath = in.readString();
        tmpCapacity = in.readLong();
    }

    public static final Creator<XWLoginInfo> CREATOR = new Creator<XWLoginInfo>() {
        @Override
        public XWLoginInfo createFromParcel(Parcel in) {
            return new XWLoginInfo(in);
        }

        @Override
        public XWLoginInfo[] newArray(int size) {
            return new XWLoginInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deviceName);
        dest.writeString(license);
        dest.writeString(serialNumber);
        dest.writeString(srvPubKey);
        dest.writeLong(productId);
        dest.writeInt(productVersion);
        dest.writeInt(networkType);
        dest.writeInt(runMode);
        dest.writeString(sysPath);
        dest.writeLong(sysCapacity);
        dest.writeString(appPath);
        dest.writeLong(appCapacity);
        dest.writeString(tmpPath);
        dest.writeLong(tmpCapacity);
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
