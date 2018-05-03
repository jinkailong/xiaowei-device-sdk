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
#ifndef __TX_CLOUD_AUDIO_COMMON_H__
#define __TX_CLOUD_AUDIO_COMMON_H__

#include "TXCAudioType.h"

CXX_EXTERN_BEGIN

/**
* 接口说明：report play state
* 返回值  ：错误码（见全局错误码表）
*/
SDK_API int txca_report_state(TXCA_PARAM_STATE *state);

/**
 * 接口说明：用户对这次请求不满意错误反馈，上报上一次请求到后台
 * 返回值  ：错误码（见全局错误码表）
 */
SDK_API int txca_error_feed_back();

/**
 * 接口说明：上报一些事件，用于计算整个链路的耗时
 * 返回值  ：错误码（见全局错误码表）
 */
SDK_API int txca_data_report(TXCA_PARAM_LOG *log);

/**
 * 接口说明：查询音乐会员信息
 * 返回值  ：错误码（见全局错误码表）
 */
SDK_API void txca_get_music_vip_info(char *voice_id);

/**
 * 获取指定格式的提示类TTS，一般用于QQ电话、消息、导航等特殊场景
 * @param voice_id  TTS的resId
 * @param tinyid    目标用户id，电话和消息需要填写
 * @param timestamp 时间 ,消息需要填，其余填0
 * @param cmd      类别
 **/
SDK_API int txca_request_protocol_tts(char* voice_id, unsigned long long tinyid, unsigned long long timestamp, int cmd);

/**
 * 接口说明：在某些场景下，可设置设备状态，正常退出场景后，需要调用txca_clear_user_state清除
 * 返回值  ：错误码（见全局错误码表）
 */
SDK_API int txca_set_user_state(TXCA_PARAM_STATE *state);

/**
 * 接口说明：清除设备状态, 与txca_set_user_state配合使用
 * 返回值 ：错误码（见全局错误码表）
 */
SDK_API int txca_clear_user_state();

///////////////////////////////////////////////////

CXX_EXTERN_END

#endif // __TX_CLOUD_AUDIO_COMMON_H__
