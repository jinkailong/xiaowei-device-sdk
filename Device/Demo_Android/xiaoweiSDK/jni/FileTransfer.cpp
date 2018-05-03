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
#include "TXCAudioMsg.h"


#ifdef __cplusplus
extern "C" {
#endif

extern jobject tx_service;
extern jclass s_clsTransferInfo;

/**
 * 传输进度
 * @param transfer_cookie
 * @param transfer_progress
 * @param max_transfer_progress
 */
void on_transfer_progress(unsigned long long transfer_cookie, unsigned long long transfer_progress,
                          unsigned long long max_transfer_progress) {
    if (NULL == tx_service) {
        __android_log_print(ANDROID_LOG_INFO, LOGFILTER, "on_transfer_progress NULL == tx_service");
        return;
    }

    bool needRelease = false;
    JNIEnv *env = Util_CreateEnv(&needRelease);
    if (!env) return;

    jclass cls = env->GetObjectClass(tx_service);
    jmethodID methodID = env->GetMethodID(cls, "onTransferProgress", "(JJJ)V");
    if (methodID) {
        env->CallVoidMethod(tx_service, methodID, transfer_cookie, transfer_progress,
                            max_transfer_progress);
    }

    env->DeleteLocalRef(cls);
    if (needRelease) Util_ReleaseEnv();
}

/**
 * 传输结束
 * @param transfer_cookie
 * @param err_code
 * @param fnInfo
 */
void on_transfer_complete(unsigned long long transfer_cookie, int err_code,
                          TXCA_FILE_TRANSFER_INFO *fnInfo) {
    if (NULL == tx_service || NULL == s_clsTransferInfo) {
        __android_log_print(ANDROID_LOG_INFO, LOGFILTER,
                            "on_transfer_complete NULL == tx_service || NULL == s_clsTransferInfo");
        return;
    }

    bool needRelease = false;
    JNIEnv *env = Util_CreateEnv(&needRelease);
    if (!env) return;

    jclass cls = env->GetObjectClass(tx_service);

    if (!cls) {
        if (needRelease) Util_ReleaseEnv();
        return;
    }
    jmethodID methodID = env->GetMethodID(cls, "onTransferComplete",
                                          "(JILcom/tencent/xiaowei/info/XWFileTransferInfo;)V");
    if (methodID) {
        std::string file_path;
        if (fnInfo != NULL)
            Translate2UTF8(fnInfo->file_path,
                           file_path); // file_path.assign(fnInfo->file_path, strlen(fnInfo->file_path));

        jbyteArray file_key = NULL;
        if (fnInfo != NULL) file_key = env->NewByteArray(fnInfo->key_length);
        if (file_key && fnInfo != NULL) {
            env->SetByteArrayRegion(file_key, 0, fnInfo->key_length, (jbyte *) fnInfo->file_key);
        }

        jbyteArray mini_token = NULL;
        if (fnInfo != NULL && fnInfo->mini_token != NULL)
            mini_token = env->NewByteArray(strlen(fnInfo->mini_token));
        if (mini_token && fnInfo != NULL) {
            env->SetByteArrayRegion(mini_token, 0, strlen(fnInfo->mini_token),
                                    (jbyte *) fnInfo->mini_token);
        }

        int transfer_type = 0;
        if (fnInfo != NULL) transfer_type = fnInfo->transfer_type;

        int channel_type = 0;
        if (fnInfo != NULL) channel_type = fnInfo->channel_type;

        int file_type = 0;
        if (fnInfo != NULL) file_type = fnInfo->file_type;

        unsigned long long file_size = 0;
        if (fnInfo != NULL) file_size = fnInfo->file_size;

        jbyteArray buffer_extra = NULL;
        if (fnInfo != NULL) buffer_extra = env->NewByteArray(fnInfo->buff_length);
        if (buffer_extra && fnInfo != NULL) {
            env->SetByteArrayRegion(buffer_extra, 0, fnInfo->buff_length,
                                    (jbyte *) fnInfo->buff_with_file);
        }

        std::string business_name;
        if (fnInfo != NULL)
            Translate2UTF8(fnInfo->bussiness_name,
                           business_name); // business_name.assign(fnInfo->bussiness_name, 64);

        jclass clsTransferInfo = s_clsTransferInfo;
        jfieldID path = env->GetFieldID(clsTransferInfo, "filePath", "Ljava/lang/String;");
        jfieldID key = env->GetFieldID(clsTransferInfo, "fileKey", "[B");
        jfieldID key2 = env->GetFieldID(clsTransferInfo, "miniToken", "[B");
        jfieldID type = env->GetFieldID(clsTransferInfo, "transferType", "I");
        jfieldID buff = env->GetFieldID(clsTransferInfo, "bufferExtra", "[B");
        jfieldID busi = env->GetFieldID(clsTransferInfo, "businessName", "Ljava/lang/String;");

        jfieldID chType = env->GetFieldID(clsTransferInfo, "channelType", "I");
        jfieldID fileSize = env->GetFieldID(clsTransferInfo, "fileSize", "J");
        jfieldID fileType = env->GetFieldID(clsTransferInfo, "fileType", "I");

        jmethodID init = env->GetMethodID(clsTransferInfo, "<init>", "()V");
        jobject obj = env->NewObject(clsTransferInfo, init);

        jstring jPath;
        ConvChar2JString(env, file_path.c_str(), jPath);
        env->SetObjectField(obj, path, jPath);
        env->SetObjectField(obj, key, file_key);
        env->SetObjectField(obj, key2, mini_token);
        env->SetIntField(obj, type, (jint) transfer_type);
        env->SetIntField(obj, chType, (jint) channel_type);
        env->SetObjectField(obj, buff, buffer_extra);

        jstring jBusinessName;
        ConvChar2JString(env, business_name.c_str(), jBusinessName);
        env->SetObjectField(obj, busi, jBusinessName);
        env->SetLongField(obj, fileSize, (jlong) file_size);
        env->SetIntField(obj, fileType, (jint) file_type);

        if (file_key) env->DeleteLocalRef(file_key);
        if (mini_token) env->DeleteLocalRef(mini_token);
        if (buffer_extra) env->DeleteLocalRef(buffer_extra);

        env->CallVoidMethod(tx_service, methodID, transfer_cookie, err_code, obj);

        env->DeleteLocalRef(jPath);
        env->DeleteLocalRef(jBusinessName);
    }

    env->DeleteLocalRef(cls);
    if (needRelease) Util_ReleaseEnv();
}

int on_auto_download_callback(unsigned long long file_size, unsigned int channel_type)
{
    if (NULL == tx_service || NULL == s_clsTransferInfo) {
        __android_log_print(ANDROID_LOG_INFO, LOGFILTER, "on_auto_download_callback NULL == tx_service");
        return -1;
    }

    __android_log_print(ANDROID_LOG_INFO, LOGFILTER, "on_auto_download_callback size:%llu channel:%u",
        file_size, channel_type);

    bool needRelease = false;
    JNIEnv *env = Util_CreateEnv(&needRelease);
    if (!env) return -1;

    jclass cls = env->GetObjectClass(tx_service);

    if (!cls) {
        if (needRelease) Util_ReleaseEnv();
        return -1;
    }

    jint ret = 0;
    jmethodID methodID = env->GetMethodID(cls, "onAutoDownloadFileCallback", "(JI)I");
    if (methodID) {
        ret = env->CallIntMethod(tx_service, methodID, (jlong)file_size, (jint)channel_type);
    }

    env->DeleteLocalRef(cls);
    if (needRelease)
        Util_ReleaseEnv();

    return (int)ret;
}

/*
 * Class:     com_tencent_xiaowei_sdk_XWSDKJNI
 * Method:    uploadFile 上传文件
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_uploadFile
        (JNIEnv *env, jclass, jstring file_path, jint channeltype, jint fileType) {
    if (NULL == file_path) {
        return 0;
    }

    const char *pFilePath = env->GetStringUTFChars(file_path, 0);
    unsigned long long cookie = 0;
    txca_upload_file(channeltype, fileType, (char *) pFilePath, &cookie);
    env->ReleaseStringUTFChars(file_path, pFilePath);

    return cookie;
}

/*
 * Class:     com_tencent_xiaowei_sdk_XWSDKJNI
 * Method:    cancelTransfer 取消传输
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_cancelTransfer
        (JNIEnv *env, jclass, jlong transfer_cookie) {
    unsigned int cookie = transfer_cookie;
    txca_cancel_transfer(cookie);
}

/**
 * 获得上传的文件下载的url
 * @param env
 * @param cls
 * @param fileId
 * @param fileType
 * @return
 */
JNIEXPORT jstring JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_getMiniDownloadURL(JNIEnv *env, jclass cls, jstring fileId,
                                                       jint fileType) {
    jstring jstrUrl;
    if (fileId == NULL) {
        jstrUrl = env->NewStringUTF("");
        return jstrUrl;
    }
    const char *pFileId = env->GetStringUTFChars(fileId, 0);

    char szUrl[512] = {0};
    int ret = txca_get_minidownload_url((char *) pFileId, fileType, szUrl);
    if (ret != err_null) {
        jstrUrl = env->NewStringUTF("");
        return jstrUrl;
    }
    std::string strMiniURL;
    Translate2UTF8(szUrl, strMiniURL);

    ConvChar2JString(env, strMiniURL.c_str(), jstrUrl);

    env->ReleaseStringUTFChars(fileId, pFileId);
    return jstrUrl;
}

JNIEXPORT jlong JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_downloadMiniFile
  (JNIEnv *env, jclass, jstring file_key, jint file_type, jstring mini_token)
{
	if (NULL == file_key)
	{
		return 0;
	}

	const char * pFileKey = NULL;
	if (file_key != NULL)
	    pFileKey = env->GetStringUTFChars(file_key, 0);

	const char * pMiniToken = NULL;
	if(mini_token != NULL)
	    pMiniToken = env->GetStringUTFChars(mini_token, 0);

	unsigned long long cookie = 0;
	txca_download_file(transfer_channeltype_MINI, file_type, (char*)pFileKey, strlen(pFileKey), pMiniToken, &cookie);

	if (pFileKey)
	    env->ReleaseStringUTFChars(file_key, pFileKey);
	if (pMiniToken)
	    env->ReleaseStringUTFChars(mini_token, pMiniToken);

	return cookie;
}


void OnDPSMsgFileProgress(const unsigned int cookie, unsigned long long transfer_progress, unsigned long long max_transfer_progress)
{
	__android_log_print(ANDROID_LOG_INFO, LOGFILTER, "OnDPSMsgFileProgress: cookie [%u] progress[%llu] max_progress[%llu]", cookie, transfer_progress, max_transfer_progress);
	if (NULL == tx_service)
	{
		__android_log_print(ANDROID_LOG_INFO, LOGFILTER, "OnDPSMsgFileProgress NULL == tx_service");
		return;
	}

	bool needRelease = false;
	JNIEnv *env = Util_CreateEnv(&needRelease);
	if(!env) return;

	jclass cls = env->GetObjectClass(tx_service);
	jmethodID methodID = env->GetMethodID(cls, "OnRichMsgSendProgress", "(IJJ)V");
	if (methodID)
	{
		env->CallVoidMethod(tx_service, methodID, cookie, transfer_progress, max_transfer_progress);
	}

	env->DeleteLocalRef(cls);
	if(needRelease) Util_ReleaseEnv();
}
void OnDPSMsgSendRet(const unsigned int cookie, int err_code)
{
	__android_log_print(ANDROID_LOG_INFO, LOGFILTER, "OnDPSMsgSendRet: cookie [%u] err_code[%d]", cookie, err_code);
	if (NULL == tx_service)
	{
		__android_log_print(ANDROID_LOG_INFO, LOGFILTER, "OnDPSMsgSendRet NULL == tx_service");
		return;
	}

	bool needRelease = false;
	JNIEnv *env = Util_CreateEnv(&needRelease);
	if(!env) return;

	jclass cls = env->GetObjectClass(tx_service);
	jmethodID methodID = env->GetMethodID(cls, "OnRichMsgSendRet", "(II)V");
	if (methodID)
	{
		env->CallVoidMethod(tx_service, methodID, cookie, err_code);
	}

	env->DeleteLocalRef(cls);
	if(needRelease) Util_ReleaseEnv();
}

JNIEXPORT jlong JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_nativeSendAudioMsg(JNIEnv *env,
    jclass,jint msg_id, jstring file_path, jint duration, jlongArray targetids)
{
	const char * pFilePath = env->GetStringUTFChars(file_path, 0);


	unsigned int cookie = 0;
	TXCA_SEND_MSG_NOTIFY notify = {0};
	notify.on_file_transfer_progress = OnDPSMsgFileProgress;
	notify.on_send_structuring_msg_ret = OnDPSMsgSendRet;

	STRUCTURING_MSG msg = {0};
	msg.file_path = (char*)pFilePath;
	msg.duration = duration;
	msg.msg_id = msg_id;
	msg.to_targetids_count = 0;
	msg.to_targetids = NULL;

	if(targetids != NULL)
	{
		msg.to_targetids_count = env->GetArrayLength(targetids);
		if (msg.to_targetids_count > 0) {
			msg.to_targetids = new unsigned long long[msg.to_targetids_count];
			memset(msg.to_targetids, 0, msg.to_targetids_count);

			env->GetLongArrayRegion(targetids, 0, msg.to_targetids_count, (jlong*)msg.to_targetids);
		}
	}

	txca_send_structuring_msg(&msg, &notify, &cookie);

	if (msg.to_targetids)
	{
		delete[] msg.to_targetids;
	}

	env->ReleaseStringUTFChars(file_path, pFilePath);
	return cookie;
}

#ifdef __cplusplus
}
#endif
