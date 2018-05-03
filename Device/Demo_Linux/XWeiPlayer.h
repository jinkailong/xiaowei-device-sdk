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

#pragma once

#include <map>
#include "AudioApp.h"
#include "Player.h"
#include "XWeiTTSManager.h"

class CAudioPlayer;

// 注意：需要使用opus库
//typedef int16_t opus_int16;
//struct OpusDecoder;

struct txc_media_t;

class CDecoderOpus
{
public:
    CDecoderOpus(size_t sample, size_t channel, size_t max_frames = 960 * 6);
    ~CDecoderOpus();

    size_t Decode(const unsigned char *data, int data_len, CAudioPlayer *player);

private:
    size_t sample_;
    size_t channel_;
    size_t max_frames_;

//    OpusDecoder *decoder_;
//    opus_int16 *opus_buffer_;
    char *pcm_buffer_;
};

class CXWeiPlayer : OnTTSPushListener
{
public:
    CXWeiPlayer();
    ~CXWeiPlayer();

    void Init(SESSION id);

    bool OnCallback(SESSION id, TXC_PLAYER_ACTION action, XWPARAM arg1, XWPARAM arg2);
    virtual void OnTTSInfo(TTSItem *ttsInfo);
    virtual void OnTTSData(XWTTSDataInfo *ttsInfo);
    bool IsPlaying();

private:
    bool OnActStop();
    bool OnActPause(bool pause);

    bool OnPlaylistAddItem(const txc_media_t **media, long count);

    bool OnPushMusicMedia(const txc_media_t *media, bool needReleaseRes);

private:
    bool isPlaying_;
    SESSION id_;
    CAudioPlayer *player_;
    CDecoderOpus *decoder_;
};

class CXWeiPlayerMgr
{
public:
    CXWeiPlayerMgr();
    ~CXWeiPlayerMgr();
    bool OnCallback(SESSION id, TXC_PLAYER_ACTION action, XWPARAM arg1, XWPARAM arg2);
    bool isPlaying();
    SESSION mCurrentSession;

private:
    std::map<SESSION, CXWeiPlayer *> map_players_;
};

extern CXWeiPlayerMgr g_xwei_player_mgr;
