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

import com.tencent.xiaowei.info.XWFileTransferInfo;
import com.tencent.xiaowei.util.QLog;

import java.util.HashMap;

/**
 * 内部使用的文件传输通道
 */
class XWFileTransferManager {

    public static final String TAG = "XWFileTransferManager";

    private static HashMap<Long, XWSDK.OnFileTransferListener> mOnFileTransferListenerMap = new HashMap<>();

    private static HashMap<Long, XWFileTransferInfo> failedCookie = new HashMap<>();
    private static HashMap<XWFileTransferInfo, Integer> failedCode = new HashMap<>();

    private static XWSDK.OnAutoDownloadCallback mAutoDownloadCallback = null;



    static void onFileTransferProgress(long cookie, final long transferProgress, final long maxTransferProgress) {
        final XWSDK.OnFileTransferListener listener = mOnFileTransferListenerMap.get(cookie);
        if (listener != null) {
            listener.onProgress(transferProgress, maxTransferProgress);
        }
    }

    static void onFileTransferComplete(final XWFileTransferInfo info, final int errorCode) {
        QLog.d(TAG, "onFileTransferComplete " + info + " " + errorCode);
        final XWSDK.OnFileTransferListener listener = mOnFileTransferListenerMap.remove(info.id);
        if (listener != null) {
            XWSDKJNI.postMain(new Runnable() {
                @Override
                public void run() {
                    listener.onComplete(info, errorCode);
                }
            });
        } else {
            failedCookie.put(info.id, info);
            failedCode.put(info, errorCode);
        }
    }

    static int onAutoDownloadFileCallback(final long size, final int channel_type) {
        QLog.d(TAG, "onAutoDownloadFileCallback size:" + size + " channel: " + channel_type);
        if (mAutoDownloadCallback != null)
            return mAutoDownloadCallback.onDownloadFile(size, channel_type);
        return 0;
    }

    public static void setAutoDownloadCallback(XWSDK.OnAutoDownloadCallback cb) {
        mAutoDownloadCallback = cb;
    }

    /**
     * 上传文件
     *
     * @param filePath
     * @param channelType 传输通道类型
     * @param fileType    输文件类型
     * @return 返回的cookie, 在传输相关的回调中作为参数，也可以cancelTransfer
     */
    public static long uploadFile(String filePath, int channelType, int fileType, XWSDK.OnFileTransferListener listener) {
        long cookie = XWSDKJNI.uploadFile(filePath, channelType, fileType);
        if (listener != null) {
            if (failedCookie.containsKey(cookie)) {
                XWFileTransferInfo info = failedCookie.remove(cookie);
                listener.onComplete(info, failedCode.remove(info));
            } else {
                mOnFileTransferListenerMap.put(cookie, listener);
            }
        }
        return cookie;
    }

    /**
     * 下载小文件
     *
     * @param fileKey
     * @param fileType
     * @param miniToken
     * @param listener
     * @return
     */
    public static long downloadMiniFile(String fileKey, int fileType, String miniToken, XWSDK.OnFileTransferListener listener) {
        long cookie = XWSDKJNI.downloadMiniFile(fileKey, fileType, miniToken);
        if (listener != null) {
            if (failedCookie.containsKey(cookie)) {
                XWFileTransferInfo info = failedCookie.remove(cookie);
                listener.onComplete(info, failedCode.remove(info));
            } else {
                mOnFileTransferListenerMap.put(cookie, listener);
            }
        }
        return cookie;
    }

    /**
     * 取消传输
     *
     * @param id TXFileTransferInfo的id
     */
    public static void cancelTransfer(long id) {
        XWSDKJNI.cancelTransfer(id);
    }


    //*****************************文件 End *****************************//

    /**
     * 获得小文件通道的url
     *
     * @param fileKey
     * @param fileType
     * @return
     */
    public static String getMiniDownloadURL(String fileKey, int fileType) {
        return XWSDKJNI.getMiniDownloadURL(fileKey, fileType);
    }

}
