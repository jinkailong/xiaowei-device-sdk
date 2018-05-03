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
#include "Medialist.hpp"
#include "TXCServices.hpp"
#include "logger.h"
#include <memory.h>
#include <sstream>

TXCMediaList::TXCMediaList()
{
    memset(&info_, 0, sizeof(info_));
}

int TXCMediaList::Add(int index, PtrMedia &media)
{
    int pos = -1;
    MediaArray::iterator itr = media_array_.begin();
    if (index >= 0 && index < (int)media_array_.size())
    {
        media_array_.insert(itr + index, media);
        pos = index;
    }
    else
    {
        media_array_.push_back(media);
        pos = (int)media_array_.size() - 1;
    }

    info_.count = (int)media_array_.size();
    return pos;
}

//int TXCMediaList::Add(int index, const TXCAutoPtr<TXCMediaList> &other)
//{
//    //  TODO:
//
//
//    info_.count = media_array_.size();
//    return info_.count;
//}

bool TXCMediaList::Remove(int index)
{
    bool exists = false;
    if (index >= 0 && index < (int)media_array_.size())
    {
        MediaArray::iterator itr = media_array_.begin() + index;
        if (media_array_.end() != itr)
        {
            PtrMedia media = *itr;

            //  删除内容，但是保留占位，保证 index 不变。
            itr->reset();

            exists = true;

            //  clear reference in media center
            if (media.use_count() == 2)
            {
                TXCServices::instance()->GetMediaCenter()->RemoveMedia(media->GetInfo().res_id);
            }

            info_.count = (int)media_array_.size();
        }
    }
    return exists;
}

void TXCMediaList::Clear()
{
    MediaArray::iterator itr = media_array_.begin();
    while (media_array_.end() != itr)
    {
        PtrMedia &media = *itr;

        //  clear reference in media center
        if (media.use_count() == 2)
        {
            TXCServices::instance()->GetMediaCenter()->RemoveMedia(media->GetInfo().res_id);
        }
        ++itr;
    }
    media_array_.clear();

    info_.count = 0;
}

const txc_playlist_t &TXCMediaList::GetInfo() const
{
    return info_;
}

void TXCMediaList::SetInfo(txc_playlist_t *info)
{
    info_.playlist_id = info->playlist_id;
    info_.count = info->count;
    info_.type = info->type;
    info_.hasMore = info->hasMore;
}

size_t TXCMediaList::Count() const
{
    return info_.count;
}

PtrMedia TXCMediaList::Get(int index)
{
    PtrMedia result;

    if (index >= 0 && index < (int)media_array_.size())
    {
        result = media_array_[index];
    }

    return result;
}

PtrMedia TXCMediaList::Get(const char *res_id)
{
    PtrMedia result;

    if (res_id && res_id[0])
    {
        MediaArray::iterator itr = media_array_.begin();
        for (; !result.get() && media_array_.end() != itr; ++itr)
        {
            PtrMedia &media = *itr;
            if (media.get())
            {
                const txc_media_t &info = media->GetInfo();
                if (info.res_id && info.res_id[0])
                {
                    if (0 == strcmp(info.res_id, res_id))
                    {
                        result = media;
                        break;
                    }
                }
            }
        }
    }

    return result;
}

int TXCMediaList::Find(const char *res_id)
{
    int index = -1;

    if (res_id && res_id[0])
    {
        int find_index = 0;
        for (MediaArray::iterator itr = media_array_.begin();
             media_array_.end() != itr;
             ++itr, find_index++)
        {
            PtrMedia &media = *itr;
            if (media.get())
            {
                const txc_media_t &info = media->GetInfo();
                if (info.res_id && info.res_id[0])
                {
                    if (0 == strcmp(info.res_id, res_id))
                    {
                        index = find_index;
                        break;
                    }
                }
            }
        }
    }

    return index;
}

std::string TXCMediaList::ToString()
{
    std::stringstream ss;
    ss<< "[TXCMediaList:";
    ss<< "size:";
    ss<< media_array_.size();
    ss<< " ";
    for (MediaArray::iterator itr = media_array_.begin();
         media_array_.end() != itr;
         ++itr)
    {
        PtrMedia &media = *itr;
        if (media.get())
        {
            ss << "{";
            const txc_media_t &info = media->GetInfo();
            if (info.res_id && info.res_id[0])
            {
                ss << "resId:";
                ss << info.res_id;
                ss <<", ";
            }
            if (info.description && info.description[0]){
            ss << "description:";
            ss<< info.description;
            }
        ss << "},";
        } else {
            ss<<"{null},";
        }
    }
    
    ss<< "]";
    return ss.str();
}

PtrMediaList GetMediaList(SESSION id)
{
    PtrMediaList result;

    PtrPlayer player = TXCServices::instance()->GetPlayerManager()->GetPlayer(id);
    if (player.get())
    {
        result = player->GetMediaList();
    }

    return result;
}

const struct txc_playlist_t *txc_get_medialist_info(int playlist_id)
{
    const struct txc_playlist_t *result = NULL;

    PtrMediaList playlist = GetMediaList(playlist_id);
    if (playlist.get())
    {
        result = &(playlist->GetInfo());
    }

    return result;
}

//  get media from playlist
const struct txc_media_t *txc_get_media(int playlist_id, long index)
{
    const struct txc_media_t *result = NULL;

    PtrMediaList playlist = GetMediaList(playlist_id);
    if (playlist.get())
    {
        PtrMedia media = playlist->Get((int)index);
        if (media.get())
        {
            result = &(media->GetInfo());
        }
    }

    return result;
}

SDK_API const void txc_clear_media(int playlist_id)
{
    PtrMediaList playlist = GetMediaList(playlist_id);
    if (playlist.get())
    {
        playlist->Clear();
    }
}

SDK_API const struct txc_playlist_t *txc_remove_media(int playlist_id, long index)
{
    const struct txc_playlist_t *result = NULL;

    PtrMediaList playlist = GetMediaList(playlist_id);
    if (playlist.get())
    {
        if (playlist->Remove((int)index))
        {
            result = &(playlist->GetInfo());
        }
    }

    return result;
}
