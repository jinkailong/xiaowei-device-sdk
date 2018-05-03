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

#include <stdio.h>
#include <string.h>
#include "AudioApp.h"
#include "AudioFocus.h"
#include "Player.h"
#include "XWeiAudioEngine.h"
#include "XWeiTTSManager.h"
#include "XWeiDevice.h"
#include "XWeiPlayer.h"
#include "Util.h"


static void* WorkThread(void* pThreadData)
{
    CXWeiAudioEngine* pEngine = (CXWeiAudioEngine*)pThreadData;
    if(NULL == pEngine) {
        printf("[error]WorkThread pEngine is null.\n");
        return NULL;
    }

    CAudioRecorder recorder(pEngine);
    if(recorder.ReadData(30000000)) {
        //do nothing
    }
    else {
        txca_request_cancel("");
    }

    pEngine = NULL;
    printf("WorkThread exit.\n");
    return NULL;
}

// 通道层请求回调处理入口
bool on_request_callback(const char* voice_id, TXCA_EVENT event, const char* state_info, const char* extend_info, unsigned int extend_info_len)
{

    if (event == txca_event_on_tts) {
        // 处理TTS推流数据
        TXCA_PARAM_AUDIO_DATA *pRsp = (TXCA_PARAM_AUDIO_DATA *)state_info;
        if (pRsp) {
            CXWeiTTSManager::instance()->write(pRsp);
        }
        return true;
    } else {

        bool handled = CXWeiApp::instance().AudioEngine().OnCallbackRequest(voice_id, event, state_info, extend_info, extend_info_len);
        
        return handled;
    }
}

// 控制层回调处理入口
XWPARAM control_callback(SESSION id, TXC_PLAYER_ACTION action, XWPARAM data, XWPARAM data_length)
{
    printf("control_callback, id:%d, action:%d.\n", id, action);
    if (action == ACT_DOWNLOAD_MSG || action == ACT_AUDIOMSG_RECORD || action == ACT_AUDIOMSG_SEND) {
        // TODO: 消息盒子处理逻辑
    } else {
        g_xwei_player_mgr.OnCallback(id, action, data, data_length);
    }

    return XWPARAM(0);
}


void CXWeiAudioEngine::OnRecorderData(char* buffer, unsigned int len)
{
    if(!m_isWakeuped) {
        return;
    }

    char voice_id[33] = {0};
    
    // 如果不是多轮会话，则重新构造context_
    if (last_session_id_.empty()) {
        memset(&context_, 0, sizeof(context_));

        context_.id = voice_id;
        context_.speak_timeout = DEFAULT_SPEAK_TIMEOUT;
        context_.silent_timeout = DEFAULT_SILENT_TIMEOUT;
    }
    
    int ret = txca_request(voice_id, txca_chat_via_voice, buffer, len, &context_);
    
    printf("txca_request voice_id[%s], ret[%d]\n", voice_id, ret);
}

bool CXWeiAudioEngine::IsStopRecorder()
{
    return !m_isWakeuped;
}

CXWeiAudioEngine::CXWeiAudioEngine()
    : m_isInited(false)
    , m_thread_work_id(0)
    , m_isWakeuped(false)
    , m_wakeup_cookie(-1)
{

}

CXWeiAudioEngine::~CXWeiAudioEngine()
{

}

// 请求事件分发处理
bool CXWeiAudioEngine::OnCallbackRequest(const char* voice_id, TXCA_EVENT event, const char* state_info, const char* extend_info, unsigned int extend_info_len)
{
    printf("OnCallbackRequest, event:%d\n", event);

    bool handled = false;

    // 两种不同的请求：
    // 1. 语音请求会有以下各个event事件的回调
    // 2. 通用请求只会有txca_event_on_response事件的回调
    switch(event)
    {
    case txca_event_on_idle:
        {
            // 空闲状态
            printf(">>>>>>>>>>>>>>OnCallbackRequest, txca_event_on_idle.\n");
        }
        break;
    case txca_event_on_request_start:
        {
            // 请求开始
            printf(">>>>>>>>>>>>>>OnCallbackRequest, on_request_start.\n");
        }
        break;
    case txca_event_on_speak:
        {
            // 检测到有说话
            printf(">>>>>>>>>>>>>>OnCallbackRequest, on_speak.\n");
        }
        break;
    case txca_event_on_silent:
        {
            // 后台返回静音尾点，停止塞语音数据
            printf(">>>>>>>>>>>>>>OnCallbackRequest, on_silent.\n");
            OnSilence();
        }
        break;
    case txca_event_on_recognize:
        {
            // 中间asr识别结果，无屏设备可忽略
            std::string param_recognize = extend_info;
            if(!param_recognize.empty()) {
                printf(">>>>>>>>>>>>>>OnCallbackRequest, recognize:%s.\n", param_recognize.c_str());
            }
        }
        break;
    case txca_event_on_response:
        {
            // 收到请求响应，将请求响应传给控制层处理
            printf(">>>>>>>>>>>>>>OnCallbackRequest, txca_event_on_response.\n");
        }
        break;
    case txca_event_on_voice_data:
        {
            // 语音请求的音频数据，用于外部保存数据到文件
            printf(">>>>>>>>>>>>>>OnCallbackRequest, txca_event_on_voice_data.\n");
        }
        break;
    default:
        break;
    }

    if (!handled) {
        // 由控制层处理响应结果数据
        handled = txc_process_response(voice_id, event, state_info, extend_info, extend_info_len);
    }

    // 释放唤醒场景下请求的声音焦点
    if (m_wakeup_cookie != INVALID_WAKEUP_COOKIE) {
        txc_abandon_audio_focus(m_wakeup_cookie);
        m_wakeup_cookie = INVALID_WAKEUP_COOKIE;
    }

    return handled;
}

void CXWeiAudioEngine::OnTextQuery()
{
    char voice_id[33] = {0};
    TXCA_PARAM_CONTEXT context = {0};
    char* text = "我要听刘德华的歌";
    txca_request(voice_id, txca_chat_via_text, text, strlen(text), &context);
}

// 本地唤醒, 语音请求
void CXWeiAudioEngine::OnWakeup()
{
    StopThread();
    
    // 唤醒时，先申请声音焦点
    if (m_wakeup_cookie == INVALID_WAKEUP_COOKIE) {
        txc_request_audio_focus(m_wakeup_cookie, AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
    }

    m_isWakeuped = true;

    context_.voice_request_begin = true;
    context_.wakeup_type = txca_wakeup_type_local;

    StartThread();
    printf("OnWakeup StartThread.\n");
}

// 后台返回静音
void CXWeiAudioEngine::OnSilence()
{
    if (IsWakeup()) {
        m_isWakeuped = false;

        last_session_id_.clear();
        memset(&context_, 0, sizeof(context_));
    }
}

void CXWeiAudioEngine::OnCancel()
{
    if (IsWakeup()) {
        m_isWakeuped = false;
        
        // 释放唤醒场景下请求的声音焦点
        if (m_wakeup_cookie != INVALID_WAKEUP_COOKIE) {
            txc_abandon_audio_focus(m_wakeup_cookie);
            m_wakeup_cookie = INVALID_WAKEUP_COOKIE;
        }

        last_session_id_.clear();
        memset(&context_, 0, sizeof(context_));
        txca_request_cancel("");
    }
}

// 多轮会话情况下自动唤醒
bool CXWeiAudioEngine::OnSupplement(long wait_time, const TXCA_PARAM_RESPONSE *response)
{
    printf("OnSupplement.wait_time[%lu]\n", wait_time);

    if (response && response->context.id && response->context.id[0]) {
        last_session_id_ = response->context.id;
        context_ = response->context;
        context_.id = last_session_id_.c_str();
        context_.speak_timeout = wait_time;
        context_.silent_timeout = DEFAULT_SILENT_TIMEOUT;
    } else {
        last_session_id_.clear();
        memset(&context_, 0, sizeof(context_));
    }

    OnWakeup();

    return true;
}

bool CXWeiAudioEngine::IsWakeup()
{
    return m_isWakeuped;
}

bool CXWeiAudioEngine::StartThread()
{
    int ret = pthread_create(&m_thread_work_id, NULL, WorkThread, (void*)this);
    if (ret || !m_thread_work_id) {
        printf("[error]CXWeiAudioEngine::StartThread pthread_create failed %d\n", ret);
        return false;
    }
    
    return true;
}

void CXWeiAudioEngine::StopThread()
{
    if(m_thread_work_id != 0) {
        pthread_join(m_thread_work_id, NULL);
        m_thread_work_id = 0;
    }
}

// 语音服务引擎初始化
bool CXWeiAudioEngine::Init()
{
    // 不要重复进行初始化
    if(m_isInited) {
        return true;
    }

    int ret = 0;

    // 1. 通道层语音服务初始化
    TXCA_CALLBACK callback = {0};
    callback.on_request_callback = on_request_callback;
    TXCA_PARAM_ACCOUNT param_account = {0};
    ret = txca_service_start(&callback, &param_account);
    if(err_null != ret) {
        printf("[error]txca_service_start failed %d\n", ret);
        return false;
    }

    // 2. 控制层初始化，控制层为独立项目已开源
    txc_xwei_control control = {0};
    control.control_callback = control_callback;
    txc_xwei_control_init(&control);

    printf("CTXAIAAudioEngine.Init.\n");
    m_isInited = true;

    return true;
}

// 反初始化语音服务
void CXWeiAudioEngine::Uninit()
{
    if(IsWakeup()) {
        OnCancel();
        StopThread();
        txca_request_cancel("");
    }

    if(m_isInited) {
        m_isInited = false;
        txca_service_stop();
        printf("CTXAIAAudioEngine.Uninit.\n");
    }
    return;
}
