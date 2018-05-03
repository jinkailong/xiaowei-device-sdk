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
package com.tencent.aiaudio.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.tencent.aiaudio.CommonApplication;
import com.tencent.aiaudio.utils.AssetsUtil;
import com.tencent.aiaudio.utils.PcmBytesPlayer;
import com.tencent.aiaudio.wakeup.RecordDataManager;
import com.tencent.xiaowei.util.QLog;
import com.tencent.xiaowei.sdk.XWDeviceBaseManager;

public class NetworkReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
            for (int i = 0; i < networkInfos.length; i++) {
                NetworkInfo.State state = networkInfos[i].getState();
                if (NetworkInfo.State.CONNECTED == state) {
                    if (!CommonApplication.isOnline && CommonApplication.isLogined) {
                        // 曾经登录过并且不在线，尝试重连
                        XWDeviceBaseManager.deviceReconnect();// 这时候登录失败，因为底层连接还没建立好
                    }
                    QLog.d(TAG, "Network connected.");
                    PcmBytesPlayer.getInstance().play(AssetsUtil.getRing("network_connected.pcm"), new PcmBytesPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion() {
                        }
                    });
                    return;
                }
            }
        }
        QLog.d(TAG, "Network disconnected.");
        CommonApplication.isOnline = false;
        RecordDataManager.getInstance().setHalfWordsCheck(false);
        context.sendBroadcast(new Intent("OFFLINE"));
        PcmBytesPlayer.getInstance().play(AssetsUtil.getRing("network_disconnected.pcm"), new PcmBytesPlayer.OnCompletionListener() {
            @Override
            public void onCompletion() {
            }
        });
    }
}
