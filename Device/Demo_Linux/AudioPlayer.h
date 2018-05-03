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

#include <stdio.h>
#include <stdlib.h>
#include <map>
#include <vector>
#include <list>
#include <queue>

#include "Player.h"

// 注意：
//     1. 真实播放器的逻辑 Linux Demo 并未实现
//     2. 播放器需要同时支持PCM流数据的播放和URL媒体资源的播放
//     3. 当资源播放结束，需要调用NotifyState(TXC_PLAYER_STATE_COMPLETE)通知控制层
class CAudioPlayer
{
public:
    CAudioPlayer(int pid);
    ~CAudioPlayer();
    int GetPid();
    void PutSessionId(int session_id);

    void Play(int type, const char *content);
    void TTSPushStart(int sample, int channel, int pcmSample);
    void TTSPushEnd();
    void TTSPush(int seq, const char *data, int len);
    void Pause();
    void Resume();
    void Stop();
    void Start();
    void Prev();
    void Next();

private:
    void NotifyState(TXC_PLAYER_STATE state);

private:
    bool m_isExit;
    float m_volume;
    int m_pid;
    int m_session_id;
};

class CAudioPlayerMgr
{
public:
    CAudioPlayerMgr();
    ~CAudioPlayerMgr();
    static CAudioPlayerMgr &instance();
    CAudioPlayer *Create();
    CAudioPlayer *GetPlayer(int pid);
    void Destroy(int pid);

private:
    std::map<int, CAudioPlayer *> m_players;
    int m_pidSeq;
};
