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
#include <string.h>
#include "TXCAudio.h"

#include "AudioApp.h"
#include "Player.h"
#include "PlayerCallback.h"
#include "ScopedJNIEnv.h"

#include "Utils.h"
#include "AudioFocus.h"

#ifdef __cplusplus
extern "C" {
#endif

extern jclass s_class_XWeiControl;
extern jobject s_obj_XWeiControl;
extern jclass s_class_Response;
extern jclass s_class_SkillInfo;
extern jclass s_class_ContextInfo;
extern jclass s_class_Resource;
extern jclass s_class_ResGroup;
extern jclass s_class_MsgInfo;

jobject g_xwei_control_jin_obj = NULL;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
//    CScopedJNIEnv::s_JVM = vm;
    CGlobalJNIEnv::s_JVM = vm;
    return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
    if (g_xwei_control_jin_obj) {
//        CScopedJNIEnv::UninitService(g_xwei_control_jin_obj);
    }
}

void android_log_mapping(int level, const char *module, int line, const char *text) {
    if (text) {
        bool needRelease = false;
        std::string str = text;
        JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
        if (!env) return;
        if (s_class_XWeiControl == NULL || s_obj_XWeiControl == NULL) {
            return;
        }
        static jmethodID onNativeLog = NULL;
        if (onNativeLog == NULL) {
            jclass cls = env->GetObjectClass(s_obj_XWeiControl);
            onNativeLog = env->GetMethodID(cls, "onNativeLog",
                                           "(ILjava/lang/String;ILjava/lang/String;)V");
            env->DeleteLocalRef(cls);
        }
        jstring jModule;
        ConvChar2JString(env, module, jModule);
        jstring jMessage;
        ConvChar2JString(env, str.c_str(), jMessage);
        env->CallVoidMethod(s_obj_XWeiControl, onNativeLog, level, jModule, line,
                            jMessage);
        env->DeleteLocalRef(jModule);
        env->DeleteLocalRef(jMessage);
        if (needRelease) {
            CGlobalJNIEnv::Util_ReleaseEnv();
        }
    }
}

void on_audio_focus_change(int cookie, int focusChange) {
    bool needRelease = false;
    JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
    if (!env) return;
    if (s_class_XWeiControl == NULL || s_obj_XWeiControl == NULL) {
        return;
    }
    static jmethodID onFocusChange = NULL;
    if (onFocusChange == NULL) {
        jclass cls = env->GetObjectClass(s_obj_XWeiControl);
        onFocusChange = env->GetMethodID(cls, "onFocusChange", "(II)V");
        env->DeleteLocalRef(cls);
    }
    env->CallVoidMethod(s_obj_XWeiControl, onFocusChange, cookie, focusChange);
    if (needRelease) {
        CGlobalJNIEnv::Util_ReleaseEnv();
    }
}

JNIEXPORT void JNICALL
Java_com_tencent_xiaowei_control_XWeiControl_nativeInit(JNIEnv *env, jclass service) {
    CPlayerCallback::InitClasses(env, service);

    txc_xwei_control xwei_control = {0};
    xwei_control.control_callback = &CPlayerCallback::txc_control_android_callback;
    txc_xwei_control_init(&xwei_control);
    txc_set_audio_focus_change_callback(on_audio_focus_change);

    txc_set_log_function(android_log_mapping);

}

JNIEXPORT void JNICALL
Java_com_tencent_xiaowei_control_XWeiControl_nativeUninit(JNIEnv *env, jclass service) {
//    CScopedJNIEnv::UninitService(g_xwei_control_jin_obj);
}

JNIEXPORT jint JNICALL
Java_com_tencent_xiaowei_control_XWeiControl_nativeRequestAudioFocus(JNIEnv *env, jclass service,
                                                             jint cookie, jint duration) {
    txc_request_audio_focus(cookie, (DURATION_HINT) duration);
    return cookie;
}

JNIEXPORT void JNICALL
Java_com_tencent_xiaowei_control_XWeiControl_nativeAbandonAudioFocus(JNIEnv *env, jclass service, jint cookie) {
    txc_abandon_audio_focus(cookie);
}

JNIEXPORT void JNICALL
Java_com_tencent_xiaowei_control_XWeiControl_nativeAbandonAllAudioFocus(JNIEnv *env, jclass service) {
    txc_abandon_all_audio_focus();
}

JNIEXPORT void JNICALL
Java_com_tencent_xiaowei_control_XWeiControl_nativeSetAudioFocus(JNIEnv *env, jclass service, int focus) {
    txc_set_audio_focus((DURATION_HINT) focus);
}

JNIEXPORT jboolean JNICALL
Java_com_tencent_xiaowei_control_XWeiControl_nativeProcessResponse(JNIEnv *env, jclass service,
                                                           jstring objVoiceId, jint event, jobject objResponse,
                                                           jbyteArray objExtendInfo) {
    TXCA_EVENT txca_event = (TXCA_EVENT)event;
    __android_log_print(ANDROID_LOG_INFO, "XWeiControlJNI_",
                                "nativeProcessResponse event:%d", event);
    if (txca_event != txca_event_on_response) //非txca_event_on_response的事件另外处理
    {
        TXCA_PARAM_RESPONSE pRsp = {0};
        const char *pVoiceId = objVoiceId != NULL ? env->GetStringUTFChars(objVoiceId, NULL) : NULL;
        unsigned int extendInfoLen =
                    objExtendInfo == NULL ? 0 : (unsigned int) env->GetArrayLength(objExtendInfo);
        char pExtendInfo[extendInfoLen + 1];

        if (objExtendInfo) {
            jbyte *buf = env->GetByteArrayElements(objExtendInfo, NULL);
            memset(pExtendInfo, 0, extendInfoLen);
            memcpy(pExtendInfo, buf, extendInfoLen);
            pExtendInfo[extendInfoLen] = 0;
            env->ReleaseByteArrayElements(objExtendInfo, buf, 0);
        }

        jboolean ret = (jboolean) txc_process_response(pVoiceId, txca_event,
                                                           (const char *) &pRsp, pExtendInfo,
                                                           extendInfoLen);

        if (objVoiceId) {
            env->ReleaseStringUTFChars(objVoiceId, pVoiceId);
        }
        return ret;
    }

    if (objResponse == NULL) {
        return (jboolean) false;
    }

    TXCA_PARAM_RESPONSE pRsp = {0};
    const char *pVoiceId = objVoiceId != NULL ? env->GetStringUTFChars(objVoiceId, NULL) : NULL;
    unsigned int extendInfoLen =
            objExtendInfo == NULL ? 0 : (unsigned int) env->GetArrayLength(objExtendInfo);
    char pExtendInfo[extendInfoLen + 1];

    if (objExtendInfo) {
        jbyte *buf = env->GetByteArrayElements(objExtendInfo, NULL);
        memset(pExtendInfo, 0, extendInfoLen);
        memcpy(pExtendInfo, buf, extendInfoLen);
        pExtendInfo[extendInfoLen] = 0;
        env->ReleaseByteArrayElements(objExtendInfo, buf, 0);
    }

    jclass clsResponse = s_class_Response;
    jclass clsSkillInfo = s_class_SkillInfo;
    jclass clsContextInfo = s_class_ContextInfo;
    jclass clsResource = s_class_Resource;
    jclass clsResGroup = s_class_ResGroup;

    jfieldID jfSkillInfo = env->GetFieldID(clsResponse, "appInfo",
                                           "Lcom/tencent/xiaowei/info/XWAppInfo;");
    jfieldID jfLastSkillInfo = env->GetFieldID(clsResponse, "lastAppInfo",
                                           "Lcom/tencent/xiaowei/info/XWAppInfo;");
    jfieldID jfContextInfo = env->GetFieldID(clsResponse, "context",
                                             "Lcom/tencent/xiaowei/info/XWContextInfo;");
    jfieldID jfResGroup = env->GetFieldID(clsResponse, "resources",
                                          "[Lcom/tencent/xiaowei/info/XWResGroupInfo;");

    jfieldID jfRequestText = env->GetFieldID(clsResponse, "requestText", "Ljava/lang/String;");
    jfieldID jfResponseData = env->GetFieldID(clsResponse, "responseData", "Ljava/lang/String;");
    jfieldID jfAutoTestData = env->GetFieldID(clsResponse, "autoTestData", "Ljava/lang/String;");
    jfieldID jfResponseType = env->GetFieldID(clsResponse, "responseType", "I");
    jfieldID jfResultCode = env->GetFieldID(clsResponse, "resultCode", "I");
    jfieldID jfHasMore = env->GetFieldID(clsResponse, "hasMorePlaylist", "Z");
    jfieldID jfIsRecovery = env->GetFieldID(clsResponse, "recoveryAble", "Z");
    jfieldID jfPlayBehavior = env->GetFieldID(clsResponse, "playBehavior", "I");
    jfieldID jfIsNotify = env->GetFieldID(clsResponse, "isNotify", "Z");

    jstring strRequestText = (jstring) env->GetObjectField(objResponse, jfRequestText);
    jstring strResponseData = (jstring) env->GetObjectField(objResponse, jfResponseData);
    jstring strAutoTestData = (jstring) env->GetObjectField(objResponse, jfAutoTestData);

    if (pVoiceId) {
        memset(pRsp.voice_id, 0, 33);
        memcpy(pRsp.voice_id, pVoiceId, 33);
    }
    if (strRequestText) {
        pRsp.request_text = env->GetStringUTFChars(strRequestText, NULL);
    }
    if (strResponseData) {
        pRsp.response_data = env->GetStringUTFChars(strResponseData, NULL);
    }

    pRsp.error_code = (unsigned int) env->GetIntField(objResponse, jfResultCode);
    pRsp.response_type = (unsigned int) env->GetIntField(objResponse, jfResponseType);
    pRsp.has_more_playlist = (bool) env->GetBooleanField(objResponse, jfHasMore);
    pRsp.is_recovery = (bool) env->GetBooleanField(objResponse, jfIsRecovery);
    pRsp.is_notify = (bool) env->GetBooleanField(objResponse, jfIsNotify);
    pRsp.play_behavior = TXCA_PLAYLIST_ACTION(env->GetIntField(objResponse, jfPlayBehavior));

    // SKILL Info
    jobject objSkillInfo = env->GetObjectField(objResponse, jfSkillInfo);
    jfieldID jfSkillName = env->GetFieldID(clsSkillInfo, "name", "Ljava/lang/String;");
    jfieldID jfSkillId = env->GetFieldID(clsSkillInfo, "ID", "Ljava/lang/String;");
    jfieldID jfType = env->GetFieldID(clsSkillInfo, "type", "I");

    jstring strSkillName = NULL;
    jstring strSkillId = NULL;
    if (objSkillInfo) {
        strSkillName = (jstring) env->GetObjectField(objSkillInfo, jfSkillName);
        strSkillId = (jstring) env->GetObjectField(objSkillInfo, jfSkillId);
        pRsp.skill_info.type = (unsigned int) env->GetIntField(objSkillInfo, jfType);
    }
    if (strSkillId) {
        pRsp.skill_info.id = env->GetStringUTFChars(strSkillId, NULL);
    }
    if (strSkillName) {
        pRsp.skill_info.name = env->GetStringUTFChars(strSkillName, NULL);
    }

    // Last SKILL Info
    jobject objLastSkillInfo = env->GetObjectField(objResponse, jfLastSkillInfo);

    jstring strSkillName2 = NULL;
    jstring strSkillId2 = NULL;
    if (objLastSkillInfo) {
        strSkillName2 = (jstring) env->GetObjectField(objLastSkillInfo, jfSkillName);
        strSkillId2 = (jstring) env->GetObjectField(objLastSkillInfo, jfSkillId);
        pRsp.last_skill_info.type = (unsigned int) env->GetIntField(objLastSkillInfo, jfType);
    }
    if (strSkillId2) {
        pRsp.last_skill_info.id = env->GetStringUTFChars(strSkillId2, NULL);
    }
    if (strSkillName2) {
        pRsp.last_skill_info.name = env->GetStringUTFChars(strSkillName2, NULL);
    }

    // Context Info
    jobject objContextInfo = env->GetObjectField(objResponse, jfContextInfo);
    jfieldID jfContextId = env->GetFieldID(clsContextInfo, "ID", "Ljava/lang/String;");
    jfieldID jfSpeakTimeout = env->GetFieldID(clsContextInfo, "speakTimeout", "I");
    jfieldID jfSilentTimeout = env->GetFieldID(clsContextInfo, "silentTimeout", "I");
    jfieldID jfVoiceRequestBegin = env->GetFieldID(clsContextInfo, "voiceRequestBegin", "Z");
    jfieldID jfVoiceRequestEnd = env->GetFieldID(clsContextInfo, "voiceRequestBegin", "Z");
    jfieldID jfProfileType = env->GetFieldID(clsContextInfo, "profileType", "I");

    jstring strContextId = NULL;
    if (objContextInfo) {
        strContextId = (jstring) env->GetObjectField(objContextInfo, jfContextId);
        pRsp.context.speak_timeout = (unsigned int) env->GetIntField(objContextInfo,
                                                                     jfSpeakTimeout);
        pRsp.context.silent_timeout = (unsigned int) env->GetIntField(objContextInfo,
                                                                      jfSilentTimeout);
        pRsp.context.wakeup_profile = TXCA_WAKEUP_PROFILE(
                env->GetIntField(objContextInfo, jfProfileType));
        pRsp.context.voice_request_begin = env->GetBooleanField(objContextInfo,
                                                                jfVoiceRequestBegin);
        pRsp.context.voice_request_end = env->GetBooleanField(objContextInfo, jfVoiceRequestEnd);

    }
    if (strContextId) {
        pRsp.context.id = env->GetStringUTFChars(strContextId, NULL);
    }

    // RESOURCE GROUP List
    jobjectArray objArrayGroup = (jobjectArray)env->GetObjectField(objResponse, jfResGroup);

    jfieldID jfResources = env->GetFieldID(clsResGroup, "resources", "[Lcom/tencent/xiaowei/info/XWResourceInfo;");

    jfieldID jfFormat = env->GetFieldID(clsResource, "format", "I");
    jfieldID jfOffset = env->GetFieldID(clsResource, "offset", "I");
    jfieldID jfPlayCount = env->GetFieldID(clsResource, "playCount", "I");
    jfieldID jfResourceId = env->GetFieldID(clsResource, "ID", "Ljava/lang/String;");
    jfieldID jfContent = env->GetFieldID(clsResource, "content", "Ljava/lang/String;");
    jfieldID jfExtendInfo = env->GetFieldID(clsResource, "extendInfo", "Ljava/lang/String;");

    if (objArrayGroup) {
        jsize groupLength = env->GetArrayLength(objArrayGroup);
        __android_log_print(ANDROID_LOG_INFO, "XWeiControlJNI_",
                            "nativeProcessResponse groupLength:%d", groupLength);

        pRsp.resource_groups_size = (unsigned int) groupLength;
        pRsp.resource_groups = (TXCA_PARAM_RES_GROUP *) malloc(
                sizeof(TXCA_PARAM_RES_GROUP) * pRsp.resource_groups_size);
        memset(pRsp.resource_groups, 0, sizeof(TXCA_PARAM_RES_GROUP) * pRsp.resource_groups_size);

        for (jsize i = 0; i < groupLength; i++) {
            jobject objResGroup = env->GetObjectArrayElement(objArrayGroup, i);
            jobjectArray objArrayResource = (jobjectArray)env->GetObjectField(objResGroup, jfResources);
            jsize resLength = env->GetArrayLength(objArrayResource);
            pRsp.resource_groups[i].resources_size = (unsigned int)resLength;
            pRsp.resource_groups[i].resources = (TXCA_PARAM_RESOURCE *) malloc(
                    sizeof(TXCA_PARAM_RESOURCE) * pRsp.resource_groups[i].resources_size);
            memset(pRsp.resource_groups[i].resources, 0, sizeof(TXCA_PARAM_RESOURCE) * pRsp.resource_groups[i].resources_size);

            for (jsize j = 0; j < resLength; j++) {
                jobject item = env->GetObjectArrayElement(objArrayResource, j);
                jstring strResourceId = (env->GetObjectField(item, jfResourceId) != NULL
                                         ? (jstring) env->GetObjectField(item, jfResourceId) : NULL);
                if (strResourceId) {
                    (pRsp.resource_groups[i].resources + j)->id = (char *) env->GetStringUTFChars(strResourceId, NULL);
                }
                jstring strContent = (env->GetObjectField(item, jfContent) != NULL
                                      ? (jstring) env->GetObjectField(item, jfContent) : NULL);
                if (strContent) {
                    (pRsp.resource_groups[i].resources + j)->content = (char *) env->GetStringUTFChars(strContent, NULL);
                }
                jstring strExtendInfo = (env->GetObjectField(item, jfExtendInfo) != NULL
                                         ? (jstring) env->GetObjectField(item, jfExtendInfo) : NULL);
                if (strExtendInfo) {
                    (pRsp.resource_groups[i].resources + j)->extend_buffer = (char *) env->GetStringUTFChars(strExtendInfo,
                                                                                          NULL);
                }

                (pRsp.resource_groups[i].resources + j)->format = TXCA_RESOURCE_FORMAT(env->GetIntField(item, jfFormat));
                (pRsp.resource_groups[i].resources + j)->offset = (unsigned int) env->GetIntField(item, jfOffset);
                (pRsp.resource_groups[i].resources + j)->play_count = env->GetIntField(item, jfPlayCount);
            }
        }
    }

    jboolean ret = (jboolean) txc_process_response(pVoiceId, txca_event_on_response,
                                                   (const char *) &pRsp, pExtendInfo,
                                                   extendInfoLen);

    if (objVoiceId) {
        env->ReleaseStringUTFChars(objVoiceId, pVoiceId);
    }
    if (strRequestText) {
        env->ReleaseStringUTFChars(strRequestText, pRsp.request_text);
    }
    if (strResponseData) {
        env->ReleaseStringUTFChars(strResponseData, pRsp.response_data);
    }
    if (strSkillId) {
        env->ReleaseStringUTFChars(strSkillId, pRsp.skill_info.id);
    }
    if (strSkillName) {
        env->ReleaseStringUTFChars(strSkillName, pRsp.skill_info.name);
    }
    if (strSkillId2) {
        env->ReleaseStringUTFChars(strSkillId2, pRsp.last_skill_info.id);
    }
    if (strSkillName2) {
        env->ReleaseStringUTFChars(strSkillName2, pRsp.last_skill_info.name);
    }
    if (strContextId) {
        env->ReleaseStringUTFChars(strContextId, pRsp.context.id);
    }

    if (objArrayGroup) {
        jsize groupLength = env->GetArrayLength(objArrayGroup);
        for (jsize i = 0; i < groupLength; i++) {
            jobject objResGroup = env->GetObjectArrayElement(objArrayGroup, i);
            jobjectArray objArrayResource = (jobjectArray)env->GetObjectField(objResGroup, jfResources);
            jsize resLength = env->GetArrayLength(objArrayResource);
            for (jsize j = 0; j < resLength; j++) {
                jobject item = env->GetObjectArrayElement(objArrayResource, j);
                if ((pRsp.resource_groups[i].resources + j)->id) {
                    jstring strResourceId = (jstring) env->GetObjectField(item, jfResourceId);
                    env->ReleaseStringUTFChars(strResourceId, (pRsp.resource_groups[i].resources + j)->id);
                }
                if ((pRsp.resource_groups[i].resources + j)->content) {
                    jstring strContent = (jstring) env->GetObjectField(item, jfContent);
                    env->ReleaseStringUTFChars(strContent, (pRsp.resource_groups[i].resources + j)->content);
                }
                if ((pRsp.resource_groups[i].resources + j)->extend_buffer) {
                    jstring strExtendInfo = (jstring) env->GetObjectField(item, jfExtendInfo);
                    env->ReleaseStringUTFChars(strExtendInfo, (pRsp.resource_groups[i].resources +j)->extend_buffer);
                }
            }

            if (pRsp.resource_groups[i].resources) {
                free(pRsp.resource_groups[i].resources);
                pRsp.resource_groups[i].resources = NULL;
            }
        }

        if (pRsp.resource_groups != NULL) {
            free(pRsp.resource_groups);
            pRsp.resource_groups = NULL;
        }
    }

    return ret;
}

JNIEXPORT void JNICALL
Java_com_tencent_xiaowei_control_XWeiControl_nativeAddMsgToMsgbox(JNIEnv *env, jclass service, jobject msgInfo)
{
    jclass cls_MsgInfo = s_class_MsgInfo;
    jfieldID jfTinyId = env->GetFieldID(cls_MsgInfo, "tinyId", "J");
    jfieldID jfType = env->GetFieldID(cls_MsgInfo, "type", "I");
    jfieldID jfContent = env->GetFieldID(cls_MsgInfo, "content", "Ljava/lang/String;");
    jfieldID jfDuration = env->GetFieldID(cls_MsgInfo, "duration", "I");
    jfieldID jfTimeStamp = env->GetFieldID(cls_MsgInfo, "timestamp", "I");
    jfieldID jfIsRecv = env->GetFieldID(cls_MsgInfo, "isRecv", "Z");

    jlong tinyId = env->GetLongField(msgInfo, jfTinyId);
    jint type = env->GetIntField(msgInfo, jfType);
    jstring content = (jstring) env->GetObjectField(msgInfo, jfContent);
    jint duration = env->GetIntField(msgInfo, jfDuration);
    jint timestamp = env->GetIntField(msgInfo, jfTimeStamp);
    jboolean isRecv = (jboolean) env->GetBooleanField(msgInfo, jfIsRecv);

    txc_msg_info info = {0};
    info.tinyId = tinyId;
    info.type = type;
    info.content = env->GetStringUTFChars(content, NULL);
    info.duration = duration;
    info.timestamp = timestamp;
    info.isRecv = isRecv;

    txc_xwei_msgbox_addmsg(&info);

    if (content) {
        env->ReleaseStringUTFChars(content, info.content);
    }
}


#ifdef __cplusplus
}
#endif
