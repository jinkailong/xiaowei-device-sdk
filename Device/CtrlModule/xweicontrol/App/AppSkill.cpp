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
#include "AppSkill.hpp"
#include "Medialist.hpp"
#include "Playlist.hpp"
#include "Player.hpp"
#include "MediaTTS.hpp"
#include "MediaMusic.hpp"
#include "MediaText.hpp"
#include "TXCServices.hpp"
#include "PlayerControl.hpp"
#include "logger.h"
#include "OuterSkillMgr.h"
#include "CommonMgr.h"
#include "TXCSkillsDefineEx.h"
#include "AudioFocusManager.hpp"
#include "document.h"
#include "writer.h"
#include "stringbuffer.h"
#include <memory.h>
#include <sstream>
#include "Util.hpp"
#include "AudioApp.hpp"

AppSkill::AppSkill(int app_id)
    : app_id_(app_id)
{
}

PlayerKit::PlayerKit(int app_id)
    : AppSkill(app_id), m_is_need_play(false), m_isRecovery(false)
{
    playlist_.reset(new CPlaylist(REPEAT_SEQUENCE, app_id));
    control_.reset(new CPlayerControl(app_id, playlist_.get()));
    player_ = TXCServices::instance()->GetPlayerManager()->NewPlayer(app_id_, playlist_->GetRepeat());
}

PlayerKit::PlayerKit(int app_id, REPEAT_MODE repeat_mode)
    : AppSkill(app_id), m_is_need_play(false), m_isRecovery(false)
{
    playlist_.reset(new CPlaylist(repeat_mode, app_id));
    control_.reset(new CPlayerControl(app_id, playlist_.get()));
    player_ = TXCServices::instance()->GetPlayerManager()->NewPlayer(app_id_, playlist_->GetRepeat());
}

PlayerKit::~PlayerKit()
{
    TXCServices::instance()->GetAudioFocusManager()->AbandonAudioFocus(this);
}

// 预处理控制指令，每个TXCA_PARAM_RESPONSE都最多只有一个command，如果存在，肯定是在第一个资源。
// 在这里判断并处理掉控制指令，如果不能处理，返回false
bool PlayerKit::PreProcessResourceCommand(const TXCA_PARAM_RES_GROUP *v_groups, size_t count)
{
    if (v_groups && count > 0)
    {
        for (unsigned int i = 0; i < count; i++)
        {
            for (unsigned int j = 0; j < v_groups[i].resources_size; j++)
            {
                const TXCA_PARAM_RESOURCE *resource = v_groups[i].resources + j;
                if (txca_resource_command == resource->format)
                {
                    if (resource->id && resource->id[0])
                    {
                        int cmd_id = atoi(resource->id);
                        TLOG_DEBUG("sessionId=%d OnAiAudioRsp PlayerKit::PreProcessResourceCommand m_is_need_play=%d command_id=%d content=%s.",
                                   app_id_, m_is_need_play, cmd_id, (resource->content ? resource->content : ""));
                        switch (cmd_id)
                        {
                        case PROPERTY_ID_PLAY:
                            if(!m_is_need_play) {
                                return true;
                            }
                            TXCServices::instance()->GetAudioFocusManager()->RequestAudioFocus(app_id_, this, m_isRecovery ? AUDIOFOCUS_GAIN : AUDIOFOCUS_GAIN_TRANSIENT);
                            player_->Play();
                            break;
                        case PROPERTY_ID_NEXT:
                            if(!m_is_need_play) {
                                return true;
                            }
                            TXCServices::instance()->GetAudioFocusManager()->RequestAudioFocus(app_id_, this, m_isRecovery ? AUDIOFOCUS_GAIN : AUDIOFOCUS_GAIN_TRANSIENT);
                            player_->Next(1);
                            break;
                        case PROPERTY_ID_PREV:
                            if(!m_is_need_play) {
                                return true;
                            }
                            TXCServices::instance()->GetAudioFocusManager()->RequestAudioFocus(app_id_, this, m_isRecovery ? AUDIOFOCUS_GAIN : AUDIOFOCUS_GAIN_TRANSIENT);
                            player_->Next(-1);
                            break;
                        case PROPERTY_ID_PAUSE:
                        case PROPERTY_ID_STOP:
                        case PROPERTY_ID_EXIT:
                                if(!m_is_need_play) {
                                    return true;
                                }
                                player_->Pause();
                            break;
                        case PROPERTY_ID_REPEAT:
                            player_->Next(0);
                            break;
                        case PROPERTY_ID_PLAYMODE_LOOP:
                            player_->SetRepeat(REPEAT_LOOP);
                            break;
                        case PROPERTY_ID_PLAYMODE_ORDER:
                            player_->SetRepeat(REPEAT_SEQUENCE);
                            break;
                        case PROPERTY_ID_PLAYMODE_RANDOM:
                            player_->SetRepeat(REPEAT_RANDOM);
                            break;
                        case PROPERTY_ID_PLAYMODE_SINGLE:
                            player_->SetRepeat(REPEAT_SINGLE);
                            break;
                        case PROPERTY_ID_KEEP_SHARE: // TTS
                            if (CommonMgr.on_common_control)
                            {
                                CommonMgr.on_common_control(TYPE_SET_FAVORITE, resource->content);
                            }
                            break;
                        default: // 目前只有一个command并且肯定是在第一个资源。
                            return false;
                        }
                    }
                }
            }
        }
    }

    return true;
}

// 是否能处理通知类型的TXCA_PARAM_RESPONSE。一般都使用AppKitNotify来处理通知类型的响应。这类响应一般不需要更新屏幕上显示的播放资源。
bool PlayerKit::CanProcessNotify(const TXCA_PARAM_RESPONSE &cRsp)
{
    return false;
}

SESSION PlayerKit::GetSessionId()
{
    return app_id_;
}

// 这个场景的音频焦点变化
void PlayerKit::OnAudioFocusChange(int focus_change)
{
    TLOG_DEBUG("sessionId=%d PlayerKit::OnAudioFocusChange hint=%s", app_id_, Util::ToString((DURATION_HINT)focus_change).c_str());
    switch (focus_change)
    {
    case AUDIOFOCUS_GAIN:
        player_->Play(true);
        post_message(app_id_, XWM_VOLUME, (XWPARAM)100, 0);
        break;
    case AUDIOFOCUS_GAIN_TRANSIENT:
        player_->Play(true);
        post_message(app_id_, XWM_VOLUME, (XWPARAM)100, 0);
        break;
    case AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
        player_->Play(true);
        post_message(app_id_, XWM_VOLUME, (XWPARAM)100, 0);
        break;
    case AUDIOFOCUS_LOSS_TRANSIENT:
        // 可以暂停了
        player_->Pause(true);
        break;
    case AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
        player_->Play(true);
        post_message(app_id_, XWM_VOLUME, (XWPARAM)20, 0);
        break;
    case AUDIOFOCUS_LOSS:
        // 可以释放播放资源了
        {
            PtrMediaList &media_list = player_->GetMediaList();
            if (media_list->Count() > 0)
            {
                send_message(app_id_, XWM_STOP, 0, 0);
                send_message(app_id_, XWM_LIST_REMOVED, XWPARAM(0), XWPARAM(playlist_->Count()));
                TLOG_DEBUG("sessionId=%d PlayerKit::AUDIOFOCUS_LOSS XWM_LIST_REMOVED", app_id_);

                media_list->Clear();
                playlist_->Clear();
            }
            m_is_need_play = false;

            control_->ReportPlayState(txca_playstate_finished);
        }
        break;
    default:
        break;
    }
}

/*
 1.通知类型的过滤
 2.预处理控制指令
 3.根据play_behavior 来决定如何处理资源列表
    a.如果是txca_playlist_replace_all类型，直接替换列表并且中断播放，并且通知UI层需要打开界面，并且需要清楚旧的，添加新的播放列表到UI。
    b.如果是txca_playlist_enqueue_back类型，添加新的播放列表到UI的尾部。
    c.如果是txca_playlist_enqueue_front类型，添加新的播放列表到UI的顶部。
    d.如果是txca_playlist_update_enqueue类型，替换部分字段并且不中断播放。
    e.如果是txca_playlist_replace_enqueue类型，直接替换列表并且不中断播放，并且通知UI层更新列表的详情。
    f.如果是txca_playlist_remove类型，从当前播放列表中移除这些资源。
 */
bool PlayerKit::OnAiAudioRsp(const TXCA_PARAM_RESPONSE &cRsp)
{
    TLOG_DEBUG("sessionId=%d %s::OnAiAudioRsp.", app_id_, GetClassName().c_str());
    bool handled = false;

    if (!PreProcessResourceCommand(cRsp.resource_groups, cRsp.resource_groups_size))
    {
        TLOG_DEBUG("sessionId=%d %s::OnAiAudioRsp finished, because of rsp has unknow command.", app_id_, GetClassName().c_str());
        return handled;
    }

    // 如果只有一个操作指令，就不要再继续了
    if(cRsp.resource_groups_size == 1 && cRsp.resource_groups[0].resources_size == 1 && cRsp.resource_groups[0].resources[0].format == txca_resource_command)
    {
        TLOG_DEBUG("sessionId=%d %s::OnAiAudioRsp finished, because of rsp has only a command.", app_id_, GetClassName().c_str());
        return true;
    }
    
    if(!Util::IsVaild(cRsp)) {
        // 这里不处理没任何资源的请求
        TLOG_DEBUG("sessionId=%d %s::OnAiAudioRsp finished, because of rsp has not resource without command.", app_id_, GetClassName().c_str());
        return handled;
    }
    
    if(cRsp.is_notify && !CanProcessNotify(cRsp))
    {
        TLOG_DEBUG("sessionId=%d %s::OnAiAudioRsp finished, goto notify app.", app_id_, GetClassName().c_str());
        return CAudioApp::notify_app_->OnAiAudioRsp(cRsp);
    }
    
    ProcessPlayList(cRsp, handled);
    
    return handled;
}

void PlayerKit::ProcessPlayList(const TXCA_PARAM_RESPONSE &cRsp, bool& handled)
{
    TLOG_DEBUG("sessionId=%d OnAiAudioRsp %s::ProcessPlayList resource_groups_size=%d play_behavior=%d.", app_id_, GetClassName().c_str(), cRsp.resource_groups_size, cRsp.play_behavior);

    // 先判断下发的资源是不是一个提示(例如：收藏成功、当前在播放xxx)
    bool isNotify = cRsp.is_notify;
    
    if (cRsp.play_behavior == txca_playlist_replace_all || cRsp.play_behavior == txca_playlist_enqueue_back || cRsp.play_behavior == txca_playlist_enqueue_front)
    {
        
        PtrMediaList &media_list = player_->GetMediaList();
        size_t first_index = cRsp.play_behavior == txca_playlist_enqueue_front ? 0 : playlist_->Count();
        
        if (first_index > 0 && cRsp.play_behavior == txca_playlist_replace_all)
        {
            send_message(app_id_, XWM_STOP, 0, 0);
            send_message(app_id_, XWM_LIST_REMOVED, XWPARAM(0), XWPARAM(first_index));
            TLOG_DEBUG("sessionId=%d OnAiAudioRsp %s::ProcessPlayList XWM_LIST_REMOVED", app_id_, GetClassName().c_str());
            
            media_list->Clear();
            playlist_->Clear();
            first_index = 0;
        }
        
        size_t added_count = 0; // 媒体资源新增个数
        size_t org_playlist_count = playlist_->Count();
        size_t new_playlist_count = 0;
        PtrMedia media_album;
        int index_album = -1;
        
        if (cRsp.response_data)
        {
            int index = player_->AddResponseData(cRsp.response_type, cRsp.response_data, media_album);
            if (index >= 0 && media_album.get())
            {
                index_album = index;
                added_count++;
            }
            TLOG_DEBUG("sessionId=%d OnAiAudioRsp %s::ProcessPlayList index_album=%u added_count=%lu\n", app_id_, GetClassName().c_str(), index_album, added_count);
        }

        added_count += AddList(cRsp, media_album, index_album);
        new_playlist_count = playlist_->Count();
        TLOG_DEBUG("sessionId=%d OnAiAudioRsp %s::ProcessPlayList after AddList index_album=%d added_count=%d new_playlist_count=%d org_playlist_count=%d", app_id_, GetClassName().c_str(), index_album, added_count, new_playlist_count, org_playlist_count);

        //  如果是第一次在某个场景新增资源，那么需要添加封面媒体资源通知UI层
        if (!isNotify && index_album >= 0 && media_album.get())
        {
            send_message(app_id_, XWM_ALBUM_ADDED, XWPARAM((long)index_album), XWPARAM(0));
        }
        //  media list added
        //if (0 < added_count ) {
        //    send_message(app_id_, XWM_MEDIA_ADDED, XWPARAM(first_index), XWPARAM(added_count));
        //}
        //  UI层playlist新增播放资源
        if (new_playlist_count > org_playlist_count)
        {
            send_message(app_id_, XWM_LIST_ADDED, XWPARAM(first_index), XWPARAM(new_playlist_count - org_playlist_count));
        }
        
        //  play
        TLOG_DEBUG("sessionId=%d OnAiAudioRsp %s::ProcessPlayList goto play. player_status=%d", app_id_, GetClassName().c_str(), player_->GetStatus());
        if (cRsp.play_behavior == txca_playlist_replace_all)
        {
            m_isRecovery = cRsp.is_recovery;
        }
        
        if (org_playlist_count <= 0 && new_playlist_count > org_playlist_count)
        {
            DURATION_HINT hint = AUDIOFOCUS_GAIN;
            if (isNotify)
            {
                hint = AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;
            }
            else if (!cRsp.is_recovery)
            {
                hint = AUDIOFOCUS_GAIN_TRANSIENT;
            }
            TXCServices::instance()->GetAudioFocusManager()->RequestAudioFocus(app_id_, this, hint);
            m_is_need_play = true;
        }
        
        handled = true;
    }
    else if (cRsp.play_behavior == txca_playlist_update_enqueue)
    {
        std::vector<PtrMedia> ids;
        size_t updated_count = UpdateListItem(cRsp, ids);
        
        if (updated_count > 0)
        {
            CAutoBuffer<const txc_media_t *> msg_list(updated_count);
            int i = 0;
            for(std::vector<PtrMedia>::iterator iter = ids.begin(); iter != ids.end(); iter++) {
                msg_list[i++] = &iter->get()->GetInfo();
            }
            
            send_message(app_id_, XWM_LIST_UPDATED, XWPARAM(msg_list.Get()), XWPARAM(ids.size()));
            handled = true;
        }
    }
    else if (cRsp.play_behavior == txca_playlist_replace_enqueue)
    {
        std::vector<PtrMedia> ids;
        size_t updated_count = ReplaceListItem(cRsp, ids);
        
        if (updated_count > 0)
        {
            CAutoBuffer<const txc_media_t *> msg_list(updated_count);
            int i = 0;
            for(std::vector<PtrMedia>::iterator iter = ids.begin(); iter != ids.end(); iter++) {
                msg_list[i++] = &iter->get()->GetInfo();
            }
            
            send_message(app_id_, XWM_LIST_UPDATED, XWPARAM(msg_list.Get()), XWPARAM(ids.size()));
            handled = true;
        }
    }
    else if (cRsp.play_behavior == txca_playlist_remove)
    {
        std::vector<PtrMedia> ids;
        size_t removed_count = RemoveListItem(cRsp, ids);

        if (removed_count > 0)
        {
            CAutoBuffer<const txc_media_t *> msg_list(removed_count);
            int i = 0;
            for(std::vector<PtrMedia>::iterator iter = ids.begin(); iter != ids.end(); iter++) {
                msg_list[i++] = &iter->get()->GetInfo();
            }
            send_message(app_id_, XWM_MEDIA_REMOVED, XWPARAM(msg_list.Get()), XWPARAM(ids.size()));
        }
        handled = true;
    }
    
    OnSupplementRequest(cRsp, handled);
}

void PlayerKit::OnSupplementRequest(const TXCA_PARAM_RESPONSE &cRsp, bool& bHandled)
{
    if (cRsp.context.speak_timeout > 0)
    {
        TLOG_DEBUG("sessionId=%d OnAiAudioRsp %s::OnSupplementRequest speak_timeout=%d", app_id_, GetClassName().c_str(), cRsp.context.speak_timeout);
    
        send_message(app_id_, XWM_SUPPLEMENT_REQUEST, XWPARAM((long)cRsp.context.speak_timeout), XWPARAM(&cRsp));
        bHandled = true;
    }
}

std::string PlayerKit::GetClassName()
{
    return "PlayerKit";
}

// 将响应中的资源都加入列表，并为需要UI展示(一般TTS是不需要展示的)的album信息赋值
size_t PlayerKit::AddList(const TXCA_PARAM_RESPONSE &cRsp, _Out_ PtrMedia &album, _Out_ int &album_index)
{
    TLOG_DEBUG("sessionId=%d OnAiAudioRsp %s::AddList resource_groups_size=%d", app_id_, GetClassName().c_str(), cRsp.resource_groups_size);
    bool isUp = cRsp.play_behavior == txca_playlist_enqueue_front;
    if (!isUp)
    {
        // 音乐只有第一次需要往上拉取历史记录，所以忽略之后下发的hasMore字段
        InitList(cRsp.has_more_playlist);
    }
    
    bool isNeedAlbum = !isUp && (playlist_.get()->Count() == 0);
    std::vector<txc_play_item_t> playlist;
    size_t media_add_count = 0;
    
    for (size_t i = 0; i < cRsp.resource_groups_size; i++)
    {
        size_t res_count = cRsp.resource_groups[i].resources_size;
        txc_play_item_t play_item = {0};
        
        bool bHasMedia = false;
        int nIndex = 0;
        for (size_t j = 0; j < res_count; j++)
        {
            const TXCA_PARAM_RESOURCE *item = cRsp.resource_groups[i].resources + j;
            PtrMedia media;
            if (ResToMedia(item, media))
            {
                int src_index = player_->AddMediaItem(media);
                if (src_index >= 0)
                {
                    play_item.group[nIndex] = src_index;
                    play_item.count++;
                    
                    if (isNeedAlbum && album_index < 0 && CanBeAlbum(media))
                    {
                        album_index = src_index;
                        album = media;
                    }
                    
                    ++media_add_count;
                }
                
                bHasMedia = true;
                nIndex++;
            }
        }
        
        if (bHasMedia)
            playlist.push_back(play_item);
    }
    
    if (isUp)
    {
        playlist_->PushFront(playlist);
    }
    else
    {
        playlist_->PushBack(playlist);
    }
    
    return media_add_count;
}

bool PlayerKit::CanBeAlbum(const PtrMedia media)
{
    return true;
}

// 将TXCA_PARAM_RESOURCE 转为 Media
bool PlayerKit::ResToMedia(const TXCA_PARAM_RESOURCE *item, _Out_ PtrMedia &_media)
{
    PtrMedia temp_media;
    bool succ = false;

    if (txca_resource_tts == item->format && item->id)
    {
        const char *res_id = item->id;
        temp_media = TXCServices::instance()->GetMediaCenter()->GetMedia(res_id);
        if (!temp_media.get())
        {
            temp_media = TXCServices::instance()->GetMediaCenter()->NewMedia<CMediaTTS>(res_id);

            CMediaTTS *mediaTTS = dynamic_cast<CMediaTTS *>(temp_media.get());
            if (mediaTTS)
            {
                mediaTTS->Init(item->content, item->play_count);
            }
        }
    }
    else if (txca_resource_url == item->format)
    {
        std::string strResId;
        if (item->id != NULL)
        {
            strResId = item->id;
        }
        else
        {
            strResId = TXCServices::instance()->GetMediaCenter()->GenResourceId();
        }
        
        temp_media = TXCServices::instance()->GetMediaCenter()->GetMedia(strResId);
        if (!temp_media.get())
        {
            temp_media = TXCServices::instance()->GetMediaCenter()->NewMedia<CMediaMusic>(strResId);

            CMediaMusic *mediaMusic = dynamic_cast<CMediaMusic *>(temp_media.get());
            if (mediaMusic)
            {
                mediaMusic->Init(item->content, item->extend_buffer, item->offset, item->play_count);
            }
        }
    }
    else if (item->format == txca_resource_text)
    {
        std::string strResId;
        if (item->id != NULL)
        {
            strResId = item->id;
        }
        else
        {
            strResId = TXCServices::instance()->GetMediaCenter()->GenResourceId();
        }

        temp_media = TXCServices::instance()->GetMediaCenter()->GetMedia(strResId);
        if (!temp_media.get())
        {
            temp_media = TXCServices::instance()->GetMediaCenter()->NewMedia<CMediaText>(strResId);

            CMediaText *mediaText = dynamic_cast<CMediaText *>(temp_media.get());
            if (mediaText)
            {
                mediaText->Init(item->content, item->extend_buffer, item->play_count);
                if (item->play_count == 1) {
                    mediaText->Update(TYPE_TTS_TEXT_TIP);
                }
            }
        }
    }

    if (temp_media.get())
    {
        _media = temp_media;
        succ = true;
    }

    return succ;
}
// 更新元素，一般是更新url和清晰度等信息
size_t PlayerKit::UpdateListItem(const TXCA_PARAM_RESPONSE &cRsp, std::vector<PtrMedia> &list)
{
    PtrMediaList &media_list = player_->GetMediaList();
    size_t list_count = media_list->Count();

    if (list_count <= 0 || cRsp.resource_groups_size <= 0 || cRsp.resource_groups[0].resources_size <= 0)
    {
        return 0;
    }
    
    size_t update_count = 0;

    for (size_t i = 0; i < cRsp.resource_groups_size; i++)
    {
        for (size_t j = 0; j < cRsp.resource_groups[i].resources_size; j++)
        {
            const TXCA_PARAM_RESOURCE *item = cRsp.resource_groups[i].resources + j;
            if (player_->UpdateMediaItem(item) >= 0)
            {
                int media_index = media_list->Find(item->id);
                if(media_index != -1) {
                    PtrMedia media = media_list->Get(media_index);
                    list.push_back(media);
                }
                update_count++;
            }
        }
    }

    return update_count;
}

size_t PlayerKit::ReplaceListItem(const TXCA_PARAM_RESPONSE &cRsp, std::vector<PtrMedia> &list)
{
    PtrMediaList &media_list = player_->GetMediaList();
    size_t list_count = media_list->Count();

    if (list_count <= 0 || cRsp.resource_groups_size <= 0 || cRsp.resource_groups[0].resources_size <= 0)
    {
        return 0;
    }

    size_t replace_count = 0;

    for (size_t i = 0; i < cRsp.resource_groups_size; i++)
    {
        for (size_t j = 0; j < cRsp.resource_groups[i].resources_size; j++)
        {
            const TXCA_PARAM_RESOURCE *item = cRsp.resource_groups[i].resources + j;
            if (player_->ReplaceMediaItem(item) >= 0)
            {
                int media_index = media_list->Find(item->id);
                if(media_index != -1) {
                    PtrMedia media = media_list->Get(media_index);
                    list.push_back(media);
                }
                replace_count++;
            }
        }
    }

    return replace_count;
}

size_t PlayerKit::RemoveListItem(const TXCA_PARAM_RESPONSE &cRsp, std::vector<PtrMedia> &list)
{
    PtrMediaList &media_list = player_->GetMediaList();
    size_t list_count = media_list->Count();
    
    if (list_count <= 0 || cRsp.resource_groups_size <= 0 || cRsp.resource_groups[0].resources_size <= 0)
    {
        return 0;
    }
    size_t remove_count = 0;
    
    for (size_t i = 0; i < cRsp.resource_groups_size; i++)
    {
        for (size_t j = 0; j < cRsp.resource_groups[i].resources_size; j++)
        {
            const TXCA_PARAM_RESOURCE *item = cRsp.resource_groups[i].resources + j;
            bool removed = playlist_->Remove(item->id);
            if(removed){
                int media_index = media_list->Find(item->id);
                if(media_index != -1) {
                    PtrMedia media = media_list->Get(media_index);
                    list.push_back(media);
                    media_list->Remove(media_index);
                    remove_count++;
                }
            }
        }
    }
    return remove_count;
}
void PlayerKit::InitList(bool has_more)
{
    PtrMediaList mediaList = player_->GetMediaList();
    txc_playlist_t playListInfo = mediaList->GetInfo();
    playListInfo.hasMore = has_more;
    mediaList->SetInfo(&playListInfo);
}

// 处理播放结束等消息。
bool PlayerKit::OnMessage(XWM_EVENT event, XWPARAM arg1, XWPARAM arg2)
{
    bool handled = control_->OnMessage(event, arg1, arg2);

    if (XWM_PLAYER_STATUS_FINISH == event)
    {
        PtrMediaList &media_list = player_->GetMediaList();
        if (media_list->Count() > 0)
        {
            send_message(app_id_, XWM_STOP, 0, 0);
            send_message(app_id_, XWM_LIST_REMOVED, XWPARAM(0), XWPARAM(playlist_->Count()));
            TLOG_DEBUG("sessionId=%d OnAiAudioRsp %s::OnMessage XWM_LIST_REMOVED", app_id_, GetClassName().c_str());
            media_list->Clear();
            playlist_->Clear();
        }
        TXCServices::instance()->GetAudioFocusManager()->AbandonAudioFocus(this);
    }

    return handled;
}

AppKitMusic::AppKitMusic(int app_id)
    : PlayerKit(app_id, REPEAT_LOOP)
{
}

bool AppKitMusic::CanBeAlbum(const PtrMedia media)
{
    CMedia* media_ = media.get();
    if(media_) {
        return media_->GetInfo().type == TYPE_MUSIC_URL;
    } else {
        return false;
    }
}


std::string AppKitMusic::GetClassName()
{
    return "AppKitMusic";
}

AppKitFM::AppKitFM(int app_id)
    : PlayerKit(app_id, REPEAT_SEQUENCE)
{
}


std::string AppKitFM::GetClassName()
{
    return "AppKitFM";
}

bool AppKitFM::CanBeAlbum(const PtrMedia media)
{
    CMedia* media_ = media.get();
    if(media_) {
        return media_->GetInfo().type == TYPE_MUSIC_URL;
    } else {
        return false;
    }
}

AppKitNew::AppKitNew(int app_id)
    : PlayerKit(app_id, REPEAT_SEQUENCE)
{
}


std::string AppKitNew::GetClassName()
{
    return "AppKitFM";
}

bool AppKitNew::ResToMedia(const TXCA_PARAM_RESOURCE *item, const std::string &description, _Out_ PtrMedia &_media)
{
    bool isSuc = false;
    PtrMedia temp_media;

    if (txca_resource_tts == item->format && item->id)
    {
        const char *res_id = item->id;
        temp_media = TXCServices::instance()->GetMediaCenter()->GetMedia(res_id);
        if (!temp_media.get())
        {
            temp_media = TXCServices::instance()->GetMediaCenter()->NewMedia<CMediaTTS>(res_id);

            CMediaTTS *mediaTTS = dynamic_cast<CMediaTTS *>(temp_media.get());
            if (mediaTTS)
            {
                mediaTTS->Init(item->content, item->play_count);
            }
        }
    }
    else if (txca_resource_url == item->format && item->id)
    {
        const char *res_id = item->id;
        temp_media = TXCServices::instance()->GetMediaCenter()->GetMedia(res_id);
        if (!temp_media.get())
        {
            temp_media = TXCServices::instance()->GetMediaCenter()->NewMedia<CMediaMusic>(res_id);

            CMediaMusic *media_music = dynamic_cast<CMediaMusic *>(temp_media.get());
            if (media_music)
            {
                media_music->Init(item->content, description.c_str(), item->offset, item->play_count);
            }
            
            if (item->play_count == 1) {
                media_music->Update(TYPE_MUSIC_URL_TIP);
            }
        }
    }
    else if (txca_resource_text == item->format && item->id)
    {
        const char *res_id = item->id;
        temp_media = TXCServices::instance()->GetMediaCenter()->GetMedia(res_id);
        if (!temp_media.get())
        {
            temp_media = TXCServices::instance()->GetMediaCenter()->NewMedia<CMediaText>(res_id);

            CMediaText *media_text = dynamic_cast<CMediaText *>(temp_media.get());
            if (media_text)
            {
                media_text->Init(item->content, description.c_str(), item->play_count);
            }
        }
    }

    if (temp_media.get())
    {
        _media = temp_media;
        isSuc = true;
    }

    return isSuc;
}

size_t AppKitNew::AddList(const TXCA_PARAM_RESPONSE &cRsp, PtrMedia &album, int &album_index)
{
    TLOG_DEBUG("sessionId=%d OnAiAudioRsp %s::AddList resource_groups_size=%d", app_id_, GetClassName().c_str(), cRsp.resource_groups_size);
    InitList(cRsp.has_more_playlist);

    size_t media_count = 0;
    // TODO: 新闻场景的列表资源有多种，可能还需要适配
    bool is_url_list = (cRsp.resource_groups_size > 1);

    if (is_url_list)
    {
        std::vector<txc_play_item_t> playlist;
        std::map<std::string, std::string> id2NewInfo;

        if (cRsp.response_data)
        {
            rapidjson::Document json_doc;
            
            json_doc.Parse(cRsp.response_data);
            if (!json_doc.HasParseError()) {
                rapidjson::Value &data = json_doc["data"];
                if (data.IsArray()) {
                    for (unsigned int i = 0; i < data.Size(); i++) {
                        rapidjson::Value & originItem = data[i];
                        assert(originItem.IsObject());
                        
                        rapidjson::StringBuffer item;
                        rapidjson::Writer<rapidjson::StringBuffer> writer(item);
                        writer.StartObject();
                        
                        if (originItem.HasMember("id")) {
                            std::string idStr = originItem["id"].GetString();
                            writer.Key("playId");
                            writer.String(idStr.c_str());
                        }
                        
                        if (originItem.HasMember("source")) {
                            std::string sourceStr = originItem["source"].GetString();
                            writer.Key("artist");
                            writer.String(sourceStr.c_str());
                        }
                        
                        if (originItem.HasMember("title")) {
                            std::string titleStr = originItem["title"].GetString();
                            writer.Key("name");
                            writer.String(titleStr.c_str());
                        }
                        
                        if (originItem.HasMember("summary")) {
                            std::string summaryStr = originItem["summary"].GetString();
                            writer.Key("album");
                            writer.String(summaryStr.c_str());
                        }
                        
                        if (originItem.HasMember("pic_url")) {
                            std::string picUrlStr = originItem["pic_url"].GetString();
                            writer.Key("cover");
                            writer.String(picUrlStr.c_str());
                        }
                        
                        if (originItem.HasMember("publish_time")) {
                            std::string pubTimeStr = originItem["publish_time"].GetString();
                            writer.Key("publishTime");
                            writer.String(pubTimeStr.c_str());
                        }
                        
                        writer.EndObject();
                        
                        if (originItem.HasMember("id")) {
                            std::string idStr = originItem["id"].GetString();
                            id2NewInfo[idStr] = item.GetString();
                        }
                    }
                }
            }
        }

        for (size_t i = 0; i < cRsp.resource_groups_size; i++)
        {
            size_t resLength = cRsp.resource_groups[i].resources_size;
            txc_play_item_t play_item = {0};
            play_item.count = (int)resLength;
            for (size_t j = 0; j < resLength; j++)
            {
                const TXCA_PARAM_RESOURCE *item = cRsp.resource_groups[i].resources + j;
                PtrMedia media;
                if (ResToMedia(item, id2NewInfo[item->id], media))
                {
                    int src_index = player_->AddMediaItem(media);
                    if (src_index >= 0)
                    {
                        play_item.group[j] = src_index;

                        if (album_index < 0 && (media.get()->GetInfo().type == TYPE_MUSIC_URL
                                                || media.get()->GetInfo().type == TYPE_TTS_TEXT))
                        {
                            album_index = src_index;
                            album = media;
                        }
                        ++media_count;
                    }
                }
            }
            playlist.push_back(play_item);
        }

        playlist_->PushBack(playlist);
    }
    else
    {
        media_count = PlayerKit::AddList(cRsp, album, album_index);
    }

    return media_count;
}

AppKitCommon::AppKitCommon(int app_id)
    : PlayerKit(app_id)
{
}

std::string AppKitCommon::GetClassName()
{
    return "AppKitCommon";
}

AppKitNotify::AppKitNotify(int app_id)
    : PlayerKit(app_id)
{
}

std::string AppKitNotify::GetClassName()
{
    return "AppKitNotify";
}

bool AppKitNotify::PreProcessResourceCommand(const TXCA_PARAM_RES_GROUP *v_groups, size_t count)
{
    return true;
}

bool AppKitNotify::CanProcessNotify(const TXCA_PARAM_RESPONSE &cRsp)
{
    return true;
}

bool AppKitNotify::OnAiAudioRsp(const TXCA_PARAM_RESPONSE &cRsp)
{
    return PlayerKit::OnAiAudioRsp(cRsp);
}

AppKitGlobal::AppKitGlobal(int app_id)
    : PlayerKit(app_id)
{
}
bool AppKitGlobal::OnAiAudioRsp(const TXCA_PARAM_RESPONSE &cRsp)
{
    return PlayerKit::OnAiAudioRsp(cRsp);
}

std::string AppKitGlobal::GetClassName()
{
    return "AppKitGlobal";
}

bool AppKitGlobal::PreProcessResourceCommand(const TXCA_PARAM_RES_GROUP *v_groups, size_t count)
{
    if (v_groups && count > 0)
    {
        for (unsigned int i = 0; i < count; i++)
        {
            for (unsigned int j = 0; j < count; ++j)
            {
                const TXCA_PARAM_RESOURCE *resource = v_groups[i].resources + j;
                if (txca_resource_command == resource->format)
                {
                    if (resource->id && resource->id[0])
                    {
                        int cmd_id = atoi(resource->id);
                        TLOG_DEBUG("sessionId=%d OnAiAudioRsp AppKitGlobal::PreProcessResourceCommand m_is_need_play=%d command_id=%d content=%s.",
                                   app_id_, m_is_need_play, cmd_id, (resource->content ? resource->content : ""));
                        switch (cmd_id)
                        {
                        case PROPERTY_ID_VOLUME_DEC:
                        {
                            float volume = 0.1f;
                            if (resource->content)
                            {
                                volume = (float)strtof(resource->content, NULL);
                            }

                            if (CommonMgr.on_common_control)
                            {
                                rapidjson::StringBuffer str;
                                rapidjson::Writer<rapidjson::StringBuffer> writer(str);
                                writer.StartObject();
                                writer.Key("isIncrement");
                                writer.Bool(true);
                                writer.Key("value");
                                writer.Double(-volume);
                                writer.EndObject();
                                
                                CommonMgr.on_common_control(TYPE_VOLUME_SET, str.GetString());
                            }
                        }
                        break;
                        case PROPERTY_ID_VOLUME_INC:
                        {
                            float volume = 0.1f;
                            if (resource->content)
                            {
                                volume = (float)strtof(resource->content, NULL);
                            }
                            if (CommonMgr.on_common_control)
                            {
                                rapidjson::StringBuffer str;
                                rapidjson::Writer<rapidjson::StringBuffer> writer(str);
                                writer.StartObject();
                                writer.Key("isIncrement");
                                writer.Bool(true);
                                writer.Key("value");
                                writer.Double(volume);
                                writer.EndObject();
                                
                                CommonMgr.on_common_control(TYPE_VOLUME_SET, str.GetString());
                            }
                        }
                        break;
                        case PROPERTY_ID_VOLUME_SET:
                        {
                            if (resource->content)
                            {
                                float volume = (float)strtof(resource->content, NULL);
                                if (CommonMgr.on_common_control)
                                {
                                    rapidjson::StringBuffer str;
                                    rapidjson::Writer<rapidjson::StringBuffer> writer(str);
                                    writer.StartObject();
                                    writer.Key("isIncrement");
                                    writer.Bool(false);
                                    writer.Key("value");
                                    writer.Double(volume);
                                    writer.EndObject();
                                    
                                    CommonMgr.on_common_control(TYPE_VOLUME_SET, str.GetString());
                                }
                            }
                        }
                        break;
                        case PROPERTY_ID_VOLUME_MAX:
                            if (CommonMgr.on_common_control)
                            {
                                rapidjson::StringBuffer str;
                                rapidjson::Writer<rapidjson::StringBuffer> writer(str);
                                writer.StartObject();
                                writer.Key("isIncrement");
                                writer.Bool(false);
                                writer.Key("value");
                                writer.Int(100);
                                writer.EndObject();
                                
                                CommonMgr.on_common_control(TYPE_VOLUME_SET, str.GetString());
                            }
                            break;
                        case PROPERTY_ID_VOLUME_MIN:
                            if (CommonMgr.on_common_control)
                            {
                                rapidjson::StringBuffer str;
                                rapidjson::Writer<rapidjson::StringBuffer> writer(str);
                                writer.StartObject();
                                writer.Key("isIncrement");
                                writer.Bool(false);
                                writer.Key("value");
                                writer.Int(0);
                                writer.EndObject();

                                CommonMgr.on_common_control(TYPE_VOLUME_SET, str.GetString());
                            }
                            break;
                        case PROPERTY_ID_KEEP_SILENCE:
                            if (resource->content)
                            {
                                int mode = (int)strtol(resource->content, NULL, 0);
                                if (CommonMgr.on_common_control)
                                {
                                    rapidjson::StringBuffer str;
                                    rapidjson::Writer<rapidjson::StringBuffer> writer(str);
                                    writer.StartObject();
                                    writer.Key("silence");
                                    writer.Bool(mode == 0);
                                    writer.EndObject();

                                    CommonMgr.on_common_control(TYPE_VOLUME_SILENCE, str.GetString());
                                }
                            }
                            break;
                        case PROPERTY_ID_UPLOAD_LOG:
                            if (CommonMgr.on_common_control)
                            {
                                CommonMgr.on_common_control(TYPE_UPLOAD_LOG, resource->content);
                            }
                            break;
                        case PROPERTY_ID_ERROR_FEEDBACK:
                            if (CommonMgr.on_common_control)
                            {
                                CommonMgr.on_common_control(TYPE_FEED_BACK_ERROR, resource->content);
                            }
                            break;
                        case PROPERTY_ID_FETCH_DEVICE_INFO:
                            if (CommonMgr.on_common_control)
                            {
                                rapidjson::StringBuffer str;
                                rapidjson::Writer<rapidjson::StringBuffer> writer(str);
                                writer.StartObject();
                                writer.Key("type");
                                writer.String(resource->content);
                                writer.EndObject();
                                
                                CommonMgr.on_common_control(TYPE_FETCH_DEVICE_INFO, str.GetString());
                            }
                            break;
                        default:
                            break;
                        }
                    }
                }
            }
        }
    }
    return true;
}

OuterSkill::OuterSkill(int app_id)
    : AppSkill(app_id)
{
}

bool OuterSkill::OnAiAudioRsp(const TXCA_PARAM_RESPONSE &cRsp)
{
    bool handled = false;
    if (outer_skill_callback.send_txca_response)
    {
        handled = outer_skill_callback.send_txca_response(app_id_, const_cast<TXCA_PARAM_RESPONSE *>(&cRsp));
    }
    return handled;
}

bool OuterSkill::OnMessage(XWM_EVENT event, XWPARAM arg1, XWPARAM arg2)
{
    bool handled = false;

    return handled;
}


std::string OuterSkill::GetClassName()
{
    return "OuterSkill";
}

SESSION OuterSkill::GetSessionId()
{
    return app_id_;
}

bool OuterSkill::PreProcessResourceCommand(const TXCA_PARAM_RES_GROUP *v_groups, size_t count)
{
    return true;
}

bool OuterSkill::CanProcessNotify(const TXCA_PARAM_RESPONSE &cRsp)
{
    return false;
}
