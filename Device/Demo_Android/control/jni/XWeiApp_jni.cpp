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

#include "AudioApp.h"
#include "ScopedJNIEnv.h"

#ifdef __cplusplus
extern "C" {
#endif

CGlobalJNIEnv *g_xwei_app_jni = NULL;
jclass g_class_session_info = NULL;

JNIEXPORT void JNICALL Java_com_tencent_xiaowei_control_XWeiApp_nativeInit(JNIEnv *env, jclass service) {
    g_xwei_app_jni = new CGlobalJNIEnv;
    g_xwei_app_jni->InitJNIObject(env, service);

    jclass cls_session_info = env->FindClass("com/tencent/xiaowei/control/info/XWeiSessionInfo");
    g_class_session_info = (jclass) env->NewGlobalRef(cls_session_info);

}

JNIEXPORT void JNICALL Java_com_tencent_xiaowei_control_XWeiApp_nativeUninit(JNIEnv *env, jclass service) {

}

JNIEXPORT jintArray JNICALL
Java_com_tencent_xiaowei_control_XWeiApp_txcListSessions(JNIEnv *env, jclass service) {
    jintArray sessions_result = NULL;

    const int buffer_count = 128;
    jint sessions[buffer_count];
    int session_count = txc_list_sessions(sessions, buffer_count);

    if (0 < session_count) {
        JNIEnv *penv = env;
        if (penv) {
            sessions_result = penv->NewIntArray(session_count);
            if (sessions_result) {
                penv->SetIntArrayRegion(sessions_result, 0, session_count, sessions);
            }
        }
    }

    return sessions_result;
}

JNIEXPORT jobject JNICALL
Java_com_tencent_xiaowei_control_XWeiApp_txcGetSession(JNIEnv *env, jclass service, jint session_id) {
    jobject result_info = NULL;
    const txc_session_info *info = txc_get_session(session_id);
    if (info) {
        JNIEnv *penv = env;
        if (penv) {
            if (g_class_session_info) {
                result_info = penv->AllocObject(g_class_session_info);
                if (result_info) {
                    jfieldID fld_skillName = penv->GetFieldID(g_class_session_info, "skillName",
                                                              "Ljava/lang/String;");
                    jfieldID fld_skillId = penv->GetFieldID(g_class_session_info, "skillId",
                                                            "Ljava/lang/String;");

                    if (fld_skillName
                        && fld_skillId) {
                        if (info->skill_name && info->skill_name[0]) {
                            jstring str_skillName = env->NewStringUTF(info->skill_name);
                            penv->SetObjectField(result_info, fld_skillName, str_skillName);
                            env->DeleteLocalRef(str_skillName);
                        }

                        if (info->skill_id && info->skill_id[0]) {
                            jstring str_skillId = env->NewStringUTF(info->skill_id);
                            penv->SetObjectField(result_info, fld_skillId, str_skillId);
                            env->DeleteLocalRef(str_skillId);
                        }
                    }
                }
            }
        }
    }

    return result_info;
}

#ifdef __cplusplus
}
#endif
