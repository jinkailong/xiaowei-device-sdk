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
#ifndef _AIAUDIO_PLAYER_H_
#define _AIAUDIO_PLAYER_H_

#include "txctypedef.h"

CXX_EXTERN_BEGIN

// 播放器控制回调事件
enum TXC_PLAYER_ACTION
{
    ACT_NULL = 0,
    ACT_MUSIC_PUSH_MEDIA, // media = (const txc_media_t *)arg1; need release = (bool) arg2
    ACT_ADD_ALBUM,               // description = (const txc_media_t *)arg1; index = (long)arg2
    ACT_PLAYLIST_ADD_ITEM,       // add back,media_list = (const txc_media_t **)arg1; count = (long)arg2
    ACT_PLAYLIST_ADD_ITEM_FRONT, // add front,media_list = (const txc_media_t **)arg1; count = (long)arg2
    ACT_PLAYLIST_REMOVE_ITEM, // media = (const txc_media_t *)arg1; index = (long)arg2
    ACT_PLAYLIST_UPDATE_ITEM, // media_list = (const txc_media_t **)arg1; count = (long)arg2
    ACT_PLAYER_STOP,
    ACT_PLAYER_PAUSE, //  pause = bool(arg1)
    ACT_PLAYER_SET_REPEAT_MODE,
    ACT_PLAYER_FINISH,
    ACT_RESPONSE_DATA, //  data_type = reinterpret_cast<unsigned int>(arg1); response_json = reinterpret_cast<const char *>(arg2)
    ACT_PROGRESS, //  progress = reinterpret_cast<const txc_progress_t *>(arg1);
    ACT_NEED_SUPPLEMENT, // timeout = reinterpret_cast<long>(arg1); response = reinterpret_cast<const TXCA_PARAM_RESPONSE*>(arg2)
    ACT_NEED_TIPS, // need tips, type = reinterpret_cast<int>(arg1);
    ACT_CHANGE_VOLUME, // volume = reinterpret_cast<int>(arg1); arg1%
    ACT_REPORT_PLAY_STATE, // report playstate
    ACT_DOWNLOAD_MSG,      // data_type = reinterpret_cast<txc_download_data_t>(arg1);
    ACT_AUDIOMSG_RECORD,    //
    ACT_AUDIOMSG_SEND,      //
};

// 播放器状态定义，参考接口txc_player_statechange中的说明
enum TXC_PLAYER_STATE
{
    TXC_PLAYER_STATE_START = 1,    //开始播放: 新播放一个资源
    TXC_PLAYER_STATE_STOP = 2,     //退出播放: 终止播放, 调用ACT_PLAYER_STOP后触发
    TXC_PLAYER_STATE_COMPLETE = 3, //完成播放: 完成一首, 包括调用下一首上一首等
    TXC_PLAYER_STATE_PAUSE = 4,    //暂停播放: 暂停, 调用ACT_PLAYER_PAUSE(arg1:true)后触发
    TXC_PLAYER_STATE_CONTINUE = 5, //继续播放: 继续播放, 调用ACT_PLAYER_PAUSE(arg1:false)后触发
    TXC_PLAYER_STATE_ERR = 6,      //播放错误: 包括无法下载url，资源异常等
};

enum TXC_PLAYER_DATA_TYPE
{
    PTYPE_NULL = 0,
    PTYPE_TTS_DESCRIPTION = 1,
    PTYPE_TTS_OPUS,
    PTYPE_MUSIC_MEDIA,
    PTYPE_PLAYLIST_MEDIA,
};

// 播放器控制码定义
typedef enum player_control {
    PLAYER_CONTROL_NULL = 0,
    PLAYER_BEGIN_PLAYER_CONTROL = 1,
    PLAYER_STOP,
    PLAYER_PLAY,    //  arg1: index
    PLAYER_PAUSE,   //
    PLAYER_RESUME,  //
    PLAYER_VOLUME,  //  arg1: value of volume
    PLAYER_BEGIN_NAVIGATE = 0x100,
    PLAYER_REPEAT, //  arg1: repeat_mode
    PLAYER_NEXT,   //  arg1: +n|-n indexes
    PLAYER_SKIP,   //  arg1: skip +n|-n milliseconds

} TXC_PLAYER_CONTROL;

typedef enum player_tips {
    PLAYER_TIPS_NEXT_FAILURE = 0,
    PLAYER_TIPS_PREV_FAILURE = 1,
} TXC_PLAYER_TIPS;

/**
 * 接口说明：控制外部的播放器
 * @param id 某个场景的session id
 * @param control_code 控制码
 * @param arg1 参数1
 * @param arg2 参数2
 */
SDK_API bool txc_player_control(SESSION id, TXC_PLAYER_CONTROL control_code, int arg1, int arg2);

/**
 * 接口说明：播放器状态改变通知给控制层
 * @param id 某个场景的session id
 * @param state_code 播放状态
 */
SDK_API void txc_player_statechange(SESSION id, TXC_PLAYER_STATE state_code);

// 播放器播放模式定义
enum REPEAT_MODE
{
    REPEAT_RANDOM = 0,
    REPEAT_SINGLE,
    REPEAT_LOOP,
    REPEAT_SEQUENCE,
};

// 播放器内部状态
typedef enum player_status {
    STATUS_STOP = 0,
    STATUS_PLAY,
    STATUS_PAUSE,
} PLAYER_STATUS;

// 控制层播放器信息定义
typedef struct txc_player_info_t
{
    PLAYER_STATUS status;      // 播放器内部状态
    REPEAT_MODE repeatMode;    // 播放器播放模式定义
    int playlist_id;           // 播放列表id
    int volume;                // 音量
    int qulity;                // 品质值
} txc_player_info_t;

/**
 * 接口说明：获取某个场景关联的播放器信息
 *
 * @param id 场景信息session id
 * @return 返回场景关联的播放器信息，接口可能会返回NULL
 */
SDK_API const txc_player_info_t *txc_get_player_info(SESSION id);

CXX_EXTERN_END

#endif /* _AIAUDIO_PLAYER_H_ */
