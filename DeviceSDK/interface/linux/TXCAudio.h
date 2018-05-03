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
#ifndef __TX_CLOUD_AUDIO_H__
#define __TX_CLOUD_AUDIO_H__

#include "TXCAudioType.h"

CXX_EXTERN_BEGIN

//语音云服务回调
typedef struct _txca_callback
{
    bool (*on_request_callback)(const char *voice_id, TXCA_EVENT event, const char *state_info, const char *extend_info, unsigned int extend_info_len); // 会话请求相关回调
    bool (*on_net_delay_callback)(const char *voice_id, unsigned long long time);
} TXCA_CALLBACK;

/**
 * 接口说明：Start AI Audio相关服务，该服务需要登录成功后才能调用，否则会有错误码返回
 * 参数说明：callback AIAudio服务回调接口
 * 返回值  ：错误码（见全局错误码表）
 */
SDK_API int txca_service_start(TXCA_CALLBACK *callback, TXCA_PARAM_ACCOUNT *account);

/**
 * 接口说明：Stop AI Audio相关服务
 * 返回值  ：错误码（见全局错误码表）
 */
SDK_API int txca_service_stop();

/**
 * 接口说明：开始会话请求(普通请求同时只会有一个， 云端校验会有额外的请求)
 * 返回值  ：错误码（见全局错误码表）
 */
SDK_API int txca_request(char *voice_id, TXCA_CHAT_TYPE type, const char *chat_data, unsigned int char_data_len, TXCA_PARAM_CONTEXT *context);

/**
 * 接口说明：cancel会话请求
 * 返回值  ：错误码（见全局错误码表）
 */
SDK_API int txca_request_cancel(const char *voice_id);

/**
 * 接口说明：cancel TTS推流
 * 参数说明：res_id TTS推流对应的id
 * 返回值  ：错误码（见全局错误码表）
 */
SDK_API int txca_tts_cancel(const char *res_id);
///////////////////////////////////////////////////

CXX_EXTERN_END

#endif // __TX_CLOUD_AUDIO_H__
