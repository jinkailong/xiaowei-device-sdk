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
package com.tencent.xiaowei.control;

import com.tencent.xiaowei.control.info.XWeiMediaInfo;
import com.tencent.xiaowei.control.info.XWeiMsgInfo;
import com.tencent.xiaowei.control.info.XWeiPlayState;
import com.tencent.xiaowei.info.XWResponseInfo;
import com.tencent.xiaowei.sdk.XWSDK;
import com.tencent.xiaowei.util.QLog;

import java.util.Arrays;

/**
 * 控制层入口类
 */
public class XWeiControl {
    private static final String TAG = "XWeiControl";
    private XWeiApp xWeiApp = null;
    private XWeiMedia xWeiMedia = null;
    private XWeiCommon xWeiCommon = null;
    private XWeiOuterSkill xWeiOuterSkill = null;

    private static volatile XWeiControl mInstance;
    private IXWeiPlayerMgr mIXWeiPlayerMgr;

    private XWeiControl() {
    }

    public static synchronized XWeiControl getInstance() {
        if (mInstance == null) {
            mInstance = new XWeiControl();
        }
        return mInstance;
    }

    public void init() {
        boolean load_failed = false;
        String[] so = {"stlport_shared", "xiaoweiSDK", "xiaowei", "CtrlModule", "control"};
        for (String s : so) {
            try {
                System.loadLibrary(s);
            } catch (UnsatisfiedLinkError e) {
                load_failed = true;
                e.printStackTrace();
            }
        }

        if (!load_failed) {
            nativeInit();

            xWeiApp = new XWeiApp();
            xWeiApp.nativeInit();

            xWeiMedia = new XWeiMedia();
            xWeiMedia.nativeInit();

            xWeiCommon = new XWeiCommon();
            xWeiCommon.nativeInit();

            xWeiOuterSkill = new XWeiOuterSkill();
            xWeiOuterSkill.nativeInit();
        }
    }

    public void uninit() {
        xWeiApp.nativeUninit();
        xWeiMedia.nativeUninit();
        xWeiCommon.nativeUninit();
        xWeiOuterSkill.nativeUninit();

        nativeUninit();
    }

    public XWeiApp getAppTool() {
        return xWeiApp;
    }

    public XWeiMedia getMediaTool() {
        return xWeiMedia;
    }

    public XWeiCommon getCommonTool() {
        return xWeiCommon;
    }

    public XWeiOuterSkill getXWeiOuterSkill() {
        return xWeiOuterSkill;
    }

    private native void nativeInit();

    private native void nativeUninit();

    /**
     * * 往小微的播放控制的焦点管理器中申请一个应用层的焦点，整个应用层公用一个
     *
     * @param cookie   上层可以传下去，如果小于0，会重新生成并返回真正的cookie
     * @param duration 焦点类型
     * @return cookie
     */
    native int nativeRequestAudioFocus(int cookie, int duration);

    /**
     * 释放应用层的焦点
     */
    native void nativeAbandonAudioFocus(int cookie);

    /**
     * 释放所有的焦点，包括应用层和播放控制SDK中的
     */
    native void nativeAbandonAllAudioFocus();

    /**
     * 设置当前App获得的系统焦点
     *
     * @param focus 可以是FOCUS_TYPE，定义在AudioFocus.h
     */
    native void nativeSetAudioFocus(int focus);

    /**
     * 处理ASR/NLP或其他通用请求的数据处理
     *
     * @param voiceId    请求VoiceId
     * @param rspData    响应数据
     * @param extendData 扩展数据
     */
    public boolean processResponse(String voiceId, XWResponseInfo rspData, byte[] extendData) {
        return nativeProcessResponse(voiceId, XWSDK.EVENT_RESPONSE, rspData, extendData);
    }

    /**
     * 扩展处理response, 增加event事件分类
     *
     * @param voiceId    请求VoiceId
     * @param event      事件类型
     * @param rspData    响应数据
     * @param extendData 扩展数据
     * @return 是否处理
     */
    public boolean processResponse(String voiceId, int event, XWResponseInfo rspData, byte[] extendData) {
        return nativeProcessResponse(voiceId, event, rspData, extendData);
    }

    native boolean nativeProcessResponse(String voiceId, int event, XWResponseInfo rspData, byte[] extendData);

    /**
     * 添加消息到控制层的消息盒子，用于下载完成的文件类消息添加到消息盒子
     *
     * @param info 消息的内容
     */
    native void nativeAddMsgToMsgbox(XWeiMsgInfo info);

    /**
     * 设置播放器管理器
     *
     * @param mIXWeiPlayerMgr 播放器管理接口
     */
    public void setXWeiPlayerMgr(IXWeiPlayerMgr mIXWeiPlayerMgr) {
        this.mIXWeiPlayerMgr = mIXWeiPlayerMgr;
    }

    /**
     * 获取播放器管理器
     *
     * @return 播放器管理器
     */
    public IXWeiPlayerMgr getXWeiPlayerMgr() {
        return mIXWeiPlayerMgr;
    }

    /**
     * native层日志回调（JNI回调）
     *
     * @param level   级别
     * @param module  模块
     * @param line    行数
     * @param message 日志信息
     */
    private void onNativeLog(int level, String module, int line, String message) {
        if (level == 0 || level == 1) {
            QLog.e("XWeiControl", "[" + module + ":" + line + "]: " + message);
        } else if (level == 2) {
            QLog.w("XWeiControl", "[" + module + ":" + line + "]: " + message);
        } else if (level == 3) {
            QLog.i("XWeiControl", "[" + module + ":" + line + "]: " + message);
        } else if (level == 4) {
            QLog.d("XWeiControl", "[" + module + ":" + line + "]: " + message);
        } else {
            QLog.v("XWeiControl", "[" + module + ":" + line + "]: " + message);
        }
    }

    /**
     * 焦点改变回调 (JNI回调)
     *
     * @param cookie
     * @param focusCount 焦点类型
     */
    private void onFocusChange(int cookie, int focusCount) {
        XWeiAudioFocusManager.getInstance().onFocusChange(cookie, focusCount);
    }

    /**
     * 停止播放（JNI回调）
     *
     * @param sessionId 场景sessionId
     * @return 是否已处理
     */
    private boolean stopPlayer(int sessionId) {
        return getXWeiPlayerMgr() != null && getXWeiPlayerMgr().OnStopPlayer(sessionId);
    }

    /**
     * 播放结束（JNI回调）
     *
     * @param sessionId 场景sessionId
     * @return 是否已处理
     */
    private boolean playFinish(int sessionId) {
        return getXWeiPlayerMgr() != null && getXWeiPlayerMgr().OnPlayFinish(sessionId);
    }

    /**
     * 暂停/恢复播放（JNI回调）
     *
     * @param sessionId 场景sessionId
     * @param pause     true表示暂停，false表示恢复
     * @return 是否已处理
     */
    private boolean pausePlayer(int sessionId, boolean pause) {
        return getXWeiPlayerMgr() != null && getXWeiPlayerMgr().OnPausePlayer(sessionId, pause);
    }

    /**
     * 修改音量（JNI回调）
     *
     * @param sessionId 场景sessionId
     * @param volume    音量值
     * @return 是否已处理
     */
    private boolean changeVolume(int sessionId, int volume) {
        return getXWeiPlayerMgr() != null && getXWeiPlayerMgr().OnChangeVolume(sessionId, volume);
    }

    /**
     * 设置播放模式（JNI回调）
     *
     * @param sessionId  场景sessionId
     * @param repeatMode 播放模式 {@link Constants.RepeatMode}
     * @return 是否已处理
     */
    private boolean onSetRepeatMode(int sessionId, int repeatMode) {
        return getXWeiPlayerMgr() != null && getXWeiPlayerMgr().OnSetRepeatMode(sessionId, repeatMode);
    }

    /**
     * 播放资源
     *
     * @param sessionId      场景sessionId
     * @param mediaInfo      媒体资源 定义请参考{@link XWeiMediaInfo}
     * @param needReleaseRes 播放完毕后释放资源
     * @return 是否已处理
     */
    private boolean onPushMedia(int sessionId, XWeiMediaInfo mediaInfo, boolean needReleaseRes) {
        QLog.d(TAG, "onPushMedia sessionId: " + sessionId + " mediaInfo: " + mediaInfo);
        return getXWeiPlayerMgr() != null && getXWeiPlayerMgr().OnPushMedia(sessionId, mediaInfo, needReleaseRes);
    }

    /**
     * 启动SKILL场景UI
     *
     * @param sessionId      场景sessionId
     * @param mediaInfoArray 媒体资源
     * @return 是否已处理
     */
    private boolean onPlaylistAddAlbum(int sessionId, XWeiMediaInfo[] mediaInfoArray) {
        return getXWeiPlayerMgr() != null && getXWeiPlayerMgr().OnPlaylistAddAlbum(sessionId, mediaInfoArray);
    }

    /**
     * 播放列表新增资源项
     *
     * @param sessionId      场景sessionId
     * @param isFront        是否从队头入队
     * @param mediaInfoArray 媒体资源
     * @return 是否已处理
     */
    private boolean onPlaylistAddItem(int sessionId, boolean isFront, XWeiMediaInfo[] mediaInfoArray) {
        return getXWeiPlayerMgr() != null && getXWeiPlayerMgr().OnPlaylistAddItem(sessionId, isFront, mediaInfoArray);
    }

    /**
     * 更新播放列表中的资源项（JNI回调）
     *
     * @param sessionId      场景sessionId
     * @param mediaInfoArray 待更新的资源项
     * @return 是否已处理
     */
    public boolean onPlaylistUpdateItem(int sessionId, XWeiMediaInfo[] mediaInfoArray) {
        return getXWeiPlayerMgr() != null && getXWeiPlayerMgr().OnPlaylistUpdateItem(sessionId, mediaInfoArray);
    }

    /**
     * 删除播放列表中的资源项（JNI回调）
     *
     * @param sessionId      场景sessionId
     * @param mediaInfoArray 待删除的资源项
     * @return 是否已处理
     */
    public boolean onPlaylistRemoveItem(int sessionId, XWeiMediaInfo[] mediaInfoArray) {
        QLog.d(TAG, "onPlaylistRemoveItem sessionId: " + sessionId + " mediaInfoArray: " + Arrays.toString(mediaInfoArray));
        return getXWeiPlayerMgr() != null && getXWeiPlayerMgr().OnPlaylistRemoveItem(sessionId, mediaInfoArray);
    }

    /**
     * 需要进行多轮会话/唤醒（JNI回调）
     *
     * @param sessionId     场景sessionId
     * @param contextId     上下文ID
     * @param speakTimeout  不说话的超时时间
     * @param silentTimeout 静音尾点超时时间
     * @param requestParam  请求基础参数
     * @return 是否已处理
     */
    public boolean onSupplement(int sessionId, String contextId, int speakTimeout, int silentTimeout, long requestParam) {
        return getXWeiPlayerMgr() != null && getXWeiPlayerMgr().OnSupplement(sessionId, contextId, speakTimeout, silentTimeout, requestParam);
    }

    /**
     * 控制层需要应用播放提示信息（JNI回调）
     *
     * @param sessionId 场景sessionId
     * @param tipsType  提示类型定义 请参考{@link Constants.TXPlayerTipsType}
     */
    private void onTips(int sessionId, int tipsType) {
        if (getXWeiPlayerMgr() != null) {
            getXWeiPlayerMgr().OnTips(sessionId, tipsType);
        }
    }

    /**
     * 控制层需要应用层上报状态（JNI回调）
     *
     * @param sessionId 场景sessionId
     * @param state     上报状态
     */
    private void onNeedReportPlayState(int sessionId, XWeiPlayState state) {
        QLog.e(TAG, "onNeedReportPlayState: " + sessionId + " " + state);
        if (getXWeiPlayerMgr() != null) {
            getXWeiPlayerMgr().onNeedReportPlayState(sessionId, state);
        }
    }

    /**
     * 控制层通知下载消息文件
     *
     * @param tinyId   发送者id
     * @param channel  使用的文件通道
     * @param type     消息文件的类型
     * @param key1     文件的下载key
     * @param key2     文件的校验key
     * @param duration 消息的时长
     */
    public void onDownloadMsgFile(int sessionId, long tinyId, int channel, int type, String key1,
                                  String key2, int duration, int timestamp) {
        QLog.e(TAG, "onDownloadMsgFile: " + sessionId + " " + tinyId);
        if (getXWeiPlayerMgr() != null) {
            getXWeiPlayerMgr().onDownloadMsgFile(sessionId, tinyId, channel, type, key1, key2, duration, timestamp);
        }
    }

    public void addMsgToMsgbox(XWeiMsgInfo msgInfo) {
        nativeAddMsgToMsgbox(msgInfo);
    }

    /**
     * 通知消息开始录音
     *
     * @param sessionId
     */
    public void onAudioMsgRecord(int sessionId) {
        QLog.d(TAG, "onAudioMsgRecord: " + sessionId);
        if (getXWeiPlayerMgr() != null) {
            getXWeiPlayerMgr().onAudioMsgRecord(sessionId);
        }
    }

    /**
     * 通知消息发送
     *
     * @param sessionId
     * @param tinyId
     */
    public void onAudioMsgSend(int sessionId, long tinyId) {
        QLog.d(TAG, "onAudioMsgSend: " + sessionId);
        if (getXWeiPlayerMgr() != null) {
            getXWeiPlayerMgr().onAudioMsgSend(sessionId, tinyId);
        }
    }
}
