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

#ifndef AppkitMsgbox_hpp
#define AppkitMsgbox_hpp

#include "AppkitMsgBase.hpp"
#include "TXCMsgbox.hpp"

class AppkitMsgbox : public AppkitMsgBase
{
public:
    AppkitMsgbox(int app_id);
    virtual ~AppkitMsgbox();
    
    // 预先处理资源中的控制指令
    bool PreProcessResourceCommand(const TXCA_PARAM_RES_GROUP *v_groups, size_t count);
    
    virtual std::string GetClassName();
    
protected:
    bool ProcessTextMsg(const char* data, const char* ext_buffer);
    bool ProcessAudioMsg(int type, const char* data, const char* ext_buffer);
    bool ProcessAddCacheMsg(unsigned int msgId);
};

class AppkitQQMsg : public AppkitMsgBase
{
public:
    AppkitQQMsg(int app_id);
    virtual ~AppkitQQMsg();
    
    bool OnMessage(XWM_EVENT event, XWPARAM arg1, XWPARAM arg2);
    
    // 预先处理资源中的控制指令
    bool PreProcessResourceCommand(const TXCA_PARAM_RES_GROUP *v_groups, size_t count);
    
    // 处理发消息录音事件
    void OnSupplementRequest(const TXCA_PARAM_RESPONSE &cRsp, bool& bHandled);
    
    //void on_send_msg_ret(int err_code);
    virtual std::string GetClassName();
    
private:
    //void ProcessMsgSend(unsigned long long targetId);
    
    //bool EncodeVoiceDataToAmr(const std::string& strVoiceData, std::string& strFile);
    
    //void SendMsg(const std::string& strFile, unsigned int duration, unsigned int* cookie);
    
private:
    unsigned long long m_targetId;      // 发送消息时接收方的id
    unsigned int    m_nDuration;        // 录音时长
};

#endif /* AppkitMsgbox_hpp */
