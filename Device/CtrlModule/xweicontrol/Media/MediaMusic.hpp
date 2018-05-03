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
#ifndef MediaMusic_hpp
#define MediaMusic_hpp

#include "Media.hpp"

// url类型的Media
class CMediaMusic : public CMedia
{
  public:
    void Init(const char *content, const char *mediaInfo, unsigned long long offset, unsigned int play_count);
    void Update(const char *content, const char *mediaInfo, unsigned long long offset);
    void Update(MEDIA_TYPE type);

    //  见 txc_media_read() 声明
    virtual int Read(_Out_ const void **data, _Out_ size_t *data_size, _In_ size_t offset);

  private:
    friend class TXCMediaCenter;

    CMediaMusic(const std::string &res_id);

  private:
    bool inited_;
};

#endif /* MediaMusic_hpp */
