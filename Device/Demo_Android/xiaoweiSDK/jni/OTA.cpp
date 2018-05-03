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


#ifdef __cplusplus
extern "C" {
#endif

extern jobject tx_service;

/**
 * 查询到了OTA的信息
 * @param from 来源 0 定时自动检测 1 App操作 2 ServerPush 3 设备主动查询
 * @param force  是否强制
 * @param version  版本
 * @param title  标题
 * @param desc 描述
 * @param url 下载链接
 * @param md5 文件md5
 */
void on_ota_info(int from, bool force, unsigned int version, const char *title, const char *desc,
                 const char *url, const char *md5) {
    if (NULL == tx_service) {
        __android_log_print(ANDROID_LOG_INFO, LOGFILTER, "OTA on_ota_info NULL == tx_service");
        return;
    }

    bool needRelease = false;
    JNIEnv *env = Util_CreateEnv(&needRelease);
    if (!env) return;

    jclass cls = env->GetObjectClass(tx_service);
    jmethodID methodID = env->GetMethodID(cls, "onOTAInfo",
                                          "(IZILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    if (methodID) {
        jstring titleStr;
        ConvChar2JString(env, title, titleStr);
        jstring descStr;
        ConvChar2JString(env, desc, descStr);
        jstring descUrl;
        ConvChar2JString(env, url, descUrl);
        jstring descMD5;
        ConvChar2JString(env, md5, descMD5);
        env->CallVoidMethod(tx_service, methodID, from, force, version, titleStr, descStr, descUrl,
                            descMD5);
        env->DeleteLocalRef(titleStr);
        env->DeleteLocalRef(descStr);
        env->DeleteLocalRef(descUrl);
        env->DeleteLocalRef(descMD5);
    }

    env->DeleteLocalRef(cls);

    if (needRelease) Util_ReleaseEnv();
}

/**
 * 查询OTA更新
 * @param env
 * @return
 */
JNIEXPORT int JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_queryOtaUpdate(JNIEnv *env) {
    return tx_query_ota_update();
}

#ifdef __cplusplus
}
#endif

