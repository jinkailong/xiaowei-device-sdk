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
#pragma once

#include <string>
#include <pthread.h>
#include "TXCAudio.h"
#include "AudioRecorder.h"

#define INVALID_WAKEUP_COOKIE -1
#define DEFAULT_SPEAK_TIMEOUT 5000  // 最多多久不说话超时时间
#define DEFAULT_SILENT_TIMEOUT 500  // 静音尾点超时时间

class CXWeiAudioEngine : public IAudioRecorderDataRecver
{
public:
    CXWeiAudioEngine();
    ~CXWeiAudioEngine();

    bool Init();
    void Uninit();

    bool OnCallbackRequest(const char* voice_id, TXCA_EVENT event, const char* state_info, const char* extend_info, unsigned int extend_info_len);
    bool OnCallbackControl(const char*);

    void OnWakeup();
    void OnSilence();
    void OnCancel();
    void OnTextQuery();
    bool OnSupplement(long wait_time, const TXCA_PARAM_RESPONSE *response);

//for thread
    bool IsWakeup();
    void OnRecorderData(char* buffer, unsigned int len);
    bool IsStopRecorder();

protected:
    bool StartThread();
    void StopThread();

private:
    bool m_isInited;

    pthread_t m_thread_work_id;
    volatile bool m_isWakeuped;
    int m_wakeup_cookie;
    TXCA_PARAM_CONTEXT context_;
    std::string last_session_id_;
    std::string last_voice_id_;
};