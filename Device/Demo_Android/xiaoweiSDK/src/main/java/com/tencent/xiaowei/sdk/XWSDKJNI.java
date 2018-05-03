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

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.xiaowei.info.XWAccountInfo;
import com.tencent.xiaowei.info.XWAppInfo;
import com.tencent.xiaowei.info.XWBinderInfo;
import com.tencent.xiaowei.info.XWBinderRemark;
import com.tencent.xiaowei.info.XWCCMsgInfo;
import com.tencent.xiaowei.info.XWContactInfo;
import com.tencent.xiaowei.info.XWContextInfo;
import com.tencent.xiaowei.info.XWEventLogInfo;
import com.tencent.xiaowei.info.XWFileTransferInfo;
import com.tencent.xiaowei.info.XWLoginStatusInfo;
import com.tencent.xiaowei.info.XWPlayStateInfo;
import com.tencent.xiaowei.info.XWResponseInfo;
import com.tencent.xiaowei.info.XWTTSDataInfo;
import com.tencent.xiaowei.info.XWeiMessageInfo;
import com.tencent.xiaowei.util.QLog;
import com.tencent.xiaowei.util.QLogUtil;
import com.tencent.xiaowei.util.Singleton;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by xw on 2016/11/25.
 * 小微SDK的jni层，不对外开放
 */
public class XWSDKJNI {
    private static final String TAG = "XWSDKJNI";

    private static final Singleton<XWSDKJNI> sSingleton = new Singleton<XWSDKJNI>() {
        @Override
        protected XWSDKJNI createInstance() {
            return new XWSDKJNI();
        }
    };

    public static XWSDKJNI getInstance() {
        return sSingleton.getInstance();
    }

    static {
        String[] so = {"stlport_shared", "xiaoweiSDK", "xiaowei"};
        for (String s : so) {
            try {
                System.loadLibrary(s);
            } catch (UnsatisfiedLinkError e) {
                e.printStackTrace();
            }
        }
    }

    private long mSelfDin;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    // 联系人列表
    protected static final ArrayList<XWBinderInfo> mBinderList = new ArrayList<>();

    private HashMap<String, String[]> mLogMap = new HashMap<>(1);

    static void postMain(Runnable runnable) {
        getInstance().mHandler.post(runnable);
    }

    static void postMainDelay(Runnable runnable, long delayMillis) {
        getInstance().mHandler.postDelayed(runnable, delayMillis);
    }

    static void removeMainRunnable(Runnable runnable) {
        getInstance().mHandler.removeCallbacks(runnable);
    }

    ///////////////////////////////////////////////////native//////////////////////////////////////////
    public native void initJNI(int nativeLogLevel);

    /**
     * 初始化sdk
     *
     * @param deviceName     设备名称
     * @param license        设备授权码
     * @param serialNumber   设备序列号，长度不超过16字节
     * @param srvPubKey      IOT后台分配的公钥，和pid对应
     * @param productID      产品ID，厂商向腾讯开放平台部申请
     * @param productVersion 产品版本
     * @param networkType    网络类型，取值必须是NETWORK_TYPE_WIFI、NETWORK_TYPE_MOBILE、NETWORK_TYPE_UNICOM、NETWORK_TYPE_TELECOM、NETWORK_TYPE_HONGKONG
     * @param runMode        运行模式，取值必须是SDK_RUN_MODE_DEFAULT、SDK_RUN_MODE_LOW_POWER
     * @param sysPath        系统路径，SDK会在该目录下写入保证正常运行必需的配置信息
     * @param sysCapacity    系统路径下存储空间大小，SDK对该目录的存储空间要求小（最小大小：10K，建议大小：100K），SDK写入次数较少，读取次数较多
     * @param appPath        应用路径，SDK会在该目录下写入运行过程中的异常错误信息
     * @param appCapacity    应用路径下存储空间大小，SDK对该目录的存储空间要求较大（最小大小：500K，建议大小：1M），SDK写入次数较多，读取次数较少
     * @param tmpPath        临时路径，SDK会在该目录下写入临时文件
     * @param tmpCapacity    临时路径下存储空间大小，SDK对该目录的存储空间要求很大，建议尽可能大一些
     * @param testMode       是否连接测试环境, 0为不连接测试环境，1为连接测试环境
     */
    public native int init(String deviceName, byte[] license, String serialNumber, String srvPubKey, long productID, int productVersion, int networkType, int runMode,
                           String sysPath, long sysCapacity, String appPath, long appCapacity, String tmpPath, long tmpCapacity, int testMode);

    /**
     * 获取SDK版本信息
     *
     * @return [0] main_verison [1] sub_version [2] build_no
     */
    public native int[] getSDKVersion();

    /**
     * 当接入层出现on_online_status回调，状态变为离线后。
     * 如果确定是网络问题引起的离线，网络恢复后，可以调用该接口进行重新登录
     */
    static native void deviceReconnect();

    /**
     * 登录成功后，可以获取腾讯云服务器标准校时时间
     *
     * @return 返回是32位服务器校时时间(s)
     */
    static native int getServerTime();

    /**
     * 获取自己din
     *
     * @return
     */
    public native static long getSelfDin();

    /**
     * 生成二维码url(根据init时传入的pid,sn,license生成)
     *
     * @return
     */
    static native String getQRCodeUrl();

    /**
     * 获取绑定列表
     *
     * @return 返回null时表示正在拉取，需要监听onBinderListChange以得到拉取结果
     */
    static native XWBinderInfo[] fetchBinderList();

    /**
     * 获取绑定者主人的信息
     *
     * @return size为3的数组，[0] isBinded [1] binder_tinyid_type [2] tinyID
     */
    static native long[] getBinderAdminInfo();

    /**
     * 更新绑定者在线状态
     */
    static native void updateBinderOnlineStatus();

    /**
     * 解绑所有绑定者（须在登录成功之后调用）
     *
     * @return
     */
    static native int unBind();


    /**
     * 设备端主动查询OTA信息
     */
    static native int queryOtaUpdate();

    /**
     * 上传文件
     *
     * @param filePath
     * @param channelType 传输通道类型
     * @param fileType    输文件类型
     * @return 返回的cookie, 在传输相关的回调中作为参数，也可以cancelTransfer
     */
    static native long uploadFile(String filePath, int channelType, int fileType);

    /**
     * 获取小文件key对应的下载URL
     *
     * @param fileKey
     * @param fileType
     * @return
     */
    static native String getMiniDownloadURL(String fileKey, int fileType);

    /**
     * 下载小文件
     *
     * @param fileKey
     * @param fileType
     * @param miniToken
     * @return
     */
    static native long downloadMiniFile(String fileKey, int fileType, String miniToken);

    /**
     * 取消传输
     *
     * @param cookie
     */
    static native void cancelTransfer(long cookie);


    /**
     * wifi配网信息解析，启动解析模块
     *
     * @param key        smartlink配网, 设备的GUID 16字符的字符串。
     * @param sampleRate 声波配网, 设备实际录音的采样率，填的不对，会导致解声波信息失败。
     * @param mode       3 同时支持声波配网和smartlink配网
     * @return
     */
    static native int startWifiDecoder(String key, int sampleRate, int mode);

    /**
     * 填充wav数据。
     *
     * @param wav wav 是PCM 16bit 单声道的，size < 2048Byte
     */
    static native void fillVoiceWavData(byte[] wav);

    /**
     * wifi配网信息解析，停止解析模块
     *
     * @return
     */
    static native int stopWifiDecoder();

    /**
     * 配网完成，且初始化完SDK后，用这个接口通知手Q设备已经联网。 参见: onReceiveWifiInfo回调说明。
     *
     * @param ip   同步过来的ip
     * @param port 同步过来的port
     */
    native void ackApp(int ip, int port);


    /**
     * 上报日志
     *
     * @param url
     * @param text
     * @return
     */
    static native String uploadLog(String url, String text);

    /**********语音识别核心请求 begin**********/
    static native int startXiaoweiService(XWAccountInfo accountInfo);

    static native int stopXiaoweiService();

    static native String request(int type, byte[] requestData, XWContextInfo context);

    static native int cancelRequest(String voiceID);

    static native void cancelTTS(String resId);

    /**********语音识别核心请求 end**********/
    /**
     * 上报事件
     *
     * @param log
     */
    static native void reportEvent(XWEventLogInfo log);

    static native int reportPlayState(XWPlayStateInfo stateInfo);

    static native int setUserState(XWPlayStateInfo stateInfo);

    static native int clearUserState();

    static native String getMorePlaylist(XWAppInfo appInfo, String playID, int maxListSize, boolean isUp);

    static native String getPlayDetailInfo(XWAppInfo appInfo, String[] listPlayID);

    static native String getPlayDetailInfo(String appInfo, String[] listPlayID);

    static native String refreshPlayList(XWAppInfo appInfo, String[] listPlayID);

    /**
     * 设置品质 (通道层接口)
     *
     * @param quality 设置的品质
     * @return 0成功 非0失败
     */
    static native int setQuality(int quality);

    /**
     * 开启可见可答，实时取词
     *
     * @param enable true:开启 false:关闭
     * @return 0成功 非0失败
     */
    static native int enableV2A(boolean enable);

    /**
     * 设置词库列表
     *
     * @param type       词库类型 {@link XWCommonDef.WordListType}
     * @param words_list 词库
     * @return 0:success else failed
     */
    static native int setWordslist(int type, String[] words_list);

    static native String setFavorite(XWAppInfo appInfo, String playID, boolean favorite);

    /**
     * 拉取设备闹钟列表
     *
     * @return 请求sessionID
     */
    static native String getDeviceAlarmList();

    /**
     * 操作设备闹钟
     *
     * @param optType      操作类型（1：新增，2：更新，3：删除}
     * @param strAlarmJson 一个闹钟项的json串
     * @return 请求sessionID
     */
    static native String setDeviceAlarm(int optType, String strAlarmJson);

    /**
     * 拉取定时任务的播放资源
     *
     * @param strAlarmId 一个闹钟项的ID
     * @return 请求sessionID
     */
    static native String getTimingSkillResource(String strAlarmId);

    /**
     * 设置登录态，登录态是有屏设备用于获取音乐资源、语音点播歌曲、腾讯视频登录等使用的。QQ登录态90天会过期，微信30天过期，所以可能需要在设备端提示用户重新登录并设置。（过期是一直不使用才过期，每次使用自动续期）
     *
     * @param info
     * @return
     */
    static native String setLoginStatus(XWLoginStatusInfo info);

    /**
     * 查询登录态
     *
     * @param skillID
     * @return
     */
    static native String getLoginStatus(String skillID);

    /**
     * 获取QQ音乐Vip信息
     *
     * @return
     */
    static native String getMusicVipInfo();

    static native void errorFeedBack();


    /**
     * 初始化cc消息的回调
     *
     * @return 接口调用结果
     */
    static native int initCCMsgModule();

    /**
     * 发送c2c消息 给app
     *
     * @param tinyID app的tinyid
     * @param msg    cc消息结构
     * @return cookie
     */
    static native int sendCCMsg(long tinyID, XWCCMsgInfo msg);

    /**
     * 查询绑定者备注列表
     *
     * @return
     */
    static native int getBinderRemarkList();

    /**
     * 设置绑定者的备注
     *
     * @param tinyid
     * @param remark
     * @return
     */
    static native int setBinderRemark(long tinyid, String remark);

    /*********QQCall Begin********/
    /**
     * 把sharp打包后的呼叫请求数据发送给后台
     *
     * @param uinType
     * @param tinyId  这里填对端的QQ号
     * @param msg
     * @param length
     * @return
     */
    public native static int sendQQCallRequest(int uinType, long tinyId, byte[] msg, int length);

    public native static byte[] nativeGetVideoChatSignature();

    public native static void nativeSendVideoCall(long peerUin, int uinType, byte[] msg);

    public native static void nativeSendVideoCallM2M(long peerUin, int uinType, byte[] msg);

    public native static void nativeSendVideoCMD(long peerUin, int uinType, byte[] msg);

    public native static long getQQUin();

    public native static void statisticsPoint(String compassName, String event, String param, long time);

    /*********QQCall End********/

    /**
     * 查询小微的好友列表
     *
     * @return
     */
    static native String getAIAudioFriendList();

    /**
     * 请求指定格式的TTS，给视频通话、消息、导航等特殊场景使用
     *
     * @param tinyid    目标用户id，电话和消息需要填写
     * @param timestamp 时间 ,消息需要填，其余填0
     * @param type      类别
     * @return TTS的resId
     */
    static native String requestProtocolTTS(long tinyid, long timestamp, int type);

    /**
     * 发送音频消息，使用sendMessage代替
     *
     * @param msgId     动态消息ID，请到配置平台进行注册，没有注册过的ID的消息无法送到手机QQ
     * @param file_path 文件路径
     * @param duration  音频时长
     * @param targetIds 接收者的tinyid, 填空表示发送给所有绑定者
     * @return cookie，可以根据该cookie值在OnRichMsgSendProgress和OnRichMsgSendRet回调中查询进度信息和结果信息
     */
    static native long nativeSendAudioMsg(int msgId, String file_path, int duration, long[] targetIds);

    /************************************jni callback begin************************************/

    /**
     * 请求响应
     *
     * @param msg
     * @param length
     */
    private void onReceiveQQCallReply(byte[] msg, int length) {
        Bundle bundle = new Bundle();
        bundle.putByteArray("msg", msg);
        XWAVChatManager.callbackAVChatEvent(XWAVChatManager.SUB_ACTION_ON_RECEIVE_QQ_CALL_REPLY, bundle);
    }

    private void onSendVideoCall(byte[] msg) {
        Bundle bundle = new Bundle();
        bundle.putByteArray("msg", msg);
        XWAVChatManager.callbackAVChatEvent(XWAVChatManager.SUB_ACTION_ON_SEND_VIDEO_CALL, bundle);
    }

    private void onSendVideoCallM2M(byte[] msg) {
        Bundle bundle = new Bundle();
        bundle.putByteArray("msg", msg);
        XWAVChatManager.callbackAVChatEvent(XWAVChatManager.SUB_ACTION_ON_SEND_VIDEO_CALL_M2M, bundle);
    }

    private void onSendVideoCMD(byte[] msg) {
        Bundle bundle = new Bundle();
        bundle.putByteArray("msg", msg);
        XWAVChatManager.callbackAVChatEvent(XWAVChatManager.SUB_ACTION_ON_SEND_VIDEO_CMD, bundle);
    }

    private void onReceiveVideoBuffer(byte[] msg, long uin, int uinType) {
        Log.d(TAG, "onReceiveVideoBuffer");
        Bundle bundle = new Bundle();
        bundle.putByteArray("msg", msg);
        bundle.putLong("uin", uin);
        bundle.putInt("uinType", uinType);
        XWAVChatManager.callbackAVChatEvent(XWAVChatManager.SUB_ACTION_ON_RECEIVE_VIDEO_BUFFER, bundle);
    }

    private void onLoginComplete(int error) {
        XWDeviceBaseManager.onLoginComplete(error);
    }

    private void onConnectedServer(int error) {
        XWDeviceBaseManager.onConnectedServer(error);
    }

    private void onRegisterResult(int error, int sub) {
        XWDeviceBaseManager.onRegister(error, sub);
    }

    private void onOnlineSuccess() {
        mSelfDin = XWSDKJNI.getSelfDin();
        XWSDK.online = true;
        XWDeviceBaseManager.onOnlineSuccess();
    }

    private void onOfflineSuccess() {
        XWSDK.online = false;
        XWDeviceBaseManager.onOfflineSuccess();
    }

    private void onWlanUploadRegInfoSuccess(int error) {
        XWDeviceBaseManager.onUploadRegInfoSuccess(error);
    }

    private void onUnBind(int error) {
        XWDeviceBaseManager.onUnBind(error);
    }

    private void onBinderListChange(int error, XWBinderInfo[] listBinder) {
        synchronized (mBinderList) {
            mBinderList.clear();
            for (int i = 0; listBinder != null && i < listBinder.length; ++i) {
                if (null == listBinder[i]) {
                    QLog.d(TAG, "onBinderListChange: listBinder[" + i + "] is null ");
                    continue;
                }
                mBinderList.add(listBinder[i]);
                XWDeviceBaseManager.mAllFriendListCache.put(listBinder[i].tinyID, listBinder[i]);

                QLog.d(TAG, "onBinderListChange: " + listBinder[i].type + " " + listBinder[i].tinyID + " " + " " + listBinder[i].remark + " " + listBinder[i].headUrl + " online: " + listBinder[i].online);
            }
        }
        XWDeviceBaseManager.onBinderListChange(error, getBinderList());
    }

    private void onUploadLogResult(int errCode, String sessionID) {
        String[] pathAndUrl = mLogMap.get(sessionID);
        if (pathAndUrl != null && pathAndUrl.length == 2 && !TextUtils.isEmpty(pathAndUrl[0])) {
            if (errCode == 0) {
                new File(pathAndUrl[0]).delete();
                // 日志上报成功
            } else {
                new File(pathAndUrl[0]).renameTo(new File(pathAndUrl[0] + ".failed"));
                // 日志上报失败
                QLog.e(TAG, "日志上报失败 " + pathAndUrl[1]);
            }
        }
    }

    private void onTransferProgress(long cookie, long progress, long maxProgress) {
        XWFileTransferManager.onFileTransferProgress(cookie, progress, maxProgress);
    }

    private void onTransferComplete(long cookie, int errCode, XWFileTransferInfo info) {
        info.id = cookie;
        info.sender = mSelfDin;
        XWFileTransferManager.onFileTransferComplete(info, errCode);
    }

    private void onReceiveWifiInfo(String ssid, String pwd, int ip, int port) {
        XWVoiceLinkManager.onReceiveWifiInfo(ssid, pwd, ip, port);
    }

    private void onOTAInfo(int from, boolean force, int version, String title, String desc, String url, String md5) {
        XWOTAManager.onOTAInfo(from, force, version, title, desc, url, md5);
    }


    private void onGetSDKLog(int level, String module, int line, String message) {
        if (level == 0 || level == 1) {
            QLog.e("XiaoWeiSDK", message);
        } else if (level == 2) {
            QLog.w("XiaoWeiSDK", message);
        } else if (level == 3) {
            QLog.i("XiaoWeiSDK", message);
        } else if (level == 4) {
            QLog.d("XiaoWeiSDK", message);
        } else {
            QLog.v("XiaoWeiSDK", message);
        }

        XWDeviceBaseManager.onGetSDKLog(level, module, line, message);
    }


    private boolean onRequestCallback(String voiceID, int event, XWResponseInfo rspData, byte[] extendData) {
        return XWSDK.getInstance().onRequest(voiceID, event, rspData, extendData);
    }

    private void onSetAlarmCallback(int errCode, String strVoiceID, int clockId) {
        XWSDK.getInstance().onSetAlarmCallback(errCode, strVoiceID, clockId);
    }

    private boolean onNetworkDelayCallback(String voiceID, long time) {
        return XWSDK.getInstance().onNetWorkDelay(voiceID, time);
    }

    private boolean onTTSPushCallback(String voiceID, XWTTSDataInfo tts) {
        return XWSDK.getInstance().onTTSPushData(voiceID, tts);
    }

    private void onGetAlaramList(int errCode, String strVoiceID, String[] arrayAlaramList) {
        XWSDK.getInstance().onGetAIAudioAlarmList(errCode, strVoiceID, arrayAlaramList);
    }

    /**
     * 发送c2c消息的结果通知
     *
     * @param cookie  sendCCMsg返回的cookie
     * @param to      对方的id
     * @param errCode 发送结果
     */
    private void onAISendCCMsgResult(int cookie, long to, int errCode) {
        XWCCMsgManager.onSendCCMsgResult(cookie, to, errCode);
    }

    /**
     * 收到app发的c2c消息
     *
     * @param from 发送方的id
     * @param msg  cc消息结构
     */
    private void onAIReceiveC2CMsg(long from, XWCCMsgInfo msg) {
        XWCCMsgManager.onReceiveC2CMsg(from, msg);
    }

    /**
     * 自动下载的判断通知
     *
     * @param size    下载文件的大小
     * @param channel 文件传输的使用的通道
     * @return 0 for download, other for no download
     */
    private int onAutoDownloadFileCallback(long size, int channel) {
        return XWFileTransferManager.onAutoDownloadFileCallback(size, channel);
    }

    /************************************jni callback end************************************/

    void uploadLogs(String start, String end) {
        if (TextUtils.isEmpty(start)) {
            start = "" + QLogUtil.getSerializeTime(System.currentTimeMillis() - 2 * 60 * 60 * 1000l);
        }
        if (TextUtils.isEmpty(end)) {
            end = "" + QLogUtil.getSerializeTime(System.currentTimeMillis());
        }
        final String serializeTime1 = start;
        final String serializeTime2 = end;
        new Thread() {
            public void run() {
                final String path = QLog.createLogFile(serializeTime1, serializeTime2, false);
                if (path == null) {
                    uploadLogs(serializeTime1, serializeTime2, "没有日志", path);
                    return;
                }
                QLog.d(TAG, "准备上报 " + path);
                XWFileTransferManager.uploadFile(path, XWFileTransferInfo.TYPE_TRANSFER_CHANNEL_MINI, XWFileTransferInfo.TYPE_TRANSFER_FILE_OTHER, new XWSDK.OnFileTransferListener() {
                    @Override
                    public void onProgress(long transferProgress, long maxTransferProgress) {

                    }

                    @Override
                    public void onComplete(XWFileTransferInfo info, int errorCode) {
                        String url;
                        if (errorCode == 0) {
                            url = XWFileTransferManager.getMiniDownloadURL(new String(info.fileKey), XWFileTransferInfo.TYPE_TRANSFER_FILE_OTHER) + "&filename=log.zip";
                        } else if (errorCode == 28) {
                            QLog.e(TAG, "日志上传失败，频率太快，马上重试。");
                            // 刚上线的时候上报会出错，重试
                            postMainDelay(new Runnable() {
                                @Override
                                public void run() {
                                    uploadLogs(serializeTime1, serializeTime2);
                                }
                            }, new Random().nextInt(2000) + 1000);
                            return;
                        } else {
                            url = "upload file failed " + errorCode;
                            QLog.e(TAG, "日志上传失败 " + errorCode);
                        }
                        uploadLogs(serializeTime1, serializeTime2, url, path);
                    }
                });
            }
        }.start();
    }


    private void uploadLogs(String serializeTime1, String serializeTime2, final String url, final String path) {
        QLog.d(TAG, "uploadLogs " + url);
        String session = uploadLog(url, serializeTime1 + "-" + serializeTime2);
        mLogMap.put(session, new String[]{path, url});

    }

    public static ArrayList<XWBinderInfo> getBinderList() {
        synchronized (mBinderList) {
            ArrayList<XWBinderInfo> list = new ArrayList<>(mBinderList.size());
            for (XWBinderInfo info : mBinderList) {
                try {
                    list.add((XWBinderInfo) info.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
            return list;
        }
    }

    private void onGetBinderRemarkList(final int cookie, final XWBinderRemark[] binderRemarks) {
        // 更新remark到binderlist
        if (binderRemarks != null) {
            for (XWBinderRemark remark : binderRemarks) {
                XWContactInfo info = XWDeviceBaseManager.mAllFriendListCache.get(remark.tinyid);
                if (info != null) {
                    info.remark = remark.remark;
                }
            }
        }
        XWDeviceBaseManager.onGetBinderRemarkList(cookie, binderRemarks);
    }

    public static void downloadMiniFile(String fileKey, int fileType, String miniToken, XWSDK.OnFileTransferListener listener) {
        XWFileTransferManager.downloadMiniFile(fileKey, fileType, miniToken, listener);
    }

    public static long sendMessage(XWeiMessageInfo msg, XWSDK.OnSendMessageListener listener) {
        return XWeiMsgManager.sendMessage(msg, listener);
    }

    private void OnRichMsgSendProgress(int cookie, long transfer_progress, long max_transfer_progress) {
        XWeiMsgManager.OnRichMsgSendProgress(cookie, transfer_progress, max_transfer_progress);
    }

    private void OnRichMsgSendRet(int cookie, int err_code) {
        XWeiMsgManager.OnRichMsgSendRet(cookie, err_code);
    }
}
