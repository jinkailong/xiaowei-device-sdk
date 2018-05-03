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

#ifndef AppkitMsgBase_hpp
#define AppkitMsgBase_hpp

#include "AppSkill.hpp"
#include "TXCMsgbox.hpp"
#include <queue>

class MsgPromptQueue
{
public:
    MsgPromptQueue(){
        pthread_mutex_init(&m_mutex_prompt, NULL);
    }
    ~MsgPromptQueue(){
        pthread_mutex_destroy(&m_mutex_prompt);
    }
    
    static MsgPromptQueue& instance() {
        static MsgPromptQueue _instance;
        return _instance;
    }
    
    void AddPromptToneQueue(bool isRecv) {
        pthread_mutex_lock(&m_mutex_prompt);
        m_queuePrompt.push_back(isRecv);
        pthread_mutex_unlock(&m_mutex_prompt);
    }
    
    bool GetPromptTone(bool& isRecv) {
        pthread_mutex_lock(&m_mutex_prompt);
        if (m_queuePrompt.empty())
        {
            pthread_mutex_unlock(&m_mutex_prompt);
            return false;
        }
        
        isRecv = m_queuePrompt.front();
        m_queuePrompt.pop_front();
        pthread_mutex_unlock(&m_mutex_prompt);
        return true;
    }
    
private:
    pthread_mutex_t m_mutex_prompt;     //通知音队列锁
    std::deque<bool> m_queuePrompt;     //通知音队列
};

class AppkitMsgBase : public PlayerKit
{
public:
    AppkitMsgBase(int app_id);
    virtual ~AppkitMsgBase();
    
    bool OnMessage(XWM_EVENT event, XWPARAM arg1, XWPARAM arg2);
    
protected:
    bool AddMsgToMsgbox(CTXCMsgBase* pMsg);
    void AddPromptToneQueue(bool isRecv = true);
    bool PlayNextPromptTone();
    void PlayPromptTone(bool isRecv = true);    //播放提示音
    void PlayMsg(CTXCMsgBase* pMsg);
    void ClearPlayList();
    void AddMsgToPlayList(CTXCMsgBase* pMsg);
    void PlayEndMsg();  // 没有更多消息
    
private:
    unsigned int    m_nCurMsgId;  //当前播放的消息的id
};


#endif /* AppkitMsgBase_hpp */
