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

#ifdef JNI_VERSION_1_4
#define JNI_VER JNI_VERSION_1_4
#endif

#ifdef JNI_VERSION_1_5
#undef JNI_VER
#define JNI_VER JNI_VERSION_1_5
#endif
#ifdef JNI_VERSION_1_6
#undef JNI_VER
#define JNI_VER JNI_VERSION_1_6
#endif


#ifdef __cplusplus
extern "C" {
#endif

static JavaVM *g_JVM = NULL;

jobject tx_service = NULL;
jclass s_serviceClass = NULL;
jclass s_clsBinderInfo = NULL;
jclass s_clsTransferInfo = NULL;
jclass s_clsAILog = NULL;
jclass s_clsAIC2CMsg = NULL;

//new audio interface
jclass s_clsAudioAccount = NULL;
jclass s_clsLoaction = NULL;
jclass s_clsAudioAppInfo = NULL;
jclass s_clsAudioContext = NULL;
jclass s_clsAudioDeviceInfo = NULL;
jclass s_clsAudioResource = NULL;
jclass s_clsAudioResGroup = NULL;
jclass s_clsAudioResponse = NULL;
jclass s_clsAudioState = NULL;
jclass s_clsTXLoginStatusInfo = NULL;
jclass s_clsAudioTTSData = NULL;
jclass s_clsBinderRemark = NULL;

int g_log_level = 5;// 允许回调<=这个级别的日志到java层，0 表示不回调

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    __android_log_print(ANDROID_LOG_INFO, LOGFILTER, "JNI_OnLoad");
    g_JVM = vm;
    return JNI_VERSION_1_4;
}

JNIEnv *Util_CreateEnv(bool *pNeedRelease) {
    if (pNeedRelease) *pNeedRelease = false;
    if (!g_JVM) {
        __android_log_write(ANDROID_LOG_ERROR, LOGFILTER, "JVM is NULL, no JVM yet");
        return NULL;
    }
    JNIEnv *env = NULL;
    if (JNI_OK != g_JVM->GetEnv(reinterpret_cast<void **> (&env), JNI_VER)) {
        if (JNI_OK == g_JVM->AttachCurrentThread(&env, NULL)) {
            if (pNeedRelease) *pNeedRelease = true;
        } else {
            __android_log_write(ANDROID_LOG_ERROR, LOGFILTER, "JVM could not create JNI env");
        }
    }
    return env;
}

void Util_ReleaseEnv() {
    if (g_JVM) {
        if (JNI_OK != g_JVM->DetachCurrentThread()) {
            __android_log_write(ANDROID_LOG_ERROR, LOGFILTER, "JVM could not release JNI env");
        }
    }
}


char checkUtfBytes(const char *bytes, const char **errorKind) {
    while (*bytes != '\0') {
        char utf8 = *(bytes++);
        // Switch on the high four bits.
        switch (utf8 >> 4) {
            case 0x00:
            case 0x01:
            case 0x02:
            case 0x03:
            case 0x04:
            case 0x05:
            case 0x06:
            case 0x07:
                // Bit pattern 0xxx. No need for any extra bytes.
                break;
            case 0x08:
            case 0x09:
            case 0x0a:
            case 0x0b:
            case 0x0f:
                /*
                 * Bit pattern 10xx or 1111, which are illegal start bytes.
                 * Note: 1111 is valid for normal UTF-8, but not the
                 * modified UTF-8 used here.
                 */
                *errorKind = "start";
                return utf8;
            case 0x0e:
                // Bit pattern 1110, so there are two additional bytes.
                utf8 = *(bytes++);
                if ((utf8 & 0xc0) != 0x80) {
                    *errorKind = "continuation";
                    return utf8;
                }
                // Fall through to take care of the final byte.
            case 0x0c:
            case 0x0d:
                // Bit pattern 110x, so there is one additional byte.
                utf8 = *(bytes++);
                if ((utf8 & 0xc0) != 0x80) {
                    *errorKind = "continuation";
                    return utf8;
                }
                break;
        }
    }
    return 0;
}

//log_func native日志都在这里输出
void log_func(int level, const char *module, int line, const char *message) {
//    int logLevel = ANDROID_LOG_VERBOSE;
//    if (level == 0) {
//        logLevel = ANDROID_LOG_ERROR;
//    } else if (level == 1) {
//        logLevel = ANDROID_LOG_ERROR;
//    } else if (level == 2) {
//        logLevel = ANDROID_LOG_WARN;
//    } else if (level == 3) {
//        logLevel = ANDROID_LOG_INFO;
//    } else if (level == 4) {
//        logLevel = ANDROID_LOG_DEBUG;
//    }

    if(level > g_log_level || g_log_level == 0) {
        return;
    }

    bool needRelease = false;
    JNIEnv *env = Util_CreateEnv(&needRelease);
    if (!env) return;

    if (s_serviceClass) {
        static jmethodID s_onGetSDKLog = NULL;
        if (s_onGetSDKLog == NULL) {
            s_onGetSDKLog = env->GetMethodID(s_serviceClass, "onGetSDKLog",
                                             "(ILjava/lang/String;ILjava/lang/String;)V");
        }

        if (s_onGetSDKLog) {
            jstring jModule;
            ConvChar2JString(env, module, jModule);
            jstring jMessage;
            ConvChar2JString(env, message, jMessage);
            env->CallVoidMethod(tx_service, s_onGetSDKLog, level, jModule, line, jMessage);
            env->DeleteLocalRef(jModule);
            env->DeleteLocalRef(jMessage);
        }
    }

    if (needRelease) Util_ReleaseEnv();

}

//设备登录过程的关键事件回调函数
extern void on_login_complete(int errcode);
extern void on_online_status(int old, int newStatus);
extern void on_binder_list_change(int error, TX_BINDER_INFO *pBinderList, int nCount);
extern void on_binder_remark_change_callback(int cookie, TX_BINDER_REMARK_INFO * pBinderRemarkList, int nCount);
extern void on_wlan_upload_register_info_success(int errcode);
extern void on_connected_server(int error_code);
extern void on_register(int error_code, int sub_error_code);

extern void on_receive_video_push(char * pBuf, int uLen, unsigned long long sendUin, int sendUinType);

//文件传输的相关回调
extern void
on_transfer_progress(unsigned long long transfer_cookie, unsigned long long transfer_progress,
                     unsigned long long max_transfer_progress);
extern void on_transfer_complete(unsigned long long transfer_cookie, int err_code,
                                 TXCA_FILE_TRANSFER_INFO *fnInfo);

extern int on_auto_download_callback(unsigned long long file_size, unsigned int channel_type);

// 查询OTA的回调
extern void
on_ota_info(int from, bool force, unsigned int version, const char *title, const char *desc,
            const char *url, const char *md5);

/**
 * 回应App配网的网络信息
 * @param env
 * @param ip
 * @param port
 */
JNIEXPORT void JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_ackApp(JNIEnv *env, jclass, jint ip, jint port) {
    tx_ack_app(ip, port);
}

/**
 * 获得sdk版本
 * @param env
 * @return
 */
JNIEXPORT jintArray JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_getSDKVersion(JNIEnv *env, jclass) {
    jintArray arr = env->NewIntArray(3);

    unsigned int main_ver = 0;
    unsigned int sub_ver = 0;
    unsigned int build_no = 0;
    tx_get_sdk_version(&main_ver, &sub_ver, &build_no);

    __android_log_print(ANDROID_LOG_ERROR, LOGFILTER, "getSDKVersion: %d %d %d", main_ver, sub_ver,
                        build_no);

    jint vers[3] = {main_ver, sub_ver, build_no};

    env->SetIntArrayRegion(arr, 0, 3, vers);

    return arr;
}

JNIEXPORT void JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_initJNI(JNIEnv *env, jobject service, jint log_level) {
    g_log_level = log_level;

    if (tx_service == NULL) {
        tx_service = env->NewGlobalRef(service);
        jclass cls = env->GetObjectClass(tx_service);
        s_serviceClass = (jclass) env->NewGlobalRef(cls);
        jclass clsBinderInfo = env->FindClass("com/tencent/xiaowei/info/XWBinderInfo");
        s_clsBinderInfo = (jclass) env->NewGlobalRef(clsBinderInfo);
        jclass clsTransferInfo = env->FindClass("com/tencent/xiaowei/info/XWFileTransferInfo");
        s_clsTransferInfo = (jclass) env->NewGlobalRef(clsTransferInfo);

        jclass clsAILog = env->FindClass("com/tencent/xiaowei/info/XWEventLogInfo");
        s_clsAILog = (jclass) env->NewGlobalRef(clsAILog);

        jclass clsAIC2CMsg = env->FindClass("com/tencent/xiaowei/info/XWCCMsgInfo");
        s_clsAIC2CMsg = (jclass) env->NewGlobalRef(clsAIC2CMsg);

        jclass clsLoaction = env->FindClass("com/tencent/xiaowei/info/XWLocationInfo");
        s_clsLoaction = (jclass) env->NewGlobalRef(clsLoaction);

        jclass clsAudioAccount = env->FindClass("com/tencent/xiaowei/info/XWAccountInfo");
        s_clsAudioAccount = (jclass) env->NewGlobalRef(clsAudioAccount);
        jclass clsAudioAppInfo = env->FindClass("com/tencent/xiaowei/info/XWAppInfo");
        s_clsAudioAppInfo = (jclass) env->NewGlobalRef(clsAudioAppInfo);
        jclass clsAudioContext = env->FindClass("com/tencent/xiaowei/info/XWContextInfo");
        s_clsAudioContext = (jclass) env->NewGlobalRef(clsAudioContext);
        jclass clsAudioDeviceInfo = env->FindClass("com/tencent/xiaowei/info/XWDeviceInfo");
        s_clsAudioDeviceInfo = (jclass) env->NewGlobalRef(clsAudioDeviceInfo);
        jclass clsAudioResource = env->FindClass("com/tencent/xiaowei/info/XWResourceInfo");
        s_clsAudioResource = (jclass) env->NewGlobalRef(clsAudioResource);
        jclass clsAudioResGroup = env->FindClass("com/tencent/xiaowei/info/XWResGroupInfo");
        s_clsAudioResGroup = (jclass) env->NewGlobalRef(clsAudioResGroup);
        jclass clsAudioResponse = env->FindClass("com/tencent/xiaowei/info/XWResponseInfo");
        s_clsAudioResponse = (jclass) env->NewGlobalRef(clsAudioResponse);
        jclass clsAudioState = env->FindClass("com/tencent/xiaowei/info/XWPlayStateInfo");
        s_clsAudioState = (jclass) env->NewGlobalRef(clsAudioState);

        jclass clsTXLoginStatusInfo = env->FindClass("com/tencent/xiaowei/info/XWLoginStatusInfo");
        s_clsTXLoginStatusInfo = (jclass) env->NewGlobalRef(clsTXLoginStatusInfo);

        jclass clsAudioTTSData = env->FindClass("com/tencent/xiaowei/info/XWTTSDataInfo");
        s_clsAudioTTSData = (jclass) env->NewGlobalRef(clsAudioTTSData);

        jclass clsBinderRemark = env->FindClass("com/tencent/xiaowei/info/XWBinderRemark");
        s_clsBinderRemark = (jclass)env->NewGlobalRef(clsBinderRemark);

    }
}

/*
 * Class:     com_tencent_device_XWSDK
 * Method:    init 初始化
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT jint JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_init
        (JNIEnv *env, jobject service, jstring strDeviceName, jbyteArray license,
         jstring strSerialNum, jstring strSrvPubkey, jlong ddwProductID, jint iProductVersion,
         jint iNetworkType, jint iRunMode,
         jstring strSysPath, jlong ddwSysCapacity, jstring strAppPath, jlong ddwAppCapacity,
         jstring strTmpPath, jlong ddwTmpCapacity, jint testMode) {
    if (NULL == strDeviceName || NULL == license || NULL == strSerialNum || NULL == strSysPath ||
        NULL == strAppPath || NULL == strTmpPath) {
        __android_log_print(ANDROID_LOG_INFO, LOGFILTER, "Invalid Init Parmas");
        return 3;// 参数非法
    }

    if (service == NULL) {
        __android_log_print(ANDROID_LOG_INFO, LOGFILTER,
                            "The Android Service(XWSDK) hasn't been started");
        return 1;
    }

    if (tx_service == NULL) {
        Java_com_tencent_xiaowei_sdk_XWSDKJNI_initJNI(env, service, g_log_level);
    }

    //获取参数
    const char *device_name = env->GetStringUTFChars(strDeviceName, 0);
    const char *serial_num = env->GetStringUTFChars(strSerialNum, 0);
    const char *svrPubKey = env->GetStringUTFChars(strSrvPubkey, 0);
    const char *sys_path = env->GetStringUTFChars(strSysPath, 0);
    const char *app_path = env->GetStringUTFChars(strAppPath, 0);
    const char *tmp_path = env->GetStringUTFChars(strTmpPath, 0);

    int license_length = env->GetArrayLength(license);
    char *license_buf = new char[license_length + 1];
    memset(license_buf, 0, license_length + 1);
    if (license_buf) {
        env->GetByteArrayRegion(license, 0, license_length, (jbyte *) license_buf);
    }

    char szSerialNum[9] = {0};
    char szLicense[21] = {0};
    memcpy(szSerialNum, serial_num, strlen(serial_num) > 8 ? 8 : strlen(serial_num));
    memcpy(szLicense, license_buf, license_length > 20 ? 20 : license_length);
    __android_log_print(ANDROID_LOG_INFO, LOGFILTER, "serial_num = %s********", szSerialNum);

    TX_DEVICE_INFO info = {0};
    info.os_platform = (char *) "Android";
    info.network_type = iNetworkType;
    info.device_name = (char *) device_name;
    info.device_serial_number = (char *) serial_num;
    info.device_license = (char *) license_buf;
    info.product_version = iProductVersion;
    info.test_mode = testMode;
    info.run_mode = iRunMode;

    info.product_id = ddwProductID;
    info.server_pub_key = (char *) svrPubKey;

    TX_DEVICE_NOTIFY notify = {0};
    notify.on_login_complete = on_login_complete;
    notify.on_online_status = on_online_status;
    notify.on_binder_list_change = on_binder_list_change;
    notify.on_wlan_upload_register_info_success = on_wlan_upload_register_info_success;
    notify.on_binder_remark_change = on_binder_remark_change_callback;
    notify.on_connected_server      = on_connected_server;
    notify.on_register      = on_register;

    TX_INIT_PATH init_path = {0};
    init_path.system_path = (char *) sys_path;
    init_path.system_path_capicity = ddwSysCapacity;
    init_path.app_path = (char *) app_path;
    init_path.app_path_capicity = ddwAppCapacity;
    init_path.temp_path = (char *) tmp_path;
    init_path.temp_path_capicity = ddwTmpCapacity;

    //设置Log回调
    tx_set_log_func(log_func, 1, g_log_level >= 4);


	// 设置Android视频通话相关的回调
	tx_av_chat_notify av_notify = {0};
	av_notify.on_receive_video_push   = on_receive_video_push;
	tx_set_av_chat_notify(&av_notify);

    //初始化设备
    int ret = tx_init_device(&info, &notify, &init_path);
    if (err_null == ret) {
        __android_log_print(ANDROID_LOG_INFO, LOGFILTER, "tx_init_device success\n");

        //初始化传文件通道
        TXCA_FILE_TRANSFER_NOTIFY file_notify = {0};
        file_notify.on_transfer_progress = on_transfer_progress;
        file_notify.on_transfer_complete = on_transfer_complete;
        txca_init_file_transfer(file_notify, (char *) tmp_path);

        txca_set_auto_download_callbak(on_auto_download_callback);
    } else {
        __android_log_print(ANDROID_LOG_INFO, LOGFILTER, "tx_init_device failed [%d]\n", ret);
    }

    if (err_null == ret) {
        TX_OTA_RESULT result = {0};
        result.on_ota_result = on_ota_info;
        tx_config_ota(&result);
    }

    env->ReleaseStringUTFChars(strDeviceName, device_name);
    env->ReleaseStringUTFChars(strSerialNum, serial_num);
    env->ReleaseStringUTFChars(strSrvPubkey, svrPubKey);
    env->ReleaseStringUTFChars(strSysPath, sys_path);
    env->ReleaseStringUTFChars(strAppPath, app_path);
    env->ReleaseStringUTFChars(strTmpPath, tmp_path);

    if (license_buf) {
        delete[] license_buf;
    }
    return ret;
}

/**
 * 重新连接服务器
 * @param env
 * @param service
 */
JNIEXPORT void JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_deviceReconnect(JNIEnv *env, jobject service) {
    tx_device_reconnect();
}

/**
 * 获得服务器时间
 * @param env
 * @param service
 * @return
 */
JNIEXPORT jint JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_getServerTime(JNIEnv *env, jobject service) {
    return tx_get_server_time();
}

/**
 * 获得绑定二维码
 * @param env
 * @param cls
 * @return
 */
JNIEXPORT jstring JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_getQRCodeUrl
        (JNIEnv *env, jclass cls) {
    jstring jstrUrl;
    char szUrl[512] = {0};
    getQRCodeUrl(szUrl, sizeof(szUrl));
    ConvChar2JString(env, szUrl, jstrUrl);
    return jstrUrl;
}

/*
 * Class:     com_tencent_device_XWSDK
 * Method:    getSelfDin 或者自己的din
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_tencent_xiaowei_sdk_XWSDKJNI_getSelfDin
        (JNIEnv *env, jclass cls) {
    unsigned long long ddwDin = tx_get_self_din();
    return ddwDin;
}

/**
 * 获得主人信息
 * @param env
 * @param service
 * @return
 */
JNIEXPORT jlongArray JNICALL
Java_com_tencent_xiaowei_sdk_XWSDKJNI_getBinderAdminInfo(JNIEnv *env, jobject service) {
    jlongArray arr = env->NewLongArray(3);

    unsigned long long isBinder = 0;
    unsigned long long binderType = 0;
    unsigned long long uin = 0;


    int nCount = 0;
    int err = tx_get_binder_list(NULL, &nCount, NULL);
    if (err_buffer_notenough == err && nCount > 0) {
        TX_BINDER_INFO *pBinderList = new TX_BINDER_INFO[nCount];
        if (pBinderList) {
            memset(pBinderList, 0, nCount * sizeof(TX_BINDER_INFO));
            int err = tx_get_binder_list(pBinderList, &nCount, NULL);
            if (err_null == err) {
                for (int i = 0; i < nCount; ++i) {
                    if (pBinderList[i].type == binder_type_owner) {
                        isBinder = 1;
                        binderType = pBinderList[i].tinyid_type;
                        uin = pBinderList[i].uin;
                        break;
                    }
                }
            }
            delete[] pBinderList;
        }
    }


    jlong vers[3] = {isBinder, binderType, uin};

    env->SetLongArrayRegion(arr, 0, 3, vers);

    return arr;
}
#ifdef __cplusplus
}
#endif
