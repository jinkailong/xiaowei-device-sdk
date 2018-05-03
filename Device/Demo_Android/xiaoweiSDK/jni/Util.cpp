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
#include "Util.h"


char checkUtfBytes(const char *bytes, const char **errorKind) {
    while (*bytes != '\0') {
        char utf8 = *(bytes++);
        // Switch on the high four bits.
        switch (utf8 >> 4) {
            case 0x00:
            case 0x01:
            case 0x02:
            case 0x03:
            case 0x04:
            case 0x05:
            case 0x06:
            case 0x07:
                // Bit pattern 0xxx. No need for any extra bytes.
                break;
            case 0x08:
            case 0x09:
            case 0x0a:
            case 0x0b:
            case 0x0f:
                /*
                 * Bit pattern 10xx or 1111, which are illegal start bytes.
                 * Note: 1111 is valid for normal UTF-8, but not the
                 * modified UTF-8 used here.
                 */
                *errorKind = "start";
                return utf8;
            case 0x0e:
                // Bit pattern 1110, so there are two additional bytes.
                utf8 = *(bytes++);
                if ((utf8 & 0xc0) != 0x80) {
                    *errorKind = "continuation";
                    return utf8;
                }
                // Fall through to take care of the final byte.
            case 0x0c:
            case 0x0d:
                // Bit pattern 110x, so there is one additional byte.
                utf8 = *(bytes++);
                if ((utf8 & 0xc0) != 0x80) {
                    *errorKind = "continuation";
                    return utf8;
                }
                break;
        }
    }
    return 0;
}

void Translate2UTF8(const char *srcStr, std::string &utfStr) {
    if (srcStr == NULL) {
        utfStr = "";
        return;
    }

    utfStr = srcStr;
    const char *errorKind = NULL;
    checkUtfBytes(srcStr, &errorKind);
    if (errorKind != NULL) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER, "string %s contain no utf8 char", srcStr);
        utfStr = "";
    }
}

bool ConvChar2JString(JNIEnv *env, const char *str, jstring &jstr) {
    if (NULL == env) {
        return false;
    }
    std::string utfStr;
    Translate2UTF8(str, utfStr);

    if (utfStr.length() == 0) {
        jstr = env->NewStringUTF("");
    } else {
        jstr = env->NewStringUTF(utfStr.c_str());
    }
    return true;
}


CJNIEnv::CJNIEnv()
        : needRelease_(false), env_(NULL) {
    env_ = Util_CreateEnv(&needRelease_);
}

CJNIEnv::~CJNIEnv() {
    if (needRelease_) {
        Util_ReleaseEnv();
    }
}

JNIEnv *CJNIEnv::Env() {
    return env_;
}