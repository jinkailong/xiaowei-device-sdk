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
#ifndef __TXCAudioWordsList_H__
#define __TXCAudioWordsList_H__

#include "TXSDKCommonDef.h"

CXX_EXTERN_BEGIN

typedef enum enum_txca_words_type {
    txca_words_command = 0, // 可见可答屏幕词表，常用于屏幕上的按钮
    txca_words_contact = 1, // 联系人词表，设置后，一直有效。重复调用将替换之前设置的联系人词表。
} TXCA_WORDS_TYPE;

/**
 * 开启可见可答功能
 * @param enable 0:关闭 1:开启
 * 返回值：错误码（见全局错误码表）
 */
SDK_API int txca_enable_v2a(bool enable);

/**
 * 设置词表的异步结果回调
 * @param op 操作 0:上传 1:删除
 * @param errCode 0:成功 非0:失败
 */
typedef void (*TXCA_SET_WORDSLIST_CALLBACK)(int op, int errCode);

/**
 * 设置词表
 * @param type 0:可见可答屏幕词表 1:联系人词表
 * @param words_list : 词表数组
 * @param list_size: 词表数量
 * @param callback: 设置词表的结果回调
 * 返回值：错误码（见全局错误码表）
 */
SDK_API int txca_set_wordslist(TXCA_WORDS_TYPE type, char** words_list, int list_size, TXCA_SET_WORDSLIST_CALLBACK callback);

CXX_EXTERN_END

#endif /* __TXCAudioWordsList_H__ */
