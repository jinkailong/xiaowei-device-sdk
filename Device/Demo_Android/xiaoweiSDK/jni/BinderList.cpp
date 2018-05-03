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
extern jclass s_clsBinderInfo;

jobjectArray ConverBinderList2JobjectArray(JNIEnv *env, TX_BINDER_INFO *pBinderList, int nCount) {
    if (!env) {
        return NULL;
    }

    if (NULL == s_clsBinderInfo) {
        __android_log_print(ANDROID_LOG_INFO, LOGFILTER,
                            "ConverBinderList2JobjectArray NULL == s_clsBinderInfo");
        return NULL;
    }

    jclass clsBinderInfo = s_clsBinderInfo;
    jobject obj = NULL;
    jfieldID type = env->GetFieldID(clsBinderInfo, "type", "I");
    jfieldID tinyid = env->GetFieldID(clsBinderInfo, "tinyID", "J");
    jfieldID name = env->GetFieldID(clsBinderInfo, "remark", "Ljava/lang/String;");
    jfieldID head = env->GetFieldID(clsBinderInfo, "headUrl", "Ljava/lang/String;");
    jmethodID init = env->GetMethodID(clsBinderInfo, "<init>", "()V");
    jfieldID onLine = env->GetFieldID(clsBinderInfo, "online", "I");


    jobjectArray arrayBinder = env->NewObjectArray(nCount, clsBinderInfo, NULL);
    if (pBinderList && nCount > 0) {
        for (int i = 0; i < nCount; ++i) {
            obj = env->NewObject(clsBinderInfo, init);
            env->SetIntField(obj, type, (jint) pBinderList[i].type);
            env->SetLongField(obj, tinyid, (jlong) pBinderList[i].tinyid);
            env->SetIntField(obj, onLine, (jint) pBinderList[i].online_status);
            jstring nick_name;
            ConvChar2JString(env, pBinderList[i].nick_name, nick_name);
            env->SetObjectField(obj, name, nick_name);
            jstring head_url;
            ConvChar2JString(env, pBinderList[i].head_url, head_url);
            env->SetObjectField(obj, head, head_url);
            env->SetObjectArrayElement(arrayBinder, i, obj);
            env->DeleteLocalRef(obj);
            env->DeleteLocalRef(nick_name);
            env->DeleteLocalRef(head_url);
        }
    }

    return arrayBinder;
}

/**
 * 绑定者列表变化了
 * @param error 非0表示查询列表失败
 * @param pBinderList
 * @param nCount
 */
void on_binder_list_change(int error, TX_BINDER_INFO *pBinderList, int nCount) {
    __android_log_print(ANDROID_LOG_INFO, LOGFILTER, "on_binder_list_change: nCount[%d]", nCount);
    if (NULL == tx_service) {
        __android_log_print(ANDROID_LOG_INFO, LOGFILTER,
                            "on_binder_list_change NULL == tx_service");
        return;
    }

    bool needRelease = false;
    JNIEnv *env = Util_CreateEnv(&needRelease);
    if (!env) return;

    jobjectArray arrayBinder = NULL;
    if (error == err_null) {
        arrayBinder = ConverBinderList2JobjectArray(env, pBinderList, nCount);
    }

    jclass cls = env->GetObjectClass(tx_service);
    jmethodID methodID = env->GetMethodID(cls, "onBinderListChange",
                                          "(I[Lcom/tencent/xiaowei/info/XWBinderInfo;)V");
    if (methodID) {
        env->CallVoidMethod(tx_service, methodID, error, arrayBinder);
    }

    env->DeleteLocalRef(cls);
    if (arrayBinder) env->DeleteLocalRef(arrayBinder);

    if (needRelease) Util_ReleaseEnv();
}

jobjectArray ConverRemarkList2JobjectArray(JNIEnv * env, TX_BINDER_REMARK_INFO * pBinderList, int nCount)
{
    if (!env)
    {
        return NULL;
    }

    extern jclass s_clsBinderRemark;
    if (NULL == s_clsBinderRemark)
    {
        __android_log_print(ANDROID_LOG_INFO, LOGFILTER, "ConverRemarkList2JobjectArray NULL == s_clsBinderInfo");
        return NULL;
    }

    jclass clsBinderInfo = s_clsBinderRemark;
    jobject obj = NULL;
    jfieldID tinyid = env->GetFieldID(clsBinderInfo, "tinyid", "J");
    jfieldID name = env->GetFieldID(clsBinderInfo, "remark", "Ljava/lang/String;");
    jmethodID init = env->GetMethodID(clsBinderInfo, "<init>", "()V");


    jobjectArray arrayBinder = env->NewObjectArray(nCount, clsBinderInfo, NULL);
    if (pBinderList && nCount > 0)
    {
        for (int i = 0; i < nCount; ++i)
        {
            obj = env->NewObject(clsBinderInfo, init);
            env->SetLongField(obj, tinyid, (jlong)pBinderList[i].tinyid);

            jstring nick_name;
            ConvChar2JString(env, pBinderList[i].remark_name, nick_name);
            env->SetObjectField(obj, name, nick_name);

            env->SetObjectArrayElement(arrayBinder, i, obj);
            env->DeleteLocalRef(obj);
            env->DeleteLocalRef(nick_name);
        }
    }

    return arrayBinder;
}

void on_binder_remark_change_callback(int cookie, TX_BINDER_REMARK_INFO * pBinderRemarkList, int nCount)
{
    __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER, "on_binder_remark_change_callback: cookie %d count: %d", cookie, nCount);
    bool needRelease = false;
    JNIEnv *env = Util_CreateEnv(&needRelease);
    if (!env) {
        return;
    }

    jobjectArray arrayBinder = NULL;
    arrayBinder = ConverRemarkList2JobjectArray(env, pBinderRemarkList, nCount);

    jclass cls = env->GetObjectClass(tx_service);
    jmethodID methodID = env->GetMethodID(cls, "onGetBinderRemarkList",
                                          "(I[Lcom/tencent/xiaowei/info/XWBinderRemark;)V");
    if (methodID)
    {
        env->CallVoidMethod(tx_service, methodID, cookie, arrayBinder);
    }

    env->DeleteLocalRef(cls);
    if (arrayBinder) env->DeleteLocalRef(arrayBinder);

    if(needRelease) Util_ReleaseEnv();
}

JNIEXPORT jobjectArray JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_fetchBinderList
        (JNIEnv *env, jclass) {
    jobjectArray arrayBinder = NULL;

    int nCount = 0;
    int err = tx_get_binder_list(NULL, &nCount, NULL);
    if (err_buffer_notenough == err && nCount > 0) {
        TX_BINDER_INFO *pBinderList = new TX_BINDER_INFO[nCount];
        if (pBinderList) {
            memset(pBinderList, 0, nCount * sizeof(TX_BINDER_INFO));
            int err = tx_get_binder_list(pBinderList, &nCount, NULL);
            if (err_null == err) {
                arrayBinder = ConverBinderList2JobjectArray(env, pBinderList, nCount);
            }
            delete[] pBinderList;
        }
    }
    return arrayBinder;
}

/**
 * 绑定者列表在线状态变化了
 * @param result
 * @param pBinderList
 * @param nCount
 */
void on_binder_list_online_status_update_cb(int result, TX_BINDER_INFO *pBinderList, int nCount) {
    if (result == 0) {
        on_binder_list_change(0, pBinderList, nCount);
    }
}

JNIEXPORT void JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_updateBinderOnlineStatus(JNIEnv *env, jclass) {
    tx_update_binder_list_online_status(on_binder_list_online_status_update_cb);
}

/**
 * 解绑了
 * @param error_code
 */
void callback_on_erase_all_binders(int error_code) {
    if (NULL == tx_service) {
        __android_log_print(ANDROID_LOG_INFO, LOGFILTER, "on_erase_all_binders NULL == tx_service");
        return;
    }

    bool needRelease = false;
    JNIEnv *env = Util_CreateEnv(&needRelease);
    if (!env) return;

    jclass cls = env->GetObjectClass(tx_service);
    jmethodID methodID = env->GetMethodID(cls, "onUnBind", "(I)V");
    if (methodID) {
        env->CallVoidMethod(tx_service, methodID, error_code);
    }

    env->DeleteLocalRef(cls);

    if (needRelease) Util_ReleaseEnv();
}


JNIEXPORT int Java_com_tencent_xiaowei_sdk_XWSDKJNI_unBind
        (JNIEnv *env, jclass) {
    return tx_erase_all_binders(callback_on_erase_all_binders);
}

JNIEXPORT jint JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_getBinderRemarkList(JNIEnv *env, jclass) {

    __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER, "getBinderRemarkList.");

    int cookie = tx_get_binder_remark_list();
    return cookie;
}

#ifdef __cplusplus
}
#endif
