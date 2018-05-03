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
#include <jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <string>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/ioctl.h>
#include <net/if.h>
#include <netinet/in.h>
#include <linux/sockios.h>
#include <errno.h>
#include <sys/un.h>
#include <unistd.h>

#include "TXDeviceSDK.h"
#include "TXCAVChat.h"
#include "TXCAudioFileTransfer.h"
#include "TXCCMsg.h"
#include "TXOTA.h"
#include "Util.h"

#ifdef __cplusplus
extern "C" {
#endif

#define		LOGFILTER		"XWSDK_JNI"

extern JNIEnv* Util_CreateEnv(bool *pNeedRelease);
extern void Util_ReleaseEnv();

#ifdef __cplusplus
}
#endif
