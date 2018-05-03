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
#ifndef PlayerControl_hpp
#define PlayerControl_hpp

#include "AudioApp.hpp"
#include "ResponseHolder.hpp"

class CPlaylist;

// 控制播放器的播放、音量、循环模式、列表、播放完毕自动唤醒等
class CPlayerControl
{
  public:
    CPlayerControl(SESSION id, CPlaylist *playlist);
    ~CPlayerControl();
    bool OnMessage(XWM_EVENT event, XWPARAM arg1, XWPARAM arg2);

    void ReportPlayState(TXCA_PLAYSTATE play_state = txca_playstate_idle);
    
    void DownloadFile(txc_download_msg_data_t* data);
    void NotifyMsgRecord(); //通知消息录音
    void NotifyMsgSend(unsigned long long tinyId);   //通知消息发送
    PLAYER_STATUS GetStatus();

  private:
    bool Play(long index);
    bool Stop();
    bool Next(long skip, bool isAuto = false);
    bool Pause(bool pause, bool isAuto);
    bool OnVolume(int volume);
    bool SetRepeat(REPEAT_MODE repeatMode);

    bool AddAlbum(long begin_index);
    bool AddList(long begin_index, long count);
    bool RemoveList(long begin_index, long count);
    bool UpdateList(long begin_index, long count);
    bool OnMediaUpdated(const char *res_id);
    bool OnPlayerStatusChanged(TXC_PLAYER_STATE state_code);
    bool OnSupplementRequest(XWPARAM arg1, XWPARAM arg2);
    bool OnIMMessage(unsigned long msg_id);
    bool OnProgress(const txc_progress_t *progress);

    bool SetStatus(PLAYER_STATUS status);
    bool IsMediaReady();

    bool PlayMediaIndex(long src_index, bool isAuto = false);
    bool IsMediaNeedPlay(long src_index);
    bool PlayMedia(const txc_media_t *media);

  private:
    SESSION id_;

    CPlaylist *play_list_;
    size_t read_offset_;

    PLAYER_STATUS status_;
    std::string play_res_id_;

    struct DelayEvent
    {
        XWM_EVENT event;
        XWPARAM arg1;
        XWPARAM arg2;
        CResponseHolder resp;
    };
    std::vector<DelayEvent> delay_events_;

    txc_xwei_control control_callback_;
    bool auto_resume_able;
};

#endif /* PlayerControl_hpp */
