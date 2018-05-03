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

import com.tencent.xiaowei.info.XWLoginInfo;
import com.tencent.xiaowei.util.QLog;

/**
 * 查询官网配置平台配置的OTA信息
 */
public class XWOTAManager {

    public static final String TAG = "XWOTAManager";

    private static OnDeviceOTAEventListener mDeviceOTAEventListener;

    /**
     * 收到OTA更新
     */
    public interface OnDeviceOTAEventListener {

        /**
         * 收到OTA更新信息
         *
         * @param from    来源 0 定时自动检测 1 App操作 2 ServerPush 3 设备主动查询
         * @param force   是否强制升级
         * @param version 版本号 {@link XWLoginInfo#productVersion}
         * @param title   更新标题
         * @param desc    更新详情
         * @param url     下载链接
         * @param md5     文件md5
         */
        void onOTAInfo(int from, boolean force, int version, String title, String desc, String url, String md5);
    }


    /**
     * 设备端主动查询OTA升级信息，只有在有更新的时候才会回调{@link OnDeviceOTAEventListener}
     */
    public static int queryOtaUpdate() {
        QLog.d(TAG, "queryOtaUpdate");
        return XWSDKJNI.queryOtaUpdate();
    }

    /**
     * 设置OTA更新的事件监听器
     *
     * @param listener {@link OnDeviceOTAEventListener}
     */
    public static void setOnDeviceOTAEventListener(OnDeviceOTAEventListener listener) {
        mDeviceOTAEventListener = listener;
    }

    static void onOTAInfo(int from, boolean force, int version, String title, String desc, String url, String md5) {
        QLog.d(TAG, "onOTAInfo from:" + from + " force:" + force + " version:" + version + " title:" + title + " desc:" + desc + " url:" + url);
        if (mDeviceOTAEventListener != null) {
            mDeviceOTAEventListener.onOTAInfo(from, force, version, title, desc, url, md5);
        }
    }

}
