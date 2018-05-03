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

// 本头文件中的接口仅提供给Android平台使用，其他平台使用这些接口，我们不能保证正常使用
#ifndef __TXCAudioPrivate_H__
#define __TXCAudioPrivate_H__

#include "TXCAudioType.h"

CXX_EXTERN_BEGIN

// 登录状态信息
typedef struct _txca_param_login_status
{
    unsigned int type;           // 登录态类型 QQ 1 WX 2
    const char *app_id;         // 登录的appid
    const char *open_id;        // 登录的openId
    const char *access_token;   // 登录的accessToken
    const char *refresh_token;  // 登录的refreshToken
} TXCA_PARAM_LOGIN_STATUS;

/**
 * 接口说明：上报设备日志
 * 需要与txc_upload_file 与 txca_get_minidownload_url配合使用
 * url : 通过txc_upload_file上传小文件后，通过txca_get_minidownload_url得到下载地址
 * time : 日志上报时间
 * 返回值 : 错误码（见全局错误码表）
 */
SDK_API int txca_upload_log(char *voice_id, const char *url, const char *time);

/**
 * 接口说明：设置登录态，有屏设备登录态过期的时候需要使用
 * 返回值  ：错误码（见全局错误码表）
 */
SDK_API void txca_set_login_status(char *voice_id, TXCA_PARAM_LOGIN_STATUS *st);

/**
 * 接口说明：查询登录态
 * 返回值  ：错误码（见全局错误码表）
 */
SDK_API void txca_get_login_status(char *voice_id, const char *skill_id);

CXX_EXTERN_END

#endif /* __TXCAudioPrivate_H__ */
