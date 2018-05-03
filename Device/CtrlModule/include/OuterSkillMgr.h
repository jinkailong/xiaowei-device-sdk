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
#ifndef OuterSkillMgr_h
#define OuterSkillMgr_h

#include "txctypedef.h"

typedef struct _txca_param_response TXCA_PARAM_RESPONSE;
struct tcx_xwei_outer_skill_callback
{
    // 当内部的几个基础skill无法处理，将回调它，应用层如果可以处理这个响应，返回true，将收到send_txca_response
    bool (*start_outer_skill)(int session_id, const char *skill_name, const char *skill_id);
    // 回调cRsp，由外面进行处理，例如：闹钟skill
    bool (*send_txca_response)(int session_id, TXCA_PARAM_RESPONSE *cRsp);
};

// 外部处理响应的回调
SDK_API extern tcx_xwei_outer_skill_callback outer_skill_callback;

#endif /* OuterSkillMgr_h */
