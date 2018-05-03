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
#ifndef __TXCAudioRemind_H__
#define __TXCAudioRemind_H__

#include "TXCAudioType.h"

CXX_EXTERN_BEGIN

/**
 * 获取闹钟或提醒的结果回调
 */
typedef void (*TXCA_GET_ALARM_LIST_CALLBACK)(const char* voice_id, int err_code, const char ** remind_list, unsigned int count);

/**
 * 获取闹钟提醒列表
 * @param voice_id 返回的请求id
 * @param callback 闹钟提醒结果回调
 */
SDK_API int txca_get_alarm_list(char *voice_id, TXCA_GET_ALARM_LIST_CALLBACK callback);

/**
 * 设置提醒的结果回调
 */
typedef void (*TXCA_SET_ALARM_CALLBACK)(const char* voice_id, int err_code, unsigned int alarm_id);

/**
 * 设置/更新闹钟提醒
 * @param voice_id 返回的请求id
 * @param optType 操作类型 1.增加 2.修改 3.删除 4.修改闹钟类型
 * @param alarm_info_json 操作对应的json结构
 * @param notify 结果返回
 */
SDK_API int txca_set_alarm_info(char* voice_id, int optType, const char* alarm_info_json, TXCA_SET_ALARM_CALLBACK cb);

/**
 * 拉取定时Skill的播放资源
 * @param voice_id 返回的请求id
 * @param clock_id 定时Skill任务的id
 */
SDK_API int txca_get_timing_skill_resources(char * voice_id, const char * alarm_id);

CXX_EXTERN_END

#endif /* __TXCAudioRemind_H__ */
