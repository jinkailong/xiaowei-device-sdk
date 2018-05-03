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
#include "MediaMsgPrompt.hpp"

CMediaMsgPrompt::CMediaMsgPrompt(const std::string& res_id)
: inited_(false)
{
    res_id_ = res_id;
    
    info_.res_id = res_id_.c_str();
    info_.type = TYPE_TTS_MSGPROMPT;
}

void CMediaMsgPrompt::Init(const char *content, const char *description, unsigned int repeat_count)
{
    if (!inited_) {
        content_ = (content == NULL ? "" : content);
        description_ = (description == NULL ? "" : description);
        play_count_ = (repeat_count > 0 ? repeat_count : -1);
        
        info_.content = content_.c_str();
        info_.description = description_.c_str();
        info_.play_count = play_count_;
        
        inited_ = true;
    }
}

int CMediaMsgPrompt::Read(_Out_ const void **data, _Out_ size_t *data_size, _In_ size_t offset)
{
    *data = description_.c_str();
    *data_size = description_.length();
    return ERR_SUCCESS;
}
