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
#ifndef AudioFocusManager_hpp
#define AudioFocusManager_hpp

#include <vector>
#include <map>
#include <string>
#include "AudioFocus.h"

#include "txctypedef.h"

// 音频焦点改变监听
class OnAudioFocusChangeListener
{
  public:
    virtual ~OnAudioFocusChangeListener()
    {
        
    };
    virtual void OnAudioFocusChange(int focus_change) = 0;
};

class CFocusItem
{
  public:
    CFocusItem();
    ~CFocusItem();

    int cookie;         // cookie
    DURATION_HINT hint; // 申请的焦点类型
    unsigned int need;  // 需要的焦点数量。AUDIOFOCUS_GAIN(3)、AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK(2)、AUDIOFOCUS_GAIN_TRANSIENT(3)
    unsigned int old;   // 记录改变之前的焦点数量
    unsigned int cur;   // 记录改变后的焦点数量
    bool recoverable;   // 焦点是否可恢复 AUDIOFOCUS_GAIN(true)
    OnAudioFocusChangeListener *listener;

    std::string ToString();
};

class COuterFocusListener : public OnAudioFocusChangeListener
{
public:
    COuterFocusListener()
    : cookie(-1)
    {
    };
    
    int cookie;
    virtual void OnAudioFocusChange(int focusChange);
};

class TXCAudioFocusManager
{
  public:
    TXCAudioFocusManager();
    virtual ~TXCAudioFocusManager();

    // 使用SESSIONID来请求焦点，如果该id不存在关联的OnAudioFocusChangeListener，会返回false，是否申请到以关联的listener的回调为准
    bool RequestAudioFocus(SESSION id);
    // 为listener申请duration类型的焦点，并关联id和listener，是否申请到以listener的回调为准
    void RequestAudioFocus(SESSION id, OnAudioFocusChangeListener *listener, DURATION_HINT duration);
    
    // 使用SESSIONID来释放焦点，如果该id不存在关联的OnAudioFocusChangeListener，会返回false
    bool AbandonAudioFocus(SESSION id);
    // 释放listener的焦点
    bool AbandonAudioFocus(OnAudioFocusChangeListener *listener);
    // 释放所有焦点，这个操作会导致所有注册的listener都收到AUDIOFOCUS_LOSS
    void AbandonAllAudioFocus();
    // 设置可以用的焦点，例如Android的音乐APP占用了焦点，那么XweiControl中分配焦点数量会相应调整
    void SetAudioFocus(DURATION_HINT hint);

    bool HandleAudioFocusMessage(SESSION id, XWM_EVENT event, XWPARAM arg1, XWPARAM arg2);
private:
    bool RequestOutAudioFocus(int cookie);
    void RequestOutAudioFocus(int cookie, COuterFocusListener *listener, DURATION_HINT duration);
    bool AbandonOutAudioFocus(int cookie);
    
    int GetCookie(SESSION id);
    bool RemoveFocusItem(int cookie);
    unsigned int RemoveFocusItem(OnAudioFocusChangeListener *listener);
    void DispatchAudioFocus(unsigned int focus);
    bool CallbackFocusChange(CFocusItem item);
    int GetFocus(DURATION_HINT duration);
  private:
    std::vector<CFocusItem> mFocusItems;
    std::map<int, CFocusItem> mFocusItemsMap;
    std::map<int, int> mId2Cookie;
    // 记录可分配的焦点数量
    unsigned int mFocus;
    bool mFocusTransitivity;
};

#endif /* AudioFocusManager_hpp */
