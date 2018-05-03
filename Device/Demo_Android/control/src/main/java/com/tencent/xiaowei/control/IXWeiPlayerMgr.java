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
import com.tencent.xiaowei.control.info.XWeiPlayState;

/**
 * 播放器管理
 */
public interface IXWeiPlayerMgr {

    IXWeiPlayer getXWeiPlayer(int sessionId);

    /**
     * 这个场景播放结束
     *
     * @param sessionId 场景相关联的id
     * @return
     */
    boolean OnPlayFinish(int sessionId);

    /**
     * 需要停止播放
     *
     * @param sessionId
     * @return
     */
    boolean OnStopPlayer(int sessionId);

    /**
     * 需要暂停播放
     *
     * @param sessionId
     * @param pause
     * @return
     */
    boolean OnPausePlayer(int sessionId, boolean pause);

    /**
     * 需要改变音量
     *
     * @param sessionId
     * @param volume
     * @return
     */
    boolean OnChangeVolume(int sessionId, int volume);

    /**
     * 修改了循环模式
     *
     * @param sessionId
     * @param repeatMode
     * @return
     */
    boolean OnSetRepeatMode(int sessionId, int repeatMode);

    /**
     * 需要添加界面到UI
     *
     * @param sessionId
     * @param mediaInfoArray
     * @return
     */
    boolean OnPlaylistAddAlbum(int sessionId, XWeiMediaInfo[] mediaInfoArray);

    /**
     * 需要添加列表到UI
     *
     * @param sessionId
     * @param isFront
     * @param mediaInfoArray
     * @return
     */
    boolean OnPlaylistAddItem(int sessionId, boolean isFront, XWeiMediaInfo[] mediaInfoArray);

    /**
     * 需要更新列表元素
     *
     * @param sessionId
     * @param mediaInfoArray
     * @return
     */
    boolean OnPlaylistUpdateItem(int sessionId, XWeiMediaInfo[] mediaInfoArray);

    /**
     * 需要移除列表元素
     *
     * @param sessionId
     * @param mediaInfoArray
     * @return
     */
    boolean OnPlaylistRemoveItem(int sessionId, XWeiMediaInfo[] mediaInfoArray);

    /**
     * 收到了一个Media
     *
     * @param sessionId
     * @param mediaInfo
     * @param needReleaseRes
     * @return
     */
    boolean OnPushMedia(int sessionId, XWeiMediaInfo mediaInfo, boolean needReleaseRes);

    /**
     * 收藏状态改变
     *
     * @param event
     * @param playId
     * @return
     */
    boolean OnFavoriteEvent(String event, String playId);

    /**
     * 需要自动唤醒
     *
     * @param sessionId
     * @param context_id
     * @param speak_timeout
     * @param silent_timeout
     * @return
     */
    boolean OnSupplement(int sessionId, String context_id, int speak_timeout, int silent_timeout, long requestParam);

    /**
     * 需要播放提示
     *
     * @param sessionId
     * @param tipsType
     */
    void OnTips(int sessionId, int tipsType);

    /**
     * 需要上报播放状态
     *
     * @param sessionId
     * @param state
     */
    void onNeedReportPlayState(int sessionId, XWeiPlayState state);

    /**
     * 通知下载消息文件
     *
     * @param sessionId
     * @param tinyId
     * @param channel
     * @param type
     * @param key1
     * @param key2
     * @param duration
     */
    void onDownloadMsgFile(int sessionId, long tinyId, int channel, int type, String key1,
                           String key2, int duration, int timestamp);

    /**
     * 通知开始语音消息录音
     *
     * @param sessionId
     */
    void onAudioMsgRecord(int sessionId);

    /**
     * 通知发送语音消息
     *
     * @param sessionId
     * @param tinyId
     */
    void onAudioMsgSend(int sessionId, long tinyId);
}
