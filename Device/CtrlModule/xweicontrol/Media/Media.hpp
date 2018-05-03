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
#ifndef _AIAUDIO_MEDIA_HPP_
#define _AIAUDIO_MEDIA_HPP_

//////////////////// Media.hpp ////////////////////
#include "Media.h"
#include "ThirdLib.h"
#include <string>

class CMediaPiece
{
  public:
    virtual ~CMediaPiece();
    virtual bool operator<(const CMediaPiece &other) const = 0;

    const txc_media_data_t &GetData() { return data_info_; };

  protected:
    txc_media_data_t data_info_;
};

class CMedia
{
  public:
    virtual ~CMedia(){};

    const txc_media_t &GetInfo();

    void DecPlayCnt();
    int GetPlayCnt();

    //  见 txc_media_read() 声明
    virtual int Read(_Out_ const void **data, _Out_ size_t *data_size, _In_ size_t offset) = 0;

  protected:
    CMedia();
    friend class TXCMediaCenter;

    std::string res_id_;      // play resource id
    std::string content_;     // play resource content
    std::string description_; // play resource description
    unsigned long long offset_;     // play resource offset
    int play_count_;          // play count default is -1 no limit

    txc_media_t info_; //pointer to res_id_, content_, description_
};

#define TXCAutoPtr tpl::shared_ptr
#define TXCMutex fast_mutex

typedef TXCAutoPtr<CMedia> PtrMedia;

//  @res_id: resource id from txc_media_t::res_id
//  @data   out data address, don't free its memory;
//  @data_size: out data size, in BYTEs
//  @offset:    data offset
//  @return EXIT_SUCCESS    0 // 成功完成，所有数据读取完成
//          EBADF        9        /* Bad file descriptor */ // 没找到资源
//          EINVAL        22        /* Invalid argument */  // 参数错误，参数指针为空
//          EFBIG        27        /* File too large */    // 数据太多，本次读不完，后面还有数据，可继续读取
//          ERANGE        34        /* Result too large */  // offset 超出范围
//          EAGAIN        35        /* Resource temporarily unavailable */  // 暂时没有数据了，需等待传输完成
SDK_API int txc_media_read(_In_ const char *res_id, _Out_ const void **data, _Out_ size_t *data_size, _In_ size_t offset);

//void txc_media_update(const txc_media_t *media, const txc_media_data_t *media_data);
#endif /* _AIAUDIO_MEDIA_HPP_ */
