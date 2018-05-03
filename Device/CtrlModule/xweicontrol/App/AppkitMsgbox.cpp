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

#include "AppkitMsgbox.hpp"
#include <sstream>
#include <string.h>
#include "TXCSkillsDefine.h"
#include "logger.h"
#include "TXCAudioMsg.h"
#include "document.h"
#include "writer.h"
#include "stringbuffer.h"

#include "Playlist.hpp"
#include "PlayerControl.hpp"
#include "TXCServices.hpp"


AppkitMsgbox::AppkitMsgbox(int app_id)
: AppkitMsgBase(app_id)
{
    
}

AppkitMsgbox::~AppkitMsgbox()
{
    
}

std::string AppkitMsgbox::GetClassName()
{
    return "AppkitMsgbox";
}

bool AppkitMsgbox::PreProcessResourceCommand(const TXCA_PARAM_RES_GROUP *v_groups, size_t count)
{   
    if (NULL == v_groups || 0 == count) {
        return false;
    }
    
    for (unsigned int i = 0; i < count; ++i)
    {
        for (unsigned int j = 0; j < v_groups[i].resources_size; ++j)
        {
            const TXCA_PARAM_RESOURCE *resource = v_groups[i].resources + j;
            if (txca_resource_command == resource->format)
            {
                if (resource->id && resource->id[0])
                {
                    int cmd_id = atoi(resource->id);
                    TLOG_DEBUG("AppkitMsgbox::PreProcessResourceCommand cmd_id:[%d]", cmd_id);
                    switch (cmd_id) {
                        case PROPERTY_ID_IOT_TEXT: // 文本消息
                        {
                            ProcessTextMsg(resource->content, resource->extend_buffer);
                            break;
                        }
                            
                        case PROPERTY_ID_IOT_AUDIO: // 语音消息
                        {
                            ProcessAudioMsg(transfer_filetype_audio, resource->content, resource->extend_buffer);
                            break;
                        }
                        case PROPERTY_ID_IOT_CACHE:
                        {
                            if (resource->content)
                            {
                                unsigned int msgId = (unsigned int)atoi(resource->content);
                                ProcessAddCacheMsg(msgId);
                            }
                            break;
                        }
                            
                        case 11019:
                        {
                            return false;
                        }
                            
                        default:
                            break;
                    }
                }
            }
        }
    }
    return true;
}

bool AppkitMsgbox::ProcessTextMsg(const char* data, const char* ext_buffer)
{
    if (!data)
        return false;
    
    unsigned long long sender = 0;
    //解析ext_buffer
    if (ext_buffer) {
        rapidjson::Document json_doc;
        json_doc.Parse(ext_buffer);
        if (!json_doc.HasParseError()) {
            assert(json_doc.IsObject());
            if (json_doc.HasMember("sender")) {
                sender = json_doc["sender"].GetUint64();
            }
        }
        TLOG_DEBUG("AppkitMsgbox ext_buffer:%s", ext_buffer);
    }
    
    int timestamp = 0;
    std::string strText;
    if (data) {
        rapidjson::Document json_doc;
        json_doc.Parse(data);
        if (!json_doc.HasParseError()) {
            assert(json_doc.IsObject());
            if (json_doc.HasMember("text")) {
                strText = json_doc["text"].GetString();
            }
        }
        
        timestamp = (int)time(NULL); //jsonObject["time"].ToInt();
    }
    
    if (sender == 0 || strText.empty())
    {
        TLOG_ERROR("AppkitMsgbox param error sender:%llu strText:%s", sender, strText.c_str());
        return false;
    }
    
    CTXCMsgText* pMsgText = new CTXCMsgText;
    pMsgText->uin_ = sender;
    pMsgText->timestamp = timestamp;
    pMsgText->text = strText;
    TLOG_DEBUG("AppkitMsgbox::ProcessTextMsg uin:%llu text:%s", sender, strText.c_str());
    AddMsgToMsgbox(pMsgText);
    
    return true;
}

bool AppkitMsgbox::ProcessAudioMsg(int type, const char* data, const char* ext_buffer)
{
    if (!data)
        return false;
    
    unsigned long long sender = 0;
    //解析ext_buffer
    if (ext_buffer) {
        rapidjson::Document json_doc;
        json_doc.Parse(ext_buffer);
        if (!json_doc.HasParseError()) {
            assert(json_doc.IsObject());
            if (json_doc.HasMember("sender")) {
                sender = json_doc["sender"].GetUint64();
            }
        }
    }
    
    //解析property value json
    std::string strCoverKey;
    std::string strMediaKey;
    std::string strFileKey;
    std::string strBusinessName;
    std::string strExtraBuffer;
    std::string strFileKey2;
    std::string strCoverFileKey2;
    
    int timestamp = (int)time(NULL); //jsonObject["msg_time"].ToInt();
    int nDuration = 0;
    
    rapidjson::Document json_doc;
    json_doc.Parse(data);
    
    if (!json_doc.HasParseError()) {
        assert(json_doc.IsObject());
        if (json_doc.HasMember("cover_key")) {
            strCoverKey = json_doc["cover_key"].GetString();
        }
        if (json_doc.HasMember("media_key")) {
            strMediaKey = json_doc["media_key"].GetString();
        }
        if (json_doc.HasMember("file_key")) {
            strFileKey = json_doc["file_key"].GetString();
        }
        if (json_doc.HasMember("business_name")) {
            strBusinessName = json_doc["business_name"].GetString();
        }
        if (json_doc.HasMember("ext")) {
            strExtraBuffer = json_doc["ext"].GetString();
        }
        if (json_doc.HasMember("fkey2")) {
            strFileKey2 = json_doc["fkey2"].GetString();
        }
        if (json_doc.HasMember("ckey2")) {
            strCoverFileKey2 = json_doc["ckey2"].GetString();
        }
        if (transfer_filetype_audio == type && json_doc.HasMember("duration")) {
            nDuration = json_doc["duration"].GetInt();
        }
    }
    
    char szFileKey[200] = {0};
    if (!strMediaKey.empty())
    {
        memcpy(szFileKey, strMediaKey.c_str(), strMediaKey.size());
    }
    if (!strFileKey.empty())
    {
        memcpy(szFileKey, strFileKey.c_str(), strFileKey.size());
    }
    if (0 == strlen(szFileKey))
    {
        TLOG_ERROR("on_recv_minifile_data_point:invalid file_key!");
        return false;
    }
    
    txc_download_msg_data_t download_data;
    download_data.tinyId = sender;
    download_data.channel = transfer_channeltype_MINI;
    download_data.type = type;
    download_data.key = szFileKey;
    download_data.key_length = (unsigned int)strlen(szFileKey);
    download_data.mini_token = strFileKey2.c_str();
    download_data.min_token_length = (unsigned int)strFileKey2.length();
    download_data.duration = nDuration;
    download_data.timestamp = timestamp;
    control_->DownloadFile(&download_data);
    
    return true;
}

bool AppkitMsgbox::ProcessAddCacheMsg(unsigned int msgId)
{
    CTXCMsgBase* pMsg = NULL;
    if (CTXCMsgbox::instance().GetMsgCache(msgId, &pMsg)) {
        TLOG_DEBUG("AppkitMsgbox::ProcessAddCacheMsg uin:%llu", pMsg->msgId);
        AddMsgToMsgbox(pMsg);
        return true;
    }
    return false;
}

////////////////////////////////////////////////////////////////
// AppkitQQMsg
////////////////////////////////////////////////////////////////

//std::map<unsigned int, AppkitQQMsg*>  g_mapCookieToObject;

AppkitQQMsg::AppkitQQMsg(int app_id)
: AppkitMsgBase(app_id)
, m_targetId(0)
{
    
}

AppkitQQMsg::~AppkitQQMsg()
{
    
}

std::string AppkitQQMsg::GetClassName()
{
    return "AppkitQQMsg";
}

bool AppkitQQMsg::OnMessage(XWM_EVENT event, XWPARAM arg1, XWPARAM arg2)
{
    bool bHandled = AppkitMsgBase::OnMessage(event, arg1, arg2);
    
    if (XWM_SILENT == event)
    {
        TLOG_DEBUG("AppkitQQMsg XWM_SILENT");
        if (m_targetId) {
            //TODO: notify encode and send msg
            //ProcessMsgSend(m_targetId);
            control_->NotifyMsgSend(m_targetId);
            m_targetId = 0;
        }
    }
    else if (XWM_PLAYER_STATUS_FINISH == event)
    {
        //这里应该是“请在嘀声后留言”结束
        if (m_targetId) {
            //TODO: notify start record
            control_->NotifyMsgRecord();
        }
    }
    
    return bHandled;
}

bool AppkitQQMsg::PreProcessResourceCommand(const TXCA_PARAM_RES_GROUP *v_groups, size_t count)
{
    if (NULL == v_groups || 0 == count) {
        return false;
    }
    
    for (unsigned int i = 0; i < count; ++i)
    {
        for (unsigned int j = 0; j < v_groups[i].resources_size; ++j)
        {
            const TXCA_PARAM_RESOURCE *resource = v_groups[i].resources + j;
            if (txca_resource_command == resource->format)
            {
                if (resource->id && resource->id[0])
                {
                    int cmd_id = atoi(resource->id);
                    TLOG_DEBUG("AppkitQQMsg::PreProcessResourceCommand cmd_id:[%d]", cmd_id);
                    switch (cmd_id) {
                        case PROPERTY_ID_START: // 播放消息
                        {
                            CTXCMsgbox::instance().SetMsgReadIndex(0);
                            CTXCMsgBase* pMsg = CTXCMsgbox::instance().GetNextUnReadMsg();
                            PlayMsg(pMsg);
                            break;
                        }
                            
                        case PROPERTY_ID_PREV:  //播放上一条消息
                        {
                            CTXCMsgBase* pMsg = CTXCMsgbox::instance().GetPrevMsg();
                            PlayMsg(pMsg);
                            break;
                        }
                            
                        case PROPERTY_ID_NEXT:  //播放下一条消息
                        {
                            CTXCMsgBase* pMsg = CTXCMsgbox::instance().GetNextMsg();
                            PlayMsg(pMsg);
                            break;
                        }
                            
                        case PROPERTY_ID_START_BY_ID: // 根据消息id播放指定的消息
                        {
                            if (resource->content) {
                                unsigned int msgId = (unsigned int)strtoul(resource->content, NULL, 0);
                                CTXCMsgBase* pMsg = CTXCMsgbox::instance().GetMsgById(msgId);
                                PlayMsg(pMsg);
                            } else {
                                TLOG_ERROR("PropertyId %d content is NULL", cmd_id);
                            }
                            break;
                        }
                            
                        case PROPERTY_ID_MSG_READED:  // 标记消息已读
                        {
                            if (resource->content) {
                                unsigned int msgId = (unsigned int)strtoul(resource->content, NULL, 0);
                                CTXCMsgbox::instance().SetMsgReaded(msgId);
                            } else {
                                TLOG_ERROR("PropertyId %d content is NULL", cmd_id);
                            }
                            break;
                        }
                            
                        case PROPERTY_ID_STOP:  // 停止播放
                        {
                            if(!m_is_need_play) {
                                return false;
                            }
                            player_->Pause();
                            break;
                        }
                            
                        case PROPERTY_ID_SEND_IOT_AUDIO_MSG: // 发送语音消息给手Q
                        {
                            if (resource->content)
                            {
                                m_targetId = strtoull(resource->content, NULL, 0);
                                TLOG_DEBUG("PropertyId PROPERTY_ID_SEND_IOT_AUDIO_MSG %llu", m_targetId);
                            }
                            break;
                        }
                            
                        case 11019: //播放消息会附带，这个命令用途未知，空实现处理
                        {
                            return false;
                        }
                            
                        default:
                            break;
                    }
                }
            }
        }
    }
    return true;
}

void AppkitQQMsg::OnSupplementRequest(const TXCA_PARAM_RESPONSE &cRsp, bool& bHandled)
{
    if (m_targetId) {
        TLOG_DEBUG("AppkitQQMsg::OnSupplementRequest");
        TXCA_PARAM_RESPONSE tmpRsp = cRsp;
        tmpRsp.context.request_param |= txca_param_only_vad;
        tmpRsp.context.speak_timeout = 2000;
        
        send_message(app_id_, XWM_SUPPLEMENT_REQUEST, XWPARAM((long)tmpRsp.context.speak_timeout), XWPARAM(&tmpRsp));
        bHandled = true;
    }
}

