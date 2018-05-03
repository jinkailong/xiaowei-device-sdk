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
package com.tencent.aiaudio.player;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.tencent.aiaudio.msg.XWeiMsgTransfer;
import com.tencent.xiaowei.control.Constants;
import com.tencent.xiaowei.control.IXWeiPlayer;
import com.tencent.xiaowei.control.IXWeiPlayerMgr;
import com.tencent.xiaowei.control.info.XWeiMediaInfo;
import com.tencent.xiaowei.control.info.XWeiPlayState;

import java.util.HashMap;

/**
 * 播放器示例
 */
public class XWeiPlayerMgr implements IXWeiPlayerMgr {

    private static int mAudioSessionId;
    private static SkillUIEventListener mSkillUIEventListener;
    private HashMap<Integer, IXWeiPlayer> players = new HashMap<>();
    private Context context;

    public static final String ACTION_ON_SUPPLEMENT = "action_on_supplement";  // 开启多轮会话
    public static final String EXTRA_KEY_SESSION_ID = "extra_key_session_id";
    public static final String EXTRA_KEY_CONTEXT_ID = "extra_key_context_id";
    public static final String EXTRA_KEY_SPEAK_TIMEOUT = "extra_key_speak_timeout";
    public static final String EXTRA_KEY_SILENT_TIMEOUT = "extra_key_silent_timeout";

    public XWeiPlayerMgr(Context context) {
        this.context = context;
    }

    public static void setAudioSessionId(int audioSessionId) {
        mAudioSessionId = audioSessionId;
    }

    public static void setPlayerEventListener(SkillUIEventListener skillUIEventListener) {
        mSkillUIEventListener = skillUIEventListener;
    }

    public synchronized IXWeiPlayer getPlayer(int sessionId) {

        IXWeiPlayer player = players.get(sessionId);

        synchronized (this) {
            if (player == null) {
                player = new XWeiPlayer(sessionId);
                players.put(sessionId, player);
            }

        }

        return player;
    }

    @Override
    public IXWeiPlayer getXWeiPlayer(int sessionId) {
        return players.get(sessionId);
    }

    @Override
    public boolean OnPlayFinish(int sessionId) {
        IXWeiPlayer player = getPlayer(sessionId);

        boolean handled = (player != null && player.stop(sessionId));

        if (mSkillUIEventListener != null) {
            mSkillUIEventListener.onFinish(sessionId);
        }

        return handled;
    }

    @Override
    public boolean OnStopPlayer(int sessionId) {
        return OnPausePlayer(sessionId, true);
    }

    @Override
    public boolean OnPausePlayer(int sessionId, boolean pause) {
        IXWeiPlayer player = getPlayer(sessionId);

        boolean handled = (player != null && (pause ? player.pause(sessionId) : player.resume(sessionId)));
        if (mSkillUIEventListener != null) {
            if (pause) {
                mSkillUIEventListener.onPause(sessionId);
            } else {
                mSkillUIEventListener.onResume(sessionId);
            }
        }

        return handled;
    }

    @Override
    public boolean OnChangeVolume(int sessionId, int volume) {
        IXWeiPlayer player = getPlayer(sessionId);
        return (player != null && player.changeVolume(sessionId, volume));
    }

    @Override
    public boolean OnSetRepeatMode(int sessionId, int repeatMode) {
        if (mSkillUIEventListener != null) {
            mSkillUIEventListener.onSetPlayMode(sessionId, repeatMode);
        }
        return true;
    }

    @Override
    public boolean OnPlaylistAddAlbum(int sessionId, XWeiMediaInfo[] mediaInfoArray) {
        if (mSkillUIEventListener != null) {
            mSkillUIEventListener.onPlaylistAddAlbum(sessionId, mediaInfoArray[0]);
        }
        return true;
    }

    @Override
    public boolean OnPlaylistAddItem(int sessionId, boolean isFront, XWeiMediaInfo[] mediaInfoArray) {
        if (mSkillUIEventListener != null) {
            mSkillUIEventListener.onPlaylistAddItem(sessionId, isFront, mediaInfoArray);
        }
        return true;
    }

    @Override
    public boolean OnPlaylistUpdateItem(int sessionId, XWeiMediaInfo[] mediaInfoArray) {
        if (mSkillUIEventListener != null) {
            mSkillUIEventListener.onPlaylistUpdateItem(sessionId, mediaInfoArray);
        }
        return true;
    }

    @Override
    public boolean OnPlaylistRemoveItem(int sessionId, XWeiMediaInfo[] mediaInfoArray) {
        if (mSkillUIEventListener != null) {
            mSkillUIEventListener.onPlayListRemoveItem(sessionId, mediaInfoArray);
        }
        return true;
    }

    @Override
    public boolean OnPushMedia(int sessionId, XWeiMediaInfo mediaInfo, boolean needReleaseRes) {
        IXWeiPlayer player = getPlayer(sessionId);
        boolean handled = (player != null && player.playMediaInfo(sessionId, mediaInfo, needReleaseRes));

        if (mSkillUIEventListener != null) {
            mSkillUIEventListener.onPlay(sessionId, mediaInfo, false);
        }

        return handled;
    }

    @Override
    public boolean OnFavoriteEvent(String event, String playId) {
        if (mSkillUIEventListener != null) {
            mSkillUIEventListener.onFavoriteEvent(event, playId);
        }
        return true;
    }

    @Override
    public boolean OnSupplement(int sessionId, String contextId, int speakTimeout, int silentTimeout,long requestParam) {
        if (mSkillUIEventListener != null) {
            mSkillUIEventListener.onAutoWakeup(sessionId, contextId, speakTimeout, silentTimeout, requestParam);
        }
        return true;
    }

    @Override
    public void OnTips(int sessionId, int tipsType) {
        if (mSkillUIEventListener != null) {
            mSkillUIEventListener.onTips(tipsType);
        }

    }

    @Override
    public void onNeedReportPlayState(int sessionId, XWeiPlayState state) {
        IXWeiPlayer player = getPlayer(sessionId);
        if (player != null) {
            player.onNeedReportPlayState(sessionId, state);
        }
    }

    @Override
    public void onAudioMsgRecord(int sessionId) {
        XWeiMsgTransfer.getInstance().onAudioMsgRecord();
    }

    @Override
    public void onAudioMsgSend(int sessionId, long tinyId) {
        XWeiMsgTransfer.getInstance().onAudioMsgSend(tinyId);
    }

    public void onDownloadMsgFile(int sessionId, long tinyId, int channel, int type, String key1,
                           String key2, int duration, int timestamp) {
        //控制层通知消息文件到来的信息，由app控制是否下载
        XWeiMsgTransfer.getInstance().onDownloadMsgFile(sessionId, tinyId, channel, type, key1, key2, duration, timestamp);
    }

    private void notifyControlEvent(String action, Bundle extra) {
        Intent intent = new Intent(action);
        if (extra != null) {
            intent.putExtras(extra);
        }

        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
    }


    /**
     * Skill场景UI事件监听
     */
    public interface SkillUIEventListener {
        /**
         * 播放列表封面信息
         *
         * @param sessionId 场景sessionId
         * @param mediaInfo 新增播放列表
         */
        void onPlaylistAddAlbum(int sessionId, XWeiMediaInfo mediaInfo);

        /**
         * 播放列表新增资源
         *
         * @param sessionId      场景sessionId
         * @param mediaInfoArray 新增播放资源
         */
        void onPlaylistAddItem(int sessionId, boolean isFront, XWeiMediaInfo[] mediaInfoArray);

        /**
         * 播放列表资源信息更新
         *
         * @param sessionId      场景sessionId
         * @param mediaInfoArray 更新的播放资源
         */
        void onPlaylistUpdateItem(int sessionId, XWeiMediaInfo[] mediaInfoArray);

        /**
         * 播放列表删减资源
         *
         * @param sessionId      场景sessionId
         * @param mediaInfoArray 新增播放资源
         */
        void onPlayListRemoveItem(int sessionId, XWeiMediaInfo[] mediaInfoArray);

        /**
         * 开始播放一首歌
         *
         * @param sessionId 场景sessionId
         * @param mediaInfo 播放列表中的媒体信息
         * @param fromUser  true 表示为用户主动切歌，如果界面被盖住了，需要重新展示界面
         */
        void onPlay(int sessionId, XWeiMediaInfo mediaInfo, boolean fromUser);

        /**
         * 歌曲暂停播放
         *
         * @param sessionId 场景sessionId
         */
        void onPause(int sessionId);

        /**
         * 歌曲恢复播放
         *
         * @param sessionId 场景sessionId
         */
        void onResume(int sessionId);

        /**
         * 设置了播放模式
         *
         * @param sessionId  场景sessionId
         * @param repeatMode {@linkplain Constants.RepeatMode }
         */
        void onSetPlayMode(int sessionId, int repeatMode);

        /**
         * 列表的所有资源播放完毕了
         *
         * @param sessionId 场景sessionId
         */
        void onFinish(int sessionId);

        /**
         * 歌曲收藏或取消收藏事件
         *
         * @param event  "收藏"或"取消收藏"
         * @param playId 播放资源ID
         */
        void onFavoriteEvent(String event, String playId);

        /**
         * 播放列表操作提示
         *
         * @param tipsType 提示类型 {@link Constants.TXPlayerTipsType}
         */
        void onTips(int tipsType);

        /**
         * 设备应该自动唤醒
         *
         * @param sessionId
         * @param contextId
         * @param speakTimeout
         * @param silentTimeout
         */
        void onAutoWakeup(int sessionId, String contextId, int speakTimeout, int silentTimeout, long requestParam);
    }

}
