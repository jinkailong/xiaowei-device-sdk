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
#ifndef _AIAUDIO_TXCAppManager_HPP_
#define _AIAUDIO_TXCAppManager_HPP_

//////////////////// interface of AudioApp.h ////////////////////
#include "TXSDKCommonDef.h"
#include "AudioApp.hpp"

class TXCAppManager
{
  public:
    TXCAppManager();
    ~TXCAppManager();

    // 收到一个响应
    void OnAiAudioRsp(TXCA_PARAM_RESPONSE &cRsp);
    
    // 收到一个静音通知
    void OnSilence(int errCode);

    const std::vector<int> &GetAllApp();

    // 根据session获取skill信息
    const txc_session_info *GetSessionInfo(SESSION id);

    bool OnMessage(SESSION id, XWM_EVENT event, XWPARAM arg1, XWPARAM arg2);

  public:
    txc_xwei_control callback_;

  private:
    //  return true if responce handled, otherwise return false;
    bool ProcessRsp(const TXCA_PARAM_RESPONSE &cRsp);
    void DumpAppStack(std::string tag);
    CAudioApp *NewApp(const TXCA_PARAM_RESPONSE &cRsp);
    bool DeleteApp(SESSION process_id);
    void OrderAppIds(SESSION process_id);

    void Clear();

    std::map<int, CAudioApp *> app_list_; //  app id => app
    std::vector<int> app_id_list_;              //  app ids

    THREAD_CHECKER;
};

class TXCWakeupStatus
{
};

#endif /* _AIAUDIO_TXCAppManager_HPP_ */
