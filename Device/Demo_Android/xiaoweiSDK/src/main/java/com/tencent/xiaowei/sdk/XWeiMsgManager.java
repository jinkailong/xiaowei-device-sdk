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

import android.text.TextUtils;

import com.tencent.xiaowei.info.XWeiMessageInfo;
import com.tencent.xiaowei.util.QLog;

import java.util.ArrayList;
import java.util.HashMap;


public class XWeiMsgManager {
    private static final String TAG = "XWeiMsgManager";

    private static HashMap<Long, XWSDK.OnSendMessageListener> mOnSendMessageListener = new HashMap<>();


    public static long sendMessage(XWeiMessageInfo msg, XWSDK.OnSendMessageListener listener) {
        if (msg == null)
            return -1;

        if (msg.receiver == null || msg.receiver.size() == 0)
            return -1;

        ArrayList<Long> receiver = new ArrayList<>();
        for (String s : msg.receiver) {
            if (!TextUtils.isEmpty(s)) {
                try {
                    receiver.add(Long.valueOf(s));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (receiver.size() == 0)
            return -1;

        long[] targetIds = new long[receiver.size()];
        for (int i = 0; i < receiver.size(); i++) {
            targetIds[i] = receiver.get(i);
        }

        long cookie = -1;
        switch (msg.type) {
            case XWeiMessageInfo.TYPE_AUDIO:
                cookie = XWSDKJNI.getInstance().nativeSendAudioMsg(3, msg.content, msg.duration, targetIds);
                break;
            default:
                break;
        }

        QLog.d(TAG, "sendMessage type: " + msg.type + " cookie: " + cookie);

        if (cookie != -1 && listener != null) {
            QLog.d(TAG, "sendMessage add message listener");
            mOnSendMessageListener.put(cookie, listener);
        }

        return cookie;
    }

    public static void OnRichMsgSendProgress(int cookie, long transfer_progress, long max_transfer_progress){
        QLog.d(TAG, "mOnSendMessageListener size:" + mOnSendMessageListener.size());
        final XWSDK.OnSendMessageListener listener = mOnSendMessageListener.get((long)cookie);
        if (listener != null) {
            listener.onProgress(transfer_progress, max_transfer_progress);
        } else {
            QLog.e(TAG, "OnRichMsgSendProgress no listener of cookie " + cookie);
        }
    }

    public static void OnRichMsgSendRet(int cookie, int err_code){
        QLog.d(TAG, "mOnSendMessageListener size:" + mOnSendMessageListener.size());
        final XWSDK.OnSendMessageListener listener = mOnSendMessageListener.get((long)cookie);
        if (listener != null) {
            listener.onComplete(err_code);
        } else {
            QLog.e(TAG, "OnRichMsgSendRet no listener of cookie " + cookie);
        }
    }
}
