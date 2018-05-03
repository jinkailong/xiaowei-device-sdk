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
#ifndef _AIAUDIO_TXCServices_HPP_
#define _AIAUDIO_TXCServices_HPP_

#include "Player.h"
#include "AudioApp.hpp"
#include "TXCAppManager.hpp"
#include "TXCMessageQueue.hpp"
#include "AudioFocusManager.hpp"

template <typename T>
class Singleton
{
  public:
    static T *instance()
    {
        if (!instance_)
        {
            instance_ = new T;
        }
        return instance_;
    }

  private:
    static T *instance_;
};
template <typename T>
T *Singleton<T>::instance_ = NULL;

class TXCServices
    : public Singleton<TXCServices>
{
  public:
    TXCAudioService *GetAudioService();
    TXCAppManager *GetAppManager();
    TXCMessageQueue *GetMessageQueue();
    TXCMediaCenter *GetMediaCenter();
    TXCPlayerManager *GetPlayerManager();
    TXCAudioFocusManager *GetAudioFocusManager();

  private:
    friend class Singleton<TXCServices>;
    TXCServices();
    ~TXCServices();

  private:
    TXCAudioService *audio_service_;
    TXCAppManager *app_manager_;
    TXCMessageQueue *message_queue_;
    TXCMediaCenter *media_center_;
    TXCPlayerManager *player_manager_;
    TXCAudioFocusManager *audio_focus_manager_;
};

#endif /* _AIAUDIO_TXCServices_HPP_ */
