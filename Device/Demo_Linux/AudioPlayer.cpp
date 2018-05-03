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

#include <list>
#include <string>
#include "AudioPlayer.h"

#define NUM_CHANNELS (2)
#define SAMPLE_SILENCE (0.0f)

CAudioPlayer::CAudioPlayer(int pid)
    : m_pid(pid), m_volume(1.0f), m_isExit(false), m_session_id(0)
{
}

CAudioPlayer::~CAudioPlayer()
{
}

void CAudioPlayer::Play(int type, const char *url)
{
    
}

void CAudioPlayer::TTSPushStart(int sample, int channel, int pcmSample)
{
    // TTS 准备开始推送
}

void CAudioPlayer::TTSPush(int seq, const char *data, int len)
{
    // TTS 数据推送中
}

void CAudioPlayer::TTSPushEnd()
{
    // TTS 数据推送结束
}

int CAudioPlayer::GetPid()
{
    return m_pid;
}

void CAudioPlayer::Stop()
{
}

void CAudioPlayer::Start()
{
}

void CAudioPlayer::Resume()
{
}

void CAudioPlayer::Pause()
{

}

void CAudioPlayer::Prev()
{
    NotifyState(TXC_PLAYER_STATE_COMPLETE);
}

void CAudioPlayer::Next()
{
    NotifyState(TXC_PLAYER_STATE_COMPLETE);
}

void CAudioPlayer::NotifyState(TXC_PLAYER_STATE state) {
    // 通知控制层播放器的状态变化
    txc_player_statechange(m_session_id, (TXC_PLAYER_STATE)state);
}

void CAudioPlayer::PutSessionId(int session_id)
{
    m_session_id = session_id;
}

CAudioPlayerMgr::CAudioPlayerMgr()
{
}

CAudioPlayerMgr::~CAudioPlayerMgr()
{
}

CAudioPlayerMgr &CAudioPlayerMgr::instance()
{
    static CAudioPlayerMgr _instance;
    return _instance;
}

CAudioPlayer *CAudioPlayerMgr::Create()
{
    m_pidSeq++;

    CAudioPlayer *player = new CAudioPlayer(m_pidSeq);
    m_players.insert(std::make_pair(m_pidSeq, player));
    return player;
}

void CAudioPlayerMgr::Destroy(int pid)
{
    std::map<int, CAudioPlayer *>::iterator iter = m_players.find(pid);
    if (iter != m_players.end())
    {
        delete iter->second;
        m_players.erase(iter);
    }
}

CAudioPlayer *CAudioPlayerMgr::GetPlayer(int pid)
{
    std::map<int, CAudioPlayer *>::iterator iter = m_players.find(pid);
    if (iter != m_players.end())
    {
        return iter->second;
    }
    return NULL;
}
