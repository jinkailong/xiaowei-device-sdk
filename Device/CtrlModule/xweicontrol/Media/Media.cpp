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
#include "Media.hpp"
#include <cstddef>
#include <string.h>
#include "TXCServices.hpp"

CMedia::CMedia()
{
    memset(&info_, 0, sizeof(info_));
    offset_ = 0;
    play_count_ = 0;
}

const txc_media_t &CMedia::GetInfo()
{
    return info_;
}

void CMedia::DecPlayCnt()
{
    if (play_count_ > 0)
    {
        play_count_--;
        info_.play_count = play_count_;
    }
}

int CMedia::GetPlayCnt()
{
    return play_count_;
}

CMediaPiece::~CMediaPiece()
{
}

int txc_media_read(_In_ const char *res_id, _Out_ const void **data, _Out_ size_t *data_size, _In_ size_t offset)
{
    return TXCServices::instance()->GetMediaCenter()->ReadMedia(res_id, data, data_size, offset);
}
