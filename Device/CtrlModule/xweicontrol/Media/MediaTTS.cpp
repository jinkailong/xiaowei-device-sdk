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
#include <string.h>
#include "MediaTTS.hpp"
#include "TXCServices.hpp"
#include "logger.h"

CMediaTTS::CMediaTTS(const std::string &res_id)
{
    memset(&info_, 0, sizeof(info_));
    res_id_ = res_id;
    offset_ = 0;
    play_count_ = -1;

    info_.res_id = res_id_.c_str();
    info_.type = TYPE_TTS_OPUS;

    sample_ = 0;
    channel_ = 0;
    pcm_sample_ = 0;

    inited_ = false;
    completed_ = false;
}

void CMediaTTS::Init(const char *content, unsigned int play_count)
{
    if (!inited_)
    {
        content_ = (content == NULL ? "" : content);
        play_count_ = play_count > 0 ? play_count : -1;

        info_.content = content_.c_str();
        info_.play_count = play_count_;
        inited_ = true;
    }
}

void CMediaTTS::SetComplete()
{
    completed_ = true;
}

int CMediaTTS::Read(_Out_ const void **data, _Out_ size_t *data_size, _In_ size_t offset)
{
    return ERR_SUCCESS;
}
