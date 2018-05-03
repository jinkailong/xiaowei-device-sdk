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
import android.media.MediaPlayer;

import java.io.IOException;


public class MusicPlayer extends BasePlayer {

    private MediaPlayer mMediaPlayer;
    private static final int STATE_INIT = -1;
    private static final int STATE_INITED = 0;
    private static final int STATE_PREPARED = 1;
    private static final int STATE_STARTED = 2;
    private static final int STATE_PAUSED = 3;
    private static final int STATE_STOPED = 4;
    private static final int STATE_RELEASED = 5;

    private int state = STATE_INIT;


    public MusicPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                notifyOnPrepared();
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                notifyOnCompletion();
            }
        });
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                state = STATE_PREPARED;
                notifyOnPrepared();
            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                notifyOnError(what, extra);
                return true;
            }
        });
        state = STATE_INITED;
    }

    @Override
    public void setAudioSessionId(int sessionId) {
        mMediaPlayer.setAudioSessionId(sessionId);
    }

    @Override
    public void setDataSource(String pathOrUrl) throws IOException, UnsupportedOperationException {
        mMediaPlayer.reset();
        mMediaPlayer.setDataSource(pathOrUrl);
    }

    @Override
    public int getAudioSessionId() {
        return mMediaPlayer.getAudioSessionId();
    }

    @Override
    public void setAudioStreamType(int type) {
        mMediaPlayer.setAudioStreamType(type);
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        mMediaPlayer.prepareAsync();
    }

    @Override
    public void start() {
        if (state == STATE_PREPARED || state == STATE_PAUSED) {
            mMediaPlayer.start();
            state = STATE_STARTED;
        }
    }

    @Override
    public void stop() {
        if (state == STATE_STARTED || state == STATE_PAUSED) {
            try {
                mMediaPlayer.stop();
                state = STATE_STOPED;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void pause() {
        if (isPlaying()) {
            try {
                mMediaPlayer.pause();
                state = STATE_PAUSED;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void seekTo(int progress) {
        if (state >= STATE_PREPARED) {
            mMediaPlayer.seekTo(progress);
        }
    }

    @Override
    public void reset() {
        if (state >= STATE_INITED) {
            try {
                mMediaPlayer.reset();
                state = STATE_INITED;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void release() {
        if (state >= STATE_INITED) {
            try {
                mMediaPlayer.release();
                state = STATE_RELEASED;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        clearListener();
    }

    @Override
    public long getDuration() {
        if (state >= STATE_PREPARED) {
            try {
                return mMediaPlayer.getDuration();
            } catch (Exception e) {

            }
        }
        return 0;
    }

    @Override
    public long getCurrentPosition() {
        if (state >= STATE_PREPARED) {
            try {
                return mMediaPlayer.getCurrentPosition();
            } catch (Exception e) {
            }
        }
        return 0;
    }

    @Override
    public void setVolume(float left, float right) {
        if (state >= STATE_INITED) {
            try {
                mMediaPlayer.setVolume(left, right);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public boolean isPlaying() {
        if (state >= STATE_STARTED) {
            return mMediaPlayer.isPlaying();
        } else {
            return false;
        }
    }

    @Override
    public void setLooping(boolean isLooping) {
        mMediaPlayer.setLooping(isLooping);
    }
}
