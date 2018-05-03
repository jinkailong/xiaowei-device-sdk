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
#ifndef _AIAUDIO_COMMONMGR_H_
#define _AIAUDIO_COMMONMGR_H_
#include "txctypedef.h"

CXX_EXTERN_BEGIN

//////////////////// interface of media.h ////////////////////
enum CommonControlType
{
    TYPE_VOLUME_SET = 1,
    TYPE_VOLUME_SILENCE,
    TYPE_UPLOAD_LOG,
    TYPE_FETCH_DEVICE_INFO,
    TYPE_FEED_BACK_ERROR,
    TYPE_SET_FAVORITE,
};

// 通用控制回调
struct CommonMgrCallback
{
    void (*on_common_control)(int type, const char *data);
};

SDK_API extern CommonMgrCallback CommonMgr;

SDK_API void txc_set_common_mgr_callback(CommonMgrCallback *callback);

CXX_EXTERN_END

#endif // _AIAUDIO_COMMONMGR_H_
