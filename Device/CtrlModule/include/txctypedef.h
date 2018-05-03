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
#ifndef _AIAUDIO_TYPEDEF_H_
#define _AIAUDIO_TYPEDEF_H_

////////////////////////////////
//  OS detecte
////////////////////////////////
#ifdef _WIN32
//define something for Windows (32-bit and 64-bit, this part is common)
#ifdef _WIN64
//define something for Windows (64-bit only)
#else
//define something for Windows (32-bit only)
#endif
#elif __APPLE__
#include "TargetConditionals.h"
#if TARGET_IPHONE_SIMULATOR
// iOS Simulator
#elif TARGET_OS_IPHONE
// iOS device
#elif TARGET_OS_MAC
#define OS_MAC
// Other kinds of Mac OS
#else
#error "Unknown Apple platform"
#endif
#elif defined(__ANDROID__)
#define OS_ANDROID
#elif defined(__ANDROID_API__)
#define OS_ANDROID
#elif __linux__
#define OS_LINUX
#elif __unix__ // all unices not caught above
#define OS_UNIX
#elif defined(_POSIX_VERSION)
// POSIX
#else
#error "Unknown compiler"
#endif

////////////////////////////////
//  end of OS detecte
////////////////////////////////

#include <stddef.h>
#include <errno.h>

#define _RE_ARCHITECTURE_

#ifndef _In_
#define _In_
#endif

#ifndef _Out_
#define _Out_
#endif

#ifndef _InOut_
#define _InOut_
#endif

#define SDK_API __attribute__((visibility("default")))

#ifndef __cplusplus
#define bool _Bool
#define true 1
#define false 0
#define CXX_EXTERN_BEGIN
#define CXX_EXTERN_END
#define C_EXTERN extern
#else
#define _Bool bool
#define CXX_EXTERN_BEGIN extern "C" {
#define CXX_EXTERN_END }
#define C_EXTERN
#endif

#ifndef ERR_SUCCESS
#define ERR_SUCCESS 0
#endif

typedef unsigned int uint32;
typedef long long int64;
typedef void *XWPARAM;
typedef int SESSION;

//  TODO:
//#    define _XP_API                __attribute__((visibility("hidden")))
//#    define _XP_CLS                __attribute__((visibility("hidden")))

#endif /* _AIAUDIO_TYPEDEF_H_ */
