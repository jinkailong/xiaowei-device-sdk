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
package com.tencent.aiaudio.bledemo;

import android.bluetooth.BluetoothClass;

import com.tencent.xiaowei.util.JsonUtil;

public class BLEDeviceInfo {

    /**
     * 未配对
     */
    public static final int STATE_BOND_NONE = 10;
    /**
     * 配对中
     */
    public static final int STATE_BOND_BONDING = 11;
    /**
     * 已配对
     */
    public static final int STATE_BOND_BONDED = 12;
    /**
     * 已连接
     */
    public static final int STATE_CONNECTED = 13;

    /**
     * 连接中
     */
    public static final int STATE_CONNECTING = 14;

    /**
     * 蓝牙设备名字
     */
    public String name;

    /**
     * 蓝牙设备地址
     */
    public String address;

    /**
     * 蓝牙设备状态
     */
    public int state;

    /**
     * 蓝牙设备支持的模式 {@link Major}
     */
    public int major;

    /**
     * Defines all major device class constants.
     * <p>See {@link BluetoothClass.Device} for minor classes.
     */
    public static class Major {
        private static final int BITMASK = 0x1F00;

        public static final int MISC = 0x0000;
        public static final int COMPUTER = 0x0100;
        public static final int PHONE = 0x0200;
        public static final int NETWORKING = 0x0300;
        public static final int AUDIO_VIDEO = 0x0400;
        public static final int PERIPHERAL = 0x0500;
        public static final int IMAGING = 0x0600;
        public static final int WEARABLE = 0x0700;
        public static final int TOY = 0x0800;
        public static final int HEALTH = 0x0900;
        public static final int UNCATEGORIZED = 0x1F00;
    }

    @Override
    public boolean equals(Object o) {
        try {
            if (o instanceof BLEDeviceInfo) {
                return address.equals(((BLEDeviceInfo) o).address);
            }
        } catch (Exception e) {

        }
        return false;
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
