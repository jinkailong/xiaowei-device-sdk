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
#include "TXCAppManager.hpp"
#include "TXCSkillsDefineEx.h"
#include "logger.h"
#include <memory.h>
#include <sstream>
#include "Util.hpp"

#include "TXCServices.hpp"

TXCAppManager::TXCAppManager()
{
}
TXCAppManager::~TXCAppManager()
{
    Clear();
}

void TXCAppManager::Clear()
{
    std::map<int, CAudioApp *>::iterator itr = app_list_.begin();
    for (; itr != app_list_.end(); ++itr)
    {
        CAudioApp *app = itr->second;
        delete app;
    }

    app_list_.clear();
    app_id_list_.clear();
}



void TXCAppManager::DumpAppStack(std::string tag)
{
    std::stringstream ss;
    ss << "TXCAppManager::DumpAppStack[";
    ss << tag;
    ss << "]:";
    
    for (std::vector<int>::iterator itr_order = app_id_list_.begin(); app_id_list_.end() != itr_order; ++itr_order)
    {
        const int app_id = *itr_order;

            std::map<int, CAudioApp *>::iterator itr_app = app_list_.find(app_id);
            if (app_list_.end() != itr_app)
            {
                CAudioApp *app = itr_app->second;
                if (app)
                {
                    ss << "[session=";
                    ss << app->GetSessionId();
                    ss << ",cls_name=";
                    ss << app->GetAppType();
                    txc_session_info info = app->GetInfo();
                    if (info.skill_id)
                    {
                        ss << ",skill_id=";
                        ss << info.skill_id;
                    }
                    if (info.skill_name)
                    {
                        ss << ",skill_name=";
                        ss << info.skill_name;
                    }
                    ss << "]";
                }
        }
    }
    if(CAudioApp::notify_app_) {
        ss << "[session=";
        ss << CAudioApp::notify_app_->GetSessionId();
        ss << ",cls_name=";
        ss << CAudioApp::notify_app_->GetClassName();
        ss << "]";
    }
    TLOG_TRACE("%s", ss.str().c_str());
}

//  return true if responce handled, otherwise return false;
bool TXCAppManager::ProcessRsp(const TXCA_PARAM_RESPONSE &cRsp)
{
    TLOG_DEBUG("TXCAppManager::ProcessRsp rsp=%s", Util::ToString(cRsp).c_str());
    DumpAppStack("ProcessRsp Begin");
    bool handled = false;
    Thread_Check;
    CAudioApp *app = NULL;

    {
        //rsp => appMgr => appList => NewApp
        // 首先看这个响应需不需要处理，如果没有任何播放资源和结构化数据，只有一个错误码之类的，就不用处理了。
        if(Util::IsInvaild(cRsp)) {
            handled = true;
            TLOG_WARNING("TXCAppManager::ProcessRsp rsp is invaild.");
        }
        
        //  appList
        // 再看已经存在的App能否处理这个响应
        if (!handled)
        {
            for (std::vector<int>::iterator itr_order = app_id_list_.begin();
                 !handled && app_id_list_.end() != itr_order;
                 ++itr_order) // 循环总是只循环一次
            {
                const int app_id = *itr_order;

                    std::map<int, CAudioApp *>::iterator itr_app = app_list_.find(app_id);
                    if (app_list_.end() != itr_app)
                    {
                        app = itr_app->second;
                        if (app)
                        {
                            handled = app->OnAiAudioRsp(cRsp);
                        }
                    }
            }
        }
        //  没被处理并且是合理的响应，就 NewApp
        if(Util::IsVaild(cRsp)) {
            if (!handled)
            {
                app = NewApp(cRsp);
                handled = app->OnAiAudioRsp(cRsp);
            }
        }
        if(handled && app) {
            OrderAppIds(app->GetSessionId());
        }
    }
    DumpAppStack("ProcessRsp End");

    return handled;
}

void TXCAppManager::OnAiAudioRsp(TXCA_PARAM_RESPONSE &cRsp)
{
    {
        ProcessRsp(cRsp);
    }
}

void TXCAppManager::OnSilence(int errCode)
{
    std::map<SESSION, CAudioApp *>::iterator it = app_list_.begin();
    for (; it != app_list_.end(); ++it) {
        CAudioApp *app = it->second;
        if (app)
        {
            app->OnMessage(XWM_SILENT, NULL, NULL);
        }
    }
}

const std::vector<int> &TXCAppManager::GetAllApp()
{
    return app_id_list_;
}

const txc_session_info *TXCAppManager::GetSessionInfo(SESSION id)
{
    const txc_session_info *info = NULL;

    std::map<SESSION, CAudioApp *>::iterator itr = app_list_.find(id);
    if (app_list_.end() != itr)
    {
        CAudioApp *app = itr->second;
        if (app)
        {
            info = &(app->GetInfo());
        }
    }

    return info;
}

// 如果当前没有可以处理这个rsp的App，就会创建一个。否则尽量重复利用
CAudioApp *TXCAppManager::NewApp(const TXCA_PARAM_RESPONSE &cRsp)
{
    int process_id = Util::GetNewProcessId();
    CAudioApp *app = new CAudioApp(process_id);
    app->SetAppType(cRsp);
    TLOG_DEBUG("TXCAppManager::ProcessRsp NewApp sessionId=%d app=%s.", process_id, app->GetAppType().c_str());

    app_list_[process_id] = app;
    app_id_list_.push_back(process_id);

    return app;
}

bool TXCAppManager::DeleteApp(SESSION process_id)
{
    bool deleted = false;

    CAudioApp *app = NULL;
    std::map<SESSION, CAudioApp *>::iterator itr = app_list_.find(process_id);
    if (app_list_.end() != itr)
    {
        app = itr->second;
        app_list_.erase(itr);

        delete app;

        deleted = true;
    }

    std::vector<SESSION>::iterator itr_order = std::find(app_id_list_.begin(), app_id_list_.end(), process_id);
    if (app_id_list_.end() != itr_order)
    {
        app_id_list_.erase(itr_order);
    }

    return deleted;
}

void TXCAppManager::OrderAppIds(SESSION process_id)
{
    int active_app = -1;
    int inactive_app = -1;
    
    std::vector<int>::iterator itr = app_id_list_.begin();
    if(app_id_list_.end() != itr) {
        inactive_app = *itr;
    }
    
    if(process_id == inactive_app || process_id < 0) {
        return;
    }
    
    // 移除process_id，加到最前面
    std::vector<SESSION>::iterator itr_order = std::find(app_id_list_.begin(), app_id_list_.end(), process_id);
    if (app_id_list_.end() != itr_order)
    {
        app_id_list_.erase(itr_order);
    }
    app_id_list_.insert(app_id_list_.begin(), process_id);
    active_app = process_id;
    
    if (inactive_app > 0)
    {
        post_message(inactive_app, XWM_KILLFOCUS, XWPARAM((long)active_app), NULL, 0);
    }
    if (active_app > 0)
    {
        post_message(active_app, XWM_SETFOCUS, XWPARAM((long)inactive_app), NULL, 0);
    }
}

bool TXCAppManager::OnMessage(SESSION id, XWM_EVENT event, XWPARAM arg1, XWPARAM arg2)
{
    bool handled = false;

    TLOG_DEBUG("sessionId=%d TXCAppManager::OnMessage event=%s, arg1=%ld, arg2=%ld.", id, Util::ToString(event).c_str(), arg1, arg2);
    
    handled = TXCServices::instance()->GetAudioFocusManager()->HandleAudioFocusMessage(id, event, arg1, arg2);

    if (!handled)
    {
        if(CAudioApp::notify_app_ && id == CAudioApp::notify_app_->GetSessionId()) {
            handled = CAudioApp::notify_app_->OnMessage(event, arg1, arg2);
        }
    }

    if (!handled)
    {
        std::map<SESSION, CAudioApp *>::iterator itr = app_list_.find(id);
        if (app_list_.end() != itr)
        {
            CAudioApp *app = itr->second;
            if (app)
            {
                if (!handled)
                {
                    handled = app->OnMessage(event, arg1, arg2);
                }
            }
        }
        else
        {
            TLOG_ERROR("error app id");
        }
    }

    return handled;
}
