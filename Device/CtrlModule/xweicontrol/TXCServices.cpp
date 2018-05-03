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
#include "TXCServices.hpp"
#include "AudioApp.hpp"
#include "logger.h"
#include "TXCMsgbox.hpp"

TXCAudioService::TXCAudioService()
{
}

void txc_xwei_msgbox_addmsg(txc_msg_info* msgInfo)
{
    if (NULL ==  msgInfo)
    {
        TLOG_ERROR("txc_xwei_msgbox_addmsg INVALID PARAMS.");
        return;
    }
    
    CTXCMsgbox::instance().AddMsgCache(msgInfo);
}

void TXCAudioService::Init()
{
    
}

SDK_API bool txc_add_processor(txc_event_processor processor)
{
    return TXCServices::instance()->GetMessageQueue()->AddProcessor(processor);
}
SDK_API void txc_remove_processor(txc_event_processor processor)
{
    TXCServices::instance()->GetMessageQueue()->RemoveProcessor(processor);
}

void txc_xwei_control_init(const struct txc_xwei_control *callback)
{
    TXCServices::instance()->GetAudioService()->Init();
    if (callback)
    {
        TXCServices::instance()->GetAppManager()->callback_ = *callback;
    }
}
TXCServices::TXCServices()
    : audio_service_(NULL), app_manager_(NULL), message_queue_(NULL), media_center_(NULL), player_manager_(NULL), audio_focus_manager_(NULL)
{
}
TXCServices::~TXCServices()
{
    if (player_manager_)
    {
        delete player_manager_;
    }
    if (media_center_)
    {
        delete media_center_;
    }
    if (message_queue_)
    {
        delete message_queue_;
    }
    if (app_manager_)
    {
        delete app_manager_;
    }
    if (audio_service_)
    {
        delete audio_service_;
    }
    if (audio_focus_manager_)
    {
        delete audio_focus_manager_;
    }
}

TXCPlayerManager *TXCServices::GetPlayerManager()
{
    if (!player_manager_)
    {
        player_manager_ = new TXCPlayerManager;
    }

    return player_manager_;
}

TXCMessageQueue *TXCServices::GetMessageQueue()
{
    if (!message_queue_)
    {
        message_queue_ = new TXCMessageQueue;
        message_queue_->Start();
    }

    return message_queue_;
}

TXCMediaCenter *TXCServices::GetMediaCenter()
{
    if (!media_center_)
    {
        media_center_ = new TXCMediaCenter;
    }

    return media_center_;
}

TXCAppManager *TXCServices::GetAppManager()
{
    if (!app_manager_)
    {
        app_manager_ = new TXCAppManager;
    }

    return app_manager_;
}

TXCAudioService *TXCServices::GetAudioService()
{
    if (!audio_service_)
    {
        audio_service_ = new TXCAudioService();
    }
    return audio_service_;
}

TXCAudioFocusManager *TXCServices::GetAudioFocusManager()
{
    if (!audio_focus_manager_)
    {
        audio_focus_manager_ = new TXCAudioFocusManager();
    }

    return audio_focus_manager_;
}
