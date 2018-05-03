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
#include "CommonDef.h"
#include "TXCAudio.h"
#include "TXCAudioResource.h"
#include "TXCAudioPrivate.h"
#include "TXCAudioWordsList.h"
#include "TXCAudioCommon.h"
#include "TXCAudioRemind.h"
#include "TXCCMsg.h"

#ifdef __cplusplus
extern "C" {
#endif

extern jobject tx_service;
extern jclass s_serviceClass;
extern jclass s_clsAudioAccount;
extern jclass s_clsAudioAppInfo;
extern jclass s_clsAudioContext;
extern jclass s_clsAudioDeviceInfo;
extern jclass s_clsAudioResource;
extern jclass s_clsAudioResGroup;
extern jclass s_clsAudioResponse;
extern jclass s_clsAudioTTSData;
extern jclass s_clsAudioState;
extern jclass s_clsAIC2CMsg;
extern jclass s_clsAILog;
extern jclass s_clsTXLoginStatusInfo;

/**
 * 网络耗时
 * @param voice_id
 * @param time
 * @return
 */
bool on_net_delay_callback(const char *voice_id, unsigned long long time) {
    if (NULL == tx_service) {
        return false;
    }

    CJNIEnv objEnv;
    if (NULL == objEnv.Env()) {
        return false;
    }

    jboolean bHandled = false;
    jclass cls = s_serviceClass;
    jmethodID methodID = objEnv.Env()->GetMethodID(cls, "onNetworkDelayCallback",
                                                   "(Ljava/lang/String;J)Z");
    if (methodID) {

        jstring strVoiceID;
        ConvChar2JString(objEnv.Env(), voice_id, strVoiceID);
        bHandled = objEnv.Env()->CallBooleanMethod(tx_service, methodID, strVoiceID, (jlong) time);
        objEnv.Env()->DeleteLocalRef(strVoiceID);
    }
    return bHandled;
}

/**
 * 请求结果回来了
 * @param voice_id
 * @param event
 * @param state_info
 * @param extend_info
 * @param extend_info_len
 * @return
 */
bool on_request_callback(const char *voice_id, TXCA_EVENT event, const char *state_info,
                         const char *extend_info, unsigned int extend_info_len) {
    if (NULL == tx_service) {
        __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER, "on_request_callback NULL == tx_service");
        return false;
    }

    CJNIEnv objEnv;
    if (NULL == objEnv.Env()) {
        __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER, "on_request_callback init failed");
        return false;
    }

    jboolean bHandled = false;
    jclass cls = s_serviceClass;

    if (txca_event_on_tts != event) {
        jmethodID methodID = objEnv.Env()->GetMethodID(cls, "onRequestCallback",
                                                       "(Ljava/lang/String;ILcom/tencent/xiaowei/info/XWResponseInfo;[B)Z");

        if (methodID) {
            jobject objRsp = NULL;
            TXCA_PARAM_RESPONSE *pRsp = (TXCA_PARAM_RESPONSE *) state_info;
            if (pRsp) {

                jclass clsAudioResponse = s_clsAudioResponse;
                jclass clsAudioAppInfo = s_clsAudioAppInfo;
                jclass clsAudioContext = s_clsAudioContext;
                jclass clsAudioResource = s_clsAudioResource;
                jclass clsAudioResGroup = s_clsAudioResGroup;

                jfieldID id_appInfo = objEnv.Env()->GetFieldID(clsAudioResponse, "appInfo",
                                                               "Lcom/tencent/xiaowei/info/XWAppInfo;");
                jfieldID id_lastAppInfo = objEnv.Env()->GetFieldID(clsAudioResponse, "lastAppInfo",
                                                               "Lcom/tencent/xiaowei/info/XWAppInfo;");

                jfieldID id_resultCode = objEnv.Env()->GetFieldID(clsAudioResponse, "resultCode",
                                                                  "I");
                jfieldID id_voiceID = objEnv.Env()->GetFieldID(clsAudioResponse, "voiceID",
                                                               "Ljava/lang/String;");
                jfieldID id_context = objEnv.Env()->GetFieldID(clsAudioResponse, "context",
                                                               "Lcom/tencent/xiaowei/info/XWContextInfo;");
                jfieldID id_requestText = objEnv.Env()->GetFieldID(clsAudioResponse, "requestText",
                                                                   "Ljava/lang/String;");
                jfieldID id_responseType = objEnv.Env()->GetFieldID(clsAudioResponse,
                                                                    "responseType", "I");
                jfieldID id_responseData = objEnv.Env()->GetFieldID(clsAudioResponse,
                                                                    "responseData",
                                                                    "Ljava/lang/String;");
                jfieldID id_autoTestData = objEnv.Env()->GetFieldID(clsAudioResponse,
                                                                    "autoTestData",
                                                                    "Ljava/lang/String;");
                jfieldID id_resources = objEnv.Env()->GetFieldID(clsAudioResponse, "resources",
                                                                 "[Lcom/tencent/xiaowei/info/XWResGroupInfo;");
                jfieldID id_hasMorePlaylist = objEnv.Env()->GetFieldID(clsAudioResponse,
                                                                       "hasMorePlaylist", "Z");
                jfieldID id_isRecovery = objEnv.Env()->GetFieldID(clsAudioResponse, "recoveryAble",
                                                                  "Z");
                jfieldID id_playBehavior = objEnv.Env()->GetFieldID(clsAudioResponse,
                                                                    "playBehavior", "I");
                jfieldID id_isNotify = objEnv.Env()->GetFieldID(clsAudioResponse, "isNotify",
                                                                  "Z");

                jfieldID id_wakeupFlag = objEnv.Env()->GetFieldID(clsAudioResponse, "wakeupFlag",
                                                                  "I");

                jfieldID id_skillname = objEnv.Env()->GetFieldID(clsAudioAppInfo, "name",
                                                               "Ljava/lang/String;");
                jfieldID id_skillID = objEnv.Env()->GetFieldID(clsAudioAppInfo, "ID",
                                                             "Ljava/lang/String;");
                jfieldID id_apptype = objEnv.Env()->GetFieldID(clsAudioAppInfo, "type", "I");

                jfieldID id_ctxID = objEnv.Env()->GetFieldID(clsAudioContext, "ID",
                                                             "Ljava/lang/String;");
                jfieldID id_ctxspeakTimeout = objEnv.Env()->GetFieldID(clsAudioContext,
                                                                       "speakTimeout", "I");
                jfieldID id_ctxsilentTimeout = objEnv.Env()->GetFieldID(clsAudioContext,
                                                                        "silentTimeout", "I");
                jfieldID id_ctxvoiceReqBegin = objEnv.Env()->GetFieldID(clsAudioContext,
                                                                        "voiceRequestBegin", "Z");
                jfieldID id_ctxvoiceReqEnd = objEnv.Env()->GetFieldID(clsAudioContext,
                                                                      "voiceRequestEnd", "Z");

                jfieldID id_ctxprofileType = objEnv.Env()->GetFieldID(clsAudioContext,
                                                                      "profileType", "I");

                jfieldID id_ctxvoiceWakeupType = objEnv.Env()->GetFieldID(clsAudioContext, "voiceWakeupType", "I");
                jfieldID id_voiceWakeupText = objEnv.Env()->GetFieldID(clsAudioContext, "voiceWakeupText", "Ljava/lang/String;");

                jfieldID id_resformat = objEnv.Env()->GetFieldID(clsAudioResource, "format", "I");
                jfieldID id_resOffset = objEnv.Env()->GetFieldID(clsAudioResource, "offset", "I");
                jfieldID id_resPlayCount = objEnv.Env()->GetFieldID(clsAudioResource, "playCount", "I");
                jfieldID id_resID = objEnv.Env()->GetFieldID(clsAudioResource, "ID",
                                                             "Ljava/lang/String;");
                jfieldID id_rescontent = objEnv.Env()->GetFieldID(clsAudioResource, "content",
                                                                  "Ljava/lang/String;");
                jfieldID id_resextendInfo = objEnv.Env()->GetFieldID(clsAudioResource, "extendInfo",
                                                                     "Ljava/lang/String;");

                jfieldID id_groupResources = objEnv.Env()->GetFieldID(clsAudioResGroup, "resources", "[Lcom/tencent/xiaowei/info/XWResourceInfo;");

                //creat obj
                jmethodID init = objEnv.Env()->GetMethodID(clsAudioResponse, "<init>", "()V");
                objRsp = objEnv.Env()->NewObject(clsAudioResponse, init);

                //AppInfo
                jmethodID initAppInfo = objEnv.Env()->GetMethodID(clsAudioAppInfo, "<init>", "()V");
                jobject objApp = objEnv.Env()->NewObject(clsAudioAppInfo, initAppInfo);

                jobject objLastApp = objEnv.Env()->NewObject(clsAudioAppInfo, initAppInfo);

                jstring strSkillName;
                ConvChar2JString(objEnv.Env(), pRsp->skill_info.name, strSkillName);
                jstring strSkillID;
                ConvChar2JString(objEnv.Env(), pRsp->skill_info.id, strSkillID);

                objEnv.Env()->SetObjectField(objApp, id_skillname, strSkillName);
                objEnv.Env()->SetObjectField(objApp, id_skillID, strSkillID);
                objEnv.Env()->SetIntField(objApp, id_apptype, pRsp->skill_info.type);

                jstring strSkillName2;
                ConvChar2JString(objEnv.Env(), pRsp->last_skill_info.name, strSkillName2);
                jstring strSkillID2;
                ConvChar2JString(objEnv.Env(), pRsp->last_skill_info.id, strSkillID2);

                objEnv.Env()->SetObjectField(objLastApp, id_skillname, strSkillName2);
                objEnv.Env()->SetObjectField(objLastApp, id_skillID, strSkillID2);
                objEnv.Env()->SetIntField(objLastApp, id_apptype, pRsp->last_skill_info.type);

                objEnv.Env()->SetObjectField(objRsp, id_appInfo, objApp);
                objEnv.Env()->SetObjectField(objRsp, id_lastAppInfo, objLastApp);

                objEnv.Env()->DeleteLocalRef(strSkillName);
                objEnv.Env()->DeleteLocalRef(strSkillID);
                objEnv.Env()->DeleteLocalRef(objApp);
                objEnv.Env()->DeleteLocalRef(strSkillName2);
                objEnv.Env()->DeleteLocalRef(strSkillID2);
                objEnv.Env()->DeleteLocalRef(objLastApp);

                //result code
                objEnv.Env()->SetIntField(objRsp, id_resultCode, pRsp->error_code);

                //voice id
                jstring strVoiceID;
                ConvChar2JString(objEnv.Env(), pRsp->voice_id, strVoiceID);
                objEnv.Env()->SetObjectField(objRsp, id_voiceID, strVoiceID);
                objEnv.Env()->DeleteLocalRef(strVoiceID);

                //ContextInfo
                jmethodID initContextInfo = objEnv.Env()->GetMethodID(clsAudioContext, "<init>",
                                                                      "()V");
                jobject objContext = objEnv.Env()->NewObject(clsAudioContext, initContextInfo);

                jstring strCtxID;
                ConvChar2JString(objEnv.Env(), pRsp->context.id, strCtxID);

                objEnv.Env()->SetObjectField(objContext, id_ctxID, strCtxID);
                objEnv.Env()->SetIntField(objContext, id_ctxspeakTimeout,
                                          (jint) pRsp->context.speak_timeout);
                objEnv.Env()->SetIntField(objContext, id_ctxsilentTimeout,
                                          (jint) pRsp->context.silent_timeout);
                objEnv.Env()->SetBooleanField(objContext, id_ctxvoiceReqBegin,
                                              (jboolean) pRsp->context.voice_request_begin);
                objEnv.Env()->SetBooleanField(objContext, id_ctxvoiceReqEnd,
                                              (jboolean) pRsp->context.voice_request_end);
                objEnv.Env()->SetIntField(objContext, id_ctxprofileType,
                                          (jint) pRsp->context.wakeup_profile);
                objEnv.Env()->SetIntField(objContext, id_ctxvoiceWakeupType,
                                          (jint) pRsp->context.wakeup_type);

                jstring strWakeupText;
                ConvChar2JString(objEnv.Env(), pRsp->context.wakeup_word, strWakeupText);
                objEnv.Env()->SetObjectField(objContext, id_voiceWakeupText, strWakeupText);
                objEnv.Env()->DeleteLocalRef(strWakeupText);

                objEnv.Env()->SetObjectField(objRsp, id_context, objContext);
                objEnv.Env()->DeleteLocalRef(strCtxID);
                objEnv.Env()->DeleteLocalRef(objContext);

                //request_text
                jstring strRequestText;
                ConvChar2JString(objEnv.Env(), pRsp->request_text, strRequestText);
                objEnv.Env()->SetObjectField(objRsp, id_requestText, strRequestText);
                objEnv.Env()->DeleteLocalRef(strRequestText);

                //response_type
                objEnv.Env()->SetIntField(objRsp, id_responseType, pRsp->response_type);

                //response_data
                jstring strRspExtend;
                ConvChar2JString(objEnv.Env(), pRsp->response_data, strRspExtend);
                objEnv.Env()->SetObjectField(objRsp, id_responseData, strRspExtend);
                objEnv.Env()->DeleteLocalRef(strRspExtend);

                //auto_test_data，无需关注，一般都是空值
                jstring strTestExtend;
                ConvChar2JString(objEnv.Env(), pRsp->auto_test_data, strTestExtend);
                objEnv.Env()->SetObjectField(objRsp, id_autoTestData, strTestExtend);
                objEnv.Env()->DeleteLocalRef(strTestExtend);

                //resources
                jmethodID initResGroup = objEnv.Env()->GetMethodID(clsAudioResGroup, "<init>", "()V");

                jmethodID initResource = objEnv.Env()->GetMethodID(clsAudioResource, "<init>",
                                                                   "()V");
                jobjectArray arrayGroup = objEnv.Env()->NewObjectArray(pRsp->resource_groups_size,
                                                                       clsAudioResGroup, NULL);
                for (int i = 0; i < pRsp->resource_groups_size; i++) {
                    jobject objGroup = objEnv.Env()->NewObject(clsAudioResGroup, initResGroup);
                    jobjectArray arrayRes = objEnv.Env()->NewObjectArray(pRsp->resource_groups[i].resources_size,
                                                                         clsAudioResource, NULL);
                    for (int j = 0; j < pRsp->resource_groups[i].resources_size; ++j) {
                        jobject objRes = objEnv.Env()->NewObject(clsAudioResource, initResource);

                        jstring strResID;
                        ConvChar2JString(objEnv.Env(), pRsp->resource_groups[i].resources[j].id, strResID);
                        jstring strResContent;
                        ConvChar2JString(objEnv.Env(), pRsp->resource_groups[i].resources[j].content, strResContent);
                        jstring strResExtend;
                        ConvChar2JString(objEnv.Env(), pRsp->resource_groups[i].resources[j].extend_buffer, strResExtend);


                        objEnv.Env()->SetIntField(objRes, id_resformat, pRsp->resource_groups[i].resources[j].format);
                        objEnv.Env()->SetIntField(objRes, id_resOffset, pRsp->resource_groups[i].resources[j].offset);
                        objEnv.Env()->SetIntField(objRes, id_resPlayCount, pRsp->resource_groups[i].resources[j].play_count);
                        objEnv.Env()->SetObjectField(objRes, id_resID, strResID);
                        objEnv.Env()->SetObjectField(objRes, id_rescontent, strResContent);
                        objEnv.Env()->SetObjectField(objRes, id_resextendInfo, strResExtend);

                        objEnv.Env()->SetObjectArrayElement(arrayRes, j, objRes);

                        objEnv.Env()->DeleteLocalRef(strResID);
                        objEnv.Env()->DeleteLocalRef(strResContent);
                        objEnv.Env()->DeleteLocalRef(strResExtend);
                        objEnv.Env()->DeleteLocalRef(objRes);
                    }
                    objEnv.Env()->SetObjectField(objGroup, id_groupResources, arrayRes);
                    objEnv.Env()->SetObjectArrayElement(arrayGroup, i, objGroup);
                    objEnv.Env()->DeleteLocalRef(arrayRes);
                }

                objEnv.Env()->SetObjectField(objRsp, id_resources, arrayGroup);
                objEnv.Env()->DeleteLocalRef(arrayGroup);

                //has_more_playlist
                objEnv.Env()->SetBooleanField(objRsp, id_hasMorePlaylist,
                                              (jboolean) pRsp->has_more_playlist);

                //is_recovery
                objEnv.Env()->SetBooleanField(objRsp, id_isRecovery, (jboolean) pRsp->is_recovery);
                objEnv.Env()->SetBooleanField(objRsp, id_isNotify, (jboolean) pRsp->is_notify);
                objEnv.Env()->SetIntField(objRsp, id_wakeupFlag, (jint) pRsp->wakeup_flag);

                //play_behavior
                objEnv.Env()->SetIntField(objRsp, id_playBehavior, pRsp->play_behavior);
            }
            jbyteArray jextend = NULL;
            if (extend_info_len > 0) {
                jextend = objEnv.Env()->NewByteArray(extend_info_len);
                objEnv.Env()->SetByteArrayRegion(jextend, 0, extend_info_len,
                                                 (jbyte *) extend_info);
            }

            jstring strVoiceID;
            ConvChar2JString(objEnv.Env(), voice_id, strVoiceID);
            bHandled = objEnv.Env()->CallBooleanMethod(tx_service, methodID, strVoiceID, event,
                                                       objRsp, jextend);

            if (objRsp) {
                objEnv.Env()->DeleteLocalRef(objRsp);
            }
            if (strVoiceID) {
                objEnv.Env()->DeleteLocalRef(strVoiceID);
            }
            if (jextend) {
                objEnv.Env()->DeleteLocalRef(jextend);
            }
        }
    } else if (txca_event_on_tts == event) {
        jmethodID methodID = objEnv.Env()->GetMethodID(cls, "onTTSPushCallback",
                                                       "(Ljava/lang/String;Lcom/tencent/xiaowei/info/XWTTSDataInfo;)Z");
        if (methodID) {
            jobject objRsp = NULL;
            TXCA_PARAM_AUDIO_DATA *pRsp = (TXCA_PARAM_AUDIO_DATA *) state_info;
            if (pRsp) {
                jclass clsAudioTTSData = s_clsAudioTTSData;

                jmethodID init = objEnv.Env()->GetMethodID(clsAudioTTSData, "<init>", "()V");

                jfieldID id_resID = objEnv.Env()->GetFieldID(clsAudioTTSData, "resID",
                                                             "Ljava/lang/String;");
                jfieldID id_seq = objEnv.Env()->GetFieldID(clsAudioTTSData, "seq", "I");
                jfieldID id_isEnd = objEnv.Env()->GetFieldID(clsAudioTTSData, "isEnd", "Z");
                jfieldID id_pcmSampleRate = objEnv.Env()->GetFieldID(clsAudioTTSData,
                                                                     "pcmSampleRate", "I");
                jfieldID id_sampleRate = objEnv.Env()->GetFieldID(clsAudioTTSData, "sampleRate",
                                                                  "I");
                jfieldID id_channel = objEnv.Env()->GetFieldID(clsAudioTTSData, "channel", "I");
                jfieldID id_format = objEnv.Env()->GetFieldID(clsAudioTTSData, "format", "I");
                jfieldID id_data = objEnv.Env()->GetFieldID(clsAudioTTSData, "data", "[B");


                objRsp = objEnv.Env()->NewObject(clsAudioTTSData, init);


                jstring strResID;
                ConvChar2JString(objEnv.Env(), pRsp->id, strResID);
                objEnv.Env()->SetObjectField(objRsp, id_resID, strResID);
                if (strResID) {
                    objEnv.Env()->DeleteLocalRef(strResID);
                }

                objEnv.Env()->SetIntField(objRsp, id_seq, pRsp->seq);
                objEnv.Env()->SetBooleanField(objRsp, id_isEnd, pRsp->is_end == 1);
                objEnv.Env()->SetIntField(objRsp, id_pcmSampleRate, pRsp->pcm_sample_rate);
                objEnv.Env()->SetIntField(objRsp, id_sampleRate, pRsp->sample_rate);
                objEnv.Env()->SetIntField(objRsp, id_channel, pRsp->channel);
                objEnv.Env()->SetIntField(objRsp, id_format, (int) pRsp->format);

                jbyteArray jdata = NULL;
                if (pRsp->raw_data_len > 0) {
                    jdata = objEnv.Env()->NewByteArray(pRsp->raw_data_len);
                    objEnv.Env()->SetByteArrayRegion(jdata, 0, pRsp->raw_data_len,
                                                     (jbyte *) pRsp->raw_data);
                }

                objEnv.Env()->SetObjectField(objRsp, id_data, jdata);

                if (jdata) {
                    objEnv.Env()->DeleteLocalRef(jdata);
                }
            }
            jstring strVoiceID;
            ConvChar2JString(objEnv.Env(), voice_id, strVoiceID);
            bHandled = objEnv.Env()->CallBooleanMethod(tx_service, methodID, strVoiceID, objRsp);

            if (objRsp) {
                objEnv.Env()->DeleteLocalRef(objRsp);
            }
            if (strVoiceID) {
                objEnv.Env()->DeleteLocalRef(strVoiceID);
            }
        }
    }

    return bHandled;
}

/**
 * 开启服务
 * @param env
 * @param device
 * @param accountData
 * @return
 */
JNIEXPORT jint JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_startXiaoweiService(JNIEnv *env, jclass,
                                                      jobject accountData) {
    if (NULL == s_clsAudioDeviceInfo
        || NULL == s_clsAudioAccount) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER,
                            "startXiaoweiService NULL == s_clsAudioDeviceInfo or NULL == s_clsAudioAccount");
        return 0;
    }

    TXCA_CALLBACK callback = {0};
    callback.on_request_callback = on_request_callback;
    callback.on_net_delay_callback = on_net_delay_callback;


    jstring account;
    jstring token;
    jstring appid;
    TXCA_PARAM_ACCOUNT stAccount = {0};
    if (accountData != NULL) {
        jclass clsAudioAccount = s_clsAudioAccount;
        jfieldID id_type = env->GetFieldID(clsAudioAccount, "type", "I");
        jfieldID id_account = env->GetFieldID(clsAudioAccount, "account", "Ljava/lang/String;");
        jfieldID id_token = env->GetFieldID(clsAudioAccount, "token", "Ljava/lang/String;");
        jfieldID id_appid = env->GetFieldID(clsAudioAccount, "appid", "Ljava/lang/String;");
        jfieldID id_extendBuffer = env->GetFieldID(clsAudioAccount, "extendBuffer", "[B");

        jint type = (jint) env->GetIntField(accountData, id_type);
        account = (jstring) env->GetObjectField(accountData, id_account);
        token = (jstring) env->GetObjectField(accountData, id_token);
        appid = (jstring) env->GetObjectField(accountData, id_appid);
        jbyteArray extendBuffer = (jbyteArray) env->GetObjectField(accountData, id_extendBuffer);

        stAccount.type = type;
        if (account) {
            stAccount.account = env->GetStringUTFChars(account, 0);
        }
        if (token) {
            stAccount.token = env->GetStringUTFChars(token, 0);
        }
        if (appid) {
            stAccount.appid = env->GetStringUTFChars(appid, 0);
        }
        if (extendBuffer) {
            stAccount.buffer_len = env->GetArrayLength(extendBuffer);
            if (stAccount.buffer_len > 0) {
                jbyte *buf = env->GetByteArrayElements(extendBuffer, JNI_FALSE);
                stAccount.buffer = new char[stAccount.buffer_len + 1];
                memset(stAccount.buffer, 0, (unsigned int) stAccount.buffer_len + 1);
                memcpy(stAccount.buffer, buf, (unsigned int) stAccount.buffer_len);
                env->ReleaseByteArrayElements(extendBuffer, buf, 0);
            }
        }
    }

    int nRet = txca_service_start(&callback, &stAccount);

    if (stAccount.account) {
        env->ReleaseStringUTFChars(account, stAccount.account);
    }
    if (stAccount.token) {
        env->ReleaseStringUTFChars(token, stAccount.token);
    }
    if (stAccount.appid) {
        env->ReleaseStringUTFChars(appid, stAccount.appid);
    }
    if (stAccount.buffer_len > 0 && stAccount.buffer) {
        delete[] stAccount.buffer;
        stAccount.buffer = NULL;
    }

    return nRet;
}

/**
 * 停止服务
 * @param env
 * @return
 */
JNIEXPORT jint JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_stopXiaoweiService(JNIEnv *env, jclass) {
    int nRet = txca_service_stop();
    return nRet;
}

/**
 * 请求
 * @param env
 * @param type
 * @param requestData
 * @param context
 * @return
 */
JNIEXPORT jstring JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_request(JNIEnv *env, jclass, jint type,
                                                 jbyteArray requestData, jobject context) {
    if (NULL == s_clsAudioContext) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER, "request NULL == s_clsAudioContext");
        return 0;
    }

    jclass clsAudioContext = s_clsAudioContext;
    jfieldID id_ID = env->GetFieldID(clsAudioContext, "ID", "Ljava/lang/String;");
    jfieldID id_speakTimeout = env->GetFieldID(clsAudioContext, "speakTimeout", "I");
    jfieldID id_silentTimeout = env->GetFieldID(clsAudioContext, "silentTimeout", "I");
    jfieldID id_voiceRequestBegin = env->GetFieldID(clsAudioContext, "voiceRequestBegin", "Z");
    jfieldID id_voiceRequestEnd = env->GetFieldID(clsAudioContext, "voiceRequestEnd", "Z");
    jfieldID id_ctxprofileType = env->GetFieldID(clsAudioContext, "profileType", "I");
    jfieldID id_ctxvoiceWakeupType = env->GetFieldID(clsAudioContext, "voiceWakeupType", "I");
    jfieldID id_voiceWakeupText = env->GetFieldID(clsAudioContext, "voiceWakeupText", "Ljava/lang/String;");
    jfieldID id_requestParam = env->GetFieldID(clsAudioContext, "requestParam", "J");


    jstring ID = (jstring) env->GetObjectField(context, id_ID);
    jint speakTimeout = (jint) env->GetIntField(context, id_speakTimeout);
    jint silentTimeout = (jint) env->GetIntField(context, id_silentTimeout);
    jboolean voiceRequestBegin = (jboolean) env->GetBooleanField(context, id_voiceRequestBegin);
    jboolean voiceRequestEnd = (jboolean) env->GetBooleanField(context, id_voiceRequestEnd);
    jint profileType = (jint) env->GetIntField(context, id_ctxprofileType);
    jint voiceWakeupType = (jint) env->GetIntField(context, id_ctxvoiceWakeupType);
    jstring wakeupText = (jstring) env->GetObjectField(context, id_voiceWakeupText);
    jlong requestParam = (jlong) env->GetLongField(context, id_requestParam);
    TXCA_PARAM_CONTEXT stContext = {0};
    if (NULL != ID) {
        stContext.id = env->GetStringUTFChars(ID, 0);
    }
    stContext.speak_timeout = speakTimeout;
    stContext.silent_timeout = silentTimeout;
    stContext.voice_request_begin = voiceRequestBegin;
    stContext.voice_request_end = voiceRequestEnd;
    stContext.wakeup_profile = TXCA_WAKEUP_PROFILE(profileType);
    stContext.wakeup_type = TXCA_WAKEUP_TYPE(voiceWakeupType);
    stContext.request_param = requestParam;

    if(wakeupText) {
        stContext.wakeup_word = env->GetStringUTFChars(wakeupText, 0);
    }

    char *chat_data = NULL;
    unsigned int char_data_len = (NULL == requestData) ? 0 : env->GetArrayLength(requestData);
    if (char_data_len > 0) {
        jbyte *buf = env->GetByteArrayElements(requestData, JNI_FALSE);
        chat_data = new char[char_data_len];
        memset(chat_data, 0, (unsigned int) char_data_len);
        memcpy(chat_data, buf, (unsigned int) char_data_len);
        env->ReleaseByteArrayElements(requestData, buf, 0);
    }

    char pVoiceID[33];
    memset(pVoiceID, 0, 33);
    int nRet = txca_request(pVoiceID, (TXCA_CHAT_TYPE) type, chat_data, char_data_len, &stContext);

    if (char_data_len > 0 && chat_data) {
        delete[] chat_data;
        chat_data = NULL;
    }
    if (stContext.id) {
        env->ReleaseStringUTFChars(ID, stContext.id);
    }
    if (stContext.wakeup_word) {
        env->ReleaseStringUTFChars(wakeupText, stContext.wakeup_word);
    }

    jstring strVoiceID;
    ConvChar2JString(env, pVoiceID, strVoiceID);
    return strVoiceID;
}

/**
 * 取消请求
 * @param env
 * @param voiceID
 * @return
 */
JNIEXPORT jint JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_cancelRequest(JNIEnv *env, jclass, jstring voiceID) {
    const char *pstrVoiceID = (NULL == voiceID) ? NULL : env->GetStringUTFChars(voiceID, 0);

    int nRet = txca_request_cancel(pstrVoiceID);

    if (pstrVoiceID) {
        env->ReleaseStringUTFChars(voiceID, pstrVoiceID);
    }

    return nRet;
}

/**
 * 取消TTS
 * @param env
 * @param voiceID
 * @return
 */
JNIEXPORT jint JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_cancelTTS(JNIEnv *env, jclass, jstring voiceID) {
    const char *pstrVoiceID = (NULL == voiceID) ? NULL : env->GetStringUTFChars(voiceID, 0);

    int nRet = txca_tts_cancel(pstrVoiceID);

    if (pstrVoiceID) {
        env->ReleaseStringUTFChars(voiceID, pstrVoiceID);
    }

    return nRet;
}

/**
 * 上报播放状态
 * @param env
 * @param state
 * @return
 */
JNIEXPORT jint JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_reportPlayState(JNIEnv *env, jclass, jobject state) {
    if (NULL == s_clsAudioState || NULL == s_clsAudioAppInfo) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER,
                            "reportPlayState NULL == s_clsAudioState || NULL == s_clsAudioAppInfo");
        return 0;
    }

    if (NULL == state) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER, "reportPlayState NULL == state");
        return 0;
    }

    jclass clsAudioState = s_clsAudioState;
    jfieldID id_appInfo = env->GetFieldID(clsAudioState, "appInfo",
                                          "Lcom/tencent/xiaowei/info/XWAppInfo;");
    jfieldID id_state = env->GetFieldID(clsAudioState, "state", "I");
    jfieldID id_playID = env->GetFieldID(clsAudioState, "playID", "Ljava/lang/String;");
    jfieldID id_playContent = env->GetFieldID(clsAudioState, "playContent", "Ljava/lang/String;");
    jfieldID id_playOffset = env->GetFieldID(clsAudioState, "playOffset", "J");
    jfieldID id_playMode = env->GetFieldID(clsAudioState, "playMode", "I");

    jint nState = (jint) env->GetIntField(state, id_state);
    jstring playID = (jstring) env->GetObjectField(state, id_playID);
    jstring playContent = (jstring) env->GetObjectField(state, id_playContent);
    jlong playOffset = (jlong) env->GetLongField(state, id_playOffset);
    jint playMode = (jint) env->GetIntField(state, id_playMode);

    jclass clsAudioAppInfo = s_clsAudioAppInfo;
    jfieldID id_skillName = env->GetFieldID(clsAudioAppInfo, "name", "Ljava/lang/String;");
    jfieldID id_skillID = env->GetFieldID(clsAudioAppInfo, "ID", "Ljava/lang/String;");
    jfieldID id_appType = env->GetFieldID(clsAudioAppInfo, "type", "I");

    jobject appInfo = (jobject) env->GetObjectField(state, id_appInfo);
    jstring skillName = NULL;
    jstring skillID = NULL;
    jint appType = 0;
    if (appInfo) {
        skillName = (jstring) env->GetObjectField(appInfo, id_skillName);
        skillID = (jstring) env->GetObjectField(appInfo, id_skillID);
        appType = (jint) env->GetIntField(appInfo, id_appType);
    }

    const char *pVal_skillName = NULL;
    if (skillName) {
        pVal_skillName = env->GetStringUTFChars(skillName, 0);
    }
    const char *pVal_skillID = NULL;
    if (skillID) {
        pVal_skillID = env->GetStringUTFChars(skillID, 0);
    }
    const char *pVal_playID = NULL;
    if (playID) {
        pVal_playID = env->GetStringUTFChars(playID, 0);
    }
    const char *pVal_playContent = NULL;
    if (playContent) {
        pVal_playContent = env->GetStringUTFChars(playContent, 0);
    }

    TXCA_PARAM_STATE st = {0};
    st.skill_info.name = pVal_skillName;
    st.skill_info.id = pVal_skillID;
    st.skill_info.type = appType;
    st.play_state = nState;
    st.play_id = pVal_playID;
    st.play_content = pVal_playContent;
    st.play_offset = playOffset;
    st.play_mode = playMode;

    int nRet = txca_report_state(&st);

    if (pVal_skillName) {
        env->ReleaseStringUTFChars(skillName, pVal_skillName);
    }
    if (pVal_skillID) {
        env->ReleaseStringUTFChars(skillID, pVal_skillID);
    }
    if (pVal_playID) {
        env->ReleaseStringUTFChars(playID, pVal_playID);
    }
    if (pVal_playContent) {
        env->ReleaseStringUTFChars(playContent, pVal_playContent);
    }

    return nRet;
}

/**
 * 设置自定义设备状态
 * @param env
 * @param state
 * @return
 */
JNIEXPORT jint JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_setUserState(JNIEnv *env, jclass, jobject state) {
    if (NULL == s_clsAudioState || NULL == s_clsAudioAppInfo) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER,
                            "setUserState NULL == s_clsAudioState || NULL == s_clsAudioAppInfo");
        return err_failed;
    }

    if (NULL == state) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER, "setUserState NULL == state");
        return err_invalid_param;
    }

    jclass clsAudioState = s_clsAudioState;
    jfieldID id_appInfo = env->GetFieldID(clsAudioState, "appInfo",
                                          "Lcom/tencent/xiaowei/info/XWAppInfo;");
    jfieldID id_state = env->GetFieldID(clsAudioState, "state", "I");
    jfieldID id_playID = env->GetFieldID(clsAudioState, "playID", "Ljava/lang/String;");
    jfieldID id_playContent = env->GetFieldID(clsAudioState, "playContent", "Ljava/lang/String;");
    jfieldID id_playOffset = env->GetFieldID(clsAudioState, "playOffset", "J");
    jfieldID id_playMode = env->GetFieldID(clsAudioState, "playMode", "I");

    jint nState = (jint) env->GetIntField(state, id_state);
    jstring playID = (jstring) env->GetObjectField(state, id_playID);
    jstring playContent = (jstring) env->GetObjectField(state, id_playContent);
    jlong playOffset = (jlong) env->GetLongField(state, id_playOffset);
    jint playMode = (jint) env->GetIntField(state, id_playMode);

    jclass clsAudioAppInfo = s_clsAudioAppInfo;
    jfieldID id_skillName = env->GetFieldID(clsAudioAppInfo, "name", "Ljava/lang/String;");
    jfieldID id_skillID = env->GetFieldID(clsAudioAppInfo, "ID", "Ljava/lang/String;");
    jfieldID id_appType = env->GetFieldID(clsAudioAppInfo, "type", "I");

    jobject appInfo = (jobject) env->GetObjectField(state, id_appInfo);
    jstring skillName = NULL;
    jstring skillID = NULL;
    jint appType = 0;
    if (appInfo) {
        skillName = (jstring) env->GetObjectField(appInfo, id_skillName);
        skillID = (jstring) env->GetObjectField(appInfo, id_skillID);
        appType = (jint) env->GetIntField(appInfo, id_appType);
    }

    const char *pVal_skillName = NULL;
    if (skillName) {
        pVal_skillName = env->GetStringUTFChars(skillName, 0);
    }
    const char *pVal_skillID = NULL;
    if (skillID) {
        pVal_skillID = env->GetStringUTFChars(skillID, 0);
    }
    const char *pVal_playID = NULL;
    if (playID) {
        pVal_playID = env->GetStringUTFChars(playID, 0);
    }
    const char *pVal_playContent = NULL;
    if (playContent) {
        pVal_playContent = env->GetStringUTFChars(playContent, 0);
    }

    TXCA_PARAM_STATE st = {0};
    st.skill_info.name = pVal_skillName;
    st.skill_info.id = pVal_skillID;
    st.skill_info.type = appType;
    st.play_state = nState;
    st.play_id = pVal_playID;
    st.play_content = pVal_playContent;
    st.play_offset = playOffset;
    st.play_mode = playMode;

    int nRet = txca_set_user_state(&st);

    if (pVal_skillName) {
        env->ReleaseStringUTFChars(skillName, pVal_skillName);
    }
    if (pVal_skillID) {
        env->ReleaseStringUTFChars(skillID, pVal_skillID);
    }
    if (pVal_playID) {
        env->ReleaseStringUTFChars(playID, pVal_playID);
    }
    if (pVal_playContent) {
        env->ReleaseStringUTFChars(playContent, pVal_playContent);
    }

    return nRet;
}

/**
 * 清除自定义设备状态

 * @param env
 * @param state
 * @return
 */
JNIEXPORT jint JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_clearUserState(JNIEnv *env, jclass) {
    return txca_clear_user_state();
}

/**
 * 查询更多播放列表
 * @param env
 * @param appInfo
 * @param playID
 * @param maxListSize
 * @param isUp
 * @return
 */
JNIEXPORT jstring JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_getMorePlaylist(JNIEnv *env, jclass, jobject appInfo,
                                                     jstring playID, jint maxListSize,
                                                     jboolean isUp) {
    if (NULL == s_clsAudioAppInfo) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER,
                            "getMorePlaylist NULL == s_clsAudioAppInfo");
        return 0;
    }

    if (NULL == appInfo) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER, "getMorePlaylist NULL == appInfo");
        return 0;
    }

    jclass clsAudioAppInfo = s_clsAudioAppInfo;
    jfieldID id_skillName = env->GetFieldID(clsAudioAppInfo, "name", "Ljava/lang/String;");
    jfieldID id_skillID = env->GetFieldID(clsAudioAppInfo, "ID", "Ljava/lang/String;");
    jfieldID id_appType = env->GetFieldID(clsAudioAppInfo, "type", "I");

    jstring skillName = (jstring) env->GetObjectField(appInfo, id_skillName);
    jstring skillID = (jstring) env->GetObjectField(appInfo, id_skillID);
    jint appType = (jint) env->GetIntField(appInfo, id_appType);

    const char *pVal_skillName = NULL;
    if (skillName) {
        pVal_skillName = env->GetStringUTFChars(skillName, 0);
    }
    const char *pVal_skillID = NULL;
    if (skillID) {
        pVal_skillID = env->GetStringUTFChars(skillID, 0);
    }

    TXCA_PARAM_SKILL st = {0};
    st.name = pVal_skillName;
    st.id = pVal_skillID;
    st.type = appType;

    const char *pPlayID = (NULL == playID) ? NULL : env->GetStringUTFChars(playID, 0);

    char pVoiceID[33];
    memset(pVoiceID, 0, 33);
    int nRet = txca_resource_get_list(pVoiceID, &st, pPlayID, (unsigned int) maxListSize,
                                      (bool) isUp);

    if (pVal_skillName) {
        env->ReleaseStringUTFChars(skillName, pVal_skillName);
    }
    if (pVal_skillID) {
        env->ReleaseStringUTFChars(skillID, pVal_skillID);
    }
    if (pPlayID) {
        env->ReleaseStringUTFChars(playID, pPlayID);
    }

    jstring strVoiceID;
    ConvChar2JString(env, pVoiceID, strVoiceID);
    return strVoiceID;
}
/**
 * 查询播放详情
 * @param env
 * @param appInfo
 * @param listPlayID
 * @return
 */
JNIEXPORT jstring JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_getPlayDetailInfo(JNIEnv *env, jclass, jobject appInfo,
                                                           jobjectArray listPlayID) {
    if (NULL == s_clsAudioAppInfo) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER,
                            "getPlayDetailInfo NULL == s_clsAudioAppInfo");
        return 0;
    }

    if (NULL == appInfo) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER, "getPlayDetailInfo NULL == appInfo");
        return 0;
    }

    jclass clsAudioAppInfo = s_clsAudioAppInfo;
    jfieldID id_skillName = env->GetFieldID(clsAudioAppInfo, "name", "Ljava/lang/String;");
    jfieldID id_skillID = env->GetFieldID(clsAudioAppInfo, "ID", "Ljava/lang/String;");
    jfieldID id_appType = env->GetFieldID(clsAudioAppInfo, "type", "I");

    jstring skillName = (jstring) env->GetObjectField(appInfo, id_skillName);
    jstring skillID = (jstring) env->GetObjectField(appInfo, id_skillID);
    jint appType = (jint) env->GetIntField(appInfo, id_appType);

    const char *pVal_skillName = NULL;
    if (skillName) {
        pVal_skillName = env->GetStringUTFChars(skillName, 0);
    }
    const char *pVal_skillID = NULL;
    if (skillID) {
        pVal_skillID = env->GetStringUTFChars(skillID, 0);
    }

    TXCA_PARAM_SKILL st = {0};
    st.name = pVal_skillName;
    st.id = pVal_skillID;
    st.type = appType;

    char **list_play_id = NULL;
    int list_size = (NULL == listPlayID) ? 0 : env->GetArrayLength(listPlayID);
    if (list_size > 0) {
        list_play_id = (char **) malloc(sizeof(char *) * list_size);
        memset(list_play_id, 0, (unsigned int) list_size * sizeof(char *));
        for (int i = 0; i < list_size; ++i) {
            jstring playidstr = (jstring) env->GetObjectArrayElement(listPlayID, i);
            if (playidstr != NULL) {
                list_play_id[i] = (char *) env->GetStringUTFChars(playidstr, 0);
            }
        }
    }

    char pVoiceID[33];
    memset(pVoiceID, 0, 33);
    int nRet = txca_resource_get_detail_info(pVoiceID, &st, list_play_id, list_size);

    if (pVal_skillName) {
        env->ReleaseStringUTFChars(skillName, pVal_skillName);
    }
    if (pVal_skillID) {
        env->ReleaseStringUTFChars(skillID, pVal_skillID);
    }

    if (list_size > 0 && list_play_id) {
        for (int i = 0; i < list_size; ++i) {
            jstring playidstr = (jstring) env->GetObjectArrayElement(listPlayID, i);
            env->ReleaseStringUTFChars(playidstr, list_play_id[i]);
        }
        free(list_play_id);
    }

    jstring strVoiceID;
    ConvChar2JString(env, pVoiceID, strVoiceID);
    return strVoiceID;
}

/**
 * 刷新列表
 * @param env
 * @param appInfo
 * @param listPlayID
 * @return
 */
JNIEXPORT jstring JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_refreshPlayList(JNIEnv *env, jclass, jobject appInfo,
                                                         jobjectArray listPlayID) {
    if (NULL == s_clsAudioAppInfo) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER,
                            "refreshPlayList NULL == s_clsAudioAppInfo");
        return 0;
    }

    if (NULL == appInfo) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER, "refreshPlayList NULL == appInfo");
        return 0;
    }

    jclass clsAudioAppInfo = s_clsAudioAppInfo;
    jfieldID id_skillName = env->GetFieldID(clsAudioAppInfo, "name", "Ljava/lang/String;");
    jfieldID id_skillID = env->GetFieldID(clsAudioAppInfo, "ID", "Ljava/lang/String;");
    jfieldID id_appType = env->GetFieldID(clsAudioAppInfo, "type", "I");

    jstring skillName = (jstring) env->GetObjectField(appInfo, id_skillName);
    jstring skillID = (jstring) env->GetObjectField(appInfo, id_skillID);
    jint appType = (jint) env->GetIntField(appInfo, id_appType);

    const char *pVal_skillName = NULL;
    if (skillName) {
        pVal_skillName = env->GetStringUTFChars(skillName, 0);
    }
    const char *pVal_skillID = NULL;
    if (skillID) {
        pVal_skillID = env->GetStringUTFChars(skillID, 0);
    }

    TXCA_PARAM_SKILL st = {0};
    st.name = pVal_skillName;
    st.id = pVal_skillID;
    st.type = appType;

    char **list_play_id = NULL;
    int list_size = (NULL == listPlayID) ? 0 : env->GetArrayLength(listPlayID);
    if (list_size > 0) {
        list_play_id = (char **) malloc(sizeof(char *) * list_size);
        memset(list_play_id, 0, (unsigned int) list_size * sizeof(char *));
        for (int i = 0; i < list_size; ++i) {
            jstring playidstr = (jstring) env->GetObjectArrayElement(listPlayID, i);
            if (playidstr != NULL) {
                list_play_id[i] = (char *) env->GetStringUTFChars(playidstr, 0);
            }
        }
    }

    char pVoiceID[33];
    memset(pVoiceID, 0, 33);
    int nRet = txca_resource_refresh_list(pVoiceID, &st, list_play_id, list_size);

    if (pVal_skillName) {
        env->ReleaseStringUTFChars(skillName, pVal_skillName);
    }
    if (pVal_skillID) {
        env->ReleaseStringUTFChars(skillID, pVal_skillID);
    }

    if (list_size > 0 && list_play_id) {
        for (int i = 0; i < list_size; ++i) {
            jstring playidstr = (jstring) env->GetObjectArrayElement(listPlayID, i);
            env->ReleaseStringUTFChars(playidstr, list_play_id[i]);
        }
        free(list_play_id);
    }

    jstring strVoiceID;
    ConvChar2JString(env, pVoiceID, strVoiceID);
    return strVoiceID;
}

/**
 * 设置音乐品质
 * @param env
 * @param quality
 * @return
 */
JNIEXPORT jint JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_setQuality(JNIEnv *env, jclass, jint quality) {
    int ret = txca_resource_set_quality(quality);
    return ret;
}

/**
 * 收藏
 * @param env
 * @param appInfo
 * @param playID
 * @param isFavorite
 * @return
 */
JNIEXPORT jstring JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_setFavorite(JNIEnv *env, jclass, jobject appInfo,
                                                     jstring playID, jboolean isFavorite) {
    if (NULL == s_clsAudioAppInfo) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER,
                            "setFavorite NULL == s_clsAudioAppInfo");
        return 0;
    }

    if (NULL == appInfo) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER, "setFavorite NULL == appInfo");
        return 0;
    }

    if (NULL == playID) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER, "setFavorite NULL == playID");
        return 0;
    }

    jclass clsAudioAppInfo = s_clsAudioAppInfo;
    jfieldID id_skillName = env->GetFieldID(clsAudioAppInfo, "name", "Ljava/lang/String;");
    jfieldID id_skillID = env->GetFieldID(clsAudioAppInfo, "ID", "Ljava/lang/String;");
    jfieldID id_appType = env->GetFieldID(clsAudioAppInfo, "type", "I");

    jstring skillName = (jstring) env->GetObjectField(appInfo, id_skillName);
    jstring skillID = (jstring) env->GetObjectField(appInfo, id_skillID);
    jint appType = (jint) env->GetIntField(appInfo, id_appType);

    const char *pVal_skillName = NULL;
    if (skillName) {
        pVal_skillName = env->GetStringUTFChars(skillName, 0);
    }
    const char *pVal_skillID = NULL;
    if (skillID) {
        pVal_skillID = env->GetStringUTFChars(skillID, 0);
    }

    const char *pVal_playID = NULL;
    if (playID) {
        pVal_playID = env->GetStringUTFChars(playID, 0);
    }

    TXCA_PARAM_SKILL st = {0};
    st.name = pVal_skillName;
    st.id = pVal_skillID;
    st.type = appType;

    char pVoiceID[33];
    memset(pVoiceID, 0, 33);
    int nRet = txca_resource_set_favorite(pVoiceID, &st, pVal_playID, (bool) isFavorite);

    if (pVal_skillName) {
        env->ReleaseStringUTFChars(skillName, pVal_skillName);
    }
    if (pVal_skillID) {
        env->ReleaseStringUTFChars(skillID, pVal_skillID);
    }
    if (pVal_playID) {
        env->ReleaseStringUTFChars(playID, pVal_playID);
    }

    jstring strVoiceID;
    ConvChar2JString(env, pVoiceID, strVoiceID);
    return strVoiceID;
}

//通用结果回调通知
void OnCommonRetCallback(const char *voice_id, int err_code) {

}

/**
 * 发送CC消息结果
 * @param cookie
 * @param to
 * @param err_code
 */
void on_private_send_cc_msg_result(unsigned int cookie, unsigned long long to, int err_code) {

    bool needRelease = false;
    JNIEnv *env = Util_CreateEnv(&needRelease);
    if (!env) return;

    jclass cls = env->GetObjectClass(tx_service);
    jmethodID methodID = env->GetMethodID(cls, "onAISendCCMsgResult", "(IJI)V");
    if (methodID) {
        env->CallVoidMethod(tx_service, methodID, cookie, to, err_code);
    }

    env->DeleteLocalRef(cls);
    if (needRelease)
        Util_ReleaseEnv();
}

/**
 * 收到CC消息
 * @param from
 * @param msg
 */
void on_private_cc_msg_recv(unsigned long long from, TXCA_PARAM_CC_MSG *msg) {
    if (!msg) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER,
                            "on_private_cc_msg_recv error msg! from:%llu", from);
        return;
    }

    bool needRelease = false;
    JNIEnv *env = Util_CreateEnv(&needRelease);
    if (!env) return;

    jclass cls = env->GetObjectClass(tx_service);
    jmethodID methodID = env->GetMethodID(cls, "onAIReceiveC2CMsg",
                                          "(JLcom/tencent/xiaowei/info/XWCCMsgInfo;)V");
    if (methodID) {
        jclass clsC2CMsg = s_clsAIC2CMsg;
        jmethodID init = env->GetMethodID(clsC2CMsg, "<init>", "()V");
        jobject objC2CMsg = env->NewObject(clsC2CMsg, init);

        jfieldID jBusinessName = env->GetFieldID(clsC2CMsg, "businessName", "Ljava/lang/String;");
        jstring strBusinessName;
        ConvChar2JString(env, msg->business_name, strBusinessName);
        env->SetObjectField(objC2CMsg, jBusinessName, strBusinessName);

        jbyteArray byteMsgBuf = NULL;
        if (msg->msg && msg->msg_len > 0) {
            jfieldID jMsgBuf = env->GetFieldID(clsC2CMsg, "msgBuf", "[B");
            byteMsgBuf = env->NewByteArray((jsize) msg->msg_len);
            env->SetByteArrayRegion(byteMsgBuf, 0, (jsize) msg->msg_len, (jbyte *) msg->msg);
            env->SetObjectField(objC2CMsg, jMsgBuf, byteMsgBuf);
        }

        env->CallVoidMethod(tx_service, methodID, from, objC2CMsg);

        env->DeleteLocalRef(objC2CMsg);
        env->DeleteLocalRef(strBusinessName);
        if (byteMsgBuf != NULL) {
            env->DeleteLocalRef(byteMsgBuf);
        }
    }

    env->DeleteLocalRef(cls);

    if (needRelease)
        Util_ReleaseEnv();
}

/**
 * 初始化CC消息
 * @param env
 * @return
 */
JNIEXPORT jint JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_initCCMsgModule(JNIEnv *env, jclass) {
    TXCA_CC_MSG_CALLBACK callback = {0};
    callback.on_txca_cc_msg_send_ret = on_private_send_cc_msg_result;
    callback.on_txca_cc_msg_recv = on_private_cc_msg_recv;
    int ret = txca_init_cc_msg_callback(&callback);
    return ret;

    return 0;
}

/**
 * 设置登录态
 * @param env
 * @param info
 * @return
 */
JNIEXPORT jstring JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_setLoginStatus
        (JNIEnv *env, jclass, jobject info) {
    if (NULL == s_clsTXLoginStatusInfo) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER,
                            "setLoginStatus NULL == s_clsTXLoginStatusInfo");
        return 0;
    }

    jclass clsTXLoginStatusInfo = s_clsTXLoginStatusInfo;
    jfieldID type = env->GetFieldID(clsTXLoginStatusInfo, "type", "I");
    jfieldID appID = env->GetFieldID(clsTXLoginStatusInfo, "appID", "Ljava/lang/String;");
    jfieldID openID = env->GetFieldID(clsTXLoginStatusInfo, "openID", "Ljava/lang/String;");
    jfieldID accessToken = env->GetFieldID(clsTXLoginStatusInfo, "accessToken",
                                           "Ljava/lang/String;");
    jfieldID refreshToken = env->GetFieldID(clsTXLoginStatusInfo, "refreshToken",
                                            "Ljava/lang/String;");

    int c_type = env->GetIntField(info, type);
    jstring app_id = (jstring) env->GetObjectField(info, appID);
    jstring open_id = (jstring) env->GetObjectField(info, openID);
    jstring access_token = (jstring) env->GetObjectField(info, accessToken);
    jstring refresh_token = (jstring) env->GetObjectField(info, refreshToken);

    const char *pVal_app_id = NULL;
    if (app_id) {
        pVal_app_id = env->GetStringUTFChars(app_id, 0);
    }
    const char *pVal_open_id = NULL;
    if (open_id) {
        pVal_open_id = env->GetStringUTFChars(open_id, 0);
    }
    const char *pVal_access_token = NULL;
    if (access_token) {
        pVal_access_token = env->GetStringUTFChars(access_token, 0);
    }
    const char *pVal_refresh_token = NULL;
    if (refresh_token) {
        pVal_refresh_token = env->GetStringUTFChars(refresh_token, 0);
    }

    TXCA_PARAM_LOGIN_STATUS st = {0};
    st.type = c_type;
    st.app_id = pVal_app_id;
    st.open_id = pVal_open_id;
    st.access_token = pVal_access_token;
    st.refresh_token = pVal_refresh_token;

    char szVoiceID[33] = {0};
    txca_set_login_status(szVoiceID, &st);

    if (pVal_app_id) {
        env->ReleaseStringUTFChars(app_id, pVal_app_id);
    }
    if (pVal_open_id) {
        env->ReleaseStringUTFChars(open_id, pVal_open_id);
    }
    if (pVal_access_token) {
        env->ReleaseStringUTFChars(access_token, pVal_access_token);
    }
    if (pVal_refresh_token) {
        env->ReleaseStringUTFChars(refresh_token, pVal_refresh_token);
    }

    jstring strVoiceID;
    ConvChar2JString(env, szVoiceID, strVoiceID);

    return strVoiceID;
}

/**
 * 查询登录态
 * @param env
 * @param skillID
 * @return
 */
JNIEXPORT jstring JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_getLoginStatus
        (JNIEnv *env, jclass, jstring skillID) {
    char szVoiceID[33] = {0};

    const char *pVal_skill_id = NULL;
    if (skillID) {
        pVal_skill_id = env->GetStringUTFChars(skillID, 0);
    }

    txca_get_login_status(szVoiceID, pVal_skill_id);

    jstring strVoiceID;
    ConvChar2JString(env, szVoiceID, strVoiceID);

    if (pVal_skill_id) {
        env->ReleaseStringUTFChars(skillID, pVal_skill_id);
    }
    return strVoiceID;
}

/**
 * 查询音乐付费信息
 * @param env
 * @return
 */
JNIEXPORT jstring JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_getMusicVipInfo
        (JNIEnv *env, jclass) {
    char szVoiceID[33] = {0};

    txca_get_music_vip_info(szVoiceID);

    jstring strVoiceID;
    ConvChar2JString(env, szVoiceID, strVoiceID);

    return strVoiceID;
}

/**
 * 获取提示的TTS
 * @param env
 * @return
 */
JNIEXPORT jstring JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_requestProtocolTTS
        (JNIEnv *env, jclass, jlong uin, jlong timestamp, jint type) {
    char szVoiceID[33] = {0};

    txca_request_protocol_tts(szVoiceID, uin, timestamp, type);

    jstring strVoiceID;
    ConvChar2JString(env, szVoiceID, strVoiceID);

    return strVoiceID;
}

/**
 * 上报日志文件
 */
JNIEXPORT jstring JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_uploadLog(JNIEnv *env, jclass, jstring url, jstring time) {
    const char *pVal_Url = NULL;
    const char *pVal_Time = NULL;
    if (url) {
        pVal_Url = env->GetStringUTFChars(url, 0);
    }
    if (time) {
        pVal_Time = env->GetStringUTFChars(time, 0);
    }

    char szVoiceID[33] = {0};
    int ret = txca_upload_log(szVoiceID, pVal_Url, pVal_Time);

    if (pVal_Url) {
        env->ReleaseStringUTFChars(url, pVal_Url);
    }
    if (pVal_Time) {
        env->ReleaseStringUTFChars(time, pVal_Time);
    }

    jstring strVoiceID;
    ConvChar2JString(env, szVoiceID, strVoiceID);

    return (ret == 0 ? strVoiceID : 0);
}

/**
 * 发送cc消息给小微app
 */
JNIEXPORT jint JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_sendCCMsg(JNIEnv *env, jclass, jlong tinyID, jobject msgObj) {
    if (msgObj == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER, "sendCCMsg NULL == msgObj");
        return 0;
    }

    jclass clsC2CMsg = s_clsAIC2CMsg;
    jfieldID jBusinessName = env->GetFieldID(clsC2CMsg, "businessName", "Ljava/lang/String;");
    jfieldID jMsg = env->GetFieldID(clsC2CMsg, "msgBuf", "[B");

    jstring business_name = (jstring) env->GetObjectField(msgObj, jBusinessName);
    jbyteArray msg = (jbyteArray) env->GetObjectField(msgObj, jMsg);

    jsize bufLen = env->GetArrayLength(msg);
    if (bufLen <= 0) {
        __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER, "sendCCMsg bufLen <= 0");
        return 0;
    }

    jbyte *buf = env->GetByteArrayElements(msg, JNI_FALSE);
    char cBuf[bufLen + 1];
    memset(cBuf, 0, (unsigned int) bufLen + 1);
    memcpy(cBuf, buf, (unsigned int) bufLen);
    cBuf[bufLen] = 0;  // add string terminating

    const char *cStrBusName = NULL;
    if (business_name) {
        cStrBusName = env->GetStringUTFChars(business_name, 0);
    }

    TXCA_PARAM_CC_MSG cc_msg = {0};
    cc_msg.business_name = cStrBusName;
    cc_msg.msg = cBuf;
    cc_msg.msg_len = bufLen;

    unsigned int cookie = 0;
    int nRet = txca_send_c2c_msg(tinyID, &cc_msg, &cookie);

    if (cStrBusName) {
        env->ReleaseStringUTFChars(business_name, cStrBusName);
    }

    env->ReleaseByteArrayElements(msg, buf, 0);

    return cookie;
}

//////////////////////////////////////////////////////////////
// remind interface
//////////////////////////////////////////////////////////////

void on_remind_get_alarm_list(const char *voice_id, int err_code, const char **remindlist,
                              unsigned int count) {
    __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER,
                        "on_remind_get_alarm_list voiceid:%s err_code:%d count:%u", voice_id,
                        err_code, count);

    if (NULL == tx_service) {
        __android_log_print(ANDROID_LOG_INFO, LOGFILTER,
                            "on_remind_get_alarm_list NULL == tx_service");
        return;
    }

    bool needRelease = false;
    JNIEnv *env = Util_CreateEnv(&needRelease);
    if (!env) return;

    jobjectArray arrayAlarmList = NULL;
    if (err_code == err_null) {
        jclass jstringCls = env->FindClass("java/lang/String");
        arrayAlarmList = env->NewObjectArray(count, jstringCls, NULL);
        for (int i = 0; i < count; ++i) {
            jstring alarmInfo = env->NewStringUTF("");
            ConvChar2JString(env, remindlist[i], alarmInfo);
            env->SetObjectArrayElement(arrayAlarmList, i, alarmInfo);
            env->DeleteLocalRef(alarmInfo);
        }
    }

    jclass cls = env->GetObjectClass(tx_service);
    jstring strVoiceID;
    ConvChar2JString(env, voice_id, strVoiceID);
    jmethodID methodID = env->GetMethodID(cls, "onGetAlaramList",
                                          "(ILjava/lang/String;[Ljava/lang/String;)V");
    if (methodID) {
        env->CallVoidMethod(tx_service, methodID, err_code, strVoiceID, arrayAlarmList);
    }

    env->DeleteLocalRef(cls);
    env->DeleteLocalRef(strVoiceID);
    if (arrayAlarmList)
        env->DeleteLocalRef(arrayAlarmList);

    if (needRelease)
        Util_ReleaseEnv();
}

void on_remind_set_alarm_result(const char* voice_id, int err_code, unsigned int alarm_id) {
    __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER,
                        "on_remind_set_alarm_result voice_id:%s err_code:%d alarm_id:%d",
                        voice_id, err_code, alarm_id);

    if (NULL == tx_service) {
        __android_log_print(ANDROID_LOG_INFO, LOGFILTER, "on_remind_set_alarm_result NULL == tx_service");
        return;
    }

    bool needRelease = false;
    JNIEnv *env = Util_CreateEnv(&needRelease);
    if (!env) return;

    jclass cls = env->GetObjectClass(tx_service);
    jstring strVoiceID;
    ConvChar2JString(env, voice_id, strVoiceID);
    jmethodID methodID = env->GetMethodID(cls, "onSetAlarmCallback", "(ILjava/lang/String;I)V");
    if (methodID) {
        env->CallVoidMethod(tx_service, methodID, err_code, strVoiceID, alarm_id);
    }

    env->DeleteLocalRef(cls);
    env->DeleteLocalRef(strVoiceID);

    if (needRelease)
        Util_ReleaseEnv();
}

/**
 * 拉取提醒列表
 */
JNIEXPORT jstring JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_getDeviceAlarmList(JNIEnv *env, jclass) {
    char szVoiceID[33] = {0};
    int ret = txca_get_alarm_list(szVoiceID, on_remind_get_alarm_list);
    __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER, "txca_get_alarm_list voiceid:%s", szVoiceID);

    jstring strVoiceID;
    ConvChar2JString(env, szVoiceID, strVoiceID);

    return (ret == 0 ? strVoiceID : 0);
}

/**
 * 设置提醒
 */
JNIEXPORT jstring JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_setDeviceAlarm
        (JNIEnv *env, jclass, jint optType, jstring alarmInfoJson) {
    __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER, "updateDeviceAlarm optType:%d", optType);

    if (alarmInfoJson == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER,
                            "updateDeviceAlarm: alarmInfoJson is null");
        return 0;
    }

    const char *pVal_AlarmInfo = NULL;
    if (alarmInfoJson) {
        pVal_AlarmInfo = env->GetStringUTFChars(alarmInfoJson, 0);
    }

    char szVoiceID[33] = {0};
    int ret = txca_set_alarm_info(szVoiceID, optType, pVal_AlarmInfo, on_remind_set_alarm_result);
    if (ret) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER, "txca_set_alarm_info failed! ret:%d",
                            ret);
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER, "txca_set_alarm_info voiceid:%s",
                            szVoiceID);
    }

    if (pVal_AlarmInfo) {
        env->ReleaseStringUTFChars(alarmInfoJson, pVal_AlarmInfo);
    }

    jstring strVoiceID;
    ConvChar2JString(env, szVoiceID, strVoiceID);

    return (ret == 0 ? strVoiceID : 0);;
}

/**
 * 拉取定时任务播放资源
 */
JNIEXPORT jstring JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_getTimingSkillResource
        (JNIEnv *env, jclass, jstring alarmId) {
    __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER, "getTimingSkillResource");

    if (alarmId == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER,
                            "getTimingSkillResource: alarmId is null");
        return 0;
    }

    const char *pVal_AlarmId = NULL;
    if (alarmId) {
        pVal_AlarmId = env->GetStringUTFChars(alarmId, 0);
    }

    char szVoiceID[33] = {0};
    int ret = txca_get_timing_skill_resources(szVoiceID, pVal_AlarmId);
    if (ret) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER, "txca_get_timing_skill_resources failed! ret:%d",
                            ret);
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER, "txca_get_timing_skill_resources voiceid:%s",
                            szVoiceID);
    }

    if (pVal_AlarmId) {
        env->ReleaseStringUTFChars(alarmId, pVal_AlarmId);
    }

    jstring strVoiceID;
    ConvChar2JString(env, szVoiceID, strVoiceID);

    return (ret == 0 ? strVoiceID : 0);;
}

//////////////////////////////////////////////////////////////
// wordslist interface
//////////////////////////////////////////////////////////////

/**
 * 启用屏幕取词可见可答
 * return ret
 */
JNIEXPORT jint JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_enableV2A(JNIEnv *env, jclass, jboolean enable) {
    return txca_enable_v2a(enable);
}

void on_xiaowei_setwordslist_ret(int op, int errCode)
{
    if (NULL == tx_service) {
        __android_log_print(ANDROID_LOG_INFO, LOGFILTER, "on_xiaowei_setwordslist_ret NULL == tx_service");
        return;
    }

    bool needRelease = false;
    JNIEnv *env = Util_CreateEnv(&needRelease);
    if (!env) return;

    jclass cls = env->GetObjectClass(tx_service);
    jmethodID methodID = env->GetMethodID(cls, "onSetWordsListRet", "(II)V");
    if (methodID) {
        env->CallVoidMethod(tx_service, methodID, op, errCode);
    }

    env->DeleteLocalRef(cls);

    if (needRelease)
        Util_ReleaseEnv();
}

/**
 * 设置词表
 */
JNIEXPORT jint JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_setWordslist(JNIEnv *env, jclass,
                                                                        jint type,
                                                                        jobjectArray arrayWordslist) {
    if (NULL == arrayWordslist) {
        __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER, "setWordslist NULL == arrayWordslist");
        return txca_set_wordslist((TXCA_WORDS_TYPE) type, NULL, 0, on_xiaowei_setwordslist_ret);
    }

    int list_size = env->GetArrayLength(arrayWordslist);
    if (list_size <= 0) {
        return txca_set_wordslist((TXCA_WORDS_TYPE) type, NULL, 0, on_xiaowei_setwordslist_ret);
    }

    char **words_list = (char **) malloc(list_size * sizeof(char *));
    if (words_list != NULL) {
        memset(words_list, 0, list_size * sizeof(char *));
        for (int i = 0; i < list_size; ++i) {
            jobject obj = env->GetObjectArrayElement(arrayWordslist, i);
            jstring words = (jstring) (obj);
            const char *szbuff = env->GetStringUTFChars(words, 0);
            int length = env->GetStringUTFLength(words);
            if (words && length > 0) {
                words_list[i] = (char *) malloc(sizeof(char) * (length + 1));
                memcpy(words_list[i], szbuff, length);
                words_list[i][length] = '\0';
            }
            env->ReleaseStringUTFChars(words, szbuff);
            env->DeleteLocalRef(obj);
        }
    }

    int ret = txca_set_wordslist((TXCA_WORDS_TYPE) type, words_list, list_size, on_xiaowei_setwordslist_ret);

    if (words_list != NULL) {
        for (int i = 0; i < list_size; ++i) {
            if (words_list[i] != NULL)
                free(words_list[i]);
        }
        free(words_list);
    }
    return ret;
}

/**
 * 对上一次请求进行错误反馈
 * @param env
 * @return
 */
JNIEXPORT jint JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_errorFeedBack(JNIEnv *env, jclass) {
    txca_error_feed_back();
}

/**
 * 上报关键事件
 * @param env
 * @param log
 * @return
 */
JNIEXPORT jint JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_reportEvent
        (JNIEnv *env, jclass, jobject log) {
    if (NULL == s_clsAILog) {
        __android_log_print(ANDROID_LOG_ERROR, LOGFILTER, "reportEvent NULL == s_clsAILog");
        return 0;
    }

    jclass clsAILog = s_clsAILog;
    jfieldID java_type = env->GetFieldID(clsAILog, "type", "I");
    jfieldID java_event = env->GetFieldID(clsAILog, "event", "Ljava/lang/String;");
    jfieldID java_retCode = env->GetFieldID(clsAILog, "retCode", "I");
    jfieldID java_time = env->GetFieldID(clsAILog, "time", "J");
    jfieldID java_skillName = env->GetFieldID(clsAILog, "skillName", "Ljava/lang/String;");
    jfieldID java_skillID = env->GetFieldID(clsAILog, "skillID", "Ljava/lang/String;");
    jfieldID java_voiceID = env->GetFieldID(clsAILog, "sessionID", "Ljava/lang/String;");
    jfieldID java_logData = env->GetFieldID(clsAILog, "logData", "Ljava/lang/String;");
    jfieldID java_ulsSubCmd = env->GetFieldID(clsAILog, "ulsSubCmd", "I");

    TXCA_PARAM_LOG logReq = {0};
    logReq.type = env->GetIntField(log, java_type);
    logReq.ret_code = env->GetIntField(log, java_retCode);
    logReq.time_stamp_ms = env->GetLongField(log, java_time);
    logReq.sub_cmd = env->GetIntField(log, java_ulsSubCmd);

    jstring event = (jstring) env->GetObjectField(log, java_event);
    jstring voiceID = (jstring) env->GetObjectField(log, java_voiceID);
    jstring logData = (jstring) env->GetObjectField(log, java_logData);
    jstring skillName = (jstring) env->GetObjectField(log, java_skillName);
    jstring skillID = (jstring) env->GetObjectField(log, java_skillID);

    if (event) {
        logReq.event = env->GetStringUTFChars(event, 0);
    }
    if (voiceID) {
        logReq.voice_id = env->GetStringUTFChars(voiceID, 0);
    }
    if (logData) {
        logReq.log_data = env->GetStringUTFChars(logData, 0);
    }
    if (skillName) {
        logReq.skill_name = env->GetStringUTFChars(skillName, 0);
    }
    if (skillID) {
        logReq.skill_id = env->GetStringUTFChars(skillID, 0);
    }

    int nRet = txca_data_report(&logReq);

    if (logReq.event) {
        env->ReleaseStringUTFChars(event, logReq.event);
    }
    if (logReq.voice_id) {
        env->ReleaseStringUTFChars(voiceID, logReq.voice_id);
    }
    if (logReq.log_data) {
        env->ReleaseStringUTFChars(logData, logReq.log_data);
    }
    if (logReq.skill_name) {
        env->ReleaseStringUTFChars(skillName, logReq.skill_name);
    }
    if (logReq.skill_id) {
        env->ReleaseStringUTFChars(skillID, logReq.skill_id);
    }

    return nRet;
}

#ifdef __cplusplus
}
#endif