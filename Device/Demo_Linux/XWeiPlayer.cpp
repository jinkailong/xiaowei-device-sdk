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

#include <string.h>
#include "Player.h"
#include "Playlist.h"
#include "Media.h"
#include "AudioPlayer.h"
#include "TXCAudio.h"
#include "TXCAudioCommon.h"
#include "XWeiPlayer.h"
#include "XWeiAudioEngine.h"
#include "XWeiDevice.h"

CXWeiPlayerMgr g_xwei_player_mgr;

CXWeiPlayerMgr::CXWeiPlayerMgr()
{
    mCurrentSession = -1;
}

CXWeiPlayerMgr::~CXWeiPlayerMgr()
{
}

bool CXWeiPlayerMgr::isPlaying()
{
    bool isPlaying = false;
    if (map_players_[mCurrentSession])
    {
        isPlaying = map_players_[mCurrentSession]->IsPlaying();
    }
    return isPlaying;
}

// 处理控制层相关事件回调
bool CXWeiPlayerMgr::OnCallback(SESSION id, TXC_PLAYER_ACTION action, XWPARAM arg1, XWPARAM arg2)
{
    bool handled = false;

    CXWeiPlayer *player = NULL;
    std::map<SESSION, CXWeiPlayer *>::iterator itr = map_players_.find(id);
    if (map_players_.end() != itr)
    {
        player = itr->second;
    }
    else
    {
        player = new CXWeiPlayer;
        player->Init(id);
        map_players_[id] = player;
    }

    if (action == ACT_PLAYER_PAUSE)
    {
        if (!bool(arg1))
        {
            mCurrentSession = id;
        }
    }
    else if (action == ACT_ADD_ALBUM)
    {
        // 启动某个场景，如果是有屏设备，需要显示Album信息
        mCurrentSession = id;
    }

    if (player)
    {
        handled = player->OnCallback(id, action, arg1, arg2);
    }

    return handled;
}

CXWeiPlayer::CXWeiPlayer()
{
    id_ = 0;
    decoder_ = NULL;
    player_ = NULL;
    isPlaying_ = false;
}

CXWeiPlayer::~CXWeiPlayer()
{
    if (player_)
        delete player_;
    if (decoder_)
        delete decoder_;
}

void CXWeiPlayer::Init(SESSION id)
{
    id_ = id;

    decoder_ = NULL;
    player_ = CAudioPlayerMgr::instance().Create();
    if (player_)
    {
        player_->PutSessionId(id);
    }
}

bool CXWeiPlayer::OnCallback(SESSION id, TXC_PLAYER_ACTION action, XWPARAM arg1, XWPARAM arg2)
{
    bool handled = false;
    switch (action)
    {
    case ACT_PLAYER_STOP:
    {
        handled = OnActStop();
    }
    break;
    case ACT_CHANGE_VOLUME:
    {
        int vol = reinterpret_cast<long>(arg1);
        handled = true;
    }
    break;
    case ACT_PLAYER_PAUSE:
    {
        handled = OnActPause(bool(arg1));
    }
    break;
    case ACT_PLAYLIST_ADD_ITEM:
    {
        handled = OnPlaylistAddItem(reinterpret_cast<const txc_media_t **>(arg1), reinterpret_cast<long>(arg2));
    }
    break;
    case ACT_MUSIC_PUSH_MEDIA:
    {
        handled = OnPushMusicMedia(reinterpret_cast<const txc_media_t *>(arg1), bool(arg2));
    }
    break;
    case ACT_NEED_SUPPLEMENT:
    {
        handled = CXWeiApp::instance().AudioEngine().OnSupplement(reinterpret_cast<long>(arg1), reinterpret_cast<const TXCA_PARAM_RESPONSE *>(arg2));
    }
    break;
    case ACT_NEED_TIPS:
    {
        int tipsType = reinterpret_cast<long>(arg1);
        if (tipsType == PLAYER_TIPS_NEXT_FAILURE)
        {
            std::string str = "当前列表没有更多了，您可以重新点播";
            char voice_id[64] = {0};
            TXCA_PARAM_CONTEXT context = {0};
            txca_request(voice_id, txca_chat_only_tts, str.c_str(), strlen(str.c_str()), &context);
        }
        else if (tipsType == PLAYER_TIPS_PREV_FAILURE)
        {
            std::string str = "当前列表没有上一首了";
            char voice_id[64] = {0};
            TXCA_PARAM_CONTEXT context = {0};
            txca_request(voice_id, txca_chat_only_tts, str.c_str(), strlen(str.c_str()), &context);
        }

        handled = true;
    }
    break;
    case ACT_REPORT_PLAY_STATE:
    {
        TXCA_PARAM_STATE *state = reinterpret_cast<TXCA_PARAM_STATE *>(arg1);
        state->play_offset = 222; // 填写真实的播放进度
        txca_report_state(state);
        handled = true;
    }
    break;
    default:
        break;
    }

    return handled;
}

bool CXWeiPlayer::IsPlaying()
{
    return isPlaying_;
}
bool CXWeiPlayer::OnActStop()
{
    isPlaying_ = false;
    player_->Stop();

    return true;
}

bool CXWeiPlayer::OnActPause(bool pause)
{
    if (pause)
    {
        player_->Pause();
        isPlaying_ = false;
    }
    else
    {
        player_->Resume();
        isPlaying_ = true;
    }
    return true;
}

void CXWeiPlayer::OnTTSInfo(TTSItem *ttsInfo)
{
    player_->TTSPushStart(ttsInfo->sample_rate, ttsInfo->channel, ttsInfo->pcm_sample_rate);

    const int max_frames = 960 * 6;
    if (decoder_)
    {
        delete decoder_;
    }
    decoder_ = new CDecoderOpus(ttsInfo->sample_rate, ttsInfo->channel, max_frames);
}

void CXWeiPlayer::OnTTSData(XWTTSDataInfo *ttsInfo)
{
    if (decoder_ && player_ && ttsInfo && ttsInfo->data.length() > 0)
    {
        decoder_->Decode(reinterpret_cast<const unsigned char *>(ttsInfo->data.c_str()), ttsInfo->data.length(), player_);
    }

    if (ttsInfo->is_end)
    {
        if (decoder_)
        {
            delete decoder_;
            decoder_ = NULL;
        }

        player_->TTSPushEnd();
    }
}

bool CXWeiPlayer::OnPushMusicMedia(const txc_media_t *media, bool needReleaseRes)
{
    bool handled = false;
    if (media->type == TYPE_TTS_OPUS)
    {
        CXWeiTTSManager::instance()->read(media->res_id, this);
    } else if (media->type == TYPE_TTS_MSGPROMPT) {
        unsigned long long timestamp = strtoull(media->description, NULL, 0);
        unsigned long long uin = strtoull(media->content, NULL, 0);
        char szVoiceId[100] = {0};
        if (!txca_request_protocol_tts(szVoiceId, uin, timestamp, 403))
        {
            printf("OnPushMusicMedia txca_request_protocol_tts %s\n", szVoiceId);

            CXWeiTTSManager::instance()->read(szVoiceId, this);
        }
    } else if (media->type == TYPE_TTS_TEXT || media->type == TYPE_TTS_TEXT_TIP) {
        TXCA_PARAM_CONTEXT context = {0};
        char szVoiceId[33] = {0};
        txca_request(szVoiceId, txca_chat_only_tts, media->content, strlen(media->content), &context);

        printf("OnPushMusicMedia request tts %s", media->content);

        CXWeiTTSManager::instance()->read(szVoiceId, this);
    } else if (player_ && media && media->description) {
        // 获取资源meta信息
        // using namespace rapidjson;
        // Document json_doc;
        // json_doc.Parse(media->description);
        // const std::string &album = json_doc["album"].GetString();
        // const std::string &artist = json_doc["artist"].GetString();
        // const std::string &coverUrl = json_doc["cover"].GetString();
        // const std::string &duration = json_doc["duration"].GetString();
        // bool isFavorite = json_doc["favorite"].GetBool();
        //
        // const std::string &name = json_doc["name"].GetString();

        // 获取资源播放内容
        const char *url = media->content;

        player_->Play(media->type, url);
    }

    isPlaying_ = true;
    return handled;
}

bool CXWeiPlayer::OnPlaylistAddItem(const txc_media_t **media_list, long count)
{
    if (media_list) {
        for (long i = 0; i < count; ++i) {
            const txc_media_t *media = media_list[i];
            //   update ui
        }
    }
    return false;
}

CDecoderOpus::CDecoderOpus(size_t sample, size_t channel, size_t max_frames)
    : sample_(sample), channel_(channel), max_frames_(max_frames), /*decoder_(NULL), opus_buffer_(NULL), */pcm_buffer_(NULL)
{
}
CDecoderOpus::~CDecoderOpus()
{
    // 注意：需要opus库支持
    // if (decoder_)
    //     opus_decoder_destroy(decoder_);
    // if (opus_buffer_)
    //     delete[] opus_buffer_;
    // if (pcm_buffer_)
    //     delete[] pcm_buffer_;
}

size_t CDecoderOpus::Decode(const unsigned char *data, int data_len, CAudioPlayer *player)
{
    int total_frames = 0;

    // 注意：需要opus库支持
    // if (!decoder_) {
    //     int err = 0;
    //     decoder_ = opus_decoder_create(sample_, channel_, &err);
    // }
    // if (!opus_buffer_) {
    //     opus_buffer_ = new opus_int16[max_frames_ * channel_];
    // }
    // if (!pcm_buffer_) {
    //     pcm_buffer_ = new char[max_frames_ * channel_ * 2];
    // }

    // if (decoder_ && opus_buffer_ && pcm_buffer_) {
    //     int frames = 0;
    //     do {
    //         frames = opus_decode(decoder_, data, data_len, opus_buffer_, max_frames_, 0);
    //         if (frames <= 0) {
    //             printf("CAIAudioPlayer::onTTSPush opus decode error=%d, origin len=%d\n", frames, data_len);
    //             break;
    //         }
    //         for (int i = 0; i < frames; i++) {
    //             pcm_buffer_[i * 2] = (char)((opus_buffer_[i]) & 0xFF);
    //             pcm_buffer_[i * 2 + 1] = (char)((opus_buffer_[i] >> 8) & 0xFF);
    //         }

    //         total_frames += frames;

    //         player->TTSPush(0, pcm_buffer_, frames * channel_ * 2);
    //         printf("CAIAudioPlayer::onTTSPush opus decode frames:%d, origin len=%d\n", frames, data_len);
    //     } while (frames == max_frames_);
    // }

    return total_frames;
}
