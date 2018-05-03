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
#ifndef _AIAUDIO_AUDIOAPP_H_
#define _AIAUDIO_AUDIOAPP_H_

//////////////////// interface of AudioApp.h ////////////////////
#include "txctypedef.h"
#include <stdarg.h>
#include "TXCAudioType.h"
#include "Player.h"
#include "TXCAudioFileTransfer.h"

CXX_EXTERN_BEGIN

// receive events @ message thread
// 消息类型定义
typedef enum xwm_event {
    XWM_NULL = 0,

    XWM_SUPPLEMENT_REQUEST, // arg1: speak_timeout; response   = (const TXCA_PARAM_RESPONSE*)(arg2);
    XWM_ERROR_RESPONSE,     // arg1: error_code; response   = (const TXCA_PARAM_RESPONSE*)(arg2);
    XWM_RESPONSE_DATA,      // arg1:
    XWM_SILENT,             // arg1:

    XWM_BEGIN_PLAYER_CONTROL = 0x100,
    XWM_STOP,
    XWM_PLAY,    //  arg1: index
    XWM_PAUSE,   //  arg1: 1 for pause, 0 for resume
    XWM_VOLUME,  //  arg1: value of volume

    XWM_BEGIN_NAVIGATE = 0x200,
    XWM_REPEAT, //  arg1: repeat_mode
    XWM_NEXT,   //  arg1: +n indexes
    XWM_SKIP,   //  arg1: skip n milliseconds

    XWM_SETFOCUS,  // arg1: session id lost focus
    XWM_KILLFOCUS, // arg1: session id receives focus

    XWM_REQUEST_AUDIO_FOCUS, //arg1: (int)SESSIONID ,arg2:duration
    XWM_ABANDON_AUDIO_FOCUS, //arg1: (bool) all
    XWM_SET_AUDIO_FOCUS,     //arg1: (DURATION_HINT) focus

    XWM_BEGIN_MEDIA = 0x300,
    XWM_ALBUM_ADDED,   // arg1: media index of album description;
    XWM_LIST_ADDED,    // arg1: start item index; arg2: added item count
    XWM_LIST_REMOVED,  // arg1: start item index; arg2: end item index
    XWM_LIST_UPDATED,  // arg1: start item index; arg2: updated item count
    XWM_MEDIA_ADDED,   // arg1: start item index; arg2: added item count
    XWM_MEDIA_REMOVED, // arg1: items id; arg2: count

    XWM_PROGRESS,     // progress = reinterpret_cast<const txc_progress_t *>(arg1)
    XWM_MEDIA_UPDATE, //  arg1: const char *res_id

    XWM_BEGIN_UI_FEEDBACK = 0x400,
    XWM_PLAYER_STATUS_CHANGED, // arg1: tx_ai_audio_player_state
    XWM_PLAYER_STATUS_FINISH,

    XWM_IM_MSG = 0x900,

    XWM_SYSTEM = 0X1000,

    XWM_USER = 0X2000,
} XWM_EVENT;

// 场景信息结构体定义
struct txc_session_info
{
    const char *skill_id;   // 技能id TXCSkillsDefine.h
    const char *skill_name; // 技能名字
};

// 控制层Skill控制回调定义
struct txc_xwei_control
{
    XWPARAM (*control_callback)(SESSION id, TXC_PLAYER_ACTION action, XWPARAM data, XWPARAM data_length);
};

/**
 * 接口说明：初始化小微控制层
 *
 * @param callback 控制层播放器回调接口
 */
SDK_API void txc_xwei_control_init(const struct txc_xwei_control *callback);

// 日志等级定义
typedef enum TXC_LogPriority {
    TXC_LOG_UNKNOWN = 0,
    TXC_LOG_DEFAULT,
    TXC_LOG_VERBOSE,
    TXC_LOG_DEBUG,
    TXC_LOG_INFO,
    TXC_LOG_WARN,
    TXC_LOG_ERROR,
    TXC_LOG_FATAL,
    TXC_LOG_SILENT,
} TXC_LOG_PRIORITY;

// 日志回调函数定义
typedef void (*custom_log_function)(int log_priority, const char *module, int line, const char *text);

/**
 * 接口说明：设置控制层回调接口
 *
 * @param  log_function 日志回调函数接口
 */
SDK_API void txc_set_log_function(custom_log_function log_function);

/**
 * 接口说明：控制层处理语音请求响应的入口函数
 *
 * @param voice_id 请求voice_id
 * @param event 请求返回的事件
 * @param state_info 响应信息，结构定义请参考TXCA_PARAM_RESPONSE
 * @param extend_info 扩展信息
 * @param extend_info_len 扩展信息长度
 */
SDK_API bool txc_process_response(const char *voice_id, TXCA_EVENT event, const char *state_info, const char *extend_info, unsigned int extend_info_len);

/**
 * 接口说明：获取当前的session场景列表
 *
 * @param sessions session场景列表
 * @param buffer_count 获取给定长度
 * @return 返回session列表长度
 */
SDK_API int txc_list_sessions(_Out_ SESSION *sessions, int buffer_count);

/**
 * 接口说明：根据session id获取session场景信息
 *
 * @param id 场景session id
 * @return txc_session_info 场景信息
 */
SDK_API const txc_session_info *txc_get_session(SESSION id);

// 消息结构体定义
struct txc_msg_info
{
    unsigned long long tinyId;  // 消息来源id
    int type;                // 类型
    const char* content;      // 内容
    int duration;            // 时长
    int timestamp;           // 时间戳
    bool isRecv;             // 是否接收
};

/**
 * 接口说明：添加下载完成的消息到消息盒子
 *
 * @param msgInfo 消息体
 */
SDK_API void txc_xwei_msgbox_addmsg(txc_msg_info* msgInfo);

// 消息处理回调函数定义
typedef bool (*txc_event_processor)(SESSION id, XWM_EVENT event, XWPARAM arg1, XWPARAM arg2);

/**
 * 接口说明：添加消息处理回调函数
 *
 * @param processor 消息处理回调
 * @return 添加是否成功
 */
SDK_API bool txc_add_processor(txc_event_processor processor);

/**
 * 接口说明：移除消息处理回调函数
 *
 * @param processor 消息体
 */
SDK_API void txc_remove_processor(txc_event_processor processor);

/**
 * 接口说明：POST消息到控制层工作线程，异步处理
 *
 * @param id 场景session id
 * @param event 消息事件
 * @param arg1 参数1
 * @param arg2 参数2
 * @param delay 延迟时间
 */
SDK_API bool post_message(SESSION id, XWM_EVENT event, XWPARAM arg1, XWPARAM arg2, unsigned int delay);

/**
 * 接口说明：SEND消息到控制层工作线程，同步处理
 *
 * @param id 场景session id
 * @param event 消息事件
 * @param arg1 参数1
 * @param arg2 参数2
 */
SDK_API bool send_message(SESSION id, XWM_EVENT event, XWPARAM arg1, XWPARAM arg2);

CXX_EXTERN_END

#endif /* _AIAUDIO_AUDIOAPP_HPP_ */
