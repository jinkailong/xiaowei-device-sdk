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
#ifndef _AIAUDIO_AUDIOAPP_HPP_
#define _AIAUDIO_AUDIOAPP_HPP_

//////////////////// AudioApp.hpp ////////////////////

#include "Player.hpp"
#include "AudioApp.h"
#include <pthread.h>
#include "AppSkill.hpp"

class AppSkill;

class ThreadChecker
{
  public:
    ThreadChecker();
    void Check();

  private:
    pthread_t thread_id_;
};
#define THREAD_CHECKER ThreadChecker debug_thread_checker_;
#define Thread_Check debug_thread_checker_.Check();

//
class CAudioApp
{
  public:
    ~CAudioApp();

    const txc_session_info &GetInfo();
    static AppSkill *notify_app_;
  protected:
    CAudioApp(int process_id);

    friend class TXCAppManager;
    //  change app from wakeup sence to an exactly sence
    void SetAppType(const TXCA_PARAM_RESPONSE &cRsp);
    const std::string GetAppType();
    const SESSION GetSessionId();

    //  return true if responce handled, otherwise return false;
    bool OnAiAudioRsp(const TXCA_PARAM_RESPONSE &cRsp);

    bool OnMessage(XWM_EVENT event, XWPARAM arg1, XWPARAM arg2);

  private:
    bool IsFitAppScene(const TXCA_PARAM_RESPONSE &cRsp);

    //////////////// need request here?
    //    @return session id
    SESSION Request(const char *buf, int len);
    SESSION RequestTTS(const char *);
    SESSION RequestText(const char *);
    //    report statistics dot
    void Report(const char *content);

    bool CheckSkillInfo(const TXCA_PARAM_SKILL skill_info);
  private:
    int process_id_;
    txc_session_info info_;

    std::string skill_id_;
    std::string skill_name_;
    AppSkill *strategy_;
};

////////////////////////////////////////
//////////////////// start of system architecture
//class Grammar
//{
//public:
//    int Recognize(unsigned char *wav, unsigned int len);
//};
//class Bot
//{
//public:
//
//};
//class Kit
//{
//
//};

//////////////////// end of system architecture
////////////////////////////////////////

//////// bridge ////////

class TXCAudioService
{
  public:
    void Init();

  private:
    friend class TXCServices;
    TXCAudioService();
    TXCAudioService(const TXCAudioService &);
    TXCAudioService &operator=(const TXCAudioService &);
};

bool post_message(SESSION id, XWM_EVENT event, XWPARAM arg1, XWPARAM arg2, unsigned int delay = 0);

bool send_message(SESSION id, XWM_EVENT event, XWPARAM arg1, XWPARAM arg2);

#endif /* _AIAUDIO_AUDIOAPP_HPP_ */
