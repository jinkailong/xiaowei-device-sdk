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
#ifndef _AIAUDIO_PLAYLIST_H_
#define _AIAUDIO_PLAYLIST_H_

//////////////////// interface of Playlist.h ////////////////////

#include "txctypedef.h"
#include "Media.h"

CXX_EXTERN_BEGIN

// 播放列表项结构体定义
#define PLAY_ITEM_GROUP_MAX_SIZE 32
struct txc_play_item_t
{
    long group[PLAY_ITEM_GROUP_MAX_SIZE];
    int count; // item count in group[];
};

// 播放列表信息结构体定义
struct txc_playlist_t
{
    int playlist_id; // 播放列表id
    int type;
    int count;       // 播放列表资源数
    bool hasMore;    // 列表资源是否有更多
};

/**
 * 接口说明：根据播放列表id拉取播放列表信息
 *
 * @param playlist_id 播放列表id
 * @return 播放列表信息
 */
SDK_API const struct txc_playlist_t *txc_get_medialist_info(int playlist_id);

/**
 * 接口说明：拉取播放列表中的播放项信息
 *
 * @param playlist_id 播放列表id
 * @param index 索引
 * @return 播放列表信息
 */
SDK_API const struct txc_media_t *txc_get_media(int playlist_id, long index);

/**
 * 接口说明：添加播放项到播放列表中(未实现)
 *
 * @param playlist_id 播放列表id
 * @param index 索引
 * @return 播放列表信息
 */
SDK_API txc_playlist_t *txc_add_to_playlist(int playlist_id, long index, _In_ const txc_media_t *media);

/**
 * 接口说明：移除某个播放列表中的播放项
 *
 * @param playlist_id 播放列表id
 * @param index 待删除项的索引信息
 * @return 播放列表信息
 */
SDK_API const struct txc_playlist_t *txc_remove_media(int playlist_id, long index);

/**
 * 接口说明：清空播放列表
 *
 * @param playlist_id 播放列表id
 */
SDK_API const void txc_clear_media(int playlist_id);

CXX_EXTERN_END

#endif /* _AIAUDIO_PLAYLIST_H_ */
