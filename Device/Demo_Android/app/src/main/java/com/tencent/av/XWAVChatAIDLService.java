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
package com.tencent.av;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;

import com.tencent.aiaudio.chat.AVChatManager;
import com.tencent.aiaudio.wakeup.RecordDataManager;
import com.tencent.xiaowei.info.XWAudioFrameInfo;
import com.tencent.xiaowei.info.XWBinderInfo;
import com.tencent.xiaowei.info.XWContactInfo;
import com.tencent.xiaowei.sdk.XWDeviceBaseManager;
import com.tencent.xiaowei.sdk.XWSDKJNI;
import com.tencent.xiaowei.util.QLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * 音视频通话相关的服务
 */
public class XWAVChatAIDLService extends Service {
    static final String TAG = "XWDeviceCoreService";

    public static boolean isFirst = true;
    public static boolean binder = false;
    private TXDeviceServiceBinder mServiceBinder = null;
    private static int mOffset;

    @Override
    public void onCreate() {
        super.onCreate();
        QLog.d(TAG, "onCreate");
        mServiceBinder = new TXDeviceServiceBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        QLog.d(TAG, "onStartCommand");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        QLog.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        QLog.d(TAG, "onBind");
        binder = true;
        return mServiceBinder;
    }

    public boolean onUnbind(Intent intent) {
        QLog.d(TAG, "onUnbind");
        binder = false;
        try {
            setVideoPID(0, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void onRebind(Intent intent) {
        QLog.d(TAG, "onRebind");
        binder = true;
        super.onRebind(intent);
    }

    /**
     * 跨进程AIDL实现
     */
    public class TXDeviceServiceBinder extends IXWAVChatAIDLService.Stub {
        @Override
        public long getSelfDin() throws RemoteException {
            long selfDin = XWDeviceBaseManager.getSelfDin();
            QLog.d(TAG, "getSelfDin:" + selfDin);
            return selfDin;
        }

        @Override
        public boolean isContact(long uin) {
            QLog.d(TAG, "isContact");
            return XWDeviceBaseManager.isContact(uin);
        }

        @Override
        public ArrayList<XWBinderInfo> getBinderList() throws RemoteException {
            QLog.d(TAG, "getBinderList");
            return XWSDKJNI.getBinderList();
        }

        @Override
        public XWContactInfo getXWContactInfo(String uin) throws RemoteException {
            QLog.d(TAG, "getXWContactInfo");
            return XWDeviceBaseManager.getXWContactInfo(uin);
        }

        @Override
        public void notifyVideoServiceStarted() throws RemoteException {
            QLog.d(TAG, "notifyVideoServiceStarted");
            AVChatManager.getInstance().invokePendingIntent();
        }

        @Override
        public byte[] getVideoChatSignature() throws RemoteException {
            QLog.d(TAG, "getVideoChatSignature");
            return XWSDKJNI.nativeGetVideoChatSignature();
        }

        @Override
        public void sendVideoCall(long peerUin, int uinType, byte[] msg)
                throws RemoteException {
            QLog.d(TAG, "sendVideoCall");
            XWSDKJNI.nativeSendVideoCall(peerUin, uinType, msg);
        }

        @Override
        public void sendVideoCallM2M(long peerUin, int uinType, byte[] msg)
                throws RemoteException {
            QLog.d(TAG, "sendVideoCallM2M");
            XWSDKJNI.nativeSendVideoCallM2M(peerUin, uinType, msg);
        }

        @Override
        public void sendVideoCMD(long peerUin, int uinType, byte[] msg)
                throws RemoteException {
            QLog.d(TAG, "sendVideoCMD peerUin:" + peerUin + " type:" + uinType);
            XWSDKJNI.nativeSendVideoCMD(peerUin, uinType, msg);
        }


        @Override
        public void setVideoPID(int pid, String videoService) throws RemoteException {
            QLog.d(TAG, "setVideoPID" + pid);
            XWAVChatAIDLService.setVideoPID(pid, videoService);

        }

        @Override
        public XWAudioFrameInfo readAudioData(int length) throws RemoteException {
            if (isTestDelay) {
                XWAudioFrameInfo info = read(length);
                return info;
            } else {
                byte[] buffer = new byte[length];
                int realLength = read(buffer, length);
                if (realLength == 0) {
                    return null;
                }
                XWAudioFrameInfo info = new XWAudioFrameInfo();
                info.length = realLength;
                info.data = buffer;
                return info;
            }
        }

        @Override
        public void startQQCallSkill(long uin) throws RemoteException {
            QLog.d(TAG, "requestAudioChatTips " + uin);
            AVChatManager.getInstance().startQQCallSkill(true, uin);
        }

        @Override
        public void cancelAIAudioRequest() throws RemoteException {
            QLog.d(TAG, "cancelAIAudioRequest");
            RecordDataManager.getInstance().onSleep();
        }

        @Override
        public int sendQQCallRequest(int uinType, long tinyId, byte[] msg, int length) {
            QLog.d(TAG, "sendQQCallRequest, uinType = " + uinType + ", tinyId = " + tinyId + ", thread id = " + Thread.currentThread().getId());
            return XWSDKJNI.sendQQCallRequest(uinType, tinyId, msg, length);
        }

        @Override
        public long getBindedQQUin() throws RemoteException {
            QLog.d(TAG, "getBindedQQUin");
            long selfDin = XWSDKJNI.getQQUin();
            return selfDin;
        }

        @Override
        public void statisticsPoint(String compassName, String event, String param, long time) throws RemoteException {
            QLog.d(TAG, "statisticsPoint");
            XWSDKJNI.statisticsPoint(compassName, event, param, time);
        }
    }

    private static void setVideoPID(int pid, String videoService) throws RemoteException {
        AVChatManager.getInstance().setVideoPID(pid, videoService);
        if (pid == 0) {
            AVChatManager.getInstance().setCalling(false);
            isFirst = true;
            if (isTestDelay) {
                mFrameList.clear();
            } else {
                bos.reset();
            }
            mOffset = 0;
        }
    }

    private static boolean isTestDelay;// 测试多线程音频数据延迟
    private static ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private static LinkedList<XWAudioFrameInfo> mFrameList = new LinkedList<>();

    public static void putAudioData(byte[] data, int length) {
        if (isTestDelay) {
            XWAudioFrameInfo frameInfo = new XWAudioFrameInfo();
            frameInfo.data = data;
            frameInfo.length = length;
            frameInfo.time = System.currentTimeMillis();
            mFrameList.add(frameInfo);
        } else {
            bos.write(data, 0, length);
        }
    }

    public static int read(byte[] buffer, int length) {
        if (isFirst) {
            AVChatManager.getInstance().setCalling(true);
            isFirst = false;
        }
        if (bos.size() < mOffset + length) {
            SystemClock.sleep(AVChatManager.getInstance().getReadAudioSleepTime());
            return 0;
        }
        byte[] temp = bos.toByteArray();
        System.arraycopy(temp, mOffset, buffer, 0, length);
        mOffset += length;
        if (temp.length > 1 * 1000 * 1000) {
            bos.reset();
            bos.write(temp, mOffset, temp.length - mOffset);
            mOffset = 0;
        }
        return length;
    }

    public static XWAudioFrameInfo read(int length) {
        if (isFirst) {
            AVChatManager.getInstance().setCalling(true);
            isFirst = false;
        }
        if (mFrameList.size() == 0) {
            SystemClock.sleep(AVChatManager.getInstance().getReadAudioSleepTime());
            return null;
        }
        int len = 0;
        ArrayList<XWAudioFrameInfo> list = new ArrayList<>();
        for (XWAudioFrameInfo frameInfo : mFrameList) {
            list.add(frameInfo);
            len += frameInfo.length;
            if (len >= length) {
                break;
            }
        }
        XWAudioFrameInfo info = new XWAudioFrameInfo();
        info.time = list.get(0).time;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (XWAudioFrameInfo frameInfo : list) {
            try {
                bos.write(frameInfo.data);
            } catch (IOException e) {
                e.printStackTrace();
            }
            info.length += frameInfo.length;
            mFrameList.remove(frameInfo);
        }
        try {
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        info.data = bos.toByteArray();
        return info;
    }
}
