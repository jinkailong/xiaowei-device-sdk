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
package com.tencent.xiaowei.sdk;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.tencent.xiaowei.info.XWLoginInfo;
import com.tencent.xiaowei.util.QLog;


public class XWCoreService extends Service {

    static String TAG = "XWCoreService";

    public static XWLoginInfo mXWLoginInfo;

    @Override
    public void onCreate() {
        super.onCreate();
        QLog.i(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        QLog.i(TAG, "onStartCommand");

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        QLog.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 初始化SDK
     *
     * @param context
     * @param loginInfo 登录信息
     * @return 错误码
     */
    public static int init(Context context, XWLoginInfo loginInfo) {
        return initEx(context, loginInfo, 0, 5);
    }

    /**
     * * 初始化SDK
     *
     * @param context       上下文对象
     * @param loginInfo     登录信息
     * @param testMode      是否连接测试环境, 0为不连接测试环境，1为连接测试环境
     * @param nativeLogLevel 控制打印native 的日志级别 取值[0-5],0表示关闭日志，1-5对应[e,w,i,d,v]。数字越大打印的日志级别越多。
     * @return 错误码
     * 0 成功，
     * err_failed                              = 0x00000001,       //failed 关键Service等对象不存在
     * err_unknown                             = 0x00000002,       //未知错误
     * err_invalid_param                       = 0x00000003,       //参数非法
     * err_mem_alloc                           = 0x00000005,       //分配内存失败
     * err_internal                            = 0x00000006,       //内部错误
     * err_device_inited                       = 0x00000007,       //设备已经初始化过了
     * err_invalid_device_info                 = 0x00000009,       //非法的设备信息
     * err_invalid_serial_number               = 0x0000000A,       //(10)      非法的设备序列号
     * err_invalid_system_path                 = 0x0000000E,       //(14)      非法的system_path
     * err_invalid_app_path                    = 0x0000000F,       //(15)      非法的app_path
     * err_invalid_temp_path                   = 0x00000010,       //(16)      非法的temp_path
     * err_invalid_device_name                 = 0x00000015,       //(21)      设备名没填，或者长度超过32byte
     * err_invalid_os_platform                 = 0x00000016,       //(22)      系统信息没填，或者长度超过32byte
     * err_invalid_license                     = 0x00000017,       //(23)      license没填，或者长度超过150byte
     * err_invalid_server_pub_key              = 0x00000018,       //(24)      server_pub_key没填，或者长度超过120byte
     * err_invalid_product_version             = 0x00000019,       //(25)      product_version非法
     * err_invalid_product_id                  = 0x0000001A,       //(26)      product_id非法
     * err_sys_path_access_permission          = 0x0000001D,       //(29)      system_path没有读写权限
     * err_invalid_network_type				= 0x0000001E,		//(30)		初始化时传入的网络类型非法
     * err_invalid_run_mode					= 0x0000001F,		//(31)      初始化时传入的SDK运行模式非法
     */
    public static int initEx(Context context, XWLoginInfo loginInfo, int testMode, int nativeLogLevel) {
        if (context == null) {
            throw new RuntimeException("Init XWSDK failed,context is null.");
        }
        // 先检查一遍loginInfo的基本格式
        if (!checkLoginInfo(loginInfo)) {
            return 3;
        }

        mXWLoginInfo = loginInfo;
        QLog.init(context, context.getPackageName());
        XWSDKJNI.getInstance().initJNI(nativeLogLevel);


        int ret = XWSDKJNI.getInstance().init(loginInfo.deviceName, loginInfo.license.getBytes(), loginInfo.serialNumber, loginInfo.srvPubKey, loginInfo.productId, loginInfo.productVersion, loginInfo.networkType, loginInfo.runMode,
                loginInfo.sysPath, loginInfo.sysCapacity, loginInfo.appPath, loginInfo.appCapacity, loginInfo.tmpPath, loginInfo.tmpCapacity, testMode);

        if (ret == 0) {
            Intent intent = new Intent(context, XWCoreService.class);
            context.startService(intent);
            int[] versions = XWSDKJNI.getInstance().getSDKVersion();
            QLog.setBuildNumber(versions[0] + "." + versions[1] + "." + versions[2]);
        }
        return ret;
    }

    private static boolean checkLoginInfo(XWLoginInfo loginInfo) {
        if (loginInfo == null) {
            QLog.e(TAG, "loginInfo is null.");
            return false;
        }
        if (TextUtils.isEmpty(loginInfo.serialNumber) || loginInfo.serialNumber.length() != 16) {
            QLog.e(TAG, "serialNumber is invalid.");
            return false;
        }
        return true;
    }

    public static void unInit(Context context) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, XWCoreService.class);
        context.stopService(intent);
    }

}
