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
#include <TXCAudioType.h>
#include "ScopedJNIEnv.h"

#include "OuterSkillMgr.h"
#include "Utils.h"

#ifdef __cplusplus
extern "C" {
#endif

jobject s_obj_XWeiOuterSkill = NULL;
jclass s_class_XWeiOuterSKill = NULL;
extern jclass s_class_Response;
extern jclass s_class_SkillInfo;
extern jclass s_class_ContextInfo;
extern jclass s_class_Resource;
extern jclass s_class_ResGroup;

bool start_outer_skill(int sessionId, const char *skillName, const char *skillId) {
    bool needRelease = false;
    JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
    if (!env || !s_class_XWeiOuterSKill) return false;

    bool bHandled = false;
    jmethodID onStartOuterSkill = env->GetMethodID(s_class_XWeiOuterSKill, "onStartOuterSkill",
                                             "(ILjava/lang/String;Ljava/lang/String;)Z");
    if (onStartOuterSkill) {
        jstring strSkillName;
        jstring strSkillId;
        ConvChar2JString(env, skillName, strSkillName);
        ConvChar2JString(env, skillId, strSkillId);
        bHandled = env->CallBooleanMethod(s_obj_XWeiOuterSkill, onStartOuterSkill, sessionId,
                                          strSkillName, strSkillId);
        env->DeleteLocalRef(strSkillName);
        env->DeleteLocalRef(strSkillId);
    }

    if (needRelease) {
        CGlobalJNIEnv::Util_ReleaseEnv();
    }

    return bHandled;
}

bool send_txca_response(int sessionId, TXCA_PARAM_RESPONSE *pRsp) {
    bool needRelease = false;
    JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
    if (!env || !s_class_XWeiOuterSKill) return false;

    bool bHandled;
    jmethodID onSendResponse = env->GetMethodID(s_class_XWeiOuterSKill, "onSendResponse",
                                          "(ILcom/tencent/xiaowei/info/XWResponseInfo;)Z");

    jobject objRsp = NULL;

    jfieldID jfSkillInfo = env->GetFieldID(s_class_Response, "appInfo",
                                           "Lcom/tencent/xiaowei/info/XWAppInfo;");
    jfieldID jfLastSkillInfo = env->GetFieldID(s_class_Response, "lastAppInfo",
                                           "Lcom/tencent/xiaowei/info/XWAppInfo;");
    jfieldID jfResultCode = env->GetFieldID(s_class_Response, "resultCode",
                                              "I");
    jfieldID jfVoiceId = env->GetFieldID(s_class_Response, "voiceID",
                                           "Ljava/lang/String;");
    jfieldID jfContext = env->GetFieldID(s_class_Response, "context",
                                            "Lcom/tencent/xiaowei/info/XWContextInfo;");
    jfieldID jfRequestText = env->GetFieldID(s_class_Response, "requestText",
                                              "Ljava/lang/String;");
    jfieldID jfResponseType = env->GetFieldID(s_class_Response,
                                               "responseType", "I");
    jfieldID jfResponseData = env->GetFieldID(s_class_Response,
                                               "responseData",
                                               "Ljava/lang/String;");
    jfieldID jfAutoTestData = env->GetFieldID(s_class_Response,
                                               "autoTestData",
                                               "Ljava/lang/String;");
    jfieldID jfResourceGroups = env->GetFieldID(s_class_Response, "resources",
                                            "[Lcom/tencent/xiaowei/info/XWResGroupInfo;");
    jfieldID jfResources = env->GetFieldID(s_class_ResGroup, "resources", "[Lcom/tencent/xiaowei/info/XWResourceInfo;");
    jfieldID jfHasMorePlaylist = env->GetFieldID(s_class_Response,
                                                  "hasMorePlaylist", "Z");
    jfieldID jfRecoveryAble = env->GetFieldID(s_class_Response, "recoveryAble",
                                             "Z");
    jfieldID jfPlayBehavior = env->GetFieldID(s_class_Response,
                                               "playBehavior", "I");
    jfieldID jfIsNotify = env->GetFieldID(s_class_Response, "isNotify",
                                             "Z");

    jfieldID jfSkillName = env->GetFieldID(s_class_SkillInfo, "name",
                                            "Ljava/lang/String;");
    jfieldID jfSkillID = env->GetFieldID(s_class_SkillInfo, "ID",
                                          "Ljava/lang/String;");
    jfieldID jfType = env->GetFieldID(s_class_SkillInfo, "type", "I");

    jfieldID jfContextID = env->GetFieldID(s_class_ContextInfo, "ID",
                                        "Ljava/lang/String;");
    jfieldID jfSpeakTimeout = env->GetFieldID(s_class_ContextInfo,
                                                  "speakTimeout", "I");
    jfieldID jfSilentTimeout = env->GetFieldID(s_class_ContextInfo,
                                                   "silentTimeout", "I");
    jfieldID jfVoiceReqBegin = env->GetFieldID(s_class_ContextInfo,
                                                   "voiceRequestBegin", "Z");
    jfieldID jfVoiceReqEnd = env->GetFieldID(s_class_ContextInfo,
                                                 "voiceRequestEnd", "Z");
    jfieldID jfProfileType = env->GetFieldID(s_class_ContextInfo,
                                                 "profileType", "I");

    jfieldID jfResFormat = env->GetFieldID(s_class_Resource, "format", "I");
    jfieldID jfResOffset = env->GetFieldID(s_class_Resource, "offset", "I");
    jfieldID jfPlayCount = env->GetFieldID(s_class_Resource, "playCount", "I");
    jfieldID jfResID = env->GetFieldID(s_class_Resource, "ID",
                                        "Ljava/lang/String;");
    jfieldID jfResContent = env->GetFieldID(s_class_Resource, "content",
                                             "Ljava/lang/String;");
    jfieldID jfResExtendInfo = env->GetFieldID(s_class_Resource, "extendInfo",
                                                "Ljava/lang/String;");

    //creat obj
    jmethodID init = env->GetMethodID(s_class_Response, "<init>", "()V");
    objRsp = env->NewObject(s_class_Response, init);

    //AppInfo
    jmethodID initAppInfo = env->GetMethodID(s_class_SkillInfo, "<init>", "()V");
    jobject objApp = env->NewObject(s_class_SkillInfo, initAppInfo);

    jstring strSkillName;
    ConvChar2JString(env, pRsp->skill_info.name, strSkillName);
    jstring strSkillID;
    ConvChar2JString(env, pRsp->skill_info.id, strSkillID);

    env->SetObjectField(objApp, jfSkillName, strSkillName);
    env->SetObjectField(objApp, jfSkillID, strSkillID);
    env->SetIntField(objApp, jfType, pRsp->skill_info.type);

    env->SetObjectField(objRsp, jfSkillInfo, objApp);
    env->DeleteLocalRef(strSkillName);
    env->DeleteLocalRef(strSkillID);
    env->DeleteLocalRef(objApp);

    //LastAppInfo
    jobject objLastApp = env->NewObject(s_class_SkillInfo, initAppInfo);

    jstring strSkillName2;
    ConvChar2JString(env, pRsp->last_skill_info.name, strSkillName2);
    jstring strSkillID2;
    ConvChar2JString(env, pRsp->last_skill_info.id, strSkillID2);

    env->SetObjectField(objLastApp, jfSkillName, strSkillName2);
    env->SetObjectField(objLastApp, jfSkillID, strSkillID2);
    env->SetIntField(objLastApp, jfType, pRsp->last_skill_info.type);

    env->SetObjectField(objRsp, jfLastSkillInfo, objLastApp);
    env->DeleteLocalRef(strSkillName2);
    env->DeleteLocalRef(strSkillID2);
    env->DeleteLocalRef(objLastApp);

    //result code
    env->SetIntField(objRsp, jfResultCode, pRsp->error_code);

    //voice id
    jstring strVoiceID;
    ConvChar2JString(env, pRsp->voice_id, strVoiceID);
    env->SetObjectField(objRsp, jfVoiceId, strVoiceID);
    env->DeleteLocalRef(strVoiceID);

    //ContextInfo
    jmethodID initContextInfo = env->GetMethodID(s_class_ContextInfo, "<init>",
                                                 "()V");
    jobject objContext = env->NewObject(s_class_ContextInfo, initContextInfo);

    jstring strCtxID;
    ConvChar2JString(env, pRsp->context.id, strCtxID);

    env->SetObjectField(objContext, jfContextID, strCtxID);
    env->SetIntField(objContext, jfSpeakTimeout,
                     (jint) pRsp->context.speak_timeout);
    env->SetIntField(objContext, jfSilentTimeout,
                     (jint) pRsp->context.silent_timeout);
    env->SetBooleanField(objContext, jfVoiceReqBegin,
                         (jboolean) pRsp->context.voice_request_begin);
    env->SetBooleanField(objContext, jfVoiceReqEnd,
                         (jboolean) pRsp->context.voice_request_end);
    env->SetIntField(objContext, jfProfileType,
                     (jint) pRsp->context.wakeup_profile);

    env->SetObjectField(objRsp, jfContext, objContext);
    env->DeleteLocalRef(strCtxID);
    env->DeleteLocalRef(objContext);

    //request_text
    jstring strRequestText;
    ConvChar2JString(env, pRsp->request_text, strRequestText);
    env->SetObjectField(objRsp, jfRequestText, strRequestText);
    env->DeleteLocalRef(strRequestText);

    //response_type
    env->SetIntField(objRsp, jfResponseType, pRsp->response_type);

    //response_data
    jstring strRspExtend;
    ConvChar2JString(env, pRsp->response_data, strRspExtend);
    env->SetObjectField(objRsp, jfResponseData, strRspExtend);
    env->DeleteLocalRef(strRspExtend);

    //auto_test_data
    jstring strTestExtend;
    ConvChar2JString(env, pRsp->auto_test_data, strTestExtend);
    env->SetObjectField(objRsp, jfAutoTestData, strTestExtend);
    env->DeleteLocalRef(strTestExtend);

    jmethodID initResGroup = env->GetMethodID(s_class_ResGroup, "<init>",
                                              "()V");

    jmethodID initResource = env->GetMethodID(s_class_Resource, "<init>",
                                              "()V");

    jobjectArray arrayGroup = env->NewObjectArray(pRsp->resource_groups_size,
                                                           s_class_ResGroup, NULL);
    for (int i = 0; i < pRsp->resource_groups_size; i++) {
        jobject objGroup = env->NewObject(s_class_ResGroup, initResGroup);
        jobjectArray arrayRes = env->NewObjectArray(pRsp->resource_groups[i].resources_size,
                                                    s_class_Resource, NULL);
        for (int j = 0; j < pRsp->resource_groups[i].resources_size; ++j) {
            jobject objRes = env->NewObject(s_class_Resource, initResource);

            jstring strResID;
            ConvChar2JString(env, pRsp->resource_groups[i].resources[j].id, strResID);
            jstring strResContent;
            ConvChar2JString(env, pRsp->resource_groups[i].resources[j].content, strResContent);
            jstring strResExtend;
            ConvChar2JString(env, pRsp->resource_groups[i].resources[j].extend_buffer, strResExtend);


            env->SetIntField(objRes, jfResFormat, pRsp->resource_groups[i].resources[j].format);
            env->SetIntField(objRes, jfResOffset, pRsp->resource_groups[i].resources[j].offset);
            env->SetIntField(objRes, jfPlayCount, pRsp->resource_groups[i].resources[j].play_count);
            env->SetObjectField(objRes, jfResID, strResID);
            env->SetObjectField(objRes, jfResContent, strResContent);
            env->SetObjectField(objRes, jfResExtendInfo, strResExtend);

            env->SetObjectArrayElement(arrayRes, j, objRes);

            env->DeleteLocalRef(strResID);
            env->DeleteLocalRef(strResContent);
            env->DeleteLocalRef(strResExtend);
            env->DeleteLocalRef(objRes);
        }
        env->SetObjectField(objGroup, jfResources, arrayRes);
        env->SetObjectArrayElement(arrayGroup, i, objGroup);
        env->DeleteLocalRef(arrayRes);
    }

    env->SetObjectField(objRsp, jfResourceGroups, arrayGroup);
    env->DeleteLocalRef(arrayGroup);

    //has_more_playlist
    env->SetBooleanField(objRsp, jfHasMorePlaylist,
                         (jboolean) pRsp->has_more_playlist);

    //is_recovery
    env->SetBooleanField(objRsp, jfRecoveryAble, (jboolean) pRsp->is_recovery);
    env->SetBooleanField(objRsp, jfIsNotify, (jboolean) pRsp->is_notify);

    //play_behavior
    env->SetIntField(objRsp, jfPlayBehavior, pRsp->play_behavior);

    bHandled = env->CallBooleanMethod(s_obj_XWeiOuterSkill, onSendResponse, sessionId, objRsp);

    if (objRsp) {
        env->DeleteLocalRef(objRsp);
    }

    if (needRelease) {
        CGlobalJNIEnv::Util_ReleaseEnv();
    }

    return bHandled;
}

JNIEXPORT void JNICALL
Java_com_tencent_xiaowei_control_XWeiOuterSkill_nativeInit(JNIEnv *env, jclass service) {
    s_obj_XWeiOuterSkill = env->NewGlobalRef(service);

    jclass cls_XWeiOuterSkill = env->GetObjectClass(service);
    s_class_XWeiOuterSKill = (jclass)env->NewGlobalRef(cls_XWeiOuterSkill);

    outer_skill_callback.start_outer_skill = start_outer_skill;
    outer_skill_callback.send_txca_response = send_txca_response;
}

JNIEXPORT void JNICALL
Java_com_tencent_xiaowei_control_XWeiOuterSkill_nativeUninit(JNIEnv *env, jclass service) {
    if (s_obj_XWeiOuterSkill) {
        env->DeleteGlobalRef(s_obj_XWeiOuterSkill);
        s_obj_XWeiOuterSkill = NULL;
    }

    if (s_class_XWeiOuterSKill) {
        env->DeleteGlobalRef(s_class_XWeiOuterSKill);
        s_class_XWeiOuterSKill = NULL;
    }
}


#ifdef __cplusplus
}
#endif
