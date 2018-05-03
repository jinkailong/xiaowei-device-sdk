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
//
//  TXCMessageQueue.hpp
//  txdevicesdk
//  播放器、播放列表、媒体数据等等，所有的数据写入、修改、删除，都必须在消息线程中
//  所有数据的读取也在消息线程中，对外提供的API以assert保证
//

#ifndef TXCMessageQueue_hpp
#define TXCMessageQueue_hpp

#include "AudioApp.h"
#include "Player.h"
#include <string>

namespace inner
{
class CMsgQueueImplAsyn;
}

namespace AIAudio
{
namespace Global
{
class CResponse;
}
}

struct _txca_param_response;
typedef struct _txca_param_response TXCA_PARAM_RESPONSE;

// 播放器、播放列表、媒体数据等等，所有的数据写入、修改、删除，都必须在消息线程中
//  所有数据的读取也在消息线程中，对外提供的API以assert保证
class TXCMessageQueue
{
  public:
    TXCMessageQueue();
    ~TXCMessageQueue();

    bool AddProcessor(txc_event_processor processor);
    void RemoveProcessor(txc_event_processor processor);

    //  async process at message thread
    void ProcessResponse(TXCA_PARAM_RESPONSE *response);
    
    void OnStartRequest(int errCode);
    void OnSilence(int errCode);
    void OnVoiceData(const char* data, int length);

    bool PostMessage(SESSION id, XWM_EVENT event, XWPARAM arg1, XWPARAM arg2, unsigned int delay = 0);
    bool SendMessage(SESSION id, XWM_EVENT event, XWPARAM arg1, XWPARAM arg2);

  protected:
    void Start();
    void Stop();

  private:
    friend class TXCServices;

    inner::CMsgQueueImplAsyn *msg_queue_impl_;
};

#endif /* TXCMessageQueue_hpp */
