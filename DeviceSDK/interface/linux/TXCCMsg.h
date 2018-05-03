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
#ifndef __TXCC_MSG_H__
#define __TXCC_MSG_H__

/////////////////////////////////////////////////////////////////////////////
//
//                          【向App发送CC消息】
//
/////////////////////////////////////////////////////////////////////////////

// CC消息结构定义
typedef struct _txca_param_cc_msg
{
    const char *business_name; // 业务名：ai.external.xiaowei   ai.internal
    const char *msg;           // 消息体
    unsigned int msg_len;      // 消息体长度
} TXCA_PARAM_CC_MSG;

// CC消息回调通知
typedef struct _tag_txca_cc_msg_callback
{
    void (*on_txca_cc_msg_send_ret)(unsigned int cookie, unsigned long long to, int err_code);
    void (*on_txca_cc_msg_recv)(unsigned long long from, TXCA_PARAM_CC_MSG *msg);
} TXCA_CC_MSG_CALLBACK;

/**
 * 接口说明: 初始化cc消息的回调
 * on_txca_cc_msg_send_ret: 发送cc消息的结果回调
 * on_txca_cc_msg_recv: 收到cc消息的push
 */
SDK_API int txca_init_cc_msg_callback(TXCA_CC_MSG_CALLBACK *callback);

/**
 * 发送c2c消息给小微app
 * to : target tinyid
 * msg : 发送数据内容
 * cookie：返回任务cookie
 * 返回值 : 错误码（见全局错误码表）
 */
SDK_API int txca_send_c2c_msg(unsigned long long to, TXCA_PARAM_CC_MSG *msg, unsigned int *cookie);

#endif /* __TXCC_MSG_H__ */
