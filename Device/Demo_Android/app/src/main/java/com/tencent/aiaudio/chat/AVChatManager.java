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
package com.tencent.aiaudio.chat;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.aiaudio.service.AIAudioService;
import com.tencent.av.IXWAudioChatService;
import com.tencent.av.XWAVChatAIDLService;
import com.tencent.utils.MusicPlayer;
import com.tencent.xiaowei.control.Constants;
import com.tencent.xiaowei.control.XWeiAudioFocusManager;
import com.tencent.xiaowei.control.XWeiControl;
import com.tencent.xiaowei.control.XWeiOuterSkill;
import com.tencent.xiaowei.def.XWCommonDef;
import com.tencent.xiaowei.info.XWAppInfo;
import com.tencent.xiaowei.info.XWEventLogInfo;
import com.tencent.xiaowei.info.XWPlayStateInfo;
import com.tencent.xiaowei.info.XWResGroupInfo;
import com.tencent.xiaowei.info.XWResourceInfo;
import com.tencent.xiaowei.info.XWResponseInfo;
import com.tencent.xiaowei.sdk.XWAVChatManager;
import com.tencent.xiaowei.sdk.XWSDK;
import com.tencent.xiaowei.util.QLog;
import com.tencent.xiaowei.util.Singleton;

import java.util.ArrayList;
import java.util.List;

import static com.tencent.xiaowei.control.Constants.SKILL_NAME.SKILL_NAME_QQCall;
import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_QQ_CALL;

public class AVChatManager {

    private static final String TAG = "AVChatManager";
    private static Singleton<AVChatManager> sSingleton = new Singleton<AVChatManager>() {
        @Override
        protected AVChatManager createInstance() {
            return new AVChatManager();
        }
    };
    private boolean mIsAudioMode;

    public static AVChatManager getInstance() {
        if (sSingleton == null) {
            sSingleton = new Singleton<AVChatManager>() {
                @Override
                protected AVChatManager createInstance() {
                    return new AVChatManager();
                }
            };
        }
        return sSingleton.getInstance();
    }


    public static final String ACTION_START_AUDIO_VIDEO_PROCESS = "tdas_ACTION_START_AUDIO_VIDEO_PROCESS";
    private static final String mAVPackageName = "com.tencent.aiaudio.avchat";


    public static final int QQCALL_REQUEST = 11020;    //打电话 value is enable
    public static final int QQCALL_REQUEST_NEW = 11052;   //打电话 value is enable
    public static final int QQCALL_CANCEL = 11021;
    public static final int QQCALL_ACCEPT = 11022;
    public static final int QQCALL_REJECT = 11023;
    public static final int QQCALL_BLECALL = 11061;     //蓝牙电话通知
    public static final int QQCALL_BE_INVITED = 666666;     //收到电话 value is enable
    public static final int QQCALL_CALL_OUT = 666667;     //按钮打出电话 value is enable


    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferEditor;

    private boolean isQQCallSkillPlaying;
    private boolean isQQCallSkilling;
    private String mVideoService;

    public int mState;// 0 不在电话， 1 等待自己接听， 2 等待对方接听， 3 已经接通了
    public long mCurrentPeerId = 0;
    private boolean mIsInvited;

    static ArrayList<Intent> mPendingIntent = new ArrayList<>();

    protected Context mContext;

    public static int mVideoPid;

    private XWeiAudioFocusManager.OnAudioFocusChangeListener qqCallListener;
    private IXWAudioChatService mIXWAudioChatService;
    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIXWAudioChatService = IXWAudioChatService.Stub.asInterface(service);
            try {
                mIXWAudioChatService.ifThirdManageCamera(isThirdManageCamera());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIXWAudioChatService = null;
        }
    };
    private static OnCallEventListener mOnCallEventListener;

    public void init(Context context) {
        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences("config", Context.MODE_PRIVATE);
        mSharedPreferEditor = mSharedPreferences.edit();

        XWeiControl.getInstance().getXWeiOuterSkill().registerSkillIdOrSkillName(SKILL_ID_QQ_CALL, qqCallSkillHandler);

        XWAVChatManager.setOnAVChatEventListener(new XWAVChatManager.OnAVChatEventListener() {
            @Override
            public void onEvent(String action, Bundle bundle) {
                QLog.d(TAG, "action callbackVideoMsg " + action + " bundle " + bundle.toString());
                if (action.equalsIgnoreCase(XWAVChatManager.SUB_ACTION_START_AUDIO_CHAT_ACTIVITY) || action.equalsIgnoreCase(XWAVChatManager.SUB_ACTION_START_VIDEO_CHAT_ACTIVITY)) {
                    if (XWAVChatAIDLService.binder) {
                        return;
                    }
                    if (!isInstalled(mAVPackageName)) {
                        return;
                    }
                    Intent intent = new Intent(mAVPackageName);
                    intent.setAction(mAVPackageName + ".open");
                    intent.putExtra("sub_action", action);
                    intent.putExtra("main_package", mContext.getPackageName());
                    intent.putExtra("receive", false);
                    intent.putExtras(bundle);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setPackage(mAVPackageName);//设置视频包名，要先确认包名
                    try {
                        mContext.startActivity(intent);
                    } catch (Exception e) {
                        QLog.d(TAG, e.getMessage());
                    }
                    long uin = bundle.getLong("uin", 0);
                    startQQCallSkill(false, uin);
                    mIsInvited = false;
                } else {
                    Integer uinType = bundle.getInt("uinType", 0);
                    if (XWAVChatAIDLService.binder == false
                            && action.equalsIgnoreCase(XWAVChatManager.SUB_ACTION_ON_RECEIVE_VIDEO_BUFFER)
                            && uinType == 1) {
                        if (!isInstalled(mAVPackageName)) {
                            return;
                        }
                        Intent intent = new Intent();
                        intent.setAction(mAVPackageName + ".open");
                        intent.putExtra("sub_action", action);
                        intent.putExtra("receive", true);
                        intent.putExtra("main_package", mContext.getPackageName());
                        intent.putExtras(bundle);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setPackage(mAVPackageName);//设置视频包名，要先确认包名
                        try {
                            mContext.startActivity(intent);
                        } catch (Exception e) {
                            QLog.d(TAG, e.getMessage());
                        }

                        mIsInvited = true;
                    } else {
                        Intent intent = new Intent();
                        intent.setAction(ACTION_START_AUDIO_VIDEO_PROCESS);
                        intent.putExtra("sub_action", action);
                        intent.putExtras(bundle);
                        sendVideoBroadcast(intent);
                    }
                }
            }
        });
    }


    //处理音视频电话的skill
    private XWeiOuterSkill.OuterSkillHandler qqCallSkillHandler = new XWeiOuterSkill.OuterSkillHandler() {

        @Override
        public boolean handleResponse(int sessionId, final XWResponseInfo responseInfo) {
            Log.e(TAG, "QQCall sessionId:" + sessionId + " response:" + responseInfo + " AVstate:" + mState);
            int command = checkCommandId(responseInfo.resources);
            String value = getCommandValue(responseInfo.resources);
            long peerId = 0;
            try {
                peerId = Long.valueOf(value);
            } catch (Exception e) {

            }
            switch (command) {
                case QQCALL_REQUEST:
                case QQCALL_REQUEST_NEW:
                    if (peerId > 0) {
                        startAudioVideoChat(peerId);
                    }
                    break;
                case QQCALL_ACCEPT:
                    acceptAudioChat(true);
                    break;
                case QQCALL_REJECT:
                    rejectAudioChat(true);
                    break;
                case QQCALL_CANCEL:
                    closeAudioChat(true);
                    break;
                case QQCALL_BE_INVITED:
                    if (peerId > 0)
                        XWSDK.getInstance().requestProtocolTTS(peerId, 0, XWCommonDef.RequestProtocalType.CHAT, null);
                    else {
                        Log.e(TAG, "QQCall peerid is invalid.");
                    }
                    break;
            }

            if (responseInfo.resultCode == XWCommonDef.XWeiErrorCode.VOICE_TIMEOUT && mState == 1) {
                // 如果是接听电话模式，还未接通的时候，唤醒后一直不说话，就再播放一次铃声
                if (mCurrentPeerId > 0)
                    XWSDK.getInstance().requestProtocolTTS(mCurrentPeerId, 0, XWCommonDef.RequestProtocalType.CHAT, null);
                else {
                    Log.e(TAG, "QQCall peerid is invalid.");
                }
            }


            final ArrayList<XWResourceInfo> playList = new ArrayList<>();

            for (XWResGroupInfo groupInfo : responseInfo.resources) {
                for (XWResourceInfo resourceInfo : groupInfo.resources) {
                    // play
                    if (resourceInfo.format == XWCommonDef.ResourceFormat.TTS || resourceInfo.format == XWCommonDef.ResourceFormat.TEXT || resourceInfo.format == XWCommonDef.ResourceFormat.URL) {
                        playList.add(resourceInfo);
                    }
                }
            }
            if (playList.size() > 0) {
                playIndex = 0;
                MusicPlayer.getInstance().stop();
                qqCallListener = new XWeiAudioFocusManager.OnAudioFocusChangeListener() {
                    @Override
                    public void onAudioFocusChange(int focusChange) {
                        QLog.d(TAG, "onAudioFocusChange " + focusChange);
                        if (focusChange == XWeiAudioFocusManager.AUDIOFOCUS_GAIN_TRANSIENT) {
                            MusicPlayer.getInstance().setVolume(100);
                            play(playList, new MusicPlayer.OnPlayListener() {

                                @Override
                                public void onCompletion(int error) {
                                    AIAudioService service = AIAudioService.getInstance();
                                    if (service != null && !TextUtils.isEmpty(responseInfo.context.ID) && mState > 0) {
                                        service.wakeup(responseInfo.context.ID, 5000, 500, 0);
                                    }
                                    if (mState == 0) {
                                        XWeiAudioFocusManager.getInstance().abandonAudioFocus(qqCallListener);
                                    }
                                }
                            });
                            setPlayingStatus(true);
                        } else if (focusChange == XWeiAudioFocusManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                            MusicPlayer.getInstance().setVolume(20);
                        } else if (focusChange == XWeiAudioFocusManager.AUDIOFOCUS_LOSS) {
                            MusicPlayer.getInstance().stop();

                            setPlayingStatus(false);
                        }
                    }
                };
                XWeiAudioFocusManager.getInstance().requestAudioFocus(qqCallListener, XWeiAudioFocusManager.AUDIOFOCUS_GAIN_TRANSIENT);
            } else if (QQCALL_CALL_OUT == command) {
                qqCallListener = new XWeiAudioFocusManager.OnAudioFocusChangeListener() {
                    @Override
                    public void onAudioFocusChange(int focusChange) {

                    }
                };
                XWeiAudioFocusManager.getInstance().requestAudioFocus(qqCallListener, XWeiAudioFocusManager.AUDIOFOCUS_GAIN_TRANSIENT);
            }

            return true;
        }
    };
    int playIndex = 0;

    private void play(final ArrayList<XWResourceInfo> playList, final MusicPlayer.OnPlayListener listener) {
        if (playList == null || playList.size() == 0) {
            return;
        }

        MusicPlayer.getInstance().playMediaInfo(playList.get(playIndex), new MusicPlayer.OnPlayListener() {

            @Override
            public void onCompletion(int error) {
                playIndex++;
                if (playIndex < playList.size()) {
                    MusicPlayer.getInstance().playMediaInfo(playList.get(playIndex), this);
                } else {
                    playIndex = 0;
                    listener.onCompletion(0);
                }
            }
        });
    }


    private int checkCommandId(XWResGroupInfo[] resources) {
        if (resources != null && resources.length > 0 && resources[0].resources.length > 0 && resources[0].resources[0].format == XWCommonDef.ResourceFormat.COMMAND) {
            String id = resources[0].resources[0].ID;
            try {
                return Integer.valueOf(id);
            } catch (Exception e) {
            }
        }
        return 0;
    }

    private String getCommandValue(XWResGroupInfo[] resources) {
        if (resources != null && resources.length > 0 && resources[0].resources.length > 0 && resources[0].resources[0].format == XWCommonDef.ResourceFormat.COMMAND) {
            return resources[0].resources[0].content;
        }
        return null;
    }


    private boolean isInstalled(String packageName) {
        try {
            mContext.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void setThirdManageCamera(boolean ifThirdManageCamera) {
        mSharedPreferEditor.putBoolean("is_h264_mode", ifThirdManageCamera);
        mSharedPreferEditor.commit();
        QLog.d(TAG, "ifThirdManageCamera: " + ifThirdManageCamera);
    }

    private boolean isThirdManageCamera() {
        return mSharedPreferences.getBoolean("is_h264_mode", false);
    }

    public void setVideoPID(int pid, String videoService) {
        QLog.d(TAG, "set video pid:" + pid + " old:" + mVideoPid + " videoService：" + videoService);
        mVideoService = videoService;
        if (mVideoPid == pid) {
            return;
        }
        mVideoPid = pid;
        if (pid == 0) {
            mPendingIntent.clear();
            mIXWAudioChatService = null;

            setChatState(0);
            try {
                mContext.unbindService(mConn);
            } catch (Exception e) {
                e.printStackTrace();
            }
            XWeiAudioFocusManager.getInstance().abandonAudioFocus(qqCallListener);
        } else {
            if (mIXWAudioChatService == null) {

                Intent intent = new Intent();
                intent.setAction("com.tencent.aiaudio.RemoteTXAudioChatService");
                intent.setPackage(mAVPackageName);
                boolean ret = mContext.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
                QLog.d(TAG, "bindService mIXWAudioChatService ret " + ret);
            }
            setChatState(mIsInvited ? 1 : 2);
        }
    }


    /**
     * 正在通话模式
     */
    private int mReadAudioSleepTime = 20;

    public void setReadAudioSleepTime(int time) {
        mReadAudioSleepTime = time;
    }

    public int getReadAudioSleepTime() {
        return mReadAudioSleepTime;
    }

    private void setPlayingStatus(boolean playing) {
        if (isQQCallSkillPlaying == playing) {
            return;
        }
        isQQCallSkillPlaying = playing;
        reportStateIfNecessary();

        XWPlayStateInfo stateInfo = new XWPlayStateInfo();
        stateInfo.appInfo = new XWAppInfo();
        stateInfo.appInfo.ID = SKILL_ID_QQ_CALL;
        stateInfo.appInfo.name = SKILL_NAME_QQCall;
        stateInfo.state = isQQCallSkillPlaying ? XWCommonDef.PlayState.RESUME : XWCommonDef.PlayState.PAUSE;
        QLog.d(TAG, "reportPlayState " + stateInfo.state + " avstate:" + mState + " playing:" + isQQCallSkillPlaying);
        XWSDK.getInstance().reportPlayState(stateInfo);
    }


    private void reportStateIfNecessary() {
        boolean isQQCallSkilling = mState > 0 || isQQCallSkillPlaying;
        if (this.isQQCallSkilling != isQQCallSkilling) {
            this.isQQCallSkilling = isQQCallSkilling;
            XWPlayStateInfo stateInfo = new XWPlayStateInfo();
            stateInfo.appInfo = new XWAppInfo();
            stateInfo.appInfo.ID = SKILL_ID_QQ_CALL;
            stateInfo.appInfo.name = SKILL_NAME_QQCall;
            stateInfo.state = isQQCallSkilling ? XWCommonDef.PlayState.START : XWCommonDef.PlayState.FINISH;
            QLog.d(TAG, "reportPlayState " + stateInfo.state + " avstate:" + mState + " playing:" + isQQCallSkillPlaying);
            XWSDK.getInstance().reportPlayState(stateInfo);
        }
    }

    public void setCalling(boolean in) {
        if (in) {
            setChatState(3);
            // todo 开始给语音
        } else {
            // todo 结束给语音
        }
    }

    /**
     * 需要先启动Video进程才能发送的广播
     *
     * @param intent
     */
    private void sendVideoBroadcast(Intent intent) {
        boolean videoProcessIsRun = videoProcessIsRun();
        if (!videoProcessIsRun) {
            mPendingIntent.add(intent);// 只存视频通话相关的Intent，视频通话结束后unInit时候也会清空。
//            if (isFirstCMD) {
//                isFirstCMD = false;// 发送第一个去唤起进程
            mContext.sendBroadcast(intent, BROADCAST_PERMISSION_DEVICE_SDK_EVENT);
//            }
        } else {
            invokePendingIntent();
            mContext.sendBroadcast(intent, BROADCAST_PERMISSION_DEVICE_SDK_EVENT);
        }
    }

    private boolean videoProcessIsRun() {
        if (mContext == null) {
            return false;
        }
        if (mVideoPid == 0 || mVideoService == null) {
            return false;
        }
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServiceInfos = am.getRunningServices(200);
        for (ActivityManager.RunningServiceInfo serviceInfo : runningServiceInfos) {
            if (serviceInfo.service.getClassName().equals(mVideoService)) {
                return true;
            }
        }

        setVideoPID(0, null);
        return false;
    }

    /**
     * 打电话
     *
     * @param peerId 对方id
     */
    public void startAudioVideoChat(long peerId) {
        if (peerId == 0) {
            return;
        }

        XWAVChatAIDLService.isFirst = true;
        if (mIsAudioMode) {
            XWAVChatManager.startAudioChatActivity(peerId);
        } else {
            XWAVChatManager.startVideoChatActivity(peerId);
        }
    }

    protected static String BROADCAST_PERMISSION_DEVICE_SDK_EVENT = "com.tencent.device.broadcast.permission";

    public static void setBroadcastPermissionDeviceSdkEvent(String permission) {
        QLog.d(TAG, "setBroadcastPermissionDeviceSdkEvent " + BROADCAST_PERMISSION_DEVICE_SDK_EVENT + " to " + permission);
        if (!TextUtils.isEmpty(permission)) {
            BROADCAST_PERMISSION_DEVICE_SDK_EVENT = permission;
        }
    }

    public void setAudioMode(boolean mode) {
        mIsAudioMode = mode;
    }

    public static boolean putAudioData(byte[] buffer, int read_len) {
        if (XWAVChatAIDLService.binder && getInstance().mState == 3) {
            XWAVChatAIDLService.putAudioData(buffer, read_len);
            return true;
        }
        return false;
    }


    public interface OnCallEventListener {
        void onEvent(int state);
    }

    public static void setOnCallEventListener(OnCallEventListener listener) {
        mOnCallEventListener = listener;
    }

    public void setChatState(int state) {
        if (mState != state) {
            mState = state;
            if (mOnCallEventListener != null) {
                mOnCallEventListener.onEvent(mState);
            }
            XWEventLogInfo log = new XWEventLogInfo();
            switch (mState) {
                case 0:
                    log.event = XWEventLogInfo.EVENT_QQCALL_OUT;
                    mCurrentPeerId = 0;
                    break;
                case 1:
                    log.event = XWEventLogInfo.EVENT_QQCALL_INVITE;
                    break;
                case 2:
                    log.event = XWEventLogInfo.EVENT_QQCALL_CALL_OUT;
                    break;
                case 3:
                    log.event = XWEventLogInfo.EVENT_QQCALL_ING;
                    break;
            }
            XWSDK.getInstance().reportEvent(log);
            reportStateIfNecessary();
        }
    }

    public void invokePendingIntent() {
        for (int i = 0; i < mPendingIntent.size(); ++i) {
            mContext.sendBroadcast(mPendingIntent.get(i), BROADCAST_PERMISSION_DEVICE_SDK_EVENT);
        }
        mPendingIntent.clear();
    }

    void resumePlayRing() {
        if (mIXWAudioChatService != null) {
            try {
                mIXWAudioChatService.resumePlayRing();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    void pausePlayRing() {
        if (mIXWAudioChatService != null) {
            try {
                mIXWAudioChatService.pausePlayRing();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 挂断语音通话
     */
    public void closeAudioChat(boolean isAudioOp) {
        if (mIXWAudioChatService != null) {
            try {
                mIXWAudioChatService.close(isAudioOp);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 接听语音通话
     */
    public void acceptAudioChat(boolean isAudioOp) {
        if (mIXWAudioChatService != null) {
            try {
                mIXWAudioChatService.accept(isAudioOp);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 拒绝语音通话
     */
    public void rejectAudioChat(boolean isAudioOp) {
        if (mIXWAudioChatService != null) {
            try {
                mIXWAudioChatService.reject(isAudioOp);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void startQQCallSkill(boolean invited, long uin) {

        mCurrentPeerId = uin;

        QLog.d(TAG, "startQQCallSkill " + invited + " " + uin);
        // 收到电话比较特殊，需要模拟一个语音请求的响应给控制层。控制层会生成电话APP，回调到OuterSkill中。

        XWResponseInfo responseInfo = new XWResponseInfo();
        responseInfo.appInfo = new XWAppInfo();
        responseInfo.appInfo.ID = Constants.SkillIdDef.SKILL_ID_QQ_CALL;
        responseInfo.appInfo.name = SKILL_NAME_QQCall;
        responseInfo.resources = new XWResGroupInfo[1];
        responseInfo.resources[0] = new XWResGroupInfo();
        responseInfo.resources[0].resources = new XWResourceInfo[1];
        responseInfo.resources[0].resources[0] = new XWResourceInfo();
        responseInfo.resources[0].resources[0].format = XWCommonDef.ResourceFormat.COMMAND;
        responseInfo.resources[0].resources[0].ID = "" + (invited ? QQCALL_BE_INVITED : QQCALL_CALL_OUT);
        responseInfo.resources[0].resources[0].content = "" + uin;

        XWeiControl.getInstance().processResponse("", responseInfo, null);
    }

    public boolean isCalling() {
        return mState > 0;
    }

}
