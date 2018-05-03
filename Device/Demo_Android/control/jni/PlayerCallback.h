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
#include <stdlib.h>
#include <stdio.h>
#include <jni.h>
#include <time.h>
#include <android/log.h>

#include "AudioApp.h"
#include "Player.h"
#include "Media.h"

class CPlayerCallback {
public:
    static XWPARAM
    txc_control_android_callback(SESSION id, TXC_PLAYER_ACTION action, XWPARAM arg1, XWPARAM arg2);

    static bool txc_event_processor(SESSION id, XWM_EVENT event, XWPARAM arg1, XWPARAM arg2);

    static void InitClasses(JNIEnv *env, jclass service);

private:
    static bool OnActStop(SESSION id);

    static bool OnActFinish(SESSION id);

    static bool OnActPause(SESSION id, bool pause);

    static bool OnActVolume(SESSION id, int volume);

    static bool OnActSetRepeat(SESSION id, int repeatMode);

    static bool OnAddAlbum(SESSION id, const txc_media_t *album, long index);

    static bool OnPlaylistAddItem(SESSION id, const txc_media_t **list, bool isFront, long count);

    static bool OnPlaylistUpdateItem(SESSION id, const txc_media_t **list, long count);

    static bool OnPlaylistRemoveItem(SESSION id, const txc_media_t **list, long count);

    static bool OnPushMedia(SESSION id, const txc_media_t *media, bool need_release);

    static bool OnProgress(SESSION id, const txc_progress_t *progress);

    static bool OnSupplement(SESSION id, long wait_time, const TXCA_PARAM_RESPONSE *response);

    static void OnTips(SESSION id, int tipsType);

    static long long ReportPlayState(SESSION id, TXCA_PARAM_STATE *state);

    static bool OnXWMPlaylistAddItem(SESSION id, long begin_index, long count);

    static void OnDownloadMsg(SESSION id, const txc_download_msg_data_t* data);

    static void OnAudioMsgRecord(SESSION id);

    static void OnAudioMsgSend(SESSION id, unsigned long long tinyId);

private:
    static jclass s_class_MediaInfo;
    static jclass s_class_PlayState;
};
