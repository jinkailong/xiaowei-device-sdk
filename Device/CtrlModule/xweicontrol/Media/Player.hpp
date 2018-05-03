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
#ifndef _AIAUDIO_PLAYER_HPP_
#define _AIAUDIO_PLAYER_HPP_

#include <string>
#include <vector>
#include <map>
#include "Medialist.hpp"
#include "Player.h"
#include "AudioApp.h"

//////////////////// Player.hpp ////////////////////

class TXCPlayerManager;

//  virtual player, call user specified callback to play media data
class TXCPlayer
{
  public:
    //    action
    bool Play(bool isAuto = false);
    bool Play(int index);
    bool Stop();
    bool Pause(bool isAuto = false);
    bool Resume(bool isAuto = false);
    bool Volume(int v);
    bool Next(int skip);

    bool SetRepeat(int repeatMode);

    void OnStatusChanged(PLAYER_STATUS st);

    //    triggered events
    void OnDataUpdated(const char *res_id);

    //    attributes
    PLAYER_STATUS GetStatus() const;

    void SetMedialist(PtrMediaList &playList);
    PtrMediaList &GetMediaList();

    const txc_player_info_t &GetInfo();

    // interface of CPlayerAgent
    int AddMediaItem(_In_ PtrMedia &media);
    int UpdateMediaItem(const TXCA_PARAM_RESOURCE *item);
    int ReplaceMediaItem(const TXCA_PARAM_RESOURCE *item);
    int AddResponseData(int response_type, const char *response_data, _Out_ PtrMedia &media);

  private:
    friend class TXCPlayerManager;
    TXCPlayer(int id);
    TXCPlayer(int id, REPEAT_MODE repeatMode);

  private:
    void TriggerEvent(XWM_EVENT event, XWPARAM arg1, XWPARAM arg2);

  private:
    txc_player_info_t info_;
    PtrMediaList media_list_;

    int app_id_;
};
typedef TXCAutoPtr<TXCPlayer> PtrPlayer;

//  media manager, store media data, not media info
class TXCMediaCenter
{
  public:
    int AddPlayList(SESSION id, PtrMediaList &playList);
    size_t AddMedia(PtrMedia &media);
    bool RemoveMedia(const std::string &res_id);
    bool DecMediaTipCnt(const std::string &res_id);
    bool IsMediaNeedPlay(const std::string &res_id);
    std::string GenResourceId();

    template <class T>
    PtrMedia NewMedia(const std::string &res_id);

    PtrMedia GetMedia(const std::string &res_id);
    int ReadMedia(_In_ const char *res_id, _Out_ const void **data, _Out_ size_t *data_size, _In_ size_t offset);

    int TriggerMediaUpdated(const PtrMedia &media);
    
    void SetLastActiveTime();
    long GetLastActiveTime();
    void AddVoiceData(const char* data, int length);
    void ResetVoiceData();
    std::string GetVoiceData();

  private:
    friend class TXCServices;
    TXCMediaCenter();

  private:
    std::map<std::string, PtrMedia> map_media_;
    std::map<SESSION, PtrMediaList> map_playlist_;
    
    std::string m_strVoiceData;
    time_t m_lastActiveTime;
};

template <class T>
PtrMedia TXCMediaCenter::NewMedia(const std::string &res_id)
{
    PtrMedia media;
    T *ptr_media = new T(res_id);
    if (ptr_media)
    {
        media.reset(dynamic_cast<CMedia *>(ptr_media));

        map_media_[res_id] = media;
    }

    return media;
}

//  players manager
class TXCPlayerManager
{
  public:
    PtrPlayer NewPlayer(int app_id);
    PtrPlayer NewPlayer(int id, REPEAT_MODE repeatMode);
    PtrPlayer GetPlayer(int app_id);

  private:
    friend class TXCServices;
    TXCPlayerManager();

  private:
    //    ID => player
    std::map<int, PtrPlayer> vPlayers_;
};

#endif /* _AIAUDIO_PLAYER_HPP_ */
