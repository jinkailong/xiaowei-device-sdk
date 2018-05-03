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

import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.aiaudio.tts.TTSManager;
import com.tencent.xiaowei.control.IXWeiPlayer;
import com.tencent.xiaowei.control.XWMediaType;
import com.tencent.xiaowei.control.XWeiControl;
import com.tencent.xiaowei.control.info.XWeiMediaInfo;
import com.tencent.xiaowei.control.info.XWeiPlayState;
import com.tencent.xiaowei.def.XWCommonDef;
import com.tencent.xiaowei.info.XWAppInfo;
import com.tencent.xiaowei.info.XWContextInfo;
import com.tencent.xiaowei.info.XWEventLogInfo;
import com.tencent.xiaowei.info.XWPlayStateInfo;
import com.tencent.xiaowei.info.XWResponseInfo;
import com.tencent.xiaowei.sdk.XWSDK;
import com.tencent.xiaowei.util.QLog;

import java.io.IOException;


/**
 * 播放器示例
 */
public class XWeiPlayer implements IXWeiPlayer {

    private int mPlayState;
    private int mAudioSessionId;

    static class PlayState {
        /**
         * 无状态，这时候只能变成 START
         */
        public static final int INIT = 0;

        /**
         * 开始播放一个新的资源
         */
        public static final int START = 1;
        /**
         * 终止播放，在stop后触发
         */
        public static final int ABORT = 2;

        /**
         * 播放完成，包括被调用上下一首打断
         */
        public static final int COMPLETE = 3;

        /**
         * 暂停了
         */
        public static final int PAUSE = 4;

        /**
         * 继续了
         */
        public static final int CONTINUE = 5;

        /**
         * 播放错误，包括url无法下载，资源异常
         */
        public static final int ERR = 6;

        /**
         * 播放器seek操作
         */
        public static final int SEEK = 7;
    }

    private static final String TAG = XWeiPlayer.class.getSimpleName();

    private MusicPlayer mMusicPlayer;
    private OpusPlayer mOpusPlayer;
    private BasePlayer mCurrentPlayer;
    private int mSessionId;

    private int mPostionDelay = 0;

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    public XWeiPlayer(int sessionId) {
        this.mSessionId = sessionId;
        mHandlerThread = new HandlerThread("xiaowei_player");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

    }

    private boolean needContinue() {
        return mPlayState != PlayState.PAUSE && mPlayState != PlayState.ABORT && mPlayState != PlayState.INIT;
    }

    private void initMusicPlayer() {
        if (mMusicPlayer != null) {
            final MusicPlayer player = mMusicPlayer;
            new Thread() {
                public void run() {
                    player.stop();
                    player.release();
                    QLog.d(TAG, "mMusicPlayer is released.");
                }
            }.start();
        }
        mMusicPlayer = new MusicPlayer();

        mMusicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMusicPlayer.setVolume(1f, 1f);
        mMusicPlayer.setOnPreparedListener(new BasePlayer.OnPreparedListener() {
            @Override
            public void onPrepared(BasePlayer player) {
                QLog.d(TAG, "onPrepared ");
                if (needContinue() || mPlayState == PlayState.INIT) {
                    mMusicPlayer.start();
                    XWEventLogInfo log = new XWEventLogInfo();
                    log.event = XWEventLogInfo.EVENT_PLAYER_START;
                    log.time = System.currentTimeMillis();
                    XWSDK.getInstance().reportEvent(log);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setPlayState(PlayState.START);
                        }
                    });

                    if (mPostionDelay > 0) {
                        seekTo(mPostionDelay * 1000);
                        mPostionDelay = 0;
                    }
                } else {
                    QLog.e(TAG, "need not start state:" + mPlayState);
                }
            }
        });
        mMusicPlayer.setOnErrorListener(new BasePlayer.OnErrorListener() {
            @Override
            public void onError(BasePlayer player, int what, int extra) {
                if (what != -38) {
                    QLog.e(TAG, "onError " + what + " " + extra);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (needContinue()) {
                                setPlayState(PlayState.ERR);
                            }
                        }
                    });
                }
            }
        });

        mMusicPlayer.setOnCompletionListener(new BasePlayer.OnCompletionListener() {
            @Override
            public void onCompletion(BasePlayer player) {
                QLog.d(TAG, "onCompletion ");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (needContinue()) {
                            setPlayState(PlayState.COMPLETE);// 会触发sdk播放下一首
                        }
                    }
                });
            }
        });

        mMusicPlayer.setOnSeekCompleteListener(new BasePlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(BasePlayer player) {
                Log.e(TAG, "onSeek " + player.getCurrentPosition() / 1000);
                String pos = "" + player.getCurrentPosition() / 1000;
                setPlayStateEx(PlayState.SEEK, pos);
            }
        });


    }

    private void clearCurrentPlayer() {
        if (mMusicPlayer != null) {
            final MusicPlayer player = mMusicPlayer;
            mMusicPlayer = null;
            mCurrentPlayer = null;
            new Thread() {
                public void run() {
                    player.stop();
                    player.release();
                    QLog.d(TAG, "mMusicPlayer is released.");
                }
            }.start();
        }

        if (mOpusPlayer != null) {
            final OpusPlayer player = mOpusPlayer;
            mOpusPlayer = null;
            player.release();
        }

    }

    public void setAudioSessionId(int sessionId) {
        mAudioSessionId = sessionId;
        if (mOpusPlayer != null)
            mOpusPlayer.setAudioSessionId(sessionId);
    }

    private void setPlayState(int state) {
        mPlayState = state;
        XWeiControl.getInstance().getMediaTool().txcPlayerStateChange(mSessionId, state);
    }

    private void setPlayStateEx(int state, String data) {
        mPlayState = state;
    }


    @Override
    public boolean isPlaying() {
        if (mCurrentPlayer != null) {
            return mCurrentPlayer.isPlaying();
        }

        return false;
    }

    @Override
    public void onNeedReportPlayState(int sessionId, XWeiPlayState playState) {
        if (mCurrentPlayer != null) {
            playState.position = mCurrentPlayer.getCurrentPosition() / 1000;
        }
        XWPlayStateInfo stateInfo = new XWPlayStateInfo();
        stateInfo.appInfo = new XWAppInfo();
        stateInfo.appInfo.ID = playState.skillId;
        stateInfo.appInfo.name = playState.skillName;
        stateInfo.playID = playState.resId;
        stateInfo.playContent = playState.content;
        stateInfo.state = playState.playState;
        stateInfo.playMode = playState.playMode;
        stateInfo.playOffset = playState.position;
        XWSDK.getInstance().reportPlayState(stateInfo);
    }

    @Override
    public int getDuration() {
        if (mCurrentPlayer != null) {
            return (int) mCurrentPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (mCurrentPlayer != null) {
            return (int) mCurrentPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int position) {
        if (mCurrentPlayer != null) {
            mCurrentPlayer.seekTo(position);
        } else {
            mPostionDelay = position;
        }
    }

    @Override
    public boolean stop(int sessionId) {
        if (mCurrentPlayer != null) {
            mCurrentPlayer.stop(); // stop可能会导致回调onCompletion
            setPlayState(PlayState.ABORT);
        }

        return true;
    }

    @Override
    public boolean pause(int sessionId) {
        if (mCurrentPlayer != null) {
            mCurrentPlayer.pause();
            setPlayState(PlayState.PAUSE);
        }

        return true;
    }

    @Override
    public boolean resume(int sessionId) {
        if (mCurrentPlayer != null) {
            mCurrentPlayer.start();
            setPlayState(PlayState.CONTINUE);
        }

        return true;
    }

    @Override
    public boolean changeVolume(int sessionId, int volume) {
        if (mCurrentPlayer != null) {
            mCurrentPlayer.setVolume(volume / 100f, volume / 100f);
        }
        return true;
    }

    @Override
    public boolean playMediaInfo(final int sessionId, final XWeiMediaInfo mediaInfo, final boolean needReleaseRes) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                QLog.d(TAG, "playMediaInfo " + mediaInfo);
                if (mCurrentPlayer != null && (mPlayState == PlayState.START || mPlayState == 0)) {
                    QLog.d(TAG, "现在有资源在播放。");
                    clearCurrentPlayer();
                }
                mPlayState = PlayState.INIT;// 重置状态
                if (TextUtils.isEmpty(mediaInfo.resId)) {
                    QLog.e(TAG, "playResEx error. resId is null.");
                    return;
                }
                switch (mediaInfo.mediaType) {
                    case XWMediaType.TYPE_MUSIC_URL:
                    case XWMediaType.TYPE_MUSIC_URL_TIP:
                    case XWMediaType.TYPE_LOCAL_FILE:
                        playUrl(mediaInfo.content, mediaInfo.offset);
                        break;
                    case XWMediaType.TYPE_TTS_TEXT:
                        case XWMediaType.TYPE_TTS_TEXT_TIP:
                        XWSDK.getInstance().requestTTS(mediaInfo.content.getBytes(), new XWContextInfo(), new XWSDK.RequestListener() {
                            @Override
                            public boolean onRequest(int event, XWResponseInfo rspData, byte[] extendData) {
                                QLog.d(TAG, "playMediaInfo requestTTS");
                                if (rspData.resources.length > 0
                                        && rspData.resources[0].resources.length > 0
                                        && rspData.resources[0].resources[0].format == XWCommonDef.ResourceFormat.TTS) {
                                    QLog.d(TAG, "playMediaInfo requestTTS resId: " + rspData.resources[0].resources[0].ID);
                                    playTTS(sessionId, rspData.resources[0].resources[0].ID, true);
                                }

                                return true;
                            }
                        });
                        break;
                    case XWMediaType.TYPE_TTS_OPUS:
                        playTTS(sessionId, mediaInfo.resId, needReleaseRes);
                        break;
                    case XWMediaType.TYPE_TTS_MSGPROMPT:
                        long tinyId = Long.valueOf(mediaInfo.content);
                        long timestamp = Long.valueOf(mediaInfo.description);
                        XWSDK.getInstance().requestProtocolTTS(tinyId, timestamp, 403, new XWSDK.RequestListener() {
                            @Override
                            public boolean onRequest(int event, XWResponseInfo rspData, byte[] extendData) {
                                QLog.d(TAG, "playMediaInfo requestProtocolTTS resId: " + rspData.voiceID);
                                playTTS(sessionId, rspData.voiceID, true);
                                return true;
                            }
                        });
                        break;
                    default:
                        break;
                }

            }
        });
        return false;
    }

    private void playUrl(String url, int offset) {
        initMusicPlayer();
        mCurrentPlayer = mMusicPlayer;
        try {
            mMusicPlayer.setDataSource(url);
            mMusicPlayer.prepareAsync();
            XWEventLogInfo log = new XWEventLogInfo();
            log.event = XWEventLogInfo.EVENT_PLAYER_PREPARE;
            log.time = System.currentTimeMillis();
            XWSDK.getInstance().reportEvent(log);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (offset > 0) {
            mPostionDelay = offset;
        }
    }

    private void playTTS(final int sessionId, final String resId, boolean needReleaseRes) {
        TTSManager.getInstance().associate(sessionId, resId);
        initOpusPlayer();
        try {
            mOpusPlayer.setTag(needReleaseRes ? resId : "");
            mOpusPlayer.setDataSource(resId);
            mOpusPlayer.prepareAsync();// 准备并自动播放
            mCurrentPlayer = mOpusPlayer;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initOpusPlayer() {
        if (mOpusPlayer != null) {
            mOpusPlayer.release();
        }
        mOpusPlayer = new OpusPlayer();
        mOpusPlayer.setAudioSessionId(mAudioSessionId);
        mOpusPlayer.setVolume(1f, 1f);
        mOpusPlayer.setOnPreparedListener(new BasePlayer.OnPreparedListener() {
            @Override
            public void onPrepared(BasePlayer player) {
                QLog.d(TAG, "onPrepared ");
                XWEventLogInfo log = new XWEventLogInfo();
                log.event = XWEventLogInfo.EVENT_TTS_PREPARED;
                log.time = System.currentTimeMillis();
                XWSDK.getInstance().reportEvent(log);

                mOpusPlayer.start();
                setPlayState(PlayState.START);
            }
        });
        mOpusPlayer.setOnCompletionListener(new BasePlayer.OnCompletionListener() {
            @Override
            public void onCompletion(BasePlayer player) {
                QLog.d(TAG, "onCompletion ");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (needContinue()) {
                            setPlayState(PlayState.COMPLETE);
                        }
                    }
                });
                String resId = (String) player.getTag();
                if (!TextUtils.isEmpty(resId)) {
                    TTSManager.getInstance().release(resId);
                }
            }
        });
        mOpusPlayer.setOnErrorListener(new BasePlayer.OnErrorListener() {
            @Override
            public void onError(BasePlayer player, int what, int extra) {
                QLog.e(TAG, "onError " + what + " " + extra);
                player.reset();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (needContinue()) {
                            setPlayState(PlayState.ERR);
                        }
                    }
                });
            }
        });
        mOpusPlayer.setOnSeekCompleteListener(new BasePlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(BasePlayer player) {
                QLog.d(TAG, "onSeek " + player.getCurrentPosition() / 1000);
                String pos = "" + player.getCurrentPosition() / 1000;
                setPlayStateEx(PlayState.SEEK, pos);
            }
        });
    }

}
