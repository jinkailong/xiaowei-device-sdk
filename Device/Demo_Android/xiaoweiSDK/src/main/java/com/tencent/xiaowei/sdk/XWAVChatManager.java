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

import android.os.Bundle;

/**
 * 音视频通话相关
 */

public class XWAVChatManager {

    public static final String SUB_ACTION_ON_SEND_VIDEO_CALL = "tdas_OnSendVideoCall";
    public static final String SUB_ACTION_ON_SEND_VIDEO_CALL_M2M = "tdas_OnSendVideoCallM2M";
    public static final String SUB_ACTION_ON_SEND_VIDEO_CMD = "tdas_OnSendVideoCMD";
    public static final String SUB_ACTION_ON_RECEIVE_VIDEO_BUFFER = "tdas_OnReceiveVideoBuffer";
    public static final String SUB_ACTION_ON_RECEIVE_QQ_CALL_REPLY = "tdas_OnReceiveQQCallReply";
    public static final String SUB_ACTION_START_VIDEO_CHAT_ACTIVITY = "tdas_StartVideoChatActivity";
    public static final String SUB_ACTION_START_AUDIO_CHAT_ACTIVITY = "tdas_StartAudioChatActivity";


    private static OnAVChatEventListener mOnAVChatEventListener;

    public static void setOnAVChatEventListener(OnAVChatEventListener listener) {
        mOnAVChatEventListener = listener;
    }

    static void callbackAVChatEvent(String action, Bundle bundle) {
        if (mOnAVChatEventListener != null) {
            mOnAVChatEventListener.onEvent(action, bundle);
        }
    }

    public interface OnAVChatEventListener {
        void onEvent(String action, Bundle bundle);
    }

    /**
     * 开启通话进程，成功之后会收到ACTION_START_AUDIO_VIDEO_PROCESS广播，sub_action为StartVideoChatActivity
     * 只给社平用
     *
     * @param peerId 对方id
     */
    public static void startVideoChatActivity(final long peerId) {
        Bundle bundle = new Bundle();
        bundle.putLong("peerid", peerId);
        bundle.putLong("uin", peerId);

        callbackAVChatEvent(SUB_ACTION_START_VIDEO_CHAT_ACTIVITY, bundle);
    }

    /**
     * 开启通话进程，成功之后会收到ACTION_START_AUDIO_VIDEO_PROCESS广播，sub_action为StartAudioChatActivity
     * 只给社平用
     *
     * @param peerId 对方id
     */
    public static void startAudioChatActivity(final long peerId) {
        Bundle bundle = new Bundle();
        bundle.putLong("peerid", peerId);
        bundle.putLong("uin", peerId);

        callbackAVChatEvent(SUB_ACTION_START_AUDIO_CHAT_ACTIVITY, bundle);
    }

}
