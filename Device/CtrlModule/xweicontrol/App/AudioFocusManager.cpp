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
#ifndef AudioFocusManager_cpp
#define AudioFocusManager_cpp

#include "logger.h"
#include "AudioFocusManager.hpp"
#include "Util.hpp"

#include "TXCServices.hpp"
#include <sstream>

#define MAX_FOCUS 3

static int s_cookie = 0;

CFocusItem::CFocusItem()
    : cookie(0), need(0), old(0), cur(0), recoverable(false), listener(NULL)
{
}

CFocusItem::~CFocusItem()
{
    listener = NULL;
}

std::string CFocusItem::ToString()
{
    std::stringstream str;
    str << "cookie=";
    str << cookie;
    str << ",hint=";
    str << Util::ToString(hint);
    str << ",need=";
    str << need;
    str << ",cur=";
    str << cur;
    str << ",old=";
    str << old;
    str << ",listener=";
    str << listener;
    return str.str();
}

TXCAudioFocusManager::TXCAudioFocusManager()
    : mFocus(MAX_FOCUS)
,mFocusTransitivity(true)
{
}

TXCAudioFocusManager::~TXCAudioFocusManager()
{
    mFocusItems.clear();
}

bool TXCAudioFocusManager::RequestAudioFocus(SESSION id)
{
    int cookie = GetCookie(id);
    std::map<int, CFocusItem>::iterator itor = mFocusItemsMap.find(cookie);
    if (itor != mFocusItemsMap.end())
    {
        RequestAudioFocus(id, itor->second.listener, itor->second.hint);
        return true;
    }
    return false;
}

void TXCAudioFocusManager::RequestAudioFocus(SESSION id, OnAudioFocusChangeListener *listener, DURATION_HINT duration)
{
    if (duration < AUDIOFOCUS_GAIN || duration > AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE || listener == NULL)
    {
        return;
    }
    // 如果listener已经存在，移除之前的
    RemoveFocusItem(listener);
    
    // 存储id->cookie
    int cookie = GetCookie(id);
    if(cookie < 0) {
        cookie = ++s_cookie;
        mId2Cookie[id] = cookie;
    }

    CFocusItem item;
    item.recoverable = duration == AUDIOFOCUS_GAIN;
    item.cookie = cookie;
    item.hint = duration;
    item.need = GetFocus(duration);
    item.listener = listener;

    TLOG_DEBUG("sessionId=%d TXCAudioFocusManager::RequestAudioFocus cookie=%d hint=%s", id, item.cookie, Util::ToString(item.hint).c_str());
    mFocusItems.push_back(item);
    mFocusItemsMap[cookie] = item;

    // 重新按照顺序分配焦点
    DispatchAudioFocus(mFocus);
}

bool TXCAudioFocusManager::AbandonAudioFocus(SESSION id)
{
    int cookie = GetCookie(id);
    std::map<int, CFocusItem>::iterator itor = mFocusItemsMap.find(cookie);
    if (itor != mFocusItemsMap.end())
    {
        return AbandonAudioFocus(itor->second.listener);
    }
    return false;
}

bool TXCAudioFocusManager::AbandonAudioFocus(OnAudioFocusChangeListener *listener)
{
    if (listener == NULL)
    {
        return false;
    }
    int cookie = RemoveFocusItem(listener);
    if (cookie != 0)
    {
        TLOG_DEBUG("sessionId=%d TXCAudioFocusManager::AbandonAudioFocus cookie=%d hint=AUDIOFOCUS_LOSS");
        if(mFocusTransitivity) {
            TLOG_DEBUG("cookie=%d TXCAudioFocusManager::AbandonAudioFocus want DispatchAudioFocus.", cookie);
            DispatchAudioFocus(mFocus);
        }
        if (listener != NULL)
        {
            listener->OnAudioFocusChange(AUDIOFOCUS_LOSS);
        }
    }
    return true;
}

void TXCAudioFocusManager::AbandonAllAudioFocus()
{
    TLOG_TRACE("TXCAudioFocusManager::AbandonAllAudioFocus");
    for (std::vector<CFocusItem>::iterator iter = mFocusItems.begin(); iter != mFocusItems.end(); iter++)
    {
        iter->listener->OnAudioFocusChange(AUDIOFOCUS_LOSS);
    }
    mFocusItems.clear();
}

bool TXCAudioFocusManager::RequestOutAudioFocus(int cookie)
{
    TLOG_DEBUG("cookie=%d TXCAudioFocusManager::RequestOutAudioFocus.", cookie);
    std::map<int, CFocusItem>::iterator itor = mFocusItemsMap.find(cookie);
    if (itor != mFocusItemsMap.end())
    {
        COuterFocusListener * listener = dynamic_cast<COuterFocusListener *>(itor->second.listener);
        if(listener != NULL) {
            RequestOutAudioFocus(cookie, listener, itor->second.hint);
            return true;
        }
    }
    return false;
}

void TXCAudioFocusManager::RequestOutAudioFocus(int cookie, COuterFocusListener *listener, DURATION_HINT duration)
{
    if (duration < AUDIOFOCUS_GAIN || duration > AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE || listener == NULL)
    {
        return;
    }
    // 如果listener已经存在，移除之前的
    RemoveFocusItem(cookie);

    CFocusItem item;
    item.recoverable = duration == AUDIOFOCUS_GAIN;
    item.cookie = cookie;
    item.hint = duration;
    item.need = GetFocus(duration);
    item.listener = listener;
    
    TLOG_DEBUG("cookie=%d TXCAudioFocusManager::RequestOutAudioFocus hint=%s.", item.cookie, Util::ToString(item.hint).c_str());
    mFocusItems.push_back(item);
    mFocusItemsMap[cookie] = item;
    
    // 重新按照顺序分配焦点
    DispatchAudioFocus(mFocus);
}

bool TXCAudioFocusManager::AbandonOutAudioFocus(int cookie)
{
    TLOG_TRACE("cookie=%d TXCAudioFocusManager::AbandonOutAudioFocus.", cookie);
    std::map<int, CFocusItem>::iterator itor = mFocusItemsMap.find(cookie);
    if (itor != mFocusItemsMap.end())
    {
        COuterFocusListener * listener = dynamic_cast<COuterFocusListener *>(itor->second.listener);
        if(listener != NULL) {
            listener->OnAudioFocusChange(AUDIOFOCUS_LOSS);
            TLOG_TRACE("cookie=%d TXCAudioFocusManager::AbandonOutAudioFocus cookie=%d hint=AUDIOFOCUS_LOSS", cookie, cookie);
            if (RemoveFocusItem(cookie))
            {
                if(mFocusTransitivity) {
                    TLOG_DEBUG("cookie=%d TXCAudioFocusManager::AbandonOutAudioFocus want DispatchAudioFocus.", cookie);
                    DispatchAudioFocus(mFocus);
                }
            }
            return true;
        }
    }
    return false;
}

int TXCAudioFocusManager::GetCookie(SESSION id)
{
    std::map<int, int>::iterator itor = mId2Cookie.find(id);
    if (itor != mId2Cookie.end())
    {
        return itor->second;
    }
    return -1;
}

int TXCAudioFocusManager::GetFocus(DURATION_HINT duration)
{
    if(duration == AUDIOFOCUS_GAIN){
        return MAX_FOCUS;
    }
    if(duration == AUDIOFOCUS_GAIN_TRANSIENT || duration == AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE) {
        return MAX_FOCUS;// 其他会暂停
    }
    if(duration == AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK) {
        return 2;// 其他会降低音量
    }
    return 0;
}

unsigned int TXCAudioFocusManager::RemoveFocusItem(OnAudioFocusChangeListener *listener)
{
    int cookie = 0;
    for (std::vector<CFocusItem>::iterator iter = mFocusItems.begin(); iter != mFocusItems.end(); iter++)
    {
        if (iter->listener == listener)
        {
            cookie = iter->cookie;
            RemoveFocusItem(cookie);
            break;
        }
    }
    return cookie;
}

bool TXCAudioFocusManager::RemoveFocusItem(int cookie)
{
    bool find = false;
    for (std::vector<CFocusItem>::iterator iter = mFocusItems.begin(); iter != mFocusItems.end(); iter++)
    {
        if (iter->cookie == cookie)
        {
            find = true;
            // 如果是Out，erase+delete，否则erase
            COuterFocusListener * lis = dynamic_cast<COuterFocusListener *>(iter->listener);
            mFocusItemsMap.erase(cookie);
            mFocusItems.erase(iter);
            if(lis != NULL) {
                delete lis;
            }
            break;
        }
    }
    return find;
}

void TXCAudioFocusManager::DispatchAudioFocus(unsigned int duration)
{
    if(mFocusItems.size() == 0){
        return;
    }
    unsigned int focus = duration; // 可以分配的焦点
    unsigned int found = 0;        // 之前已经分配的焦点

    // 依次取出CFocusItem，分配focus给它，如果分配给它后还有剩余focus，就分给下一个。同时记录之前分配的焦点数。直到分配完毕并且之前的焦点都找到了，就结束循环。
    for (std::vector<CFocusItem>::iterator iter = mFocusItems.end() - 1; iter >= mFocusItems.begin(); iter--)
    {
        iter->old = iter->cur;
        iter->cur = std::min(iter->need, focus);
        found += iter->old;
        focus -= iter->cur;
        
        if (iter->cur != iter->old)
        { // 旧的和新的不一致，就回调
            TLOG_TRACE("TXCAudioFocusManager::DispatchAudioFocus cookie=%d cur=%u old=%u focus=%u found=%u", iter->cookie, iter->cur, iter->old, focus, found);
            if (CallbackFocusChange(*iter))
            {
                // 这个焦点被释放了
                // 如果是Out，erase+delete，否则erase
                COuterFocusListener * listener = dynamic_cast<COuterFocusListener *>(iter->listener);
                mFocusItemsMap.erase(iter->cookie);
                mFocusItems.erase(iter);
                if(listener != NULL) {
                    delete listener;
                }
            }
        }
        
        if (found == mFocus && focus <= 0)
        {
            // 找到了所有的焦点了，没更多了
            break;
        }
    }
}

void TXCAudioFocusManager::SetAudioFocus(DURATION_HINT hint)
{
    TLOG_TRACE("TXCAudioFocusManager::SetAudioFocus hint=%s mFocus[%d] mFocusTransitivity[%d]", Util::ToString(hint).c_str(), mFocus, mFocusTransitivity);
    int focus = 0;
    bool focusTransitivity = true;
    switch (hint) {
        case AUDIOFOCUS_GAIN:
        case AUDIOFOCUS_GAIN_TRANSIENT:
            focus = 3;
            break;
        case AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
            focus = 3;
            // 不可传递
            if(mFocus == 0) {
                focusTransitivity = false;
            }
            break;
        case AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
            focus = 2;
            break;
        case AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
            focus = 1;
            break;
        default:
            focus = 0;
            break;
    }
    if ((int)mFocus != focus || mFocusTransitivity != focusTransitivity)
    {
        DispatchAudioFocus(focus);
        mFocus = focus;
        mFocusTransitivity = focusTransitivity;
    }
}

bool TXCAudioFocusManager::CallbackFocusChange(CFocusItem item)
{
    if (item.cur == item.need)
    {
        TLOG_TRACE("TXCAudioFocusManager::CallbackFocusChange @1 cookie=%d hint=%s", item.cookie, Util::ToString(item.hint).c_str());
        item.listener->OnAudioFocusChange(item.hint);
    }
    else if (item.cur == 0)
    {
        TLOG_TRACE("TXCAudioFocusManager::CallbackFocusChange @2 cookie=%d hint=%s", item.cookie, Util::ToString(item.recoverable ? AUDIOFOCUS_LOSS_TRANSIENT : AUDIOFOCUS_LOSS).c_str());
        item.listener->OnAudioFocusChange(item.recoverable ? AUDIOFOCUS_LOSS_TRANSIENT : AUDIOFOCUS_LOSS);
        return !item.recoverable;
    }
    else if (item.cur < item.need)
    {
        TLOG_TRACE("TXCAudioFocusManager::CallbackFocusChange @3 cookie=%d hint=AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK", item.cookie);
        item.listener->OnAudioFocusChange(AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK);
    }
    return false;
}

// 过滤掉焦点类型的消息
bool TXCAudioFocusManager::HandleAudioFocusMessage(SESSION id, XWM_EVENT event, XWPARAM arg1, XWPARAM arg2)
{
    TLOG_TRACE("sessionId=%d TXCAudioFocusManager::HandleAudioFocusMessage event=%s, arg1=%ld, arg2=%ld.", id, Util::ToString(event).c_str(), arg1, arg2);
    switch (event)
    {
        case XWM_REQUEST_AUDIO_FOCUS:
        {
            if (id == -1)
            {
                int cookie = (int)reinterpret_cast<long>(arg1);
                DURATION_HINT hint = (DURATION_HINT)reinterpret_cast<long>(arg2);
                COuterFocusListener* listener = new COuterFocusListener;
                listener->cookie = cookie;
                TXCServices::instance()->GetAudioFocusManager()->RequestOutAudioFocus(cookie, listener, hint);
            }
            else
            {
                TXCServices::instance()->GetAudioFocusManager()->RequestAudioFocus(id);
            }
            return true;
        }
        case XWM_ABANDON_AUDIO_FOCUS:
        {
            if ((bool)arg1)
            {
                TXCServices::instance()->GetAudioFocusManager()->AbandonAllAudioFocus();
            }
            else
            {
                int cookie = 0;
                if(arg2 != NULL) {
                   cookie = (int)reinterpret_cast<long>(arg2);
                }
                if(cookie > 0) {
                    TXCServices::instance()->GetAudioFocusManager()->AbandonOutAudioFocus(cookie);
                } else {
                    TXCServices::instance()->GetAudioFocusManager()->AbandonAudioFocus(id);
                }
            }
            return true;
        }
        case XWM_SET_AUDIO_FOCUS:
        {
            TXCServices::instance()->GetAudioFocusManager()->SetAudioFocus((DURATION_HINT)(reinterpret_cast<long>(arg1)));
            return true;
        }
        default:
            break;
    }
    return false;
}

audio_focus_change_function g_focus_change;

void COuterFocusListener::OnAudioFocusChange(int focusChange)
{
    if(g_focus_change) {
        g_focus_change(cookie, focusChange);
    }
}

SDK_API void txc_set_audio_focus_change_callback(audio_focus_change_function func)
{
    g_focus_change = func;
}

SDK_API void txc_request_audio_focus(int &cookie, DURATION_HINT duration)
{
    if(cookie <= 0) {
        cookie = ++ s_cookie;
    }
    post_message(-1, XWM_REQUEST_AUDIO_FOCUS, XWPARAM((long)cookie), XWPARAM(duration));
}

SDK_API void txc_abandon_audio_focus(int cookie)
{
    post_message(-1, XWM_ABANDON_AUDIO_FOCUS, XWPARAM(0), XWPARAM((long)cookie));
}

SDK_API void txc_abandon_all_audio_focus()
{
    post_message(-1, XWM_ABANDON_AUDIO_FOCUS, XWPARAM(1), NULL);
}

SDK_API void txc_set_audio_focus(DURATION_HINT focus)
{
    post_message(-1, XWM_SET_AUDIO_FOCUS, XWPARAM(focus), NULL);
}

#endif /* AudioFocusManager_cpp */
