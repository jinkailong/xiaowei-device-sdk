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
#include "TXVoiceLink.h"
#include <arpa/inet.h>

#ifdef __cplusplus
extern "C" {
#endif


enum e_wifi_decoder_mode {
    WDM_VOICE_LINK = 0x1,
    WDM_SMART_LINK = 0x2,
    WDM_ALL = 0xfffffff,
};

void on_wifi_decoder_finish(char *ssid, char *pwd, int ip, int port);
void on_voice_link_notify(TX_VOICELINK_PARAM *pparam) {
    on_wifi_decoder_finish(pparam->sz_ssid, pparam->sz_password, inet_addr(pparam->sz_ip),
                           pparam->sh_port);
}

extern jobject tx_service;

JNIEXPORT int JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_startWifiDecoder
        (JNIEnv *env, jclass service, jstring serial_number, jint sample_rate, jint mode) {
    if (mode & WDM_VOICE_LINK) {
        /* code */
        tx_init_decoder(on_voice_link_notify, sample_rate);
    }

    if (mode & WDM_SMART_LINK) {

    }
    return 0;
}

JNIEXPORT void JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_fillVoiceWavData
        (JNIEnv *env, jclass, jbyteArray wav) {
    int nBufLen = 0;
    char *pBuffer = NULL;
    if (NULL != wav) {
        nBufLen = env->GetArrayLength(wav);
        if (nBufLen > 0) {
            pBuffer = new char[nBufLen];
            memset(pBuffer, 0, nBufLen);
            env->GetByteArrayRegion(wav, 0, nBufLen, (jbyte *) pBuffer);

            tx_fill_audio((short *) pBuffer, nBufLen / 2);
            delete[]pBuffer;
        } else {
            __android_log_print(ANDROID_LOG_INFO, LOGFILTER, "wav is NULL\n");
        }

    }
}


JNIEXPORT int JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_stopWifiDecoder
        (JNIEnv *env, jclass) {

    tx_uninit_decoder();
    return 0;
}


void on_wifi_decoder_finish(char *ssid, char *pwd, int ip, int port) {

    if (tx_service) {
        bool needRelease = false;
        JNIEnv *env = Util_CreateEnv(&needRelease);
        if (!env)
            return;

        jclass cls = env->GetObjectClass(tx_service);
        jmethodID methodID = NULL;

        methodID = env->GetMethodID(cls, "onReceiveWifiInfo",
                                    "(Ljava/lang/String;Ljava/lang/String;II)V");

        if (methodID) {

            jstring jssid;
            ConvChar2JString(env, ssid, jssid);

            jstring jpwd;
            ConvChar2JString(env, pwd, jpwd);

            env->CallVoidMethod(tx_service, methodID, jssid, jpwd, ip, port);
            env->DeleteLocalRef(jssid);
            env->DeleteLocalRef(jpwd);
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
