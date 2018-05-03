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
#ifndef PlayList_hpp
#define PlayList_hpp

#include <vector>
#include <string>
#include "AudioApp.h"
#include "Player.h"
#include "Playlist.h"

struct txc_media_t;

class CPlaylist
{
  public:
    typedef int UI_INDEX;   //  index for UI display
    typedef long SRC_INDEX; //  index in media source

    CPlaylist();
    CPlaylist(REPEAT_MODE repeatMode, SESSION id);

    size_t Count();

    long PushBack(const std::vector<txc_play_item_t> &list);
    long PushFront(const std::vector<txc_play_item_t> &list);
    bool Remove(std::string resId);
    void Clear();
    //  TODO:
    UI_INDEX Insert(UI_INDEX index, const txc_play_item_t &item);

    void SetRepeat(REPEAT_MODE mode);

    REPEAT_MODE GetRepeat();

    const txc_play_item_t *GetItem(UI_INDEX index);
    SRC_INDEX Seek(UI_INDEX index); //  seek item in the ui list;

    SRC_INDEX NextX(long offset, bool isAuto); //  next item in the order/repeat list;
    SRC_INDEX NextY();                         //  next index in current item/group;

    UI_INDEX GetCurIndex();

  private:
    void AddRandomList(size_t first, size_t count);

  private:
    std::vector<txc_play_item_t> list_;
    std::vector<UI_INDEX> repeat_list_;

    REPEAT_MODE repeat_;
    SESSION id_;
    UI_INDEX x_;       //  current_group_index, in play order，内部列表的索引值，随机播放可能和x_in_ui_不同，其他模式是一样的
    UI_INDEX x_in_ui_; //  current_group_index, in ui order，UI界面的播放列表中的索引值
    UI_INDEX y_;       //  current_group_item_index，播放
};

#endif /* PlayList_hpp */
