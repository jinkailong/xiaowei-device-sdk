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
#ifndef loger_hpp
#define loger_hpp

#include "stdarg.h"
#include "txctypedef.h"
#include "AudioApp.h"

CXX_EXTERN_BEGIN

void TXCLog(int level, const char *file, int line, const char *fmt, ...);
#define TLOG_TRACE(...) TXCLog(TXC_LOG_VERBOSE, __FILE__, __LINE__, __VA_ARGS__)
#define TLOG_DEBUG(...) TXCLog(TXC_LOG_DEBUG, __FILE__, __LINE__, __VA_ARGS__)
#define TLOG_INFO(...) TXCLog(TXC_LOG_INFO, __FILE__, __LINE__, __VA_ARGS__)
#define TLOG_WARNING(...) TXCLog(TXC_LOG_WARN, __FILE__, __LINE__, __VA_ARGS__)
#define TLOG_ERROR(...) TXCLog(TXC_LOG_ERROR, __FILE__, __LINE__, __VA_ARGS__)

CXX_EXTERN_END

#endif /* logger_hpp */
