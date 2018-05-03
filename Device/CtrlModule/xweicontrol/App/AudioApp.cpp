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
#include <stdio.h>
#include <string>
#include <cassert>
#include <memory.h>
#include "AudioApp.hpp"
#include "AppSkill.hpp"
#include "txctypedef.h"
#include "TXCServices.hpp"
#include "OuterSkillMgr.h"
#include "TXCSkillsDefineEx.h"
#include "logger.h"
#include "AppkitMsgbox.hpp"
#include "Util.hpp"

AppSkill* CAudioApp::notify_app_ = NULL;

CAudioApp::CAudioApp(int process_id)
    : process_id_(process_id), strategy_(NULL)
{
    info_.skill_id = NULL;
    info_.skill_name = NULL;
    if(!notify_app_) {
        notify_app_ = new AppKitNotify(Util::GetNewProcessId());
    }
}

CAudioApp::~CAudioApp()
{
    if (strategy_)
    {
        delete strategy_;
    }
}

// 根据rsp为CAudioApp关联AppSkill
void CAudioApp::SetAppType(const TXCA_PARAM_RESPONSE &cRsp)
{
    assert(NULL == strategy_);
    if (NULL == strategy_)
    {
        if (cRsp.skill_info.id && cRsp.skill_info.id[0])
        {
            skill_id_ = cRsp.skill_info.id;
            info_.skill_id = skill_id_.c_str();
        }

        if (cRsp.skill_info.name && cRsp.skill_info.name[0])
        {
            skill_name_ = cRsp.skill_info.name;
            info_.skill_name = skill_name_.c_str();
        }

        const std::string &skill_id = cRsp.skill_info.id ? cRsp.skill_info.id : "";
        const std::string &skill_name = cRsp.skill_info.name ? cRsp.skill_info.name : "";

        if (DEF_TXCA_SKILL_ID_MUSIC == skill_id)
        {
            strategy_ = new AppKitMusic(process_id_);
        }
        else if (DEF_TXCA_SKILL_ID_FM == skill_id)
        {
            strategy_ = new AppKitFM(process_id_);
        }
        else if (DEF_TXCA_SKILL_ID_MSGBOX == skill_id)
        {
            strategy_ = new AppkitMsgbox(process_id_);
        }
        else if (DEF_TXCA_SKILL_ID_QQMSG == skill_id)
        {
            strategy_ = new AppkitQQMsg(process_id_);
        }
        else if (DEF_TXCA_SKILL_ID_WIKI == skill_id
                 || DEF_TXCA_SKILL_ID_DATETIME == skill_id
                 || DEF_TXCA_SKILL_ID_HISTORY == skill_id
                 || DEF_TXCA_SKILL_ID_CALC == skill_id
                 || DEF_TXCA_SKILL_ID_CHAT == skill_id
                 || DEF_TXCA_SKILL_ID_UNKNOWN == skill_id
                 || DEF_TXCA_SKILL_ID_WEATHER == skill_id)
        {
            strategy_ = new AppKitCommon(process_id_);
        }
        else if (DEF_TXCA_SKILL_ID_NEWS == skill_id)
        {
            strategy_ = new AppKitNew(process_id_);
        }
        else if (skill_id.empty())
        {
            strategy_ = new PlayerKit(process_id_);
        }
        else
        {
            bool managed = false;
            if (outer_skill_callback.start_outer_skill)
            {
                managed = outer_skill_callback.start_outer_skill(process_id_, skill_name.c_str(), skill_id.c_str());
            }

            if (managed)
            {
                strategy_ = new OuterSkill(process_id_);
            }
            else if (DEF_TXCA_SKILL_ID_GLOBAL == skill_id)
            {
                // 通用控制没App处理就到这里了
                strategy_ = new AppKitGlobal(process_id_);
            }
            else
            {
                strategy_ = new AppKitCommon(process_id_);
            }
        }
    }
}

const txc_session_info &CAudioApp::GetInfo()
{
    return info_;
}

const std::string CAudioApp::GetAppType()
{
    if(strategy_) {
        return strategy_->GetClassName();
    }
    return skill_id_;
}

const SESSION CAudioApp::GetSessionId()
{
    return process_id_;
}

bool CAudioApp::OnAiAudioRsp(const TXCA_PARAM_RESPONSE &cRsp)
{
    bool handled = false;
    if (strategy_ && IsFitAppScene(cRsp))
    {
        if (!handled)
        {
            // 给AppSkill处理，它会返回是否想要处理
            handled = strategy_->OnAiAudioRsp(cRsp);
            if(handled) {
                // 更新skillinfo
                std::string skill_name = cRsp.skill_info.name ? cRsp.skill_info.name : "";
                if (skill_name.find("通用控制") == std::string::npos)
                {
                    if (cRsp.skill_info.id && cRsp.skill_info.id[0])
                    {
                        skill_id_ = cRsp.skill_info.id;
                        info_.skill_id = skill_id_.c_str();
                    } else {
//                        skill_id_ = "";
//                        info_.skill_id = "";
                    }
                    
                    if (cRsp.skill_info.name && cRsp.skill_info.name[0])
                    {
                        skill_name_ = cRsp.skill_info.name;
                        info_.skill_name = skill_name_.c_str();
                    } else {
//                        skill_name_ = "";
//                        info_.skill_name = "";
                    }
                }
            }
            TLOG_DEBUG("sessionId=%d CAudioApp::OnAiAudioRsp finished, handled[%d] skill_id_[%s] skill_name_[%s] clsname[%s].", process_id_, handled, skill_id_.c_str(), skill_name_.c_str(), strategy_->GetClassName().c_str());
        }
    } else {
        TLOG_TRACE("sessionId=%d CAudioApp::OnAiAudioRsp but IsFitAppScene false.", process_id_);
    }

    return handled;
}

// 判断是否适合处理这个rsp
bool CAudioApp::IsFitAppScene(const TXCA_PARAM_RESPONSE &cRsp)
{
    bool is_fit = false;
    TLOG_DEBUG("sessionId=%d CAudioApp::IsFitAppScene skill_id[%s] skill_name[%s] cRsp=%s.", process_id_, skill_id_.c_str(), skill_name_.c_str(), Util::ToString(cRsp).c_str());
    
    // 使用skill_info比较
    is_fit = CheckSkillInfo(cRsp.skill_info);
    
    // 到这里，说明skill_info不足以确认是否应该放行。如果skill_info信息为空，使用last_skill_info判断。使用last_skill_info 的时候，原则上不能new新的APP来处理。
    if (!is_fit && !cRsp.skill_info.id && !cRsp.skill_info.name && !Util::IsTempRsp(cRsp))
    {
        is_fit = CheckSkillInfo(cRsp.last_skill_info);
    }
    // 到最后，skill_id都没有值，也放行。
    if (!is_fit)
    {
        if ((skill_id_.empty()) && !cRsp.skill_info.id)
        {
            is_fit = true;
        }
    }
    
    return is_fit;
}

bool CAudioApp::CheckSkillInfo(const TXCA_PARAM_SKILL skill_info)
{
    bool is_fit = false;
    if (!is_fit) // skill_id有值并且相同
    {
        if (skill_info.id && skill_info.id[0])
        {
            if (skill_id_ == skill_info.id)
            {
                is_fit = true;
            }
        }
    }
    TLOG_TRACE("sessionId=%d CAudioApp::CheckSkillInfocRsp.skill_id[%s] cRsp.skill_name[%s] is_fit[%d].", process_id_, skill_info.id, skill_info.name, is_fit);
    
    return is_fit;
}

bool CAudioApp::OnMessage(XWM_EVENT event, XWPARAM arg1, XWPARAM arg2)
{
    bool handled = false;
    if (strategy_)
    {
        handled = strategy_->OnMessage(event, arg1, arg2);
    }

    return handled;
}

ThreadChecker::ThreadChecker()
{
    thread_id_ = 0;
}

void ThreadChecker::Check()
{
    pthread_t id = pthread_self();
    if (0 == thread_id_)
    {
        thread_id_ = id;
    }
    else
    {
        if (thread_id_ != id)
        {
            printf("#### error thread %ld, %ld", (long)id, (long)thread_id_);
            assert(!"call same method from different thread!");
        }
    }
}

bool post_message(SESSION id, XWM_EVENT event, XWPARAM arg1, XWPARAM arg2, unsigned int delay)
{
    return TXCServices::instance()->GetMessageQueue()->PostMessage(id, event, arg1, arg2, delay);
}

bool send_message(SESSION id, XWM_EVENT event, XWPARAM arg1, XWPARAM arg2)
{
    return TXCServices::instance()->GetMessageQueue()->SendMessage(id, event, arg1, arg2);
}

SDK_API int txc_list_sessions(_Out_ SESSION *sessions, int buffer_count)
{
    int count = 0;

    std::vector<SESSION> apps = TXCServices::instance()->GetAppManager()->GetAllApp();
    for (int i = 0; i < buffer_count && i < (int)apps.size(); ++i)
    {
        *sessions = apps[i];
        sessions++;
    }

    return count;
}

SDK_API const txc_session_info *txc_get_session(SESSION id)
{
    return TXCServices::instance()->GetAppManager()->GetSessionInfo(id);
}
