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
#include <assert.h>
#include <memory.h>
#include "PlayerControl.hpp"
#include "Playlist.hpp"
#include "TXCServices.hpp"
#include "TXCAppManager.hpp"
#include "logger.h"
#include "Util.hpp"


CPlayerControl::CPlayerControl(SESSION id, CPlaylist *playlist)
    : id_(id), play_list_(playlist), auto_resume_able(true)
{
    assert(playlist);
    read_offset_ = 0;
    status_ = STATUS_STOP;

    control_callback_ = TXCServices::instance()->GetAppManager()->callback_;
}

CPlayerControl::~CPlayerControl()
{
    if (play_list_)
    {
        delete play_list_;
    }
}

bool CPlayerControl::OnMessage(XWM_EVENT event, XWPARAM arg1, XWPARAM arg2)
{
    TLOG_DEBUG("sessionId=%d CPlayerControl::OnMessage event=%s, arg1=%ld, arg2=%ld.", id_, Util::ToString(event).c_str(), arg1, arg2);

    bool handled = false;
    switch (event)
    {
        //  resource
    case XWM_ALBUM_ADDED:
        handled = AddAlbum(reinterpret_cast<long>(arg1));
        break;
    case XWM_LIST_ADDED:
        handled = AddList(reinterpret_cast<long>(arg1), reinterpret_cast<long>(arg2));
        break;
    case XWM_LIST_REMOVED:
        handled = RemoveList(reinterpret_cast<long>(arg1), reinterpret_cast<long>(arg2));
        break;
    case XWM_LIST_UPDATED:
        handled = control_callback_.control_callback(id_, ACT_PLAYLIST_UPDATE_ITEM, XWPARAM(arg1), XWPARAM(arg2));
        break;
    case XWM_MEDIA_REMOVED:
        handled = control_callback_.control_callback(id_, ACT_PLAYLIST_REMOVE_ITEM, XWPARAM(arg1), XWPARAM(arg2));
        break;
    case XWM_MEDIA_UPDATE:
    {
        const char *res_id = reinterpret_cast<const char *>(arg1);
        TLOG_DEBUG("sessionId=%d TXCMediaCenter::XWM_MEDIA_UPDATE, %s", id_, res_id);
        if (res_id && res_id[0])
        {
            OnMediaUpdated(res_id);
        }
        handled = true;
    }
    break;
    case XWM_PROGRESS:
    {
        const txc_progress_t *progress = reinterpret_cast<const txc_progress_t *>(arg1);
        handled = OnProgress(progress);
    }
    break;

        // control
    case XWM_PLAY:
    {
        read_offset_ = 0;
        long list_index = reinterpret_cast<long>(arg1);
        TLOG_DEBUG("sessionId=%d TXCMediaCenter::XWM_PLAY, list_index:%d", id_, list_index);
        if (0 <= list_index)
        {
            Play(list_index);
        }
        handled = true;
    }
    break;
    case XWM_NEXT:
    {
        Next(reinterpret_cast<long>(arg1));

        handled = true;
    }
    break;
    case XWM_STOP:
        Stop();
        handled = true;
        break;
    case XWM_PAUSE:
        Pause(bool(arg1), bool(arg2));
        handled = true;
        break;
    case XWM_VOLUME:
        OnVolume((int)reinterpret_cast<long>(arg1));
        handled = true;
        break;
    case XWM_REPEAT:
        SetRepeat(REPEAT_MODE(reinterpret_cast<long>(arg1)));
        handled = true;
        break;

    case XWM_PLAYER_STATUS_CHANGED:
        handled = OnPlayerStatusChanged(TXC_PLAYER_STATE(reinterpret_cast<long>(arg1)));
        break;

    case XWM_SUPPLEMENT_REQUEST:
    {
        const TXCA_PARAM_RESPONSE *response = reinterpret_cast<const TXCA_PARAM_RESPONSE *>(arg2);
        DelayEvent delayed = {event, arg1, arg2};
        delayed.resp.Copy(response);
        delay_events_.push_back(delayed);
        handled = true;
    }
        break;

    case XWM_IM_MSG:
    {
        unsigned long msgID = reinterpret_cast<unsigned long>(arg1);
        OnIMMessage(msgID);
        handled = true;
    }
    break;
    default:
        break;
    }

    return handled;
}

bool CPlayerControl::AddAlbum(long src_index)
{
    bool handled = false;
    const txc_player_info_t *player_info = txc_get_player_info(id_);
    if (player_info)
    {
        const txc_playlist_t *playlist_info = txc_get_medialist_info(player_info->playlist_id);
        if (playlist_info && playlist_info->count > 0)
        {
            const txc_media_t *media = txc_get_media(player_info->playlist_id, src_index);
            if (media)
            {
                control_callback_.control_callback(id_, ACT_ADD_ALBUM, XWPARAM(media), XWPARAM(src_index));
                handled = true;
            }
        }
    }

    return handled;
}

bool CPlayerControl::AddList(long begin_index, long count)
{
    const txc_player_info_t *player_info = txc_get_player_info(id_);
    TLOG_DEBUG("sessionId=%d CPlayerControl::AddList begin_index=%d, count=%d", id_, begin_index, count);
    if (player_info)
    {
        const txc_playlist_t *playlist_info = txc_get_medialist_info(player_info->playlist_id);
        if (playlist_info && playlist_info->count > 0)
        {
            CAutoBuffer<const txc_media_t *> msg_list(count);
            long add_count = 0;
            for (long i = 0; i < count; i++)
            {
                const txc_play_item_t *item = play_list_->GetItem((int)(begin_index + i));
                const txc_media_t *media = NULL;
                for (int j = 0; j < item->count && j < PLAY_ITEM_GROUP_MAX_SIZE; j++)
                {
                    long src_index = item->group[j];

                    const txc_media_t *temp = txc_get_media(player_info->playlist_id, src_index);
                    if (temp && temp->type == TYPE_MUSIC_URL)
                    {
                        media = temp;
                        j = item->count;
                    }

                    if (media)
                    {
                        msg_list[add_count] = media;
                        add_count++;
                    }
                }
            }

            if (add_count > 0)
            {
                control_callback_.control_callback(id_, begin_index == 0 ? ACT_PLAYLIST_ADD_ITEM_FRONT : ACT_PLAYLIST_ADD_ITEM, XWPARAM(msg_list.Get()), XWPARAM(add_count));
            }
        }
    }

    return true;
}

bool CPlayerControl::RemoveList(long begin_index, long count)
{
    const txc_player_info_t *player_info = txc_get_player_info(id_);
    TLOG_DEBUG("sessionId=%d CPlayerControl::RemoveList begin_index:%d, count:%d",id_, begin_index, count);
    if (player_info)
    {
        const txc_playlist_t *playlist_info = txc_get_medialist_info(player_info->playlist_id);
        if (playlist_info && playlist_info->count > 0)
        {
            CAutoBuffer<const txc_media_t *> msg_list(count);
            long remove_count = 0;
            for (long i = 0; i < count; i++)
            {
                const txc_play_item_t *item = play_list_->GetItem((int)(begin_index + i));
                const txc_media_t *media = NULL;
                for (int j = 0; j < item->count && j < PLAY_ITEM_GROUP_MAX_SIZE; j++)
                {
                    long src_index = item->group[j];

                    const txc_media_t *temp = txc_get_media(player_info->playlist_id, src_index);
                    if (temp && temp->type == TYPE_MUSIC_URL)
                    {
                        media = temp;
                        j = item->count;
                    }

                    if (media)
                    {
                        msg_list[remove_count] = media;
                        remove_count++;
                    }
                }
            }

            if (remove_count > 0)
            {
                control_callback_.control_callback(id_, ACT_PLAYLIST_REMOVE_ITEM, XWPARAM(msg_list.Get()), XWPARAM(remove_count));
            }
        }
    }

    return true;
}

bool CPlayerControl::UpdateList(long begin_index, long count)
{
    const txc_player_info_t *player_info = txc_get_player_info(id_);
    TLOG_DEBUG("sessionId=%d CPlayerControl::UpdateList begin_index:%d, count:%d",id_, begin_index, count);
    if (player_info)
    {
        const txc_playlist_t *playlist_info = txc_get_medialist_info(player_info->playlist_id);
        if (playlist_info && playlist_info->count > 0)
        {
            CAutoBuffer<const txc_media_t *> msg_list(count);

            for (long i = 0; i < count; i++)
            {
                const txc_media_t *media = txc_get_media(player_info->playlist_id, begin_index + i);

                if (media)
                {
                    msg_list[i] = media;
                }
            }

            if (count > 0)
            {
                control_callback_.control_callback(id_, ACT_PLAYLIST_UPDATE_ITEM, XWPARAM(msg_list.Get()), XWPARAM(count));
            }
        }
    }

    return true;
}

bool CPlayerControl::OnIMMessage(unsigned long msg_id)
{
    /*const txc_im_msg_t *msg   = txc_get_im_msg(msg_id);
    switch (msg->msg_type) {
        case txca_msg_type_im_text:
        {
            if (msg
                && msg->content
                && msg->content[0])
            {
                json::Object    j_content   = json::Deserialize(msg->content);
                
                if (j_content.HasKey(g_XWEI_KEY_text)) {
                    std::string text = j_content[g_XWEI_KEY_text];
                    std::string text_voice = j_content[g_XWEI_KEY_text_voice];
                }
            }
        }
            break;
        case txca_msg_type_im_audio:
        {
            if (msg
                && msg->content
                && msg->content[0])
            {
                json::Object    j_content   = json::Deserialize(msg->content);
                
                if (j_content.HasKey(g_XWEI_KEY_audio)) {
                    std::string file_key = j_content[g_XWEI_KEY_audio];
                    if (!file_key.empty()) {
                        unsigned long long cookie   = txc_download_msg_file(msg_id, file_key.c_str());
                    }
                }
            }
        }
            break;
        default:
            break;
    };*/

    return true;
}

bool CPlayerControl::OnSupplementRequest(XWPARAM arg1, XWPARAM arg2)
{
    bool handled = true;
    control_callback_.control_callback(id_, ACT_NEED_SUPPLEMENT, arg1, arg2);

    return handled;
}

bool CPlayerControl::OnProgress(const txc_progress_t *progress)
{
    bool handled = true;

    control_callback_.control_callback(id_, ACT_PROGRESS, (XWPARAM)progress, 0);

    return handled;
}

bool CPlayerControl::OnMediaUpdated(const char *res_id)
{
    TLOG_DEBUG("sessionId=%d CPlayerControl::OnMediaUpdated, %s, %s", id_, res_id, play_res_id_.c_str());
    if (res_id && res_id[0] && play_res_id_ == res_id)
    {
        SetStatus(STATUS_PLAY);
        const txc_player_info_t *player_info = txc_get_player_info(id_);
        if (player_info)
        {

            const txc_playlist_t *playlist_info = txc_get_medialist_info(player_info->playlist_id);
            if (playlist_info && playlist_info->count > 0)
            {
                play_list_->Seek(play_list_->GetCurIndex());
            }
        }
    }

    return true;
}

bool CPlayerControl::PlayMediaIndex(long src_index, bool isAuto)
{
    bool handled = false;
    if (0 <= src_index)
    {
        const txc_player_info_t *player_info = txc_get_player_info(id_);
        if (player_info)
        {

            const txc_playlist_t *playlist_info = txc_get_medialist_info(player_info->playlist_id);
            if (playlist_info && playlist_info->count > 0)
            {
                const txc_media_t *media = txc_get_media(player_info->playlist_id, src_index);
                if (media)
                {
                    SetStatus(STATUS_PLAY);

                    if(!isAuto && play_res_id_.length() > 0) {
                        // 之前那首歌播放被打断
                        ReportPlayState(txca_playstate_abort);
                    }
                    
                    if (media->res_id && media->res_id[0])
                    {
                        play_res_id_ = media->res_id;
                    }
                    
                    handled = PlayMedia(media);
                }
            }
        }
    }

    return handled;
}

bool CPlayerControl::Play(long ui_index)
{
    auto_resume_able = true;
    bool result = false;

    long src_index = play_list_->Seek((int)ui_index);
    TLOG_DEBUG("sessionId=%d CPlayerControl::Play, ui_index=%ld, src_index=%ld", id_, ui_index, src_index);

    if (0 <= src_index)
    {
        read_offset_ = 0;
        result = PlayMediaIndex(src_index);
    }

    return result;
}

bool CPlayerControl::Pause(bool pause, bool isAuto)
{
    TLOG_TRACE("sessionId=%d CPlayerControl::Pause pause=%d, isAuto=%d, auto_resume_able=%d.", id_, pause, isAuto, auto_resume_able);
    if (!pause && isAuto && !auto_resume_able)
    {
        return false;
    }
    if (pause && !isAuto)
    {
        auto_resume_able = false;
    }
    if (!pause)
    {
        auto_resume_able = true;
    }
    TLOG_TRACE("sessionId=%d CPlayerControl::Pause auto_resume_able=%d.", id_, auto_resume_able);

    bool ret = SetStatus(pause ? STATUS_PAUSE : STATUS_PLAY);

    if (ret)
    {
        control_callback_.control_callback(id_, ACT_PLAYER_PAUSE, XWPARAM(pause), 0);
        ReportPlayState(pause ? txca_playstate_paused : txca_playstate_resume);
    }
    return true;
}

bool CPlayerControl::OnVolume(int volume)
{
    TLOG_TRACE("sessionId=%d CPlayerControl::OnVolume volume=%d", id_, volume);

    control_callback_.control_callback(id_, ACT_CHANGE_VOLUME, XWPARAM((long)volume), 0);
    return true;
}

bool CPlayerControl::SetRepeat(REPEAT_MODE repeatMode)
{
    TLOG_DEBUG("sessionId=%d CPlayerControl::SetRepeat repeatMode[%d]", id_, repeatMode);

    play_list_->SetRepeat(repeatMode);
    control_callback_.control_callback(id_, ACT_PLAYER_SET_REPEAT_MODE, XWPARAM(repeatMode), 0);
    ReportPlayState();
    return true;
}

bool CPlayerControl::PlayMedia(const txc_media_t *media)
{
    bool result = true;

    TLOG_DEBUG("sessionId=%d CPlayerControl::PlayMedia, res_id:%s content:%s offset:%d", id_, media->res_id, media->content, media->offset);
    TXCServices::instance()->GetMediaCenter()->DecMediaTipCnt(play_res_id_);

    control_callback_.control_callback(id_, ACT_MUSIC_PUSH_MEDIA, XWPARAM(media), XWPARAM((bool)(media->play_count == 0)));

    ReportPlayState(txca_playstate_start);

    return result;
}

bool CPlayerControl::Stop()
{
    SetStatus(STATUS_STOP);
    read_offset_ = 0;
    play_res_id_.clear();

    control_callback_.control_callback(id_, ACT_PLAYER_STOP, 0, 0);
    control_callback_.control_callback(id_, ACT_PLAYER_FINISH, 0, 0);
    return true;
}

bool CPlayerControl::Next(long skip, bool isAuto)
{
    // 重置标记
    auto_resume_able = true;
    
    bool result = false;

    long src_index = play_list_->NextX(skip, isAuto);

    TLOG_DEBUG("sessionId=%d CPlayerControl::Next, cur_index=%ld, src_index=%ld", id_, play_list_->GetCurIndex(), src_index);
    if (0 <= src_index)
    {
        read_offset_ = 0;
        result = PlayMediaIndex(src_index, isAuto);
    }
    else
    {
        // 自动的就回调播放结束，否则进行没有更多了提示
        if (isAuto)
        {

            // 播放结束了可能需要自动唤醒
            if (!delay_events_.empty())
            {
                std::vector<DelayEvent>::iterator itr = delay_events_.begin();
                bool retry = true;
                while (delay_events_.end() != itr && retry)
                {
                    if (itr->event == XWM_SUPPLEMENT_REQUEST)
                    {
                        XWPARAM response = reinterpret_cast<XWPARAM>(&(itr->resp.response()));
                        OnSupplementRequest(itr->arg1, response);
                    }
                    ++itr;
                }
                delay_events_.clear();
            }

            post_message(id_, XWM_PLAYER_STATUS_FINISH, NULL, NULL, 0);
        }
        else
        {
            control_callback_.control_callback(id_, ACT_NEED_TIPS, XWPARAM((int)skip > 0 ? PLAYER_TIPS_NEXT_FAILURE : PLAYER_TIPS_PREV_FAILURE), XWPARAM(skip));
        }
    }

    return result;
}

bool CPlayerControl::SetStatus(PLAYER_STATUS status)
{
    if (status_ != status)
    {
        status_ = status;
        PtrPlayer player = TXCServices::instance()->GetPlayerManager()->GetPlayer(id_);
        if (player.get())
        {
            player->OnStatusChanged(status_);
        }
        return true;
    }
    return false;
}

PLAYER_STATUS CPlayerControl::GetStatus()
{
    return status_;
}

bool CPlayerControl::OnPlayerStatusChanged(TXC_PLAYER_STATE state_code)
{
    TLOG_DEBUG("sessionId=%d CPlayerControl::OnPlayerStatusChanged, %d", id_, Util::ToString(state_code).c_str());

    if (TXC_PLAYER_STATE_COMPLETE == state_code)
    {
        ReportPlayState(txca_playstate_stopped);

        if (STATUS_PLAY == status_ && -1 == play_list_->GetCurIndex() && play_list_->Count() == 0)
        {
            play_res_id_.clear();
        }
        else
        {
            bool handled = false;
            long list_index = play_list_->NextY();
            while (0 <= list_index && !handled)
            {
                handled = PlayMediaIndex(list_index, true);
                if (!handled)
                {
                    list_index = play_list_->NextY();
                }
            }
            if (!handled)
            {
                Next(1, true);
            }
        }
    }
    else if (TXC_PLAYER_STATE_CONTINUE == state_code)
    {
        //Next(1);
    }
    else if (TXC_PLAYER_STATE_START == state_code)
    {
    }

    return true;
}

void CPlayerControl::ReportPlayState(TXCA_PLAYSTATE play_state)
{
    if (play_state == txca_playstate_idle)
    {
        if (status_ == STATUS_STOP)
        {
            return;
        }
    }

    TXCA_PARAM_STATE state = {0};
    state.play_state = play_state == txca_playstate_idle ? status_ == STATUS_PLAY ? txca_playstate_resume : txca_playstate_paused : play_state;

    const txc_session_info *app_info = TXCServices::instance()->GetAppManager()->GetSessionInfo(id_);

    if (app_info)
    {
        state.skill_info.name = app_info->skill_name;
        state.skill_info.id = app_info->skill_id;
    }

    PtrMedia media = TXCServices::instance()->GetMediaCenter()->GetMedia(play_res_id_);
    if (media.get())
    {
        if (media->GetInfo().type == TYPE_MUSIC_URL)
        {
            state.play_mode = play_list_->GetRepeat();
            state.play_id = media->GetInfo().res_id;
            state.play_content = media->GetInfo().content;
        }
    }
    TLOG_DEBUG("sessionId=%d CPlayerControl::ReportPlayState skill_name[%s] skill_id[%s] play_state[%u] play_id[%s] play_mode[%u] play_content[%s]", id_, state.skill_info.name, state.skill_info.id, state.play_state, state.play_id, state.play_mode, state.play_content);
    control_callback_.control_callback(id_, ACT_REPORT_PLAY_STATE, (XWPARAM)(&state), NULL);
}

void CPlayerControl::DownloadFile(txc_download_msg_data_t* data)
{
    if (data) {
        control_callback_.control_callback(id_, ACT_DOWNLOAD_MSG, (XWPARAM)(data), NULL);
    }
}

void CPlayerControl::NotifyMsgRecord()
{
    control_callback_.control_callback(id_, ACT_AUDIOMSG_RECORD, NULL, NULL);
}

void CPlayerControl::NotifyMsgSend(unsigned long long tinyId)
{
    TLOG_DEBUG("sessionId=%d CPlayerControl::NotifyMsgSend tinyId:%llu", id_, tinyId);
    control_callback_.control_callback(id_, ACT_AUDIOMSG_SEND, (XWPARAM)(&tinyId), NULL);
}

bool txc_player_control(SESSION id, TXC_PLAYER_CONTROL control_code, int arg1, int arg2)
{
    PtrPlayer player = TXCServices::instance()->GetPlayerManager()->GetPlayer(id);
    if(!player.get())
    {
        return false;
    }
    bool handled = false;
    switch (control_code)
    {
    case PLAYER_STOP:
        handled = player->Stop();
        break;
    case PLAYER_PLAY:
        post_message(id, XWM_REQUEST_AUDIO_FOCUS, XWPARAM((long)id), NULL);
        handled = player->Play(arg1);
        break;
    case PLAYER_PAUSE:
        handled = player->Pause();
        break;
    case PLAYER_RESUME:
        post_message(id, XWM_REQUEST_AUDIO_FOCUS, XWPARAM((long)id), NULL);
        handled = player->Resume();
        break;
    case PLAYER_VOLUME:
        handled = player->Volume(arg1);
        break;
    case PLAYER_REPEAT:
        handled = player->SetRepeat(arg1);
        break;
    case PLAYER_NEXT:
        post_message(id, XWM_REQUEST_AUDIO_FOCUS, XWPARAM((long)id), NULL);
        handled = player->Next(arg1);
        break;
    case PLAYER_SKIP:
        break;
    default:
        break;
    }

    return handled;
}

void txc_player_statechange(SESSION id, TXC_PLAYER_STATE state_code)
{
    post_message(id, XWM_PLAYER_STATUS_CHANGED, XWPARAM(state_code), NULL, 0);
}

