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
#ifndef __TXCAUDIO_MSG_H__
#define __TXCAUDIO_MSG_H__

/////////////////////////////////////////////////////////////////////////////
//
//                          【向手机QQ发送结构化消息】
//
/////////////////////////////////////////////////////////////////////////////

// 结构化消息定义
typedef struct tag_structuring_msg
{
    // char * '\0'结尾UTF-8字符串
    int msg_id;                       // 1:图片消息 2:视频消息 3:语音消息
    char *file_path;                  // 文件的path
    char *thumb_path;                 // 缩略图path（可空）
    char *title;                      // 结构化消息标题
    char *digest;                     // 简述文字
    char *guide_words;                // 引导文字
    unsigned int duration;             // 如果是语音消息，该字段用于设置录音时长 单位:秒
    unsigned long long *to_targetids;   // 指定发给某些target
    unsigned int to_targetids_count;   // 指定发给某些target的count
} STRUCTURING_MSG;

// 消息回调通知
typedef struct _tx_send_msg_notify
{
    void (*on_file_transfer_progress)(const unsigned int cookie, unsigned long long transfer_progress, unsigned long long max_transfer_progress);
    void (*on_send_structuring_msg_ret)(const unsigned int cookie, int err_code);
} TXCA_SEND_MSG_NOTIFY;

/**
 * 接口说明：向手机QQ发送结构化消息，每条消息都会有唯一的 cookie, TXCA_SEND_MSG_NOTIFY 用于了解发送状态
 * msg：结构化消息
 * notify：进度，结果回调
 * cookie：返回任务cookie
 */
SDK_API void txca_send_structuring_msg(const STRUCTURING_MSG *msg, TXCA_SEND_MSG_NOTIFY *notify, unsigned int *cookie);


#endif /* __TXCAUDIO_MSG_H__ */
