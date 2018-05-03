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
#ifndef __TX_CLOUD_AUDIO_RESOURCE_H__
#define __TX_CLOUD_AUDIO_RESOURCE_H__

#include "TXCAudioType.h"

CXX_EXTERN_BEGIN

/**
 * 接口说明：用于预加载播放列表
 * 返回值：错误码（见全局错误码表）
 */
SDK_API int txca_resource_get_list(char *voice_id, const TXCA_PARAM_SKILL *skill_info, const char *play_id, unsigned int max_list_size, bool is_up);

/**
 * 接口说明：用于加载播放类详情信息
 * param：play_id_list playid数组
 * param：list_size    playid数组长度
 * 返回值：错误码（见全局错误码表）
 */
SDK_API int txca_resource_get_detail_info(char *voice_id, const TXCA_PARAM_SKILL *skill_info, char **list_play_id, int list_size);

/**
 * 接口说明：用于刷新播放列表的url
 * param：play_id_list playid数组
 * param：list_size    playid数组长度
 * 返回值：错误码（见全局错误码表）
 */
SDK_API int txca_resource_refresh_list(char *voice_id, const TXCA_PARAM_SKILL *skill_info, char **list_play_id, int list_size);

/**
 * 接口说明：用于设置某个资源是否被收藏
 * param：play_id 播放资源的id
 * param：is_favorite 是否要收藏 true表示收藏，false表示取消收藏
 * 返回值：错误码（见全局错误码表）
 */
SDK_API int txca_resource_set_favorite(char *voice_id, const TXCA_PARAM_SKILL *skill_info, const char *play_id, bool is_favorite);

/**
 * 接口说明:设置资源的品质（如：音乐的品质）
 * quality: 品质的值（目前用于音乐资源，0 流畅 1 标准 2 高品质 3 无损）
 * ret: 0成功 非0失败
 */
SDK_API int txca_resource_set_quality(int quality);

///////////////////////////////////////////////////

CXX_EXTERN_END

#endif // __TX_CLOUD_AUDIO_RESOURCE_H__
