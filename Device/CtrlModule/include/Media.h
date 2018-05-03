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
#ifndef _AIAUDIO_MEDIA_H_
#define _AIAUDIO_MEDIA_H_
#include "txctypedef.h"

CXX_EXTERN_BEGIN

//////////////////// interface of media.h ////////////////////
// 媒体类型定义
enum MEDIA_TYPE
{
    TYPE_UNKNOW = 0,

    TYPE_BEGIN_TTS = 1,
    TYPE_TTS_TEXT, // txc_media_t::description is the text
    TYPE_TTS_TEXT_TIP, // txc_media_t::description is the text
    TYPE_TTS_OPUS, // txc_media_t::description is the text
    TYPE_TTS_MSGPROMPT, // txc_media_t::description is timestamp txc_media_t::content is tinyid

    TYPE_BEGIN_TEXT = 0x100,
    TYPE_URL,  // txc_media_t::description is the text
    TYPE_JSON, // txc_media_t::description is the text

    TYPE_BEGIN_MUSIC = 0x200,
    TYPE_MUSIC_URL, // txc_media_t::content is the URL, txc_media_t::description is the JSON stored music/new/FM information
    TYPE_MUSIC_URL_TIP, // txc_media_t::content is the URL
    
    TYPE_BEGIN_FILE = 0x400,
    TYPE_FILE,      // txc_media_t::description is the file path

    TYPE_BEGIN_MEDIA = 0x600,
    TYPE_H264,   // use txc_media_read to read media data
    TYPE_JPEG,   // use txc_media_read to read media data
    TYPE_LYRICS, // use txc_media_read to read media data

    TYPE_BEGIN_MISC = 0x800,
    TYPE_INFO_WEATHER, //  JSON, description of weather

    TYPE_USER_DEFINED = 0x1000,
};

// 媒体资源基本结构定义
struct txc_media_t
{
    const char *res_id;         // 资源id
    MEDIA_TYPE type;            // 媒体类型，参考MEDIA_TYPE
    unsigned long long offset;   // 播放偏移量

    int play_count;           // 如果是0，播放完毕之后要释放资源，-1表示没有限制播放次数
    const char *description;   // 资源meta信息 json结构
    const char *content;       // 播放内容，例如URL，文本内容等
};

struct txc_media_data_t
{
    const unsigned char *data;
    unsigned long length;
};

struct txc_progress_t
{
    const char *res_id; // * resource identity
    MEDIA_TYPE type;    // * MEDIA_TYPE

    long current_count;
    long total_count;
};

struct txc_download_msg_data_t
{
    unsigned long long tinyId;
    unsigned int channel;
    unsigned int type;
    const char* key;
    unsigned int key_length;
    const char* mini_token;
    unsigned int min_token_length;
    unsigned int duration;
    int timestamp;
};

CXX_EXTERN_END

#endif // _AIAUDIO_MEDIA_H_
