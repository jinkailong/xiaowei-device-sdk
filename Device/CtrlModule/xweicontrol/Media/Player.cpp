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
#include "Player.hpp"
#include <stdio.h>
#include <string>
#include "AudioApp.hpp"
#include "MediaTTS.hpp"
#include "MediaMusic.hpp"
#include "MediaText.hpp"
#include "TXCServices.hpp"
#include "TXCSkillsDefine.h"
#include <string.h>
#include "logger.h"

TXCMediaCenter::TXCMediaCenter()
: m_lastActiveTime(0)
{
}

size_t TXCMediaCenter::AddMedia(PtrMedia &media)
{
    if (media.get() && media->info_.res_id && media->info_.res_id[0])
    {
        map_media_[media->info_.res_id] = media;
    }

    return map_media_.size();
}

PtrMedia TXCMediaCenter::GetMedia(const std::string &res_id)
{
    PtrMedia result;
    std::map<std::string, PtrMedia>::iterator itr = map_media_.find(res_id);
    if (map_media_.end() != itr)
    {
        result = itr->second;
    }

    return result;
}

int TXCMediaCenter::ReadMedia(_In_ const char *res_id, _Out_ const void **data, _Out_ size_t *data_size, _In_ size_t offset)
{
    int result = EINVAL;
    if (res_id && res_id[0] && data && data_size)
    {
        PtrMedia media = GetMedia(res_id);
        if (media.get())
        {
            result = media->Read(data, data_size, offset);
        }
        else
        {
            result = EBADF;
        }
    }

    return result;
}

bool TXCMediaCenter::RemoveMedia(const std::string &res_id)
{
    bool exists = false;
    if (!res_id.empty())
    {
        std::map<std::string, PtrMedia>::iterator itr = map_media_.find(res_id);
        if (map_media_.end() != itr)
        {
            map_media_.erase(itr);
            exists = true;
        }
    }

    return exists;
}

bool TXCMediaCenter::DecMediaTipCnt(const std::string &res_id)
{
    bool handled = false;
    PtrMedia media = GetMedia(res_id);
    if (media.get())
    {
        media.get()->DecPlayCnt();
    }

    return handled;
}

bool TXCMediaCenter::IsMediaNeedPlay(const std::string &res_id)
{
    bool needPlay = true;

    PtrMedia media = GetMedia(res_id);
    if (media.get())
    {
        TLOG_DEBUG("TXCMediaCenter::IsMediaNeedPlay res_id=%s play_count=%d", res_id.c_str(), media.get()->GetPlayCnt());
        needPlay = (media.get()->GetPlayCnt() > 0 || media.get()->GetPlayCnt() == -1);
    }

    return needPlay;
}

std::string TXCMediaCenter::GenResourceId()
{
    std::string session_id;
    for (int i = 0; i < 32; ++i)
    {
        session_id += ('A' + rand() % 26);
    }
    return session_id;
}

int TXCMediaCenter::AddPlayList(SESSION id, PtrMediaList &playList)
{
    map_playlist_[id] = playList;

    return (int)map_playlist_.size();
}

int TXCMediaCenter::TriggerMediaUpdated(const PtrMedia &media)
{
    int count = 0;
    if (media.get())
    {
        const char *res_id = media->GetInfo().res_id;
        if (res_id && res_id[0])
        {

            std::map<SESSION, PtrMediaList>::iterator itr = map_playlist_.begin();
            for (; map_playlist_.end() != itr; ++itr)
            {
                SESSION session_id = itr->first;
                PtrMediaList &playlist = itr->second;
                if (playlist.get())
                {
                    PtrMedia find_media = playlist->Get(res_id);
                    if (find_media.get())
                    {
                        post_message(session_id, XWM_MEDIA_UPDATE, XWPARAM(res_id), NULL, 0);
                        count++;
                    }
                }
            }
        }
    }

    return count;
}

void TXCMediaCenter::SetLastActiveTime()
{
    m_lastActiveTime = time(NULL);
}

long TXCMediaCenter::GetLastActiveTime()
{
    time_t now = time(NULL);
    return now - m_lastActiveTime;
}

void TXCMediaCenter::AddVoiceData(const char* data, int length)
{
    if (data == NULL || length == 0) {
        return;
    }
    
    std::string strBuffer;
    strBuffer.assign(data, length);
    m_strVoiceData.append(strBuffer);
}

void TXCMediaCenter::ResetVoiceData()
{
    m_strVoiceData.clear();
}

std::string TXCMediaCenter::GetVoiceData()
{
    return m_strVoiceData;
}

TXCPlayerManager::TXCPlayerManager()
{
}

PtrPlayer TXCPlayerManager::NewPlayer(int id)
{
    PtrPlayer player(new TXCPlayer(id));
    vPlayers_[id] = player;

    return player;
}

PtrPlayer TXCPlayerManager::NewPlayer(int id, REPEAT_MODE repeatMode)
{
    PtrPlayer player(new TXCPlayer(id, repeatMode));
    vPlayers_[id] = player;

    return player;
}

PtrPlayer TXCPlayerManager::GetPlayer(int app_id)
{
    PtrPlayer result;
    std::map<int, PtrPlayer>::iterator itr = vPlayers_.find(app_id);
    if (vPlayers_.end() != itr)
    {
        result = itr->second;
    }

    return result;
}

TXCPlayer::TXCPlayer(int app_id)
    : app_id_(app_id)
{
    memset(&info_, 0, sizeof(info_));
    info_.repeatMode = REPEAT_SEQUENCE;
    info_.status = STATUS_STOP;
}

TXCPlayer::TXCPlayer(int app_id, REPEAT_MODE repeatMode)
    : app_id_(app_id)
{
    memset(&info_, 0, sizeof(info_));
    info_.repeatMode = repeatMode;
    info_.status = STATUS_STOP;
}

void TXCPlayer::SetMedialist(PtrMediaList &playList)
{
    media_list_ = playList;
}

PtrMediaList &TXCPlayer::GetMediaList()
{
    if (!media_list_.get())
    {
        media_list_ = PtrMediaList(new TXCMediaList);
        //  TODO:   implementing playlist and its id
        info_.playlist_id = app_id_;

        TXCServices::instance()->GetMediaCenter()->AddPlayList(app_id_, media_list_);
    }
    return media_list_;
}

std::string genSessionID2()
{
    std::string session_id;
    for (int i = 0; i < 32; ++i)
    {
        session_id += ('A' + rand() % 26);
    }
    return session_id;
}

namespace inner
{
enum ResponseType
{
    RESPONSE_TYPE_BEGIN = 0,
    RESPONSE_TYPE_CARD = 1,              // 卡片消息
    RESPONSE_TYPE_WEATHER = 2,           // 天气信息（json格式）
    RESPONSE_TYPE_GAME = 3,              // 游戏信息
    RESPONSE_TYPE_CLOCK = 4,             // 闹钟, json
    RESPONSE_TYPE_MEDIA = 5,             // 媒体类信息 JSON  和英语跟读冲突
    RESPONSE_TYPE_LOCAL_SKILL = 6,       // 本地SKILL：意图和槽位信息
    RESPONSE_TYPE_MSGBOX = 7,            // 消息盒子 JSON
    RESPONSE_TYPE_FETCH_DEVICE_INFO = 8, // 查询设备信息 JSON
    RESPONSE_TYPE_NEWS = 9,              // 新闻 JSON
    RESPONSE_TYPE_BAIKE = 10,            // 百科 JSON
};
}
int TXCPlayer::AddResponseData(int response_type, const char *response_data, _Out_ PtrMedia &_media)
{
    int index = -1;

    PtrMedia media;
    switch (response_type)
    {
    case inner::RESPONSE_TYPE_WEATHER:
    {
        std::string res_id = genSessionID2();
        media = TXCServices::instance()->GetMediaCenter()->NewMedia<CMediaText>(res_id);
        CMediaText *media_text = dynamic_cast<CMediaText *>(media.get());
        media_text->Init(TYPE_INFO_WEATHER, response_data);
        index = media_list_->Add(-1, media);
        if (0 <= index)
        {
            _media = media;
        }
    }
    break;
    default:
        break;
    }

    return index;
}

PLAYER_STATUS TXCPlayer::GetStatus() const
{
    return info_.status;
}

int TXCPlayer::AddMediaItem(_In_ PtrMedia &media)
{
    TLOG_DEBUG("sessionId=%d TXCPlayer::AddMediaItem %s", app_id_, media.get()->GetInfo().res_id);
    return media_list_->Add(-1, media);
}

int TXCPlayer::UpdateMediaItem(const TXCA_PARAM_RESOURCE *item)
{
    int index = -1;

    if (item->format == txca_resource_url && item->content && item->extend_buffer && item->id)
    {
        const char *res_id = item->id;
        PtrMedia temp_media = TXCServices::instance()->GetMediaCenter()->GetMedia(res_id);
        index = media_list_->Find(res_id);
        if (temp_media.get() && index >= 0)
        {
            CMediaMusic *media_music = dynamic_cast<CMediaMusic *>(temp_media.get());
            if (media_music)
            {
                // TODO: need to remove
                std::string desc_item(item->extend_buffer);
                std::string curr_desc(media_music->GetInfo().description);
                std::string update_desc = desc_item.substr(desc_item.find("\"quality\""), 11);
                curr_desc.replace(curr_desc.find("\"quality\""), 11, update_desc);
                media_music->Update(item->content, curr_desc.c_str(), item->offset);
            }
        }
    }

    return index;
}

int TXCPlayer::ReplaceMediaItem(const TXCA_PARAM_RESOURCE *item)
{
    int index = -1;

    if (item->format == txca_resource_url && item->content && item->extend_buffer && item->id)
    {
        const char *res_id = item->id;
        PtrMedia temp_media = TXCServices::instance()->GetMediaCenter()->GetMedia(res_id);
        index = media_list_->Find(res_id);
        if (temp_media.get() && index >= 0)
        {
            CMediaMusic *media_music = dynamic_cast<CMediaMusic *>(temp_media.get());
            if (media_music)
            {
                media_music->Update(item->content, item->extend_buffer, item->offset);
            }
        }
    }

    return index;
}

bool TXCPlayer::Play(bool isAuto)
{
    if (info_.status == STATUS_STOP)
    {
        Play(0);
    }
    else if (info_.status == STATUS_PAUSE)
    {
        Resume(isAuto);
    }
    else if (info_.status == STATUS_PLAY)
    {
        // DO No Thing
    }

    return true;
}

bool TXCPlayer::Play(int index)
{
    TriggerEvent(XWM_PLAY, XWPARAM((long)index), 0);
    return true;
}

bool TXCPlayer::Stop()
{
    TriggerEvent(XWM_STOP, 0, 0);
    return true;
}

bool TXCPlayer::Pause(bool isAuto)
{
    TriggerEvent(XWM_PAUSE, XWPARAM(1), XWPARAM(isAuto));
    return true;
}

bool TXCPlayer::Resume(bool isAuto)
{
    TriggerEvent(XWM_PAUSE, 0, XWPARAM(isAuto));
    return true;
}

bool TXCPlayer::Volume(int v)
{
    info_.volume = v;

    TriggerEvent(XWM_VOLUME, XWPARAM((long)info_.volume), NULL);
    return true;
}

bool TXCPlayer::SetRepeat(int repeatMode)
{
    const txc_session_info *session = txc_get_session(app_id_);
    std::string strSkillId = session != NULL ? session->skill_id : "";

    // 其他场景（FM、电台等有顺序的节目，不支持切换为非顺序播放）
    if (strSkillId == DEF_TXCA_SKILL_ID_MUSIC)
    {
        // 音乐不支持顺序播放，只能循环。
        if(REPEAT_SEQUENCE == REPEAT_MODE(repeatMode))
        {
            repeatMode = REPEAT_LOOP;
        }
        info_.repeatMode = REPEAT_MODE(repeatMode);
        TriggerEvent(XWM_REPEAT, XWPARAM(info_.repeatMode), 0);
    }

    return true;
}

bool TXCPlayer::Next(int skip)
{
    TriggerEvent(XWM_NEXT, XWPARAM((long)skip), 0);
    return true;
}

void TXCPlayer::OnStatusChanged(PLAYER_STATUS st)
{
    TLOG_DEBUG("sessionId=%d TXCPlayer::OnStatusChanged, status:%d", app_id_ ,st);
    info_.status = st;
}

void TXCPlayer::TriggerEvent(XWM_EVENT event, XWPARAM arg1, XWPARAM arg2)
{
    send_message(app_id_, event, arg1, arg2);
}

const txc_player_info_t &TXCPlayer::GetInfo()
{
    return info_;
}

const txc_player_info_t *txc_get_player_info(SESSION id)
{
    PtrPlayer player = TXCServices::instance()->GetPlayerManager()->GetPlayer(id);
    if (player.get())
    {
        const txc_player_info_t &info = player->GetInfo();

        return &info;
    }

    return NULL;
}
