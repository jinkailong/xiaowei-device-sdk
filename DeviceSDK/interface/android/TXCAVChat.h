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

// 本头文件中的接口仅提供给Android平台使用，其他平台使用这些接口，我们不能保证正常使用
#ifndef __TX_CLOUD_AV_CHAT_H__
#define __TX_CLOUD_AV_CHAT_H__

#include "TXSDKCommonDef.h"

CXX_EXTERN_BEGIN

/**
 * 视频通话回调接口
 */
typedef struct tag_tx_av_chat_notify
{
	// 收到视频请求
	void (*on_receive_video_push)(char * pBufReply, int nLenReply, unsigned long long sendUin, int sendUinType);
} tx_av_chat_notify;

/**
 * 接口说明：设置音频通话相关的通知回调接口
 *
 * @param notify 视频通话回调接口
 */
SDK_API void tx_set_av_chat_notify(tx_av_chat_notify * notify);

/**
 * 发送视频请求回调接口
 */
typedef void (*on_receive_video_reply)(char * pBufReply, int nLenReply);
/**
 * 接口说明：主动请求视频通话
 *
 * @param type 类型
 * @param callback 视频通话回调接口
 */
SDK_API void tx_send_video_request(int type, unsigned long long toDin, const char * pBuff, unsigned int uLen, on_receive_video_reply callback);

/**
 * 接口说明：请求视频通话校验信息
 *
 * @param pBuf 校验信息内容
 * @param puLen 校验信息长度
 */
SDK_API int tx_get_video_chat_signature(char* pBuf, int* puLen);

CXX_EXTERN_END
#endif // __TX_CLOUD_AV_CHAT_H__
