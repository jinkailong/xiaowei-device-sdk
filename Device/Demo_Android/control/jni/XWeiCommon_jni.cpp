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
#include <stdlib.h>
#include <jni.h>
#include "ScopedJNIEnv.h"

#include "CommonMgr.h"
#include "Utils.h"

#ifdef __cplusplus
extern "C" {
#endif

jobject s_obj_XWeiCommon = NULL;
jclass s_class_XWeiCommon = NULL;

void on_common_control(int type, const char *jsonData) {
    bool needRelease = false;
    JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
    if (!env) return;
    if (s_class_XWeiCommon == NULL || s_obj_XWeiCommon == NULL) {
        return;
    }
    static jmethodID onCommonControl = NULL;
    if (onCommonControl == NULL) {
        jclass cls = env->GetObjectClass(s_obj_XWeiCommon);
        onCommonControl = env->GetMethodID(cls, "onCommonControl",
                                           "(ILjava/lang/String;)V");
        env->DeleteLocalRef(cls);
    }
    jstring jText;
    ConvChar2JString(env, jsonData, jText);
    env->CallVoidMethod(s_obj_XWeiCommon, onCommonControl, type, jText);
    env->DeleteLocalRef(jText);
    if (needRelease) {
        CGlobalJNIEnv::Util_ReleaseEnv();
    }
}

JNIEXPORT void JNICALL Java_com_tencent_xiaowei_control_XWeiCommon_nativeInit(JNIEnv *env, jclass service) {
    s_obj_XWeiCommon = env->NewGlobalRef(service);

    jclass cls_XWeiCommon = env->GetObjectClass(service);
    s_class_XWeiCommon = (jclass) env->NewGlobalRef(cls_XWeiCommon);
    CommonMgrCallback CommonMgr = {0};
    CommonMgr.on_common_control = on_common_control;
    txc_set_common_mgr_callback(&CommonMgr);

}

JNIEXPORT void JNICALL
Java_com_tencent_xiaowei_control_XWeiCommon_nativeUninit(JNIEnv *env, jclass service) {

}


#ifdef __cplusplus
}
#endif
