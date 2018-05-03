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

static jmethodID s_onSendVideoCall = NULL;
static jmethodID s_onSendVideoCallM2M = NULL;
static jmethodID s_onSendVideoCMD = NULL;
static jmethodID s_onReceiveVideoBuffer = NULL;
static jmethodID s_onReceiveQQCallReply = NULL;

/*
 * Class:     com_tencent_device_TXDeviceSDK
 * Method:    nativeGetVideoChatSignature
 * Signature: ()J
 */
JNIEXPORT jbyteArray JNICALL 
Java_com_tencent_xiaowei_sdk_XWSDKJNI_nativeGetVideoChatSignature(JNIEnv * env, jclass cls)
{
	jbyteArray jbuf = NULL;

	int uLen = 0;
	int err = tx_get_video_chat_signature(NULL, &uLen);
	if (err_buffer_notenough == err && uLen > 0)
	{
		char * pBuf = new char[uLen];
		if (pBuf)
		{
			memset(pBuf, 0, uLen);
			int err = tx_get_video_chat_signature(pBuf, &uLen);
			if (err == err_null)
			{
				jbuf=env->NewByteArray(uLen);
				env->SetByteArrayRegion(jbuf, 0, uLen, (jbyte*)pBuf);
			}
			delete [] pBuf;
			pBuf = NULL;
		}
	}

	return jbuf;
}

JNIEXPORT jint JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_statisticsPoint(
	JNIEnv *env, jclass, jstring compass_name, jstring event, jstring  param, jlong time) {
    __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER, "statisticsPoint");
    const char *pVal_compass_name = NULL;
    if (compass_name) {
        pVal_compass_name = env->GetStringUTFChars(compass_name, 0);
    }
    const char *pVal_event = NULL;
    if (event) {
        pVal_event = env->GetStringUTFChars(event, 0);
    }
    const char *pVal_param = NULL;
    if (param) {
        pVal_param = env->GetStringUTFChars(param, 0);
    }

    int nRet = tx_ai_audio_statistics_point(pVal_compass_name, pVal_event, pVal_param, time);

    __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER, "statisticsPoint: ret %d.", nRet);

    if (pVal_compass_name) {
        env->ReleaseStringUTFChars(compass_name, pVal_compass_name);
    }
    if (pVal_event) {
         env->ReleaseStringUTFChars(event, pVal_event);
     }
    if (pVal_param) {
        env->ReleaseStringUTFChars(param, pVal_param);
    }
    return nRet;
}

static void VideoCallBack(char * pBufReply, int nLenReply, jmethodID methodID)
{
	__android_log_print(ANDROID_LOG_INFO, LOGFILTER, "video reply buffer_length:%d", nLenReply);

	bool needRelease = false;
	JNIEnv *env = Util_CreateEnv(&needRelease);
	if(!env) return;
	if (methodID && tx_service)
	{
		jbyteArray jbuf=env->NewByteArray(nLenReply);
		env->SetByteArrayRegion(jbuf, 0, nLenReply, (jbyte*)pBufReply);
		env->CallVoidMethod(tx_service, methodID,jbuf,nLenReply);
		env->DeleteLocalRef(jbuf);
	}

	if(needRelease) Util_ReleaseEnv();
}

//发送Video请求
static void SendVideoBuffer(JNIEnv * env,jlong todin, jint uinType, jbyteArray data, on_receive_video_reply callBack)
{
	jbyte *buff  = env->GetByteArrayElements(data,0);
	jsize uLen = env->GetArrayLength(data);
	if (uLen > 0) {
		__android_log_print(ANDROID_LOG_INFO, LOGFILTER, "video request buffer_length:%d", uLen);
		tx_send_video_request(uinType,todin, (const char *) buff, uLen, callBack);
	}
	env->ReleaseByteArrayElements(data,buff,0);
}

void on_receive_qqcall_push(const char * pBuf, int uLen, unsigned long long sendUin, int sendUinType){
    __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER,
                        "on_receive_qqcall_push: sendUin %llu, sendUinType %d uLen %d", sendUin, sendUinType, uLen);

    //TODO: callback to 
    bool needRelease = false;
    JNIEnv *env = Util_CreateEnv(&needRelease);
    if(!env) return;

    if (s_onReceiveVideoBuffer == NULL && tx_service)
    {
        jclass cls = env->GetObjectClass(tx_service);
        s_onReceiveVideoBuffer = env->GetMethodID(cls, "onReceiveVideoBuffer", "([BJI)V");
    }

    if (s_onReceiveVideoBuffer && tx_service)
    {
        jbyteArray jbuf=env->NewByteArray(uLen);
        env->SetByteArrayRegion(jbuf, 0, uLen, (jbyte*)pBuf);

        env->CallVoidMethod(tx_service, s_onReceiveVideoBuffer,jbuf, sendUin, sendUinType);

        env->DeleteLocalRef(jbuf);
    }

    if(needRelease) Util_ReleaseEnv();
}

void on_receive_qqcall_reply(const char * pBufReply, int nLenReply)
{
    __android_log_print(ANDROID_LOG_DEBUG, LOGFILTER, "on_receive_qqcall_reply length:%d", nLenReply);

    bool needRelease = false;
    JNIEnv *env = Util_CreateEnv(&needRelease);
    if(!env) return;

    if (s_onReceiveQQCallReply == NULL && tx_service)
    {
        jclass cls = env->GetObjectClass(tx_service);
        s_onReceiveQQCallReply = env->GetMethodID(cls, "onReceiveQQCallReply", "([BI)V");
    }

    if (s_onReceiveQQCallReply && tx_service)
    {
        jbyteArray jbuf=env->NewByteArray(nLenReply);
        env->SetByteArrayRegion(jbuf, 0, nLenReply, (jbyte*)pBufReply);

        env->CallVoidMethod(tx_service, s_onReceiveQQCallReply, jbuf, nLenReply);

        env->DeleteLocalRef(jbuf);
    }

    if(needRelease) Util_ReleaseEnv();
}

/////////////////////////////////////////////////////////////////////////////
//Video Push的请求
void on_receive_video_push(char * pBuf, int uLen, unsigned long long sendUin, int sendUinType)
{
	__android_log_print(ANDROID_LOG_INFO, LOGFILTER, "on_receive_video_push buffer_length:%d", uLen);

	bool needRelease = false;
	JNIEnv *env = Util_CreateEnv(&needRelease);
	if(!env) return;

	if (s_onReceiveVideoBuffer == NULL && tx_service)
	{
		jclass cls = env->GetObjectClass(tx_service);
		s_onReceiveVideoBuffer = env->GetMethodID(cls, "onReceiveVideoBuffer", "([BJI)V");
	}

	if (s_onReceiveVideoBuffer && tx_service)
	{
		jbyteArray jbuf=env->NewByteArray(uLen);
		env->SetByteArrayRegion(jbuf, 0, uLen, (jbyte*)pBuf);

		env->CallVoidMethod(tx_service, s_onReceiveVideoBuffer,jbuf, sendUin, sendUinType);

		env->DeleteLocalRef(jbuf);
	}

	if(needRelease) Util_ReleaseEnv();
}
/////////////////////////////////////////////////////////////////////////////
static void SendVideoCallCallBack(char * pBufReply, int nLenReply)
{
	VideoCallBack(pBufReply, nLenReply,s_onSendVideoCall);
}
/*
 * Class:     com_tencent_device_TXDeviceSDK
 * Method:    nativeSendVideoCall
 * Signature: (J[B)V
 */
JNIEXPORT void JNICALL 
Java_com_tencent_xiaowei_sdk_XWSDKJNI_nativeSendVideoCall(JNIEnv * env, jclass cls, jlong todin, jint uinType, jbyteArray data)
{
	if (s_onSendVideoCall == NULL)
	{
		s_onSendVideoCall = env->GetMethodID(cls, "onSendVideoCall", "([B)V");
	}
	SendVideoBuffer(env, todin, uinType, data, SendVideoCallCallBack);
}
/////////////////////////////////////////////////////////////////////////////
static void SendVideoCMDCallBack(char * pBufReply, int nLenReply)
{
	VideoCallBack(pBufReply, nLenReply, s_onSendVideoCMD);
}
/*
 * Class:     com_tencent_device_TXDeviceSDK
 * Method:    nativeSendVideoCMD
 * Signature: (J[B)V
 */
JNIEXPORT void JNICALL 
Java_com_tencent_xiaowei_sdk_XWSDKJNI_nativeSendVideoCMD(JNIEnv * env, jclass cls, jlong todin, jint uinType, jbyteArray data)
{
	if (s_onSendVideoCMD == NULL)
	{
		s_onSendVideoCMD = env->GetMethodID(cls, "onSendVideoCMD", "([B)V");
	}
	SendVideoBuffer(env, todin, uinType, data, SendVideoCMDCallBack);
}
/////////////////////////////////////////////////////////////////////////////
static void SendVideoCallM2MCallBack(char * pBufReply, int nLenReply)
{
	VideoCallBack(pBufReply, nLenReply,s_onSendVideoCallM2M);
}
/*
 * Class:     com_tencent_device_TXDeviceSDK
 * Method:    nativeSendVideoCallM2M
 * Signature: (J[B)V
 */
JNIEXPORT void JNICALL 
Java_com_tencent_xiaowei_sdk_XWSDKJNI_nativeSendVideoCallM2M(JNIEnv * env, jclass cls, jlong todin, jint uinType, jbyteArray data)
{
	if (s_onSendVideoCallM2M == NULL)
	{
		s_onSendVideoCallM2M	= env->GetMethodID(cls, "onSendVideoCallM2M", "([B)V");
	}
	SendVideoBuffer(env, todin, uinType, data, SendVideoCallM2MCallBack);

}

#ifdef __cplusplus
}
#endif
