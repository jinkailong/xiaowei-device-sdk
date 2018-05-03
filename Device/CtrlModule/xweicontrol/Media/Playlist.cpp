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
#include <stdlib.h>
#include "Playlist.hpp"
#include "Playlist.h"
#include "Player.h"
#include "Media.h"
#include "TXCServices.hpp"
#include "logger.h"

CPlaylist::CPlaylist()
{
    repeat_ = REPEAT_SEQUENCE;
    x_ = -1;
    y_ = -1;
    id_ = -1;
}

CPlaylist::CPlaylist(REPEAT_MODE repeatMode, SESSION id)
{
    repeat_ = repeatMode;
    x_ = -1;
    y_ = -1;
    id_ = id;
}

long CPlaylist::PushBack(const std::vector<txc_play_item_t> &list)
{
    size_t count = list.size();
    size_t org_count = list_.size();
    list_.insert(list_.end(), list.begin(), list.end());

    if (count > 0 && org_count == 0)
    {
        x_ = 0;
        x_in_ui_ = 0;
    }

    if (REPEAT_RANDOM == repeat_)
    {
        AddRandomList(org_count, count);
    }

    TLOG_DEBUG("sessionId=%d CPlaylist::PushBack list[size]=%d list_[size]=%d x_=%d ui_x_=%d", id_, count, org_count, x_, x_in_ui_);
    return count;
}

long CPlaylist::PushFront(const std::vector<txc_play_item_t> &list)
{
    size_t count = list.size();
    size_t org_count = list_.size();
    list_.insert(list_.begin(), list.begin(), list.end());

    if (count > 0)
    {
        x_ += count;
        x_in_ui_ += count;
    }

    if (REPEAT_RANDOM == repeat_)
    {
        // 照顾一下历史歌曲的顺序
        repeat_list_.clear();
        for (unsigned int i = 0; i < count; i++)
        {
            repeat_list_.push_back(i);
        }
        // 之后的下标随机一下
        AddRandomList(count, org_count);
    }

    TLOG_DEBUG("sessionId=%d CPlaylist::PushFront list[size]=%d list_[size]=%d x_=%d ui_x_=%d", id_, count, org_count, x_, x_in_ui_);
    return count;
}


bool CPlaylist::Remove(std::string resId)
{
    const txc_player_info_t *player_info = txc_get_player_info(id_);
    int i = 0;
    for(std::vector<txc_play_item_t>::iterator iter = list_.begin(); iter != list_.end(); i++) {
        txc_play_item_t item = *iter;
        for (int j = 0; j < item.count && j < PLAY_ITEM_GROUP_MAX_SIZE; j++)
        {
            long src_index = item.group[j];
            
            const txc_media_t *temp = txc_get_media(player_info->playlist_id, src_index);
            
            if (temp != NULL && resId == temp->res_id)
            {
                if(i == x_in_ui_) {
                    return false;
                }
                
                list_.erase(iter++);
                
                if(i < x_in_ui_){
                    x_in_ui_--;
                }
                
                if (REPEAT_RANDOM != repeat_)
                {
                    // 如果删掉了正在播放的之前的元素，fix一下当前播放的下标。
                    if(i < x_){
                        x_--;
                    }
                } else {
                    repeat_list_.clear();
                    AddRandomList(0, list_.size());
                    
                    // fix x_
                    for(unsigned int k = 0; k < repeat_list_.size(); k++) {
                        if(x_in_ui_ == repeat_list_[k]) {
                            x_ = k;
                            break;
                        }
                    }
                }
                
                return true;
            }
        }
        iter++;
    }
    
    return false;
}

void CPlaylist::Clear()
{
    list_.clear();

    x_ = -1;
    y_ = -1;
}

size_t CPlaylist::Count()
{
    return list_.size();
}

void CPlaylist::SetRepeat(REPEAT_MODE mode)
{
    if (mode != repeat_)
    {
        if (REPEAT_RANDOM == mode)
        {
            repeat_list_.clear();
            AddRandomList(0, list_.size());
        }
        else
        {
            x_ = x_in_ui_;
        }
        repeat_ = mode;
    }
}

REPEAT_MODE CPlaylist::GetRepeat()
{
    return repeat_;
}

const txc_play_item_t *CPlaylist::GetItem(UI_INDEX index)
{
    txc_play_item_t *item = NULL;

    if (0 <= index && index < (int)list_.size())
    {
        item = &(list_[index]);
    }

    return item;
}

CPlaylist::SRC_INDEX CPlaylist::Seek(UI_INDEX index)
{ //  seek item in the list;
    TLOG_DEBUG("sessionId=%d CPlaylist::Seek index: %d", id_, index);
    CPlaylist::SRC_INDEX src_index = -1;
    y_ = -1;

    if (0 <= index && index < (int)list_.size())
    {
        x_in_ui_ = index;
        if (repeat_ != REPEAT_RANDOM)
        {
            x_ = index;
        }

        const txc_play_item_t &item = list_[index];
        if (item.count > 0)
        {
            src_index = NextY();
        }
    }

    return src_index;
}

CPlaylist::SRC_INDEX CPlaylist::NextX(long offset, bool isAuto)
{
    CPlaylist::SRC_INDEX src_index = -1;
    CPlaylist::UI_INDEX ui_index = -1;
    UI_INDEX old_y = y_;
    y_ = -1;
    const size_t list_count = list_.size();

    if (REPEAT_RANDOM == repeat_)
    {
        long index = x_ + offset;
        size_t repeat_count = repeat_list_.size();
        if (0 < repeat_count)
        {
            if(index < 0) {
                index = -index % repeat_count;
                index = repeat_count - index;
            } else {
                index = index % repeat_count;
            }

            x_ = (UI_INDEX)index;
            ui_index = repeat_list_[index];
        }
    }
    else if (REPEAT_SINGLE == repeat_)
    {
        if (isAuto)
        {
            ui_index = x_;
        }
        else
        {
            // if user do next, just link REPEAT_LOOP mode
            ui_index = x_ + (int)offset;

            if (ui_index < 0)
            {
                ui_index += (UI_INDEX)list_count;
            }
            else
            {
                ui_index = ui_index % list_count;
            }
        }
    }
    else if (REPEAT_LOOP == repeat_)
    {
        ui_index = x_ + (int)offset;

        if (ui_index < 0)
        {
            ui_index += (UI_INDEX)list_count;
        }
        else
        {
            ui_index = ui_index % list_count;
        }
    }
    else if (REPEAT_SEQUENCE == repeat_)
    {
        ui_index = x_ + (int)offset;

        if (isAuto)
        {
            if (ui_index < 0)
            {
                ui_index = x_;
            }
            else if (ui_index >= (int)list_count)
            {
                ui_index = -1;
            }
        }
        else
        {
            if (ui_index < 0 || ui_index >= (int)list_count)
            {
                ui_index = -1;
            }
        }
    }

    if (0 <= ui_index && (int)list_count > ui_index)
    {

        x_ = (repeat_ != REPEAT_RANDOM ? ui_index : x_);

        x_in_ui_ = ui_index;
        const txc_play_item_t &item = list_[ui_index];

        if (0 < item.count)
        {
            src_index = NextY();// 取这个Group的第一个可播放元素
        }
    }

    if(src_index == -1) {
        y_ = old_y;// 切换失败了恢复y
    }
    return src_index;
}

CPlaylist::UI_INDEX CPlaylist::GetCurIndex()
{
    return x_in_ui_;
}

CPlaylist::SRC_INDEX CPlaylist::NextY()
{ //  next index in current item/group;
    TLOG_DEBUG("sessionId=%d CPlaylist::NextY y_:%d", id_, y_);

    CPlaylist::SRC_INDEX src_index = -1;

    if (0 <= x_in_ui_ && x_in_ui_ < (int)list_.size())
    {
        const txc_play_item_t &item = list_[x_in_ui_];
        TLOG_DEBUG("sessionId=%d CPlaylist::NextY item.count:%d", id_, item.count);
        int i = (y_ >= 0 ? y_ + 1 : 0);

        for (; i < item.count; i++)
        {
            const txc_player_info_t *player_info = txc_get_player_info(id_);
            if (player_info)
            {
                const txc_playlist_t *playlist_info = txc_get_medialist_info(player_info->playlist_id);
                if (playlist_info && playlist_info->count > 0)
                {
                    const txc_media_t *media = txc_get_media(player_info->playlist_id, item.group[i]);
                    if (media != NULL && TXCServices::instance()->GetMediaCenter()->IsMediaNeedPlay(media->res_id))
                    {
                        src_index = item.group[i];
                        y_ = i;
                        break;
                    }
                }
            }
        }
    }

    return src_index;
}

void CPlaylist::AddRandomList(size_t first, size_t count)
{
    if (0 < count)
    {
        {
            srand((unsigned int)time(0));
            std::vector<size_t> v_rand;

            while (count > v_rand.size())
            {
                size_t target = rand() % count + first;
                std::vector<size_t>::iterator itr = std::find(v_rand.begin(), v_rand.end(), target);
                if (v_rand.end() == itr)
                {
                    v_rand.push_back(target);
                }
            }

            repeat_list_.insert(repeat_list_.end(), v_rand.begin(), v_rand.end());
        }
    }
}
