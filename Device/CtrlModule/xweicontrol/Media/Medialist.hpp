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
#ifndef _AIAUDIO_PLAYLIST_HPP_
#define _AIAUDIO_PLAYLIST_HPP_

#include <vector>
#include <map>
#include "Playlist.h"
#include "Media.hpp"
#include "Player.h"

//////////////////// Medialist.hpp ////////////////////
typedef std::vector<PtrMedia> MediaArray;

//  play list, contains a list of TXCMedia and its' play sequence
class TXCMediaList
{
  public:
    TXCMediaList();
    const txc_playlist_t &GetInfo() const;
    void SetInfo(txc_playlist_t *info);

    //  @return index
    int Add(int index, PtrMedia &media);
    int Update();
    //    int Add(int index, const TXCAutoPtr<TXCMediaList> &other);
    bool Remove(int index);
    bool Remove(const std::vector<int> &vIndexes);
    void Clear();

    size_t Count() const;
    PtrMedia Get(int index);
    PtrMedia Get(const char *res_id);
    
    int Find(const char *res_id);
    std::string ToString();

  private:
    MediaArray media_array_;     //  媒体列表
    txc_playlist_t info_;
};

typedef TXCAutoPtr<TXCMediaList> PtrMediaList;

#endif /* _AIAUDIO_PLAYLIST_HPP_ */
