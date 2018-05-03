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

#include "AppkitMsgBase.hpp"

#include "TXCAudio.h"
#include "TXCAudioCommon.h"
#include "TXCSkillsDefine.h"

#include <sstream>
#include <stdio.h>

#include "logger.h"
#include "Playlist.hpp"
#include "TXCServices.hpp"
#include "MediaText.hpp"
#include "MediaFile.hpp"
#include "MediaMusic.hpp"
#include "MediaTTS.hpp"
#include "MediaMsgPrompt.hpp"

#define RES_RING_URL      "http://qzonestyle.gtimg.cn/qzone/vas/opensns/res/doc/msg.ring.mp3"
#define RES_DONG_URL      "http://qzonestyle.gtimg.cn/qzone/vas/opensns/res/doc/dong.mp3"

static bool g_bPlayingMsg = false;  //当前是否正在播放消息
static bool g_bOldPlayingState = false; //killforce前的播放状态

AppkitMsgBase::AppkitMsgBase(int app_id)
: PlayerKit(app_id)
, m_nCurMsgId(0)
{
}

AppkitMsgBase::~AppkitMsgBase()
{
    
}

bool AppkitMsgBase::OnMessage(XWM_EVENT event, XWPARAM arg1, XWPARAM arg2)
{
    bool bHandled = PlayerKit::OnMessage(event, arg1, arg2);
    
    if (XWM_SETFOCUS == event) {
        if (g_bOldPlayingState) {
            g_bPlayingMsg = g_bOldPlayingState;
        }
        g_bOldPlayingState = false;
        TLOG_DEBUG("AppkitMsgbox XWM_SETFOCUS bPlayingMsg:%d", g_bPlayingMsg);
    }
    else if (XWM_KILLFOCUS == event) {
        g_bOldPlayingState = g_bPlayingMsg;
        g_bPlayingMsg = false;
        TLOG_DEBUG("AppkitMsgbox XWM_KILLFOCUS bPlayingMsg:%d", g_bPlayingMsg);
    }
    else if (XWM_PLAYER_STATUS_CHANGED == event) {
        TXC_PLAYER_STATE state = TXC_PLAYER_STATE(reinterpret_cast<long>(arg1));
        TLOG_DEBUG("AppkitMsgbox XWM_PLAYER_STATUS_CHANGED %d", state);
    }
    else if (XWM_PLAYER_STATUS_FINISH == event)
    {
        TLOG_DEBUG("AppkitMsgbox XWM_PLAYER_STATUS_FINISH");
        g_bPlayingMsg = false;
        if (!PlayNextPromptTone() && m_nCurMsgId > 0) //先处理提示音队列播放再处理消息播放,m_nCurMsgId >0 表明是消息播放结束了，可以尝试播放下一条未读消息
        {
            CTXCMsgbox::instance().SetMsgReaded(m_nCurMsgId);
            m_nCurMsgId = 0;
            CTXCMsgBase* pMsg = CTXCMsgbox::instance().GetNextUnReadMsg();
            if (pMsg)
                PlayMsg(pMsg);
        }
    }
    
    return bHandled;
}

bool AppkitMsgBase::AddMsgToMsgbox(CTXCMsgBase* pMsg)
{
    TLOG_DEBUG("AppkitMsgbox::AddMsgToMsgbox %u", pMsg->msgId);
    bool bAdd = CTXCMsgbox::instance().AddMsg(pMsg);
    
    // play notice or msg
    if (bAdd)
    {
        if (pMsg->isRecv)
        {
            // 活跃状态自动播放收到的消息(1分钟内有语音交互)，非活跃状态播放提示音
            if (TXCServices::instance()->GetMediaCenter()->GetLastActiveTime() <= 60) {
                TLOG_DEBUG("AppkitMsgbox::AddMsgToMsgbox m_bPlayingMsg:%d", g_bPlayingMsg);
                if (!g_bPlayingMsg)//正在播放消息的时候不处理新消息播放，等当前消息播放完成后调用下一条未读消息播放
                    PlayMsg(pMsg);
            } else {
                AddPromptToneQueue();
            }
        }
        else
        {
            AddPromptToneQueue(false);
        }
    }
    
    return bAdd;
}

void PromptToneMedia(PtrMedia& _media, bool isRecv)
{
    PtrMedia media;
    std::string strResId = TXCServices::instance()->GetMediaCenter()->GenResourceId();
    media = TXCServices::instance()->GetMediaCenter()->NewMedia<CMediaMusic>(strResId.c_str());
    if (media.get()) {
        CMediaMusic* mediaRing = dynamic_cast<CMediaMusic*>(media.get());
        if (mediaRing) {
            std::string strUrl = (isRecv ? RES_RING_URL : RES_RING_URL);
            mediaRing->Init(strUrl.c_str(), strUrl.c_str(), 0, 1);
        }
    }
    if (media.get()) {
        _media = media;
    }
}

void AppkitMsgBase::AddPromptToneQueue(bool isRecv /*= true*/)
{
    TLOG_DEBUG("AppkitMsgbox::AddPromptToneQueue isRecv:%d", isRecv);
    MsgPromptQueue::instance().AddPromptToneQueue(isRecv);
    
    if (g_bPlayingMsg)
        return;
    
    PlayNextPromptTone();
}

bool AppkitMsgBase::PlayNextPromptTone()
{
    bool isRecv = false;
    bool bRet = MsgPromptQueue::instance().GetPromptTone(isRecv);
    if (bRet)
    {
        PlayPromptTone(isRecv);
    }
    return bRet;
}

void AppkitMsgBase::PlayPromptTone(bool isRecv /*= true*/)
{
    TLOG_DEBUG("AppkitMsgbox::PlayPromptTone isRecv:%d", isRecv);
    g_bPlayingMsg = true;
    
    ClearPlayList();
    //新消息
    std::vector<txc_play_item_t> playlist;
    txc_play_item_t play_item = {0};
    play_item.count = 1;
    
    PtrMedia media;
    PromptToneMedia(media, isRecv);
    
    int src_index = player_->AddMediaItem(media);
    if (src_index >= 0)
    {
        play_item.group[0] = src_index;
    }
    playlist.push_back(play_item);
    
    playlist_->PushBack(playlist);
    
    // play
    TXCServices::instance()->GetAudioFocusManager()->RequestAudioFocus(app_id_, this, AUDIOFOCUS_GAIN_TRANSIENT);
    m_is_need_play = true;
}

void AppkitMsgBase::ClearPlayList()
{
    if (!player_ || !playlist_)
        return;
    
    PtrMediaList &media_list = player_->GetMediaList();
    size_t first_index = playlist_->Count();
    if (first_index > 0) {
        send_message(app_id_, XWM_STOP, 0, 0);
        send_message(app_id_, XWM_LIST_REMOVED, XWPARAM(0), XWPARAM(first_index));
        TLOG_DEBUG("AppkitMsgbox::ClearPlayList XWM_LIST_REMOVED");
        media_list->Clear();
        playlist_->Clear();
        first_index = 0;
    }
}

void AppkitMsgBase::PlayMsg(CTXCMsgBase* pMsg)
{
    g_bPlayingMsg = true;
    
    if (NULL == pMsg) {
        TLOG_ERROR("AppkitMsgbox::PlayMsg pMsg == NULL");
        PlayEndMsg();
        return;
    }
    TLOG_DEBUG("AppkitMsgbox::PlayMsg %s", pMsg->toString().c_str());
    
    // 记录当前播放的消息id
    m_nCurMsgId = pMsg->msgId;
    // 播放时先清空播放列表
    ClearPlayList();
    // 添加新播放顺序资源到播放列表
    AddMsgToPlayList(pMsg);
    // 标记消息已读
    //CTXCMsgbox::instance().SetMsgReaded(pMsg->msgId);
    // play
    TXCServices::instance()->GetAudioFocusManager()->RequestAudioFocus(app_id_, this, AUDIOFOCUS_GAIN);
    m_is_need_play = true;
}

// 播放消息前缀提示tts(xx点xx分收到xx的消息)
bool GenMsgPromptMedia(CTXCMsgBase* pMsg, PtrMedia& _media)
{
    std::string strResId = TXCServices::instance()->GetMediaCenter()->GenResourceId();
    PtrMedia media;
    media = TXCServices::instance()->GetMediaCenter()->NewMedia<CMediaMsgPrompt>(strResId.c_str());
    CMediaMsgPrompt* mediaMsgPrompt = dynamic_cast<CMediaMsgPrompt *>(media.get());
    if (mediaMsgPrompt)
    {
        std::stringstream ss;
        ss << pMsg->timestamp;
        std::string strDesc = ss.str();

        char szTinyId[100] = {0};
        snprintf(szTinyId, sizeof(szTinyId), "%llu", pMsg->uin_);        
        
        mediaMsgPrompt->Init(szTinyId, strDesc.c_str(), 1);
    }
    if (media.get())
    {
        _media = media;
    }
    return true;
}

bool MsgToMedia(CTXCMsgBase* pMsg, PtrMedia& _media)
{
    bool bSuc = false;
    std::string strResId = TXCServices::instance()->GetMediaCenter()->GenResourceId();
    PtrMedia media;
    if (pMsg->msgType == txc_msg_type_iot_text)
    {
        CTXCMsgText* pMsgText = dynamic_cast<CTXCMsgText*>(pMsg);
        if (pMsgText && !pMsgText->text.empty())
        {
            media = TXCServices::instance()->GetMediaCenter()->NewMedia<CMediaText>(strResId.c_str());
            CMediaText* mediaText = dynamic_cast<CMediaText*>(media.get());
            if (mediaText) {
                mediaText->Init(pMsgText->text.c_str(), pMsgText->text.c_str(), 1);
            }
        }
    }
    else if (txc_msg_type_iot_audio == pMsg->msgType)
    {
        CTXCMsgAudio* pMsgAudio = dynamic_cast<CTXCMsgAudio*>(pMsg);
        if (pMsgAudio) {
            media = TXCServices::instance()->GetMediaCenter()->NewMedia<CMediaFile>(strResId.c_str());
            CMediaFile* mediaFile = dynamic_cast<CMediaFile*>(media.get());
            if (mediaFile) {
                mediaFile->Init(TYPE_FILE, pMsgAudio->localUrl.c_str());
            }
        }
    }
    
    if (media.get())
    {
        _media = media;
        bSuc = true;
    }
    return bSuc;
}

void AppkitMsgBase::AddMsgToPlayList(CTXCMsgBase* pMsg)
{
    std::vector<txc_play_item_t> playlist;
    txc_play_item_t play_item = {0};
    
    //添加消息播放tts
    {
        PtrMedia media;
        if (GenMsgPromptMedia(pMsg, media))
        {
            int src_index = player_->AddMediaItem(media);
            if (src_index >= 0)
            {
                play_item.group[0] = src_index;
                play_item.count++;
                TLOG_DEBUG("AppkitMsgbox::AddMsgToPlayList Add msg tts. count:%d", play_item.count);
            }
        }
    }
    
    //添加消息内容(文本->tts 语音->localurl)
    {
        PtrMedia media;
        if (MsgToMedia(pMsg, media))
        {
            int src_index = player_->AddMediaItem(media);
            if (src_index >= 0)
            {
                play_item.group[1] = src_index;
                play_item.count++;
                TLOG_DEBUG("AppkitMsgbox::AddMsgToPlayList Add msg %u. count:%d", pMsg->msgId, play_item.count);
            }
        }
    }
    playlist.push_back(play_item);
    
    playlist_->PushBack(playlist);
}

// 播放没有更多消息的tts
void AppkitMsgBase::PlayEndMsg()
{
    ClearPlayList();
    //新消息
    std::vector<txc_play_item_t> playlist;
    txc_play_item_t play_item = {0};
    play_item.count = 1;
    
    CTXCMsgText msg;
    msg.text = "没有更多消息了";
    
    PtrMedia media;
    if (MsgToMedia(&msg, media))
    {
        int src_index = player_->AddMediaItem(media);
        if (src_index >= 0)
        {
            play_item.group[0] = src_index;
        }
        playlist.push_back(play_item);
    }
    
    playlist_->PushBack(playlist);
    
    // play
    TXCServices::instance()->GetAudioFocusManager()->RequestAudioFocus(app_id_, this, AUDIOFOCUS_GAIN);
    m_is_need_play = true;
}
