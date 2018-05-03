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

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.tencent.xiaowei.def.XWCommonDef;
import com.tencent.xiaowei.info.XWAccountInfo;
import com.tencent.xiaowei.info.XWAppInfo;
import com.tencent.xiaowei.info.XWContextInfo;
import com.tencent.xiaowei.info.XWEventLogInfo;
import com.tencent.xiaowei.info.XWFileTransferInfo;
import com.tencent.xiaowei.info.XWLoginStatusInfo;
import com.tencent.xiaowei.info.XWPlayStateInfo;
import com.tencent.xiaowei.info.XWResponseInfo;
import com.tencent.xiaowei.info.XWTTSDataInfo;
import com.tencent.xiaowei.info.XWeiMessageInfo;
import com.tencent.xiaowei.util.QLog;
import com.tencent.xiaowei.util.Singleton;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 语音服务接口
 */
public class XWSDK {
    private static final String TAG = "XWSDK";

    private ConcurrentHashMap<String, RequestListener> mMapRequestListener = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, GetAlarmListRspListener> mDeviceGetAlarmListListener = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, SetAlarmRspListener> mDeviceSetAlarmListener = new ConcurrentHashMap<>();
    private OnSetWordsListListener mSetWordsListListener = null;
    static boolean online;
    private Context mContext;
    private Thread mUiThread;
    private Handler mainHandler;

    private AudioRequestListener audioRequestListener;
    private NetworkDelayListener mNetworkDelayListener;
    private OnReceiveTTSDataListener mOnReceiveTTSDataListener;

    //response event enum
    public final static int EVENT_IDLE = 0;          //空闲
    public final static int EVENT_REQUEST_START = 1; //请求开始
    public final static int EVENT_SPEAK = 2;         //检测到说话
    public final static int EVENT_SILENT = 3;        //检测到静音
    public final static int EVENT_RECOGNIZE = 4;     //识别到文本实时返回
    public final static int EVENT_RESPONSE = 5;      //请求收到响应
    public final static int EVENT_TTS = 6;           //小微后台推送的tts

    private XWSDK() {

    }

    private static final Singleton<XWSDK> sSingleton = new Singleton<XWSDK>() {
        @Override
        protected XWSDK createInstance() {
            return new XWSDK();
        }
    };

    /**
     * 获取语音服务实例
     *
     * @return
     */
    public static XWSDK getInstance() {
        return sSingleton.getInstance();
    }

    /**
     * 初始化语音服务
     *
     * @param context     上下文对象，不能为null
     * @param accountInfo 账户信息，使用小微App对接传null即可
     */
    public int init(Context context, XWAccountInfo accountInfo) {
        mContext = context.getApplicationContext();
        if (mainHandler == null) {
            mainHandler = new Handler(context.getMainLooper());
        }
        if (mUiThread == null) {
            mUiThread = context.getMainLooper().getThread();
        }
        return XWSDKJNI.startXiaoweiService(accountInfo);
    }

    /**
     * 反初始化语音服务
     */
    public int unInit() {
        return XWSDKJNI.stopXiaoweiService();
    }

    /**
     * 设置语音请求状态回调接口
     */
    public void setAudioRequestListener(AudioRequestListener listener) {
        this.audioRequestListener = listener;
    }

    /**
     * 语音请求
     *
     * @param type        请求类型 {@link XWCommonDef.RequestType}
     * @param requestData 请求数据
     * @param context     上下文，用于携带额外的会话信息
     * @return 本次请求对应的voiceID
     */
    public String request(int type, byte[] requestData, XWContextInfo context) {
        String strVoiceID = XWSDKJNI.request(type, requestData, context);
        if (strVoiceID.isEmpty()) {
            QLog.e(TAG, "request voiceID is null.");
            return null;
        }

        return strVoiceID;
    }


    /**
     * 取消语音请求
     *
     * @param voiceId 要取消的voiceID，当为0的时候，表示取消所有请求
     */
    public int requestCancel(String voiceId) {
        return XWSDKJNI.cancelRequest(voiceId);
    }


    /**
     * 根据文本转TTS
     *
     * @param strText     请求的文本
     * @param contextInfo 上下文
     * @param listener    回调监听
     * @return 本次请求对应的VoiceID
     */
    public String requestTTS(@NonNull byte[] strText, XWContextInfo contextInfo, RequestListener listener) {

        String strVoiceID = XWSDKJNI.request(XWCommonDef.RequestType.ONLY_TTS, strText, contextInfo);

        if (strVoiceID.isEmpty()) {
            QLog.e(TAG, "request voiceID is null.");
            return strVoiceID;
        }

        if (listener != null) {
            mMapRequestListener.put(strVoiceID, listener);
        }

        return strVoiceID;
    }

    /**
     * 取消TTS的传输
     *
     * @param resId
     */
    public void cancelTTS(String resId) {
        XWSDKJNI.cancelTTS(resId);
    }

    /**
     * 拉取更多列表请求
     * 在Response.hasMorePlaylist，且当前列表已经快播放完成或者用户滑动到底部时调用
     *
     * @param appInfo     场景信息，表示在哪个场景下，该接口暂时只支持音乐场景
     * @param playID      当前正在播放的playID
     * @param maxListSize 要拉取的最大数量，暂时不支持自定义，向下是6，向上是20
     * @param isUp        往前面查询
     * @param listener    请求回调
     * @return 本次请求对应的voiceID
     */
    public String getMorePlaylist(XWAppInfo appInfo, String playID, int maxListSize, boolean isUp, RequestListener listener) {
        if (null == listener) {
            return "";
        }

        String strVoiceID = XWSDKJNI.getMorePlaylist(appInfo, playID, maxListSize, isUp);
        if (strVoiceID.isEmpty()) {
            return strVoiceID;
        }

        mMapRequestListener.put(strVoiceID, listener);
        return strVoiceID;
    }


    /**
     * 拉取播放资源详情
     * 用于拉取歌词、是否收藏等信息时调用
     *
     * @param appInfo    场景信息，表示在哪个场景下，该接口暂时只支持音乐场景
     * @param listPlayID 要拉取详情的playID
     * @param listener   请求回调
     * @return 本次请求对应的voiceID
     */
    public String getPlayDetailInfo(XWAppInfo appInfo, String[] listPlayID, RequestListener listener) {
        if (null == listener) {
            return "";
        }

        String strVoiceID = XWSDKJNI.getPlayDetailInfo(appInfo, listPlayID);
        if (strVoiceID.isEmpty()) {
            return strVoiceID;
        }

        mMapRequestListener.put(strVoiceID, listener);
        return strVoiceID;
    }

    /**
     * 更新播放列表url信息
     *
     * @param appInfo    场景信息，表示在哪个场景下，该接口暂时只支持音乐场景
     * @param listPlayID 要拉取详情的playID
     * @param listener   请求回调
     * @return 本次请求对应的voiceID
     */
    public String refreshPlayList(XWAppInfo appInfo, String[] listPlayID, RequestListener listener) {
        String strVoiceID = XWSDKJNI.refreshPlayList(appInfo, listPlayID);
        if (strVoiceID.isEmpty()) {
            return strVoiceID;
        }

        if (listener != null) {
            mMapRequestListener.put(strVoiceID, listener);
        }

        return strVoiceID;
    }

    /**
     * 设置QQ音乐品质
     *
     * @param quality {@link XWCommonDef.PlayQuality}
     * @return 返回值请参考 {@link XWCommonDef.ErrorCode}
     */
    public int setMusicQuality(int quality) {
        return XWSDKJNI.setQuality(quality);
    }

    void runOnMainThread(Runnable runnable) {
        if (mUiThread == Thread.currentThread()) {
            runnable.run();
            return;
        }

        if (mainHandler != null) {
            mainHandler.post(runnable);
        }
    }

    boolean onRequest(final String voiceID, final int event, final XWResponseInfo rspData, final byte[] extendData) {
        RequestListener listener = mMapRequestListener.remove(voiceID);
        if (listener != null) {
            return listener.onRequest(event, rspData, extendData);
        } else if (audioRequestListener != null) {
            return audioRequestListener.onRequest(voiceID, event, rspData, extendData);
        }

        return false;
    }

    boolean onTTSPushData(String voiceId, XWTTSDataInfo tts) {
        if (mOnReceiveTTSDataListener != null) {
            return mOnReceiveTTSDataListener.onReceive(voiceId, tts);
        }
        return false;
    }

    boolean onNetWorkDelay(final String voiceID, final long time) {
        if (mNetworkDelayListener != null) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    mNetworkDelayListener.onDelay(voiceID, time);
                }
            });
        }
        return true;
    }

    /**
     * 事件上报
     *
     * @param log
     */
    public void reportEvent(XWEventLogInfo log) {
        XWSDKJNI.reportEvent(log);
    }

    /**
     * 上报播放状态
     *
     * @param stateInfo 要上报的当前状态
     */
    public int reportPlayState(XWPlayStateInfo stateInfo) {
        QLog.d(TAG, "reportPlayState " + stateInfo);
        return XWSDKJNI.reportPlayState(stateInfo);
    }


    /**
     * 通用请求回调通知
     */
    public interface RequestListener {
        /**
         * 收到通知
         * 根据event分为多种事件
         *
         * @param event      当前通知事件 {@link XWCommonDef.XWEvent}
         * @param rspData    标准信息
         * @param extendData 扩展信息
         */
        boolean onRequest(int event, XWResponseInfo rspData, byte[] extendData);
    }

    /**
     * 语音服务状态回调接口
     */
    public interface AudioRequestListener {
        /**
         * 收到通知
         * 根据event分为多种事件
         *
         * @param voiceId    当次请求的VoiceId
         * @param event      当前通知事件 {@link XWCommonDef.XWEvent}
         * @param rspData    标准信息
         * @param extendData 扩展信息
         * @return 收到状态通知后，上层是否需要处理
         */
        boolean onRequest(String voiceId, int event, XWResponseInfo rspData, byte[] extendData);
    }

    /**
     * 网络延迟的监听，每次小微的网络请求都会回调，从开始发送到收到响应的时差
     */
    public interface NetworkDelayListener {
        void onDelay(String voiceId, long ms);
    }

    /**
     * 设置网络延迟的监听
     *
     * @param listener {@link NetworkDelayListener}
     */
    public void setNetworkDelayListener(NetworkDelayListener listener) {
        mNetworkDelayListener = listener;
    }

    /**
     * 网络延迟的监听，每次小微的网络请求都会回调，从开始发送到收到响应的时差
     */
    public interface OnReceiveTTSDataListener {
        boolean onReceive(String voiceId, XWTTSDataInfo ttsData);
    }

    /**
     * 设置接受TTS数据的监听
     *
     * @param listener {@link OnReceiveTTSDataListener}
     */
    public void setOnReceiveTTSDataListener(OnReceiveTTSDataListener listener) {
        mOnReceiveTTSDataListener = listener;
    }

    /**
     * 拉取提醒的回调定义
     */
    public interface GetAlarmListRspListener {
        /**
         * 拉取提醒类型回调方法
         *
         * @param errCode        返回码，请参考 {@link XWCommonDef.ErrorCode}
         * @param strVoiceID     请求VoiceID
         * @param arrayAlarmList 提醒列表
         */
        void onGetAlarmList(int errCode, String strVoiceID, String[] arrayAlarmList);
    }

    /**
     * 设置/更新/删除提醒的回调定义
     */
    public interface SetAlarmRspListener {
        /**
         * 设置/更新/删除提醒的回调方法
         *
         * @param errCode    返回码，请参考 {@link XWCommonDef.ErrorCode}
         * @param strVoiceID 请求VoiceID
         * @param alarmId    设置/更新/删除的闹钟ID
         */
        void onSetAlarmList(int errCode, String strVoiceID, int alarmId);
    }


    /**
     * 获取提醒列表
     *
     * @param listener 响应回调接口 定义请参考{@link XWSDK.GetAlarmListRspListener}
     * @return 接口调用结果，请参考{@link XWCommonDef.ErrorCode}
     */
    public int getDeviceAlarmList(GetAlarmListRspListener listener) {
        if (mContext == null) {
            throw new RuntimeException("You need to call init at first.");
        }

        String voiceId = XWSDKJNI.getDeviceAlarmList();

        if (!TextUtils.isEmpty(voiceId)) {
            mDeviceGetAlarmListListener.put(voiceId, listener);
        } else {
            listener.onGetAlarmList(XWCommonDef.ErrorCode.ERROR_FAILED, voiceId, null);
        }

        return (TextUtils.isEmpty(voiceId) ? XWCommonDef.ErrorCode.ERROR_FAILED : XWCommonDef.ErrorCode.ERROR_NULL_SUCC);
    }

    /**
     * 回调通知提醒列表
     *
     * @param errCode        错误码
     * @param strVoiceID     请求id
     * @param arrayAlarmList 提醒列表
     */
    public void onGetAIAudioAlarmList(final int errCode, final String strVoiceID, final String[] arrayAlarmList) {

        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                GetAlarmListRspListener listener = mDeviceGetAlarmListListener.remove(strVoiceID);
                if (listener != null) {
                    listener.onGetAlarmList(errCode, strVoiceID, arrayAlarmList);
                }
            }
        });
    }

    /**
     * 设置闹钟或提醒
     *
     * @param opType       操作类型 1.增加 2.修改 3.删除 {@link XWCommonDef.AlarmOptType}
     * @param strAlarmJson 操作对应的json结构
     * @param listener     设置结果的回调通知 定义请参考{@link XWSDK.SetAlarmRspListener}
     * @return 接口调用返回结果 请参考{@link XWCommonDef.ErrorCode}
     */
    public int setDeviceAlarmInfo(int opType, String strAlarmJson, SetAlarmRspListener listener) {
        if (mContext == null) {
            throw new RuntimeException("You need to call init at first.");
        }

        String voiceId = XWSDKJNI.setDeviceAlarm(opType, strAlarmJson);

        if (!TextUtils.isEmpty(voiceId)) {
            mDeviceSetAlarmListener.put(voiceId, listener);
        } else {
            listener.onSetAlarmList(XWCommonDef.ErrorCode.ERROR_FAILED, null, 0);
        }

        return (TextUtils.isEmpty(voiceId) ? XWCommonDef.ErrorCode.ERROR_FAILED : XWCommonDef.ErrorCode.ERROR_NULL_SUCC);
    }

    /**
     * 拉取定时播放任务资源
     *
     * @param strAlarmId 定时
     * @param listener   响应回调
     * @return 接口调用返回结果 请参考{@link XWCommonDef.ErrorCode}
     */
    public int getTimingSkillResource(String strAlarmId, RequestListener listener) {
        if (mContext == null) {
            throw new RuntimeException("You need to call init at first.");
        }

        String voiceId = XWSDKJNI.getTimingSkillResource(strAlarmId);

        if (!TextUtils.isEmpty(voiceId)) {
            mMapRequestListener.put(voiceId, listener);
        }

        return (TextUtils.isEmpty(voiceId) ? XWCommonDef.ErrorCode.ERROR_FAILED : XWCommonDef.ErrorCode.ERROR_NULL_SUCC);
    }

    /**
     * 上报日志文件
     */
    public void uploadLogs(String start, String end) {
        XWSDKJNI.getInstance().uploadLogs(start, end);
    }

    /**
     * 用户不满意上次的识别，将上一次的记录上报到后台
     */
    public void errorFeedBack() {
        XWSDKJNI.errorFeedBack();
    }

    /**
     * 设置主人登录态
     *
     * @param info
     * @param listener
     */
    public void setLoginStatus(XWLoginStatusInfo info, RequestListener listener) {
        if (null == listener) {
            return;
        }

        String strVoiceID = XWSDKJNI.setLoginStatus(info);
        if (strVoiceID.isEmpty()) {
            return;
        }
        mMapRequestListener.put(strVoiceID, listener);
    }

    /**
     * 获取主人登录态
     *
     * @param skillId
     * @param listener
     */
    public void getLoginStatus(String skillId, RequestListener listener) {
        if (null == listener) {
            return;
        }

        String strVoiceID = XWSDKJNI.getLoginStatus(skillId);
        if (strVoiceID.isEmpty()) {
            return;
        }
        mMapRequestListener.put(strVoiceID, listener);
    }

    /**
     * 获得音乐会员信息
     *
     * @param listener
     */
    public void getMusicVipInfo(RequestListener listener) {
        if (null == listener) {
            return;
        }

        String strVoiceID = XWSDKJNI.getMusicVipInfo();
        if (strVoiceID.isEmpty()) {
            return;
        }
        mMapRequestListener.put(strVoiceID, listener);
    }


    /**
     * 设置词表的结果回调通知
     */
    public interface OnSetWordsListListener {
        /**
         * 回调
         *
         * @param op      设置词表的操作 0:上传 1:删除
         * @param errCode 0:成功 非0:失败
         */
        void OnReply(int op, int errCode);
    }

    /**
     * 初始化设置词表回调
     *
     * @param listener 回调
     */
    public void initSetWordsListListener(OnSetWordsListListener listener) {
        mSetWordsListListener = listener;
    }

    /**
     * 开启可见可答
     *
     * @param enable true:open, false:close
     * @return 0:success else failed
     */
    public int enableV2A(boolean enable) {
        return XWSDKJNI.enableV2A(enable);
    }

    void onSetWordsListRet(int op, int errCode) {
        QLog.e(TAG, "onSetWordsListRet op:" + op + " errCode:" + errCode);
        if (mSetWordsListListener != null) {
            mSetWordsListListener.OnReply(op, errCode);
        }
    }

    /**
     * 设置词库列表
     *
     * @param type       词库类型 {@link XWCommonDef.WordListType}
     * @param words_list 词库
     * @return 0:success else failed
     */
    public int setWordslist(int type, String[] words_list) {
        return XWSDKJNI.setWordslist(type, words_list);
    }

    /**
     * 通知命令执行结果
     *
     * @param errCode    错误码
     * @param strVoiceID 请求id
     */
    void onSetAlarmCallback(final int errCode, final String strVoiceID, final int clockId) {

        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                SetAlarmRspListener listener = mDeviceSetAlarmListener.remove(strVoiceID);
                if (listener != null) {
                    listener.onSetAlarmList(errCode, strVoiceID, clockId);
                }
            }
        });
    }

    /**
     * 收藏FM或者QQ音乐，收藏结果需要关注700126的propertyId
     *
     * @param appInfo    场景信息
     * @param playId     资源ID
     * @param isFavorite true表示收藏，false表示取消收藏
     */
    public void setFavorite(XWAppInfo appInfo, String playId, boolean isFavorite) {
        XWSDKJNI.setFavorite(appInfo, playId, isFavorite);
    }

    public boolean isOnline() {
        return online;
    }

    /**
     * 请求指定格式的TTS，给视频通话、消息、导航等特殊场景使用
     *
     * @param tinyid    目标用户id，电话和消息需要填写
     * @param timestamp 时间 ,消息需要填，其余填0
     * @param type      类别 {@link XWCommonDef.RequestProtocalType}
     * @return TTS的resId
     */
    public String requestProtocolTTS(long tinyid, long timestamp, int type, RequestListener listener) {
        String strVoiceID = XWSDKJNI.requestProtocolTTS(tinyid, timestamp, type);
        if (!strVoiceID.isEmpty() && listener != null) {
            QLog.e(TAG, "requestProtocolTTS voiceId: " + strVoiceID);
            mMapRequestListener.put(strVoiceID, listener);
        }
        return strVoiceID;
    }

    /**
     * 文件传输的进度与结果
     */
    public interface OnFileTransferListener {
        /**
         * 文件传输的进度
         *
         * @param transferProgress
         * @param maxTransferProgress
         */
        void onProgress(long transferProgress, long maxTransferProgress);

        /**
         * 文件传输的结果
         *
         * @param info
         * @param errorCode
         */
        void onComplete(XWFileTransferInfo info, int errorCode);
    }

    public void downloadMiniFile(String fileKey, int fileType, String miniToken, OnFileTransferListener listener) {
        XWSDKJNI.downloadMiniFile(fileKey, fileType, miniToken, listener);
    }


    /**
     * 设置自动下载的回调通知
     */
    public interface OnAutoDownloadCallback {
        /**
         * 调用下载接口后，通知文件大小和使用的文件通道
         *
         * @param size      文件大小
         * @param channel   文件通道
         * @return          0:下载 非0:取消下载
         */
        int onDownloadFile(long size, int channel);
    }

    /**
     * 设置是否自动下载的callback
     * 可以在这里决定是否下载文件
     *
     * @param cb    回调
     */
    public void setAutoDownloadFileCallback(OnAutoDownloadCallback cb) {
        XWFileTransferManager.setAutoDownloadCallback(cb);
    }

    /**
     * 发送消息的进度和结果
     */
    public interface OnSendMessageListener {
        /**
         * 发送消息的进度
         *
         * @param transferProgress
         * @param maxTransferProgress
         */
        void onProgress(long transferProgress, long maxTransferProgress);

        /**
         * 发送消息的结果
         *
         * @param errCode
         */
        void onComplete(int errCode);
    }

    /**
     * 发送消息
     *
     * @param info 消息体
     * @param listener 监听器
     */
    public void sendMessage(XWeiMessageInfo info, OnSendMessageListener listener) {
        XWSDKJNI.sendMessage(info, listener);
    }

    /**
     * 在某些场景下，可设置设备状态，正常退出场景后，需要调用clearUserState清除
     *
     * @param stateInfo 自定义状态
     */
    public int setUserState(XWPlayStateInfo stateInfo) {
        return XWSDKJNI.setUserState(stateInfo);
    }

    /**
     * 清除自定义设备状态, 与setUserState配合使用
     *
     */
    public int clearUserState() {
        return XWSDKJNI.clearUserState();
    }

}
