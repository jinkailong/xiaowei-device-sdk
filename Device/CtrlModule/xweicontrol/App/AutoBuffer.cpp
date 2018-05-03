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
#include "AutoBuffer.hpp"

CStringHolder::~CStringHolder()
{
    std::vector<std::string *>::iterator itr = storage_.begin();
    while (storage_.end() != itr)
    {
        std::string *&str = *itr;
        if (str)
        {
            delete str;
            str = NULL;
        }
        ++itr;
    }
    storage_.clear();
}

const char *CStringHolder::Hold(const char *content)
{
    const char *holded = NULL;

    if (content && content[0])
    {
        std::string *new_content = new std::string(content);
        storage_.push_back(new_content);
        holded = new_content->c_str();
    }

    return holded;
}

const char *CStringHolder::Hold(const char *content, size_t length)
{
    const char *holded = NULL;

    if (content && 0 < length)
    {
        std::string *new_content = new std::string(content, length);
        storage_.push_back(new_content);
        holded = new_content->c_str();
    }

    return holded;
}

void CStringHolder::Drop(const char *holder)
{
    for (std::vector<std::string *>::iterator itr = storage_.begin();
         storage_.end() != itr;
         ++itr)
    {
        std::string *holded = *itr;
        if (holded)
        {
            delete holded;
        }
        itr = storage_.erase(itr);
    }
}
