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
#ifndef __TX_CLOUD_AUDIO_TYPE_H__
#define __TX_CLOUD_AUDIO_TYPE_H__

#include "TXSDKCommonDef.h"

CXX_EXTERN_BEGIN

/**
 *
 * 语音云通道层接口
 *
 */

// txca_request单次音频数据最大/小长度
#define TXCA_BUF_MAX_LENGTH 6400
#define TXCA_BUF_MIN_LENGTH 64

// 各个上报事件的宏定义
#define TXCA_PARAM_LOG_EVENT_PLAYER_START        "client_OnStart"          // 播放器开始播放
#define TXCA_PARAM_LOG_EVENT_PLAYER_PREPARE      "client_OnPrepareAsync"   // 播放器准备播放
#define TXCA_PARAM_LOG_EVENT_TTS_BEGIN           "client_OnTTSBegin"       // TTS推流开始
#define TXCA_PARAM_LOG_EVENT_TTS_PREPARED        "client_OnTTSCallback"    // TTS推流中
#define TXCA_PARAM_LOG_EVENT_TTS_END             "client_OnTTSEnd"         // TTS推流结束
#define TXCA_PARAM_LOG_EVENT_QQCALL_CALL_OUT     "status_qqcall_call_out"  // 主叫
#define TXCA_PARAM_LOG_EVENT_QQCALL_CALL_INVITE  "status_qqcall_invite"    // 被叫
#define TXCA_PARAM_LOG_EVENT_QQCALL_ING          "status_qqcall_ing"       // 通话中
#define TXCA_PARAM_LOG_EVENT_QQCALL_OUT          "status_qqcall_out"       // 退出通话

// 语音请求回调on_request_callback接口相关事件定义
typedef enum _txca_event {
    txca_event_on_idle          = 0,    // 空闲
    txca_event_on_request_start = 1,    // 请求开始
    txca_event_on_speak         = 2,    // 检测到说话
    txca_event_on_silent        = 3,    // 检测到静音(only@device has not txca_device_local_vad)
    txca_event_on_recognize     = 4,    // 识别文本实时返回
    txca_event_on_response      = 5,    // 请求收到响应
    txca_event_on_tts           = 6,    // 小微后台推送的TTS信息
    txca_event_on_voice_data    = 7,    // 语音请求的音频数据，用于外部保存数据到文件
} TXCA_EVENT;

// 硬件设备支持属性定义
typedef enum _txca_param_property {
    txca_param_local_vad = 0x0000000000000001,    // 使用本地静音检测
    txca_param_gps       = 0x0000000000000002,    // 使用GPS位置
    txca_param_local_tts = 0x0000000000000004,    // 使用本地TTS
    txca_param_dump_silk = 0x0000000000000008,    // dump silk编码的音频文件
    txca_param_only_vad  = 0x0000000000000010,    // 只做后台静音检测
} TXCA_PARAM_PROPERTY;

// 资源格式定义
typedef enum _txca_resource_format {
    txca_resource_url      = 0,    // url资源
    txca_resource_text     = 1,    // 纯文本(无TTS)
    txca_resource_tts      = 2,    // TTS播放(附带文本信息)
    txca_resource_file     = 3,    // 提醒类
    txca_resource_location = 4,    // 位置
    txca_resource_command  = 5,    // 指令类型
    txca_resource_intent   = 6,    // 语义类型
    txca_resource_unknown  = 99,   // 未知类型
} TXCA_RESOURCE_FORMAT;

// 资源播放类型操作定义
typedef enum _txca_playlist_action {
    txca_playlist_replace_all     = 0,    // 中断当前播放，替换列表
    txca_playlist_enqueue_front   = 1,    // 拼接到列表队头
    txca_playlist_enqueue_back    = 2,    // 拼接到列表队尾
    txca_playlist_replace_enqueue = 3,    // 不中断当前播放的资源，替换列表的详情
    txca_playlist_update_enqueue  = 4,    // 不中断播放，更新列表中某些播放资源的url和quality字段信息
    txca_playlist_remove          = 5,    // 从播放列表中移除这些资源
} TXCA_PLAYLIST_ACTION;

// 语音请求类型
typedef enum _txca_chat_type {
    txca_chat_via_voice        = 0,    // 语音请求
    txca_chat_via_text         = 1,    // 文本请求
    txca_chat_only_tts         = 2,    // tts请求
    txca_chat_via_intent       = 3,    // 意图请求
    txca_chat_via_wakeup_check = 4,    // 唤醒校验请求
} TXCA_CHAT_TYPE;

// 设备播放状态
typedef enum _txca_playstate {
    txca_playstate_preload  = 0,    // 预加载
    txca_playstate_start    = 1,    // 一首歌开始播放
    txca_playstate_paused   = 2,    // 暂停
    txca_playstate_stopped  = 3,    // 一首歌播放完毕
    txca_playstate_finished = 4,    // 所有资源播放结束，停止播放了
    txca_playstate_idle     = 5,    // 空闲状态
    txca_playstate_resume   = 6,    // 继续
    txca_playstate_abort   = 11,   // 播放中断
} TXCA_PLAYSTATE;

// 使用自有 App 绑定小微设备的账号类型
typedef enum _txca_account_type {
    txca_account_null = 0,  // 默认值，使用小微 App 时使用
    txca_account_qq   = 1,  // QQ 账户登录
    txca_account_wx   = 2,  // 微信账户绑定登录
    txca_account_3rd  = 3,  // 第三方账户云接口绑定
} TXCA_ACCOUNT_TYPE;

// 云端校验唤醒返回的标识位
typedef enum _txca_wakeup_flag {
    txca_wakeup_flag_no           = 0,    // 不是云端校验唤醒的结果
    txca_wakeup_flag_fail         = 1,    // 唤醒校验失败
    txca_wakeup_flag_suc          = 2,    // 成功唤醒，只说了唤醒词没有连续说话
    txca_wakeup_flag_suc_rsp      = 3,    // 成功唤醒并且收到了最终响应
    txca_wakeup_flag_suc_continue = 4,    // 成功唤醒并且还需要继续传声音，还不知道会不会连续说话
} TXCA_WAKEUP_FLAG;

// 音频数据编码格式
typedef enum _txca_audio_data_format {
    txca_audio_data_pcm  = 0,
    txca_audio_data_silk = 1,
    txca_audio_data_opus = 2,
} TXCA_AUDIO_DATA_FORMAT;

// 唤醒类型
typedef enum _txca_wakeup_type {
    txca_wakeup_type_local           = 0,    // 本地唤醒
    txca_wakeup_type_cloud           = 1,    // 云端校验唤醒
    txca_wakeup_type_local_with_text = 2,    // 本地唤醒带唤醒词文本
} TXCA_WAKEUP_TYPE;

// 唤醒场景定义
typedef enum _txca_wakeup_profile {
    txca_wakeup_profile_far  = 0,    // 远场
    txca_wakeup_profile_near = 1,    // 近场，例如遥控器
} TXCA_WAKEUP_PROFILE;

//  使用自有 App 绑定小微设备的账号相关信息
typedef struct _txca_param_account
{
    unsigned int type;         // 请参考TXCA_ACCOUNT_TYPE
    const char *account;      // 账户名，如果type是QQ/WX登录，则表示openid；如果type是3rd，表示使用云绑定接口时传入的App自有账户名
    const char *token;        // 账户token，如果type是QQ/WX登录表示accessToken，其他类型传空即可
    const char *appid;        // 账户名和token对应的appid，在QQ/WX登录时使用，其他类型传空即可
    char *buffer;            // 账号扩展参数
    unsigned int buffer_len;  // 账号扩展参数长度
} TXCA_PARAM_ACCOUNT;

typedef struct _txca_param_log
{
    int type; //1=ULS; 2=ALR; 3=All;

    const char *event;
    int ret_code;
    unsigned long long time_stamp_ms;
    const char *voice_id;
    const char *log_data;
    unsigned int sub_cmd;

    const char *skill_name;
    const char *skill_id;
} TXCA_PARAM_LOG;

// 场景信息
typedef struct _txca_param_skill
{
    const char *name;    // 场景名
    const char *id;      // 场景ID
    int type;
} TXCA_PARAM_SKILL;

// 播放资源结构定义
typedef struct _txca_param_resource
{
    TXCA_RESOURCE_FORMAT format;    // 资源格式定义
    char *id;                    // 资源ID
    char *content;              //  资源内容
    int play_count;             // 资源播放次数, 如果 value == -1, means no limit
    unsigned long long offset;         // URL播放资源
    char *extend_buffer;       // json格式的资源描述信息
} TXCA_PARAM_RESOURCE;

// 播放资源集合
typedef struct _txca_param_res_group
{
    TXCA_PARAM_RESOURCE *resources;    // 一个集合中包含的播放资源
    unsigned int resources_size;     // 资源个数
} TXCA_PARAM_RES_GROUP;

// 上下文
typedef struct _txca_param_context
{
    const char *id;                    // 上下文 ID，多轮会话情况下需要带上该ID
    unsigned int speak_timeout;        // 等待用户说话的超时时间
    unsigned int silent_timeout;       // 用户说话的静音尾点时间 单位：ms
    bool voice_request_begin;        // 声音请求的首包标志，首包时，必须为true
    bool voice_request_end;          // 当使用本地VAD时，声音尾包置为true
    TXCA_WAKEUP_PROFILE wakeup_profile; // 唤醒场景，远场or近场
    TXCA_WAKEUP_TYPE wakeup_type;      // 唤醒类型
    const char *wakeup_word;         // 唤醒词文本
    unsigned long long request_param; // 请求参数 TXCA_PARAM_PROPERTY
} TXCA_PARAM_CONTEXT;

// 语音云返回数据
typedef struct _txca_param_response
{
    TXCA_PARAM_SKILL skill_info;                // 场景信息
    TXCA_PARAM_SKILL last_skill_info;           // 之前的场景信息

    unsigned int error_code;                   // 请求错误码

    char voice_id[33];                        // 请求id
    TXCA_PARAM_CONTEXT context;                 // 上下文信息

    const char *request_text;                 // ASR结果文本
    unsigned int response_type;               // 用于信息展示的json数据type
    const char *response_data;               // 用于信息展示的json数据

    unsigned int resource_groups_size;       // 资源集合列表size
    TXCA_PARAM_RES_GROUP *resource_groups;     // 资源集合列表
    bool has_more_playlist;                // 是否可以加载更多
    bool is_recovery;                      // 是否可以恢复播放
    bool is_notify;                        // 是通知
    unsigned int wakeup_flag;               // 请参考TXCA_WAKEUP_FLAG 云端校验唤醒请求带下来的结果，0表示非该类结果，1表示唤醒失败，2表示唤醒成功并且未连续说话，3表示说的指令唤醒词，4可能为中间结果，表示唤醒成功了，还在继续检测连续说话或者已经在连续说话了
    TXCA_PLAYLIST_ACTION play_behavior;       // 列表拼接类型
    
    char *auto_test_data;            // 用于自动化测试的相关信息，可以忽略
} TXCA_PARAM_RESPONSE;

// 上报状态信息
typedef struct _txca_param_state
{
    TXCA_PARAM_SKILL skill_info;    // 场景信息
    unsigned int play_state;       // 请参考TXCA_PLAYSTATE
    const char *play_id;           // 资源ID
    const char *play_content;      // 资源内容
    unsigned long long play_offset; // 播放偏移量，单位：s
    unsigned int play_mode;        // 播放模式
} TXCA_PARAM_STATE;

// 音频数据 TTS
typedef struct _txca_param_audio_data
{
    const char *id;                // 资源id
    unsigned int seq;              // 序号
    unsigned int is_end;           // 最后一包了
    unsigned int pcm_sample_rate;  // pcm采样率
    unsigned int sample_rate;      // 音频数据的(例如:opus)采样率
    unsigned int channel;          // 声道
    TXCA_AUDIO_DATA_FORMAT format;   // 格式(例如:opus)
    const char *raw_data;         // 数据内容
    unsigned int raw_data_len;    // 数据长度
} TXCA_PARAM_AUDIO_DATA;

//消息 接收方/发送方 信息
typedef struct tag_txca_ccmsg_inst_info
{
    unsigned long long target_id; // ccmsg target id for send to / from
    unsigned int appid;
    unsigned int instid;
    unsigned int platform;       // 指定平台
    unsigned int open_appid;     // 开平分配给第三方app的appid
    unsigned int productid;      //
    unsigned int sso_bid;       // SSO终端管理分配的appid
    char *guid;              // 设备的唯一标识
    int guid_len;
} TXCA_CCMSG_INST_INFO;

// 语音请求的音频数据，用于外部保存数据到文件
typedef struct tag_tx_voice_data
{
    char* raw_data;            // 音频数据
    unsigned int raw_data_len;  // 音频数据长度
}TXCA_VOICE_DATA;

CXX_EXTERN_END

#endif // __TX_CLOUD_AUDIO_TYPE_H__
