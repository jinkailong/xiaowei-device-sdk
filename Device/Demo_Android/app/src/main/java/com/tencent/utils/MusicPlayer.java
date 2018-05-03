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
package com.tencent.utils;

import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.aiaudio.player.BasePlayer;
import com.tencent.aiaudio.player.OpusPlayer;
import com.tencent.xiaowei.control.XWMediaType;
import com.tencent.xiaowei.control.info.XWeiMediaInfo;
import com.tencent.xiaowei.def.XWCommonDef;
import com.tencent.xiaowei.info.XWContextInfo;
import com.tencent.xiaowei.info.XWEventLogInfo;
import com.tencent.xiaowei.info.XWResourceInfo;
import com.tencent.xiaowei.info.XWResponseInfo;
import com.tencent.xiaowei.sdk.XWSDK;
import com.tencent.xiaowei.util.QLog;
import com.tencent.xiaowei.util.Singleton;

import java.io.IOException;
import java.util.HashMap;

/**
 * 一个简单的音乐播放器
 */
public class MusicPlayer {
    private static final String TAG = "MusicPlayer";

    private com.tencent.aiaudio.player.MusicPlayer mMusicPlayer;
    private OpusPlayer mOpusPlayer;
    private BasePlayer mCurrentPlayer;

    private int mPostionDelay = 0;

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private HashMap<String, OnPlayListener> listenerHashMap = new HashMap<>(2);

    private MusicPlayer() {
        mHandlerThread = new HandlerThread("xiaowei_player");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

    }

    private static Singleton<MusicPlayer> sSingleton;
    private static Singleton<MusicPlayer> sSingleton2;

    public static MusicPlayer getInstance() {
        if (sSingleton == null) {
            sSingleton = new Singleton<MusicPlayer>() {
                @Override
                protected MusicPlayer createInstance() {
                    return new MusicPlayer();
                }
            };
        }
        return sSingleton.getInstance();
    }

    public static MusicPlayer getInstance2() {
        if (sSingleton2 == null) {
            sSingleton2 = new Singleton<MusicPlayer>() {
                @Override
                protected MusicPlayer createInstance() {
                    return new MusicPlayer();
                }
            };
        }
        return sSingleton2.getInstance();
    }


    private void initMusicPlayer() {
        if (mMusicPlayer != null) {
            final com.tencent.aiaudio.player.MusicPlayer player = mMusicPlayer;
            new Thread() {
                public void run() {
                    player.stop();
                    player.release();
                    QLog.d(TAG, "mMusicPlayer is released.");
                }
            }.start();
        }
        mMusicPlayer = new com.tencent.aiaudio.player.MusicPlayer();
        mMusicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMusicPlayer.setVolume(1f, 1f);
        mMusicPlayer.setOnPreparedListener(new BasePlayer.OnPreparedListener() {
            @Override
            public void onPrepared(BasePlayer player) {
                QLog.d(TAG, "onPrepared ");
                mMusicPlayer.start();

                if (mPostionDelay > 0) {
                    seekTo(mPostionDelay * 1000);
                    mPostionDelay = 0;
                }
            }
        });
        mMusicPlayer.setOnErrorListener(new BasePlayer.OnErrorListener() {
            @Override
            public void onError(BasePlayer player, int what, int extra) {
                if (what != -38) {
                    QLog.e(TAG, "onError " + what + " " + extra);
                    OnPlayListener listener = listenerHashMap.get(player.getTag());
                    if (listener != null) {
                        listener.onCompletion(what);
                    }
                }
            }
        });

        mMusicPlayer.setOnCompletionListener(new BasePlayer.OnCompletionListener() {
            @Override
            public void onCompletion(BasePlayer player) {
                QLog.d(TAG, "onCompletion ");
                OnPlayListener listener = listenerHashMap.get(player.getTag());
                if (listener != null) {
                    listener.onCompletion(0);
                }
            }
        });

        mMusicPlayer.setOnSeekCompleteListener(new BasePlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(BasePlayer player) {
                Log.e(TAG, "onSeek " + player.getCurrentPosition() / 1000);
            }
        });


    }

    private void clearCurrentPlayer() {
        if (mMusicPlayer != null) {
            final com.tencent.aiaudio.player.MusicPlayer player = mMusicPlayer;
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


    public boolean isPlaying() {
        if (mCurrentPlayer != null) {
            return mCurrentPlayer.isPlaying();
        }

        return false;
    }


    public int getDuration() {
        if (mCurrentPlayer != null) {
            return (int) mCurrentPlayer.getDuration();
        }
        return 0;
    }

    public int getCurrentPosition() {
        if (mCurrentPlayer != null) {
            return (int) mCurrentPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void seekTo(int position) {
        if (mCurrentPlayer != null) {
            mCurrentPlayer.seekTo(position);
        } else {
            mPostionDelay = position;
        }
    }

    public boolean stop() {
        if (mCurrentPlayer != null) {
            mCurrentPlayer.stop(); // stop可能会导致回调onCompletion
        }

        return true;
    }

    public boolean pause() {
        if (mCurrentPlayer != null) {
            mCurrentPlayer.pause();
        }

        return true;
    }

    public boolean resume() {
        if (mCurrentPlayer != null) {
            mCurrentPlayer.start();
        }

        return true;
    }

    public boolean setVolume(int volume) {
        if (mCurrentPlayer != null) {
            mCurrentPlayer.setVolume(volume / 100f, volume / 100f);
        }
        return true;
    }

    public void playMediaInfo(String url, OnPlayListener listener, boolean isLooping) {
        XWResourceInfo resourceInfo = new XWResourceInfo();
        resourceInfo.ID = url;
        resourceInfo.content = url;
        resourceInfo.format = XWCommonDef.ResourceFormat.URL;
        playMediaInfo(resourceInfo, listener, isLooping);
    }

    public void playMediaInfo(String url, OnPlayListener listener) {
        XWResourceInfo resourceInfo = new XWResourceInfo();
        resourceInfo.ID = url;
        resourceInfo.content = url;
        resourceInfo.format = XWCommonDef.ResourceFormat.URL;
        playMediaInfo(resourceInfo, listener, false);
    }

    public void playMediaInfo(XWResourceInfo resourceInfo, final OnPlayListener listener) {
        playMediaInfo(resourceInfo, listener, false);
    }

    public void playMediaInfo(XWResourceInfo resourceInfo, final OnPlayListener listener, final boolean isLooping) {
        final XWeiMediaInfo mediaInfo = change(resourceInfo);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                QLog.d(TAG, "playMediaInfo " + mediaInfo);
                if (mCurrentPlayer != null) {
                    QLog.d(TAG, "现在有资源在播放。");
                    clearCurrentPlayer();
                }
                if (TextUtils.isEmpty(mediaInfo.resId)) {
                    QLog.e(TAG, "playResEx error. resId is null.");
                    return;
                }
                listenerHashMap.put(mediaInfo.resId, listener);
                switch (mediaInfo.mediaType) {
                    case XWMediaType.TYPE_MUSIC_URL:
                        playUrl(mediaInfo.content, mediaInfo.offset, isLooping);
                        mCurrentPlayer.setTag(mediaInfo.resId);
                        break;
                    case XWMediaType.TYPE_TTS_TEXT:
                        XWSDK.getInstance().requestTTS(mediaInfo.content.getBytes(), new XWContextInfo(), new XWSDK.RequestListener() {
                            @Override
                            public boolean onRequest(int event, XWResponseInfo rspData, byte[] extendData) {
                                QLog.d(TAG, "playMediaInfo requestTTS");
                                if (rspData.resources.length > 0
                                        && rspData.resources[0].resources.length > 0
                                        && rspData.resources[0].resources[0].format == XWCommonDef.ResourceFormat.TTS) {
                                    QLog.d(TAG, "playMediaInfo requestTTS resId: " + rspData.resources[0].resources[0].ID);
                                    playTTS(rspData.resources[0].resources[0].ID);
                                    mCurrentPlayer.setTag(mediaInfo.resId);
                                }

                                return true;
                            }
                        });
                        break;
                    case XWMediaType.TYPE_TTS_OPUS:
                        playTTS(mediaInfo.resId);
                        mCurrentPlayer.setTag(mediaInfo.resId);
                        break;
                    default:
                        break;
                }

            }
        });
    }

    private XWeiMediaInfo change(XWResourceInfo resourceInfo) {
        XWeiMediaInfo info = new XWeiMediaInfo();
        info.resId = resourceInfo.ID;
        info.content = resourceInfo.content;
        if (TextUtils.isEmpty(info.resId)) {
            info.resId = resourceInfo.content;
        }
        if (resourceInfo.format == XWCommonDef.ResourceFormat.URL) {
            info.mediaType = XWMediaType.TYPE_MUSIC_URL;
        } else if (resourceInfo.format == XWCommonDef.ResourceFormat.TEXT) {
            info.mediaType = XWMediaType.TYPE_TTS_TEXT;
        } else if (resourceInfo.format == XWCommonDef.ResourceFormat.TTS) {
            info.mediaType = XWMediaType.TYPE_TTS_OPUS;
        }
        info.offset = resourceInfo.offset;
        return info;
    }

    private void playUrl(String url, int offset, boolean isLooping) {
        initMusicPlayer();
        mCurrentPlayer = mMusicPlayer;
        try {
            mMusicPlayer.setDataSource(url);
            mMusicPlayer.setLooping(isLooping);
            mMusicPlayer.prepareAsync();


            XWEventLogInfo log = new XWEventLogInfo();
            log.event = XWEventLogInfo.EVENT_PLAYER_PREPARE;
            log.time = System.currentTimeMillis();
            XWSDK.getInstance().reportEvent(log);
        } catch (Exception ex) {
            ex.printStackTrace();
            OnPlayListener listener = listenerHashMap.get(mCurrentPlayer.getTag());
            if (listener != null) {
                listener.onCompletion(0);
            }
        }
        if (offset > 0) {
            mPostionDelay = offset;
        }
    }

    private void playTTS(final String resId) {
        initOpusPlayer();
        try {
            mOpusPlayer.setDataSource(resId);
            mOpusPlayer.prepareAsync();// 准备并自动播放
            mCurrentPlayer = mOpusPlayer;
        } catch (IOException e) {
            e.printStackTrace();
            OnPlayListener listener = listenerHashMap.get(mCurrentPlayer.getTag());
            if (listener != null) {
                listener.onCompletion(0);
            }
        }
    }

    private void initOpusPlayer() {
        if (mOpusPlayer != null) {
            mOpusPlayer.release();
        }
        mOpusPlayer = new OpusPlayer();
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
            }
        });
        mOpusPlayer.setOnCompletionListener(new BasePlayer.OnCompletionListener() {
            @Override
            public void onCompletion(BasePlayer player) {
                QLog.d(TAG, "onCompletion ");
                OnPlayListener listener = listenerHashMap.get(player.getTag());
                if (listener != null) {
                    listener.onCompletion(0);
                }
            }
        });
        mOpusPlayer.setOnErrorListener(new BasePlayer.OnErrorListener() {
            @Override
            public void onError(BasePlayer player, int what, int extra) {
                QLog.e(TAG, "onError " + what + " " + extra);
                player.reset();
                OnPlayListener listener = listenerHashMap.get(player.getTag());
                if (listener != null) {
                    listener.onCompletion(what);
                }
            }
        });
        mOpusPlayer.setOnSeekCompleteListener(new BasePlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(BasePlayer player) {
                QLog.d(TAG, "onSeek " + player.getCurrentPosition() / 1000);
            }
        });
    }

    public interface OnPlayListener {
        void onCompletion(int error);
    }
}