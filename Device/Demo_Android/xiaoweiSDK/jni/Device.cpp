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
 * 上传注册信息的结果
 * @param error_code
 */
void on_wlan_upload_register_info_success(int error_code)
{
	__android_log_print(ANDROID_LOG_INFO, LOGFILTER, "on_wlan_upload_register_info_success");

	if (tx_service)
	{
		bool needRelease = false;
		JNIEnv *env = Util_CreateEnv(&needRelease);
		if (!env)  return;

		jclass cls = env->GetObjectClass(tx_service);
		jmethodID methodID = env->GetMethodID(cls, "onWlanUploadRegInfoSuccess", "(I)V");
		if (methodID)
		{
			env->CallVoidMethod(tx_service, methodID, error_code);
		}

		env->DeleteLocalRef(cls);
		if (needRelease) Util_ReleaseEnv();
	}
}

/**
 * 连接服务器回调，断网恢复后也会回调
 * @param error_code 错误码，0 连接成功，1 网络不通， 2 ping不通服务器
 */
void on_connected_server(int error_code)
{
    __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER, "on_connected_server: error_code %d", error_code);
    bool needRelease = false;
    JNIEnv *env = Util_CreateEnv(&needRelease);
    if (!env) {
        return;
    }

    jclass cls = env->GetObjectClass(tx_service);
    jmethodID methodID = env->GetMethodID(cls, "onConnectedServer",
                                          "(I)V");
    if (methodID)
    {
        env->CallVoidMethod(tx_service, methodID, error_code);
    }

    env->DeleteLocalRef(cls);
    if(needRelease) Util_ReleaseEnv();
}

/**
 * 注册的回调
 * @param error_code 错误码 0 注册成功，1 信息不对， 2 未知错误
 * @param sub_error_code 子错误码
 */
void on_register(int error_code, int sub_error_code)
{
    __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER, "on_register: error_code %d sub_error_code: %d", error_code, sub_error_code);
    bool needRelease = false;
    JNIEnv *env = Util_CreateEnv(&needRelease);
    if (!env) {
        return;
    }

    jclass cls = env->GetObjectClass(tx_service);
    jmethodID methodID = env->GetMethodID(cls, "onRegisterResult",
                                          "(II)V");
    if (methodID)
    {
        env->CallVoidMethod(tx_service, methodID, error_code, sub_error_code);
    }

    env->DeleteLocalRef(cls);

    if(needRelease) Util_ReleaseEnv();
}

/**
 * 登录结果
 * @param errcode
 */
void on_login_complete(int errcode)
{
	__android_log_print(ANDROID_LOG_INFO, LOGFILTER, "on_login_complete | code[%d]\n", errcode);

	if (tx_service)
	{
		bool needRelease = false;
		JNIEnv *env = Util_CreateEnv(&needRelease);
		if (!env)  return;

		jclass cls = env->GetObjectClass(tx_service);
		jmethodID methodID = env->GetMethodID(cls, "onLoginComplete", "(I)V");
		if (methodID)
		{
			env->CallVoidMethod(tx_service, methodID, errcode);
		}

		env->DeleteLocalRef(cls);
		if (needRelease) Util_ReleaseEnv();
	}
}

/**
 * 在线状态变化
 * @param old
 * @param newStatus
 */
void on_online_status(int old, int newStatus) {
	__android_log_print(ANDROID_LOG_INFO, LOGFILTER, "on_online_status: old[%d] new[%d]\n", old,
						newStatus);

	if (tx_service) {
		bool needRelease = false;
		JNIEnv *env = Util_CreateEnv(&needRelease);
		if (!env)
			return;

		jclass cls = env->GetObjectClass(tx_service);
		jmethodID methodID = NULL;
		if (11 == newStatus) {
			methodID = env->GetMethodID(cls, "onOnlineSuccess", "()V");
		} else if (21 == newStatus) {
			methodID = env->GetMethodID(cls, "onOfflineSuccess", "()V");
		}

		if (methodID) {
			env->CallVoidMethod(tx_service, methodID);
		}

		env->DeleteLocalRef(cls);

		if (needRelease) {
			Util_ReleaseEnv();
		}
	}
}
#ifdef __cplusplus
}
#endif
