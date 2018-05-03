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

import android.support.v4.util.LongSparseArray;

import com.tencent.xiaowei.info.XWCCMsgInfo;
import com.tencent.xiaowei.util.QLog;

/**
 *    C2C消息，适用于设备和小微App直接通信
 */
public class XWCCMsgManager {
    private static final String TAG = XWCCMsgManager.class.getSimpleName();

    private static final LongSparseArray<OnSendCCMsgListener> mSendCCMsgListeners = new LongSparseArray<>();

    private static OnReceiveC2CMsgListener mOnReceiveC2CMsgListener;

    /**
     * C2C消息监听
     */
    public interface OnReceiveC2CMsgListener {

        /**
         * C2C消息接收回调
         *
         * @param from 消息来源
         * @param msg  消息体
         */
        void onReceiveC2CMsg(long from, XWCCMsgInfo msg);
    }

    /**
     * C2C消息发送结果监听
     */
    public interface OnSendCCMsgListener {

        /**
         * C2C消息发送结果回调
         *
         * @param to      消息接收者
         * @param errCode 结果码
         */
        void onResult(long to, int errCode);
    }

    /**
     * 初始化c2c消息模块
     *
     * @return errCode 结果码
     */
    public static int initC2CMsgModule() {
        return XWSDKJNI.initCCMsgModule();
    }


    /**
     * 发送C2C消息
     *
     * @param to       消息接收者
     * @param msg      消息体
     * @param listener 消息监听
     */
    public static void sendCCMsg(long to, XWCCMsgInfo msg, OnSendCCMsgListener listener) {
        long cookie = XWSDKJNI.sendCCMsg(to, msg);
        if (listener != null) {
            if (cookie > 0) {
                mSendCCMsgListeners.put(cookie, listener);
            } else {
                listener.onResult(to, -1);
            }
        }
    }

    /**
     * 设置C2C消息接收监听器
     *
     * @param onReceiveC2CMsgListener C2C消息接收监听器
     */
    public static void setOnReceiveC2CMsgListener(OnReceiveC2CMsgListener onReceiveC2CMsgListener) {
        mOnReceiveC2CMsgListener = onReceiveC2CMsgListener;
    }

    /**
     * 接收C2C消息
     *
     * @param from 消息来源
     * @param msg  消息体
     */
    static void onReceiveC2CMsg(final long from, final XWCCMsgInfo msg) {
        QLog.d(TAG, "onReceiveC2CMsg from: " + from);
        if (mOnReceiveC2CMsgListener != null) {
            XWSDKJNI.postMain(new Runnable() {
                @Override
                public void run() {
                    mOnReceiveC2CMsgListener.onReceiveC2CMsg(from, msg);
                }
            });
        }
    }

    /**
     * C2C消息发送结果通知
     *
     * @param cookie  消息cookie
     * @param to      消息接收者
     * @param errCode 消息发送返回码
     */
    static void onSendCCMsgResult(long cookie, final long to, final int errCode) {
        QLog.d(TAG, "onSendCCMsgResult cookie: " + cookie + " to: " + to + " errCode: " + errCode);
        final OnSendCCMsgListener listener = mSendCCMsgListeners.get(cookie);
        if (listener != null) {
            XWSDKJNI.postMain(new Runnable() {
                @Override
                public void run() {
                    listener.onResult(to, errCode);
                }
            });
        }
    }
}
