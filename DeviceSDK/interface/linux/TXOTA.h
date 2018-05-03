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
#ifndef __OTA_H__
#define __OTA_H__

#include "TXSDKCommonDef.h"

CXX_EXTERN_BEGIN

// 新的OTA通知
typedef struct _tx_ota_result
{
    /**
     * 收到OTA更新信息
     *
     * @param from    来源 0 定时自动检测 1 App操作 2 ServerPush 3 设备主动查询
     * @param force   是否强制升级
     * @param version 版本号
     * @param title   更新标题
     * @param desc    更新详情
     * @param url     下载链接
     * @param md5     升级包Md5值
     */
    void (*on_ota_result)(int from, bool force, unsigned int version, const char *title, const char *desc, const char *url, const char *md5); //SDK状态回调
} TX_OTA_RESULT;

/**
 * 主动查询OTA升级信息
 */
SDK_API int tx_query_ota_update();

/**
 * 配置ota信息的回调
 *
 * @param callback 回调接口，请参考结构体TX_OTA_RESULT定义
 */
SDK_API void tx_config_ota(TX_OTA_RESULT *callback);

CXX_EXTERN_END

#endif
