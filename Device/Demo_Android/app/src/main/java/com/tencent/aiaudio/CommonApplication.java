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
package com.tencent.aiaudio;

import android.app.ActivityManager;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import com.tencent.aiaudio.bledemo.BLEManager;
import com.tencent.aiaudio.bledemo.BLEService;
import com.tencent.aiaudio.chat.AVChatManager;
import com.tencent.aiaudio.demo.BuildConfig;
import com.tencent.aiaudio.demo.R;
import com.tencent.aiaudio.player.XWeiPlayerMgr;
import com.tencent.aiaudio.service.AIAudioService;
import com.tencent.aiaudio.service.WakeupAnimatorService;
import com.tencent.aiaudio.tts.TTSManager;
import com.tencent.aiaudio.utils.AssetsUtil;
import com.tencent.aiaudio.utils.PcmBytesPlayer;
import com.tencent.aiaudio.utils.UIUtils;
import com.tencent.aiaudio.wakeup.RecordDataManager;
import com.tencent.av.XWAVChatAIDLService;
import com.tencent.xiaowei.control.XWeiAudioFocusManager;
import com.tencent.xiaowei.control.XWeiControl;
import com.tencent.xiaowei.info.XWAccountInfo;
import com.tencent.xiaowei.info.XWBinderInfo;
import com.tencent.xiaowei.info.XWCCMsgInfo;
import com.tencent.xiaowei.info.XWLoginInfo;
import com.tencent.xiaowei.info.XWTTSDataInfo;
import com.tencent.xiaowei.sdk.XWCCMsgManager;
import com.tencent.xiaowei.sdk.XWCoreService;
import com.tencent.xiaowei.sdk.XWDeviceBaseManager;
import com.tencent.xiaowei.sdk.XWSDK;
import com.tencent.xiaowei.util.QLog;

import java.util.ArrayList;
import java.util.List;

public class CommonApplication extends Application {

    public static final String ACTION_LOGIN_SUCCESS = "ACTION_LOGIN_SUCCESS_DEMO";
    public static final String ACTION_LOGIN_FAILED = "ACTION_LOGIN_FAILED";

    public static final String ACTION_ON_BINDER_LIST_CHANGE = "BinderListChange_DEMO";   //绑定列表变化

    public static final String AIC2CBusiness_GetVolume = "ai.internal.xiaowei.GetVolumeMsg";    //获取当前音量
    public static final String AIC2CBusiness_SetVolume = "ai.internal.xiaowei.SetVolumeMsg";    //设置音量
    public static final String AIC2CBusiness_ReturnVolume = "ai.internal.xiaowei.Cur100VolMsg"; //cc消息返回音量，返回GetVolumeMsg

    private static final String TAG = "CommonApplication";

    ////////////////////////////////////////////
    protected final static String URI_DEVICE_ICON_FORMAT = "http://i.gtimg.cn/open/device_icon/%s/%s/%s/%s/%s.png";
    ////////////////////////////////////////////

    public static Handler mHandler = new Handler();

    public static boolean isLogined;// 用来标记成功登录过
    public static boolean isOnline;// 用来标记当前是否在线

    public static Context mApplication;

    public static String mStoragePath;
    public static String mReceiveFileMenuPath;
    private static Toast mToast;

    @Override
    public void onCreate() {
        super.onCreate();
        mStoragePath = Environment.getExternalStorageDirectory().toString();
        mReceiveFileMenuPath = Environment.getExternalStoragePublicDirectory("tencent") + "/device/file";

        AVChatManager.setBroadcastPermissionDeviceSdkEvent("com.tencent.xiaowei.demo.chat");
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler.getInstance());// 保存java层的日志
        if (!getPackageName().equals(getProcessName(this, android.os.Process.myPid()))) {
            return;
        }
        mApplication = this;

        AssetsUtil.init(this);

        initListener();// 初始化小微sdk的事件监听器

        if (!PidInfoConfig.init()) {
            Toast.makeText(this, "请先运行sn生成工具，再重新打开Demo", Toast.LENGTH_LONG).show();
            return;
        }

        if (!UIUtils.isNetworkAvailable(this)) {
            PcmBytesPlayer.getInstance().play(AssetsUtil.getRing("network_disconnected.pcm"), null);
        }

        // 构造登录信息
        XWLoginInfo login = new XWLoginInfo();
        login.deviceName = getString(R.string.app_name);
        login.license = PidInfoConfig.licence;
        login.serialNumber = PidInfoConfig.sn;
        login.srvPubKey = PidInfoConfig.srvPubKey;
        login.productId = PidInfoConfig.pid;
        login.productVersion = UIUtils.getVersionCode(this);// build.gradle中的versionCode，用来检测更新
        login.networkType = XWLoginInfo.TYPE_NETWORK_WIFI;
        login.runMode = XWLoginInfo.SDK_RUN_MODE_DEFAULT;
        login.sysPath = getCacheDir().getAbsolutePath();
        login.sysCapacity = 1024000l;
        login.appPath = getCacheDir().getAbsolutePath();
        login.appCapacity = 1024000l;
        login.tmpPath = Environment.getExternalStoragePublicDirectory("tencent") + "/device/file/";
        login.tmpCapacity = 1024000l;

        int ret = XWCoreService.init(getApplicationContext(), login);// 初始化小微sdk

        if (ret != 0) {
            showToast("初始化失败");
            return;
        }

        if (!BuildConfig.IS_NEED_VOICE_LINK) {
            startService(new Intent(this, WakeupAnimatorService.class));
        }

        XWAccountInfo accountInfo = new XWAccountInfo();
        XWSDK.getInstance().init(this, accountInfo);
        QLog.e(TAG, "onCreate");

        XWeiControl.getInstance().init();
        XWeiControl.getInstance().setXWeiPlayerMgr(new XWeiPlayerMgr(getApplicationContext()));

        XWSDK.getInstance().setOnReceiveTTSDataListener(new XWSDK.OnReceiveTTSDataListener() {
            @Override
            public boolean onReceive(String voiceId, XWTTSDataInfo ttsData) {
                TTSManager.getInstance().write(ttsData);
                return true;
            }
        });

        XWSDK.getInstance().setAutoDownloadFileCallback(new XWSDK.OnAutoDownloadCallback() {
            @Override
            public int onDownloadFile(long size, int channel) {
                QLog.e(TAG, "onDownloadFile size: " + size + " channel: " + channel);
                //返回0表示继续下载，返回非0表示停止下载
                return 0;
            }
        });

        //初始化cc消息
        XWCCMsgManager.initC2CMsgModule();
        XWCCMsgManager.setOnReceiveC2CMsgListener(new XWCCMsgManager.OnReceiveC2CMsgListener() {
            @Override
            public void onReceiveC2CMsg(long from, XWCCMsgInfo msg) {
                if (msg.businessName.equals("蓝牙")) {
                    BLEManager.onCCMsg(from, new String(msg.msgBuf));
                } else if (msg.businessName.equals(AIC2CBusiness_GetVolume)) {
                    AIAudioService service = AIAudioService.getInstance();
                    if (service != null) {
                        int volume = service.getVolume();
                        QLog.d(TAG, "GetVolume " + volume);

                        XWCCMsgInfo ccMsgInfo = new XWCCMsgInfo();
                        ccMsgInfo.businessName = AIC2CBusiness_ReturnVolume;
                        ccMsgInfo.msgBuf = Integer.toString(volume).getBytes();
                        XWCCMsgManager.sendCCMsg(from, ccMsgInfo, new XWCCMsgManager.OnSendCCMsgListener() {

                            @Override
                            public void onResult(long to, int errCode) {
                                QLog.d(TAG, "sendCCMsg result to: " + to + " errCode: " + errCode);
                            }
                        });
                    }
                } else if (msg.businessName.equals(AIC2CBusiness_SetVolume)) {
                    if (msg.msgBuf != null) {
                        int volume = Integer.valueOf(new String(msg.msgBuf));
                        AIAudioService service = AIAudioService.getInstance();
                        if (service != null) {
                            service.setVolume(volume);
                        }
                    }
                }
            }
        });

        if (BluetoothAdapter.getDefaultAdapter() != null) {
            startService(new Intent(this, BLEService.class));
        }


        // 初始化音视频Service
        startService(new Intent(this, XWAVChatAIDLService.class));
        AVChatManager.getInstance().init(this);
    }

    public static String getDeviceHeadUrl(String strPID) {
        if (TextUtils.isEmpty(strPID)) {
            return "";
        }

        String fullAppid = strPID;
        //容错补零
        if (strPID.length() < 8) {
            String fillZeroString = "00000000";
            fullAppid = (fillZeroString + strPID);
        }

        fullAppid = fullAppid.substring(fullAppid.length() - 8);
        String iconUrl = String.format(URI_DEVICE_ICON_FORMAT, fullAppid.substring(0, 2),
                fullAppid.substring(2, 4), fullAppid.substring(4, 6),
                fullAppid.substring(6, 8),
                strPID);
        QLog.d(TAG, iconUrl);
        return iconUrl;
    }

    private void initListener() {
        XWDeviceBaseManager.setOnDeviceRegisterEventListener(new XWDeviceBaseManager.OnDeviceRegisterEventListener() {
            @Override
            public void onConnectedServer(int errorCode) {
                if (errorCode != 0)
                    showToast("连接服务器失败 " + errorCode);
            }

            @Override
            public void onRegister(int errorCode, int subCode) {
                // errorCode为1需要关注init的参数和配置平台的配置是否都正确。其他错误可以反馈小微。
                if (errorCode != 0)
                    showToast("注册失败 " + subCode + ",请检查网络以及登录的相关信息是否正确。");
            }
        });

        XWDeviceBaseManager.setOnBinderEventListener(new XWDeviceBaseManager.OnBinderEventListener() {

            @Override
            public void onBinderListChange(int error, ArrayList<XWBinderInfo> arrayList) {
                // 刷新MainActivity的列表以及判断是否需要关闭已经打开的Activity
                if (error == 0) {
                    sendBroadcast(ACTION_ON_BINDER_LIST_CHANGE);
                    if (arrayList.size() == 0) {
                        XWeiAudioFocusManager.getInstance().abandonAllAudioFocus();// 解绑了应该停止所有的资源的播放
                    }
                }

            }
        });// 被绑定、列表变化、擦除所有设备了
        XWDeviceBaseManager.setOnDeviceSDKEventListener(new XWDeviceBaseManager.OnDeviceLoginEventListener() {
            @Override
            public void onLoginComplete(int error) {
                QLog.i(TAG, "onLoginComplete: error =  " + error);
                if (error == 0) {
                    isLogined = true;
                    showToastMessage("登录成功");
                    sendBroadcast(ACTION_LOGIN_SUCCESS);
                } else {
                    sendBroadcast(ACTION_LOGIN_FAILED);
                    showToastMessage("登录失败");
                }
            }

            @Override
            public void onOnlineSuccess() {
                isOnline = true;
                RecordDataManager.getInstance().setHalfWordsCheck(true);

                QLog.i(TAG, "onOnlineSuccess");
                showToastMessage("上线成功");
                sendBroadcast("ONLINE");
            }

            @Override
            public void onOfflineSuccess() {
                isOnline = false;
                RecordDataManager.getInstance().setHalfWordsCheck(false);

                QLog.i(TAG, "onOfflineSuccess ");
                showToastMessage("离线");
                sendBroadcast("OFFLINE");
            }

            @Override
            public void onUploadRegInfo(int error) {
                QLog.i(TAG, "onUploadRegInfoSuccess " + error);
                if (error == 0) {
//                    showToastMessage("上传注册信息成功");
                }
            }
        });// 登录成功、上下线
    }

    private void sendBroadcast(String action) {
        sendBroadcast(action, null);
    }

    private void sendBroadcast(String action, Bundle bundle) {
        Intent intent = new Intent(action);
        if (bundle != null)
            intent.putExtras(bundle);
        sendBroadcast(intent);
        QLog.d(TAG, "send a broadcast:" + action);
    }

    public static void showToastMessage(final String text) {
        showToastMessage(text, true);
    }

    public static void showToastMessage(final String text, boolean show) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mApplication, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static String getProcessName(Context context, int pid) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps != null && !runningApps.isEmpty()) {
            for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
                if (procInfo.pid == pid) {
                    return procInfo.processName;
                }
            }
        }
        return null;
    }

    public static void showToast(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(mApplication, text, Toast.LENGTH_SHORT);
                mToast.show();
            }
        });
    }

}
