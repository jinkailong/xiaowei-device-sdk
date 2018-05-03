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
package com.tencent.xiaowei.sdk;

import android.util.Log;

/**
 * 声波配网
 */
public class XWVoiceLinkManager {

    public static final String TAG = "XWVoiceLinkManager";

    private static OnStartWifiDecoderListener mOnStartWifiDecoderListener;


    //*****************************配网 Begin *****************************//

    /**
     * 配网的结果
     */
    public interface OnStartWifiDecoderListener {
        void onReceiveWifiInfo(String ssid, String pwd, int ip, int port);
    }

    /**
     * 开始配网
     *
     * @param key        smartlink配网, 设备的GUID 16字符的字符串。
     * @param samplerate 声波配网, 设备实际录音的采样率，填的不对，会导致解声波信息失败。
     * @param mode       3 同时支持声波配网和smartlink配网
     * @param listener
     */
    public static void startWifiDecoder(String key, int samplerate, int mode, OnStartWifiDecoderListener listener) {
        Log.d(TAG, "startWifiDecoder " + key + " " + samplerate);
        XWSDKJNI.startWifiDecoder(key, samplerate, mode);
        mOnStartWifiDecoderListener = listener;
    }

    /**
     * Wifi配网信息解析成功通知回调
     *
     * @param ssid wifi的ssid
     * @param pwd  wifi的密码
     * @param ip   手Q的IP ， 用于ackapp通知手Q设备已联网
     * @param port 手Q的端口
     */
    static void onReceiveWifiInfo(final String ssid, final String pwd, final int ip, final int port) {
        Log.d(TAG, "startWifiDecoder " + ssid + " " + pwd + " " + ip + " " + port);
        if (mOnStartWifiDecoderListener != null) {
            XWSDKJNI.postMain(new Runnable() {
                @Override
                public void run() {
                    mOnStartWifiDecoderListener.onReceiveWifiInfo(ssid, pwd, ip, port);
                }
            });
        }
    }

    /**
     * 填充wav数据。
     *
     * @param wav wav 是PCM 16bit 单声道的,size小于2048Byte
     */
    public static void fillVoiceWavData(byte[] wav) {
        Log.d(TAG, "fillVoiceWavData");
        XWSDKJNI.fillVoiceWavData(wav);
    }

    /**
     * wifi配网信息解析，停止解析模块
     */
    public static void stopWifiDecoder() {
        Log.d(TAG, "stopWifiDecoder ");
        XWSDKJNI.stopWifiDecoder();
    }

    /**
     * 配网完成，且初始化完SDK后，用这个接口通知手Q设备已经联网。 参见: onReceiveWifiInfo回调说明。
     *
     * @param ip   同步过来的ip
     * @param port 同步过来的port
     */
    public static void ackApp(int ip, int port) {
        Log.d(TAG, "ackApp " + ip + " " + port);
        XWSDKJNI.getInstance().ackApp(ip, port);
    }
    //*****************************配网 End *****************************//
}
