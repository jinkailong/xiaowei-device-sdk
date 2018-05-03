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

import android.util.Log;

import com.tencent.xiaowei.sdk.XWCCMsgManager;
import com.tencent.xiaowei.info.XWCCMsgInfo;
import com.tencent.xiaowei.util.JsonUtil;

public class BLEManager {

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_DEVICE_NOT_IMPL = 1;
    public static final int RESULT_EXCEPTION = 2;
    public static final int RESULT_BLE_OPEND = 3;
    public static final int RESULT_BLE_CLOSED = 4;
    public static final int RESULT_BLE_DONT_SUPPORT_AUDIO_VIDEO = 5;
    public static final int RESULT_IS_BUSY = 6;
    public static final int RESULT_OP_FAILED = 7;

    private static final String TAG = "BLEManager";
    private static OnBLEEventListener mOnBLEEventListener;
    private static long binder;

    /**
     * App控制设备蓝牙的相关事件
     */
    public interface OnBLEEventListener {

        /**
         * 查询蓝牙是否打开
         *
         * @return 蓝牙是否打开
         */
        int isBLEOpen();

        /**
         * 查询当前蓝牙已连接的设备，没有连接返回null
         *
         * @return {@link BLEDeviceInfo}
         */
        BLEDeviceInfo onGetCurrentConnectedBLEDevice();

        /**
         * 控制设备打开蓝牙
         *
         * @return 这次操作执行成功返回true，否则返回false，例如已经打开了蓝牙再被调用该回调，返回false
         */
        int openBLE();

        /**
         * 控制设备关闭蓝牙
         *
         * @return 这次操作执行成功返回true，否则返回false，例如已经关闭了蓝牙再被调用该回调，返回false
         */
        int closeBLE();

        /**
         * 控制设备扫描支持音频输出的蓝牙设备
         *
         * @return 这次操作执行成功返回true，否则返回false，例如已经关闭了蓝牙再被调用该回调，返回false
         */
        int startDiscovery();

        /**
         * 控制设备蓝牙配对 成功后调用tx_ai_audio_on_ble_bond回传结果，失败后调用tx_ai_audio_on_ble_unbond回传结果
         *
         * @return 这次操作执行成功返回true，否则返回false，例如正在配对中或者连接中导致这次操作无法执行，返回false
         */
        int bond(String address);

        /**
         * 控制设备蓝牙解除配对 成功后调用tx_ai_audio_on_ble_unbond回传结果
         *
         * @return 这次操作执行成功返回true，否则返回false，例如正在配对中或者连接中导致这次操作无法执行，返回false
         */
        int unBond(String address);

        /**
         * 控制设备蓝牙连接设备 成功后调用tx_ai_audio_on_ble_connected(true,device)回传结果，失败后调用tx_ai_audio_on_ble_connected(false,device)回传结果
         *
         * @return 这次操作执行成功返回true，否则返回false，例如正在配对中或者连接中导致这次操作无法执行，返回false
         */
        int connect(String address);
    }

    public static void setOnBLEEventListener(OnBLEEventListener listener) {
        mOnBLEEventListener = listener;
    }

    public static void onCCMsg(long from, String json) {
        binder = from;
        String event = JsonUtil.getValue(json, "event");
        switch (event) {
            case "BLE_OPEN": {
                int ret = RESULT_DEVICE_NOT_IMPL;
                if (mOnBLEEventListener != null) {
                    ret = mOnBLEEventListener.openBLE();
                }
                if (ret != 0) {
                    // 回包
                    TXBLEResultInfo resultInfo = new TXBLEResultInfo();
                    resultInfo.event = "BLE_OPEN";
                    resultInfo.time = System.currentTimeMillis();
                    resultInfo.result = 1;
                    Log.e(TAG, "BLE_OPEN: " + resultInfo.toString());
                    sendMsg(resultInfo);
                }
            }
            break;
            case "BLE_CLOSE": {
                int ret = RESULT_DEVICE_NOT_IMPL;
                if (mOnBLEEventListener != null) {
                    ret = mOnBLEEventListener.closeBLE();
                }
                if (ret != 0) {
                    // 回包
                    TXBLEResultInfo resultInfo = new TXBLEResultInfo();
                    resultInfo.event = "BLE_CLOSE";
                    resultInfo.time = System.currentTimeMillis();
                    resultInfo.result = 1;
                    Log.e(TAG, "BLE_CLOSE: " + resultInfo.toString());
                    sendMsg(resultInfo);
                }
            }
            break;
            case "BLE_GET_STATE": {
                if (mOnBLEEventListener != null) {
                    int isOpen = mOnBLEEventListener.isBLEOpen();
                    BLEDeviceInfo info = mOnBLEEventListener.onGetCurrentConnectedBLEDevice();
                    TXBLEResultInfo resultInfo = new TXBLEResultInfo();
                    resultInfo.event = "BLE_GET_STATE";
                    resultInfo.time = System.currentTimeMillis();
                    resultInfo.result = isOpen;
                    if (info != null) {
                        resultInfo.device = JsonUtil.toJson(info);
                    }
                    Log.e(TAG, "BLE_GET_STATE: " + resultInfo.toString());
                    sendMsg(resultInfo);
                } else {
                    TXBLEResultInfo resultInfo = new TXBLEResultInfo();
                    resultInfo.event = "BLE_GET_STATE";
                    resultInfo.time = System.currentTimeMillis();
                    resultInfo.result = 1;
                    Log.e(TAG, "BLE_GET_STATE: " + resultInfo.toString());
                    sendMsg(resultInfo);
                }

            }
            break;
            case "BLE_DISCOVERY": {
                int ret = RESULT_DEVICE_NOT_IMPL;
                if (mOnBLEEventListener != null) {
                    ret = mOnBLEEventListener.startDiscovery();
                }
                if (ret != 0) {
                    // 回包
                    TXBLEResultInfo resultInfo = new TXBLEResultInfo();
                    resultInfo.event = "BLE_DISCOVERY";
                    resultInfo.time = System.currentTimeMillis();
                    resultInfo.result = 1;
                    Log.e(TAG, "BLE_DISCOVERY: " + resultInfo.toString());
                    sendMsg(resultInfo);
                }
            }
            break;
            case "BLE_BOND": {
                int ret = RESULT_DEVICE_NOT_IMPL;
                if (mOnBLEEventListener != null) {
                    ret = mOnBLEEventListener.bond(JsonUtil.getValue(json, "address"));
                }
                if (ret != 0) {
                    // 回包
                    TXBLEResultInfo resultInfo = new TXBLEResultInfo();
                    resultInfo.event = "BLE_BOND";
                    resultInfo.time = System.currentTimeMillis();
                    resultInfo.result = 1;
                    BLEDeviceInfo deviceInfo = new BLEDeviceInfo();
                    deviceInfo.address = JsonUtil.getValue(json, "address");
                    resultInfo.device = JsonUtil.toJson(deviceInfo);
                    Log.e(TAG, "BLE_BOND: " + resultInfo.toString());
                    sendMsg(resultInfo);
                }
            }
            break;
            case "BLE_UNBOND": {
                int ret = RESULT_DEVICE_NOT_IMPL;
                if (mOnBLEEventListener != null) {
                    ret = mOnBLEEventListener.unBond(JsonUtil.getValue(json, "address"));
                }
                if (ret != 0) {
                    // 回包
                    TXBLEResultInfo resultInfo = new TXBLEResultInfo();
                    resultInfo.event = "BLE_UNBOND";
                    resultInfo.time = System.currentTimeMillis();
                    resultInfo.result = 1;
                    BLEDeviceInfo deviceInfo = new BLEDeviceInfo();
                    deviceInfo.address = JsonUtil.getValue(json, "address");
                    resultInfo.device = JsonUtil.toJson(deviceInfo);
                    Log.e(TAG, "BLE_UNBOND: " + resultInfo.toString());
                    sendMsg(resultInfo);
                }
            }
            break;
            case "BLE_CONNECT": {
                int ret = RESULT_DEVICE_NOT_IMPL;
                if (mOnBLEEventListener != null) {
                    ret = mOnBLEEventListener.connect(JsonUtil.getValue(json, "address"));
                }
                if (ret != 0) {
                    // 回包
                    TXBLEResultInfo resultInfo = new TXBLEResultInfo();
                    resultInfo.event = "BLE_CONNECT";
                    resultInfo.time = System.currentTimeMillis();
                    resultInfo.result = 1;
                    BLEDeviceInfo deviceInfo = new BLEDeviceInfo();
                    deviceInfo.address = JsonUtil.getValue(json, "address");
                    resultInfo.device = JsonUtil.toJson(deviceInfo);
                    Log.e(TAG, "BLE_CONNECT: " + resultInfo.toString());
                    sendMsg(resultInfo);
                }
            }
            break;
        }
    }

    private static void sendMsg(TXBLEResultInfo info) {
        if (binder == 0) {
            return;
        }
        XWCCMsgInfo msg = new XWCCMsgInfo();
        msg.businessName = "蓝牙";
        msg.msgBuf = JsonUtil.toJson(info).getBytes();
        XWCCMsgManager.sendCCMsg(binder, msg, null);
    }


    private static class TXBLEResultInfo {


        /**
         * 蓝牙事件
         */
        public String event;

        /**
         * 查询结果 或者 操作结果
         */
        public int result;

        /**
         * 相关的蓝牙设备 json格式
         */
        public String device;

        /**
         * 发生的时间
         */
        public long time;

        @Override
        public String toString() {
            return JsonUtil.toJson(this);
        }
    }

    /**
     * 通知App蓝牙已打开
     */
    public static void onBLEOpen() {
        TXBLEResultInfo resultInfo = new TXBLEResultInfo();
        resultInfo.event = "BLE_OPEN";
        resultInfo.time = System.currentTimeMillis();
        Log.e(TAG, "BLE_OPEN: " + resultInfo.toString());
        sendMsg(resultInfo);
    }

    /**
     * 通知App蓝牙已关闭
     */
    public static void onBLEClose() {
        TXBLEResultInfo resultInfo = new TXBLEResultInfo();
        resultInfo.event = "BLE_CLOSE";
        resultInfo.time = System.currentTimeMillis();
        Log.e(TAG, "BLE_CLOSE: " + resultInfo.toString());
        sendMsg(resultInfo);
    }

    /**
     * 通知App蓝牙配对结果
     *
     * @param info {@link BLEDeviceInfo}
     */
    public static void onBLEDeviceBond(BLEDeviceInfo info) {
        TXBLEResultInfo resultInfo = new TXBLEResultInfo();
        resultInfo.event = "BLE_BOND";
        resultInfo.time = System.currentTimeMillis();
        resultInfo.device = JsonUtil.toJson(info);
        Log.e(TAG, "BLE_BOND: " + resultInfo.toString());
        sendMsg(resultInfo);
    }

    /**
     * 通知App蓝牙配对失败或解除配对结果
     *
     * @param info {@link BLEDeviceInfo}
     */
    public static void onBLEDeviceUnBond(BLEDeviceInfo info) {
        TXBLEResultInfo resultInfo = new TXBLEResultInfo();
        resultInfo.event = "BLE_UNBOND";
        resultInfo.time = System.currentTimeMillis();
        resultInfo.device = JsonUtil.toJson(info);
        Log.e(TAG, "BLE_UNBOND: " + resultInfo.toString());
        sendMsg(resultInfo);
    }

    /**
     * 通知App蓝牙连接的相关事件
     * connected true表示device已经连接  false表示device断开连接
     *
     * @param connected info处于连接或者未连接状态
     * @param info      {@link BLEDeviceInfo}
     */
    public static void onBLEDeviceConnected(boolean connected, BLEDeviceInfo info) {
        TXBLEResultInfo resultInfo = new TXBLEResultInfo();
        resultInfo.event = "BLE_CONNECT";
        resultInfo.time = System.currentTimeMillis();
        resultInfo.result = connected ? 0 : 1;
        resultInfo.device = JsonUtil.toJson(info);
        Log.e(TAG, "BLE_CONNECT: " + resultInfo.toString());
        sendMsg(resultInfo);
    }

    /**
     * 通知App蓝牙扫描结果
     *
     * @param list 扫描到符合条件的蓝牙设备列表
     */
    public static void onDiscoveryBLE(BLEDeviceInfo[] list) {
        TXBLEResultInfo resultInfo = new TXBLEResultInfo();
        resultInfo.event = "BLE_DISCOVERY";
        resultInfo.time = System.currentTimeMillis();
        resultInfo.device = JsonUtil.toJson(list);
        Log.e(TAG, "BLE_DISCOVERY: " + resultInfo.toString());
        sendMsg(resultInfo);
    }

    public static void onDiscoveryBLEStart() {
        TXBLEResultInfo resultInfo = new TXBLEResultInfo();
        resultInfo.event = "BLE_DISCOVERY_START";
        resultInfo.time = System.currentTimeMillis();
        Log.e(TAG, "BLE_DISCOVERY_START");
        sendMsg(resultInfo);
    }

    /**
     * 通知App蓝牙扫描结果
     *
     * @param item 扫描到符合条件的蓝牙设备
     */
    public static void onDiscoveryBLE(BLEDeviceInfo item) {
        TXBLEResultInfo resultInfo = new TXBLEResultInfo();
        resultInfo.event = "BLE_DISCOVERY";
        resultInfo.time = System.currentTimeMillis();
        resultInfo.device = JsonUtil.toJson(item);
        Log.e(TAG, "BLE_DISCOVERY: " + resultInfo.toString());
        sendMsg(resultInfo);
    }

    public static void onDiscoveryBLEStop() {
        TXBLEResultInfo resultInfo = new TXBLEResultInfo();
        resultInfo.event = "BLE_DISCOVERY_STOP";
        resultInfo.time = System.currentTimeMillis();
        Log.e(TAG, "BLE_DISCOVERY_STOP");
        sendMsg(resultInfo);
    }
}
