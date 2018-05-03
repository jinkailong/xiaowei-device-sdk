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

#include "TXCMsgbox.hpp"
#include <sstream>
#include <stdio.h>
#include <string.h>
#include "logger.h"
#include "TXCServices.hpp"

static int g_seedMsgId = 1000;

////////////////////////////////////////////////////////////////
// CTXCMsgBase
////////////////////////////////////////////////////////////////
CTXCMsgBase::CTXCMsgBase()
: msgId(g_seedMsgId++)
, uin_(0)
, timestamp(0)
, isReaded(false)
, isRecv(true)
, msgType(txc_msg_type_invalid)
{
}

CTXCMsgBase::~CTXCMsgBase()
{
    
}

bool CTXCMsgBase::isPlayable() const
{
    return false;//default implement
}

void CTXCMsgBase::Clear()
{
    // do nothing
}

std::string CTXCMsgBase::toString()
{
    std::stringstream ss;
    
    ss << "{";
    ToString(ss);
    ss << "}";
    
    std::string strData = ss.str();
    return strData;
}

void CTXCMsgBase::ToString(std::stringstream& ss) const
{
    ss << "msgId:";     ss << msgId;
    ss << ", msgType:"; ss << msgType;
    char szUin[10];
    snprintf(szUin, 10, "%llu", uin_);
    ss << ", uin:";     ss << szUin;
    ss << ", isReaded:";ss << isReaded;
    ss << ", isRecv:";  ss << isRecv;
    ss << ", timestamp:";   ss << timestamp;
}

////////////////////////////////////////////////////////////////
// CTXCMsgText
////////////////////////////////////////////////////////////////
CTXCMsgText::CTXCMsgText()
{
    msgType = txc_msg_type_iot_text;
}

CTXCMsgText::~CTXCMsgText()
{
    
}

void CTXCMsgText::ToString(std::stringstream& ss) const
{
    CTXCMsgBase::ToString(ss);
    ss << ", text:";    ss << text;
}

////////////////////////////////////////////////////////////////
// CTXCMsgAudio
////////////////////////////////////////////////////////////////
CTXCMsgAudio::CTXCMsgAudio()
: duration(0)
{
    msgType = txc_msg_type_iot_audio;
}

CTXCMsgAudio::~CTXCMsgAudio()
{
    
}

bool CTXCMsgAudio::isPlayable() const
{
    return true;
}

void CTXCMsgAudio::Clear()
{
    if (!localUrl.empty()) {
        remove(localUrl.c_str());
        localUrl = "";
    }
}

void CTXCMsgAudio::ToString(std::stringstream& ss) const
{
    CTXCMsgBase::ToString(ss);
    
    ss << ", amr:";         ss << localUrl;
    ss << ", duration:";    ss << duration;
}

////////////////////////////////////////////////////////////////
// CTXCMsgbox
////////////////////////////////////////////////////////////////
CTXCMsgbox::CTXCMsgbox()
: m_nMaxMsgSize(50)
, m_nMsgIndex(-1)
, m_uin(0)
{
    
}

CTXCMsgbox::~CTXCMsgbox()
{
    //TODO: 释放内存
}

CTXCMsgbox& CTXCMsgbox::instance()
{
    static CTXCMsgbox _instance;
    return _instance;
}

void CTXCMsgbox::SetMaxMsgSize(unsigned int nMaxSize)
{
    m_nMaxMsgSize = nMaxSize;
    TLOG_DEBUG("CTXCMsgbox::SetMaxMsgSize %u", m_nMaxMsgSize);
}

bool CTXCMsgbox::AddMsg(CTXCMsgBase* pMsg)
{
    if (NULL == pMsg) {
        TLOG_ERROR("CTXCMsgbox::AddMsg pMsg is NULL.");
        return false;
    }
    
    bool bAdd = AddMsgInner(pMsg);
    
    if (bAdd)
        NotifyMsgAdd(pMsg);
    
    return bAdd;
}

bool CTXCMsgbox::AddMsgInner(CTXCMsgBase* pMsg)
{
    bool bAdd = false;
    unsigned int nSize = (unsigned int)m_msgboxList.size();
    m_msgboxList.push_back(pMsg);
    bAdd = (m_msgboxList.size() > nSize) ? true : false;
    ResizeMsgboxSize(&m_msgboxList);
    return bAdd;
}

void CTXCMsgbox::ResizeMsgboxSize(MsgBoxList* pMsgboxList)
{
    if (NULL == pMsgboxList) return;
    
    while (pMsgboxList->size() > m_nMaxMsgSize) {
        CTXCMsgBase* pMsg = *(pMsgboxList->begin());
        if (pMsg) {
            pMsg->Clear();
            delete pMsg;
            pMsg = NULL;
        }
        pMsgboxList->erase(pMsgboxList->begin());
    }
}

void CTXCMsgbox::NotifyMsgAdd(CTXCMsgBase* pMsg)
{
    
}

CTXCMsgBase* CTXCMsgbox::GetNextUnReadMsg()
{
    int nOffset = (m_nMsgIndex >= 0 ? m_nMsgIndex : 0);
    MsgBoxList::iterator it = m_msgboxList.begin();
    if (m_nMsgIndex > 0) {
        std::advance(it, nOffset);
    }
    for (; it != m_msgboxList.end(); ++it) {
        if ((m_uin != 0 && (*it)->uin_ == m_uin) || m_uin == 0) {
            if (!(*it)->isReaded) {
                SetMsgReadIndex(nOffset);
                return (*it);
            }
        }
        nOffset++;
    }
    return NULL;
}

CTXCMsgBase* CTXCMsgbox::GetPrevMsg()
{
    int nOffset = (m_nMsgIndex >= 0 ? m_nMsgIndex : 0);
    if (nOffset == 0) {
        TLOG_DEBUG("CTXCMsgbox::GetPrevMsg no prev msg");
        return NULL;
    }
    
    MsgBoxList::iterator it = m_msgboxList.begin();
    std::advance(it, nOffset - 1);
    SetMsgReadIndex(nOffset - 1);
    return (*it);
}

CTXCMsgBase* CTXCMsgbox::GetNextMsg()
{
    unsigned int nOffset = (m_nMsgIndex >= 0 ? m_nMsgIndex : 0);
    if (nOffset >= m_msgboxList.size()) {
        TLOG_DEBUG("CTXCMsgbox::GetNextMsg no next msg");
        return NULL;
    }
    
    MsgBoxList::iterator it = m_msgboxList.begin();
    std::advance(it, nOffset + 1);
    SetMsgReadIndex(nOffset + 1);
    return (*it);
}

CTXCMsgBase* CTXCMsgbox::GetMsgById(unsigned int msgId)
{
    MsgBoxList::iterator it = m_msgboxList.begin();
    for (; it != m_msgboxList.end(); ++it) {
        if ((*it)->msgId == msgId) {
            return (*it);
        }
    }
    return NULL;
}

void CTXCMsgbox::SetMsgReadIndex(int nIndex)
{
    TLOG_DEBUG("CTXCMsgbox::SetMsgReadIndex %d", nIndex);
    m_nMsgIndex = nIndex;
}

bool CTXCMsgbox::SetMsgReaded(unsigned int msgId)
{
    TLOG_DEBUG("CTXCMsgbox::SetMsgReaded %u", msgId);
    bool bSuc = false;
    MsgBoxList::iterator it = m_msgboxList.begin();
    for (; it != m_msgboxList.end(); ++it) {
        if ((*it)->msgId == msgId) {
            (*it)->isReaded = true;
            bSuc = true;
            break;
        }
    }
    return bSuc;
}

void CTXCMsgbox::SetSingleUinMode(unsigned long long uin)
{
    TLOG_DEBUG("CTXCMsgbox::SetSingleUinMode %llu", uin);
    m_uin = uin;
}

void CTXCMsgbox::AddMsgCache(txc_msg_info* msgInfo)
{
    if (NULL == msgInfo)
        return;
    
    CTXCMsgBase* pMsg = NULL;
    if (msgInfo->type == transfer_filetype_audio)
    {
        CTXCMsgAudio* pMsgAudio = new CTXCMsgAudio();
        if (pMsgAudio)
        {
            pMsgAudio->uin_ = msgInfo->tinyId;
            pMsgAudio->timestamp = msgInfo->timestamp;
            pMsgAudio->localUrl = msgInfo->content;
            pMsgAudio->duration = msgInfo->duration;
            pMsgAudio->isRecv = msgInfo->isRecv;
            if (!msgInfo->isRecv)   //发送的消息默认标记为已读
                pMsgAudio->isReaded = true;
            pMsg = pMsgAudio;
        }
    }
    
    if (pMsg) {
        m_mapCacheMsg.insert(std::make_pair(pMsg->msgId, pMsg));
        
        //通知msgbox skill
        std::stringstream ss;
        ss << pMsg->msgId;
        std::string strData = ss.str();
        TXCA_PARAM_RESPONSE rsp = {0};
        std::string strId = DEF_TXCA_SKILL_ID_MSGBOX;
        PackResponse(strId, "消息盒子", PROPERTY_ID_IOT_CACHE, strData, &rsp);
        TXCServices::instance()->GetMessageQueue()->ProcessResponse(&rsp);
        FreeResponse(&rsp);
    }
}

void CTXCMsgbox::PackResponse(const std::string& id, const std::string& name, unsigned int propId, const std::string& propValue, TXCA_PARAM_RESPONSE* rsp)
{
    rsp->skill_info.id = id.c_str();
    rsp->skill_info.name = name.c_str();
    rsp->resource_groups = (TXCA_PARAM_RES_GROUP*)malloc(sizeof(TXCA_PARAM_RES_GROUP));
    rsp->resource_groups_size = 1;
    memset(rsp->resource_groups, 0, sizeof(TXCA_PARAM_RES_GROUP));
    if (rsp->resource_groups) {
        rsp->resource_groups[0].resources_size = 1;
        rsp->resource_groups[0].resources = (TXCA_PARAM_RESOURCE*)malloc(sizeof(TXCA_PARAM_RESOURCE));
        memset(rsp->resource_groups[0].resources, 0, sizeof(TXCA_PARAM_RESOURCE));
        if (rsp->resource_groups[0].resources) {
            rsp->resource_groups[0].resources[0].format = txca_resource_command;
            std::stringstream ss;
            ss << propId;
            std::string strPropId = ss.str();
            
            int length = (int)strPropId.length();
            rsp->resource_groups[0].resources[0].id = (char*)malloc(length + 1);
            memcpy(rsp->resource_groups[0].resources[0].id, strPropId.c_str(), length);
            rsp->resource_groups[0].resources[0].id[length] = 0;
            
            length = (int)propValue.length();
            rsp->resource_groups[0].resources[0].content = (char*)malloc(length + 1);
            memcpy(rsp->resource_groups[0].resources[0].content, propValue.c_str(), length);
            rsp->resource_groups[0].resources[0].content[length] = 0;
        }
    }
}

void CTXCMsgbox::FreeResponse(TXCA_PARAM_RESPONSE* rsp)
{
    if (rsp && rsp->resource_groups) {
        for (unsigned int i = 0; i < rsp->resource_groups_size; ++i)
        {
            TXCA_PARAM_RES_GROUP* group = &rsp->resource_groups[i];
            for (unsigned int j = 0; j < group->resources_size; ++j)
            {
                TXCA_PARAM_RESOURCE* resource = &group->resources[j];
                if (resource && resource->id) {
                    free(resource->id);
                }
                if (resource && resource->content) {
                    free(resource->content);
                }
                if (resource && resource->extend_buffer) {
                    free(resource->extend_buffer);
                }
            }
            free(group->resources);
        }
        free(rsp->resource_groups);
    }
}

bool CTXCMsgbox::GetMsgCache(unsigned int msgId, CTXCMsgBase** pMsg)
{
    std::map<unsigned int, CTXCMsgBase*>::iterator it = m_mapCacheMsg.find(msgId);
    if (it != m_mapCacheMsg.end()) {
        *pMsg = &*(it->second);
        m_mapCacheMsg.erase(it);
        return true;
    }
    return false;
}
