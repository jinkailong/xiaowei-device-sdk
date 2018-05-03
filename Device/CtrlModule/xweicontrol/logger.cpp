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
#include "logger.h"
#include "../library/log_c/src/log.h"
#include "string.h"

#define _MAX_EVT_LEN 10240
inline void TXCDefaultLog(int level, const char *module, int line, const char *text);
custom_log_function g_custom_log = TXCDefaultLog;

char *getLogText(const char *format, va_list args)
{
    char msg[_MAX_EVT_LEN + 7] = {0}; /*5bytes for '...' and '\r\n'*/

    int len = 0, _r = 0;
    /*log message*/
    _r = vsnprintf(&(msg[len]), _MAX_EVT_LEN - len, format, args);
    if (_r < 0 || _r > (_MAX_EVT_LEN - len))
    {
        /*utf-8 truncate*/
        int maxln = _MAX_EVT_LEN;
        if (msg[_MAX_EVT_LEN - 1] < 0)
        {
            maxln--;
            if (msg[_MAX_EVT_LEN - 2] < 0)
            {
                maxln--;
            }
        }
        strcpy(&(msg[maxln]), "...");
        len = maxln + 3;
    }
    else
    {
        len += _r;
    }
    msg[len] = 0;
    char *str = msg;
    return str;
}

void TXCLog(int level, const char *file, int line, const char *fmt, ...)
{
    if (g_custom_log)
    {
        if (file && file[0])
        {
            const char *fn = strrchr(file, '/');
            if (fn)
            {
                file = fn + 1;
            }
        }

        va_list ap;
        va_start(ap, fmt);
        if (g_custom_log)
        {
            g_custom_log(level, file, line, getLogText(fmt, ap));
        }
        va_end(ap);
    }
}

inline void TXCDefaultLog(int level, const char *module, int line, const char *text)
{

    //printf("[%s], %s\n", module, text);
    //  default log.c
    //    https://github.com/waynetran/log.c
    log_log(level - 2, module, line, text);
}

void txc_set_log_function(custom_log_function func)
{
    g_custom_log = func;
}
