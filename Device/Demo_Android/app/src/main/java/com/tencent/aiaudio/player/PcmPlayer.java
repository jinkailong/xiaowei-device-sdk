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

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.tencent.xiaowei.util.QLog;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PcmPlayer extends BasePlayer {

    protected static int PLAY_ERROR = 1;
    protected static int WRITE_ERROR = 2;
    protected static int PAUSE_ERROR = 3;

    public final static int STATE_INITED = 0;
    public final static int STATE_PLAYING = 1;
    public final static int STATE_PAUSED = 2;
    public final static int STATE_STOPED = 3;

    private int playState;

    private static final String TAG = "PcmPlayer";
    private AudioTrack mAudioTrack = null;

    private LooperThread mLooperThread;

    private AudioParam mAudioParam;
    private int mDataLen;// 总长度
    private int mPlayOffset;// 实际播放完毕的长度

    private ConcurrentLinkedQueue<byte[]> queue = new ConcurrentLinkedQueue();

    private boolean isPlaying;
    private int mSessionId;

    public void setAudioParam(AudioParam audioParam) {
        this.mAudioParam = audioParam;
    }

    class LooperThread extends Thread {
        private Handler mHandler;

        @Override
        public void run() {
            Looper.prepare();
            mHandler = new Handler();
            Looper.loop();
        }

        public void postTask(Runnable runnable) {
            if (mHandler != null) {
                mHandler.post(runnable);
            } else {
                Log.d(TAG, "postTask mHandler == null.");
            }
        }

        public void clear() {
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
            }
        }
    }

    public PcmPlayer() {
        AudioParam audioParam = new AudioParam();
        audioParam.mFrequency = 16000;
        audioParam.mChannel = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        audioParam.mSampBit = AudioFormat.ENCODING_PCM_16BIT;
        setAudioParam(audioParam);
    }

    public PcmPlayer(AudioParam audioParam) {
        setAudioParam(audioParam);
    }

    public void setAudioSessionId(int sessionId) {
        mSessionId = sessionId;
    }

    @Override
    public void setDataSource(String s) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException, UnsupportedOperationException {

    }


    @Override
    public int getAudioSessionId() {
        return mAudioTrack == null ? 0 : mAudioTrack.getAudioSessionId();
    }


    @Override
    public void setAudioStreamType(int i) {
    }


    @Override
    public void prepareAsync() throws IllegalStateException {
        QLog.d(TAG, "prepareAsync");
        int minBufSize = AudioTrack.getMinBufferSize(mAudioParam.mFrequency,
                mAudioParam.mChannel, mAudioParam.mSampBit);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mAudioParam.mFrequency,
                mAudioParam.mChannel, mAudioParam.mSampBit,
                minBufSize, AudioTrack.MODE_STREAM, mSessionId);

        if (AudioTrack.STATE_INITIALIZED != mAudioTrack.getState()) {
            Log.e(TAG, "AudioTrack state is not AudioTrack.STATE_INITIALIZED. the state is" + mAudioTrack.getState());
        }

        if (mLooperThread == null) {
            mLooperThread = new LooperThread();
            mLooperThread.start();
        }
        try {
            mAudioTrack.play();
            notifyOnPrepared();
        } catch (Exception e) {
            e.printStackTrace();
            notifyOnError(PLAY_ERROR, 0);
        }

    }

    public void write(byte[] buffer) {
        if (buffer == null) {
            buffer = new byte[0];
        }
        final byte[] data = buffer;
        queue.add(data);
        mDataLen += data.length;

        // 没暂停

        if (playState != STATE_PAUSED && playState != STATE_STOPED) {
            play(buffer);
        }
    }

    private void play() {
        for (byte[] data : queue) {
            play(data);
        }
    }

    private void play(final byte[] data) {
        if (mLooperThread == null) {
            return;
        }
        playState = STATE_PLAYING;
        mLooperThread.postTask(new Runnable() {
            @Override
            public void run() {
                if (data.length == 0) {
                    isPlaying = false;
                    notifyOnCompletion();
                    return;
                }
                isPlaying = true;
                try {
                    if (mAudioTrack != null) {
                        mAudioTrack.write(data, 0, data.length);
                        queue.remove(data);
                        mPlayOffset += data.length;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    isPlaying = false;
                    notifyOnError(WRITE_ERROR, 0);
                }
            }
        });
    }

    @Override
    public void start() throws IllegalStateException {
        // 恢复播放
        if (mAudioTrack == null) {
            prepareAsync();
        }
        play();
    }


    @Override
    public void stop() throws IllegalStateException {
        reset();
        pause();
        playState = STATE_STOPED;
        if (mLooperThread != null) {
            mLooperThread.postTask(new Runnable() {
                @Override
                public void run() {
                    Looper.myLooper().quit();
                }
            });
            mLooperThread = null;
        }

    }

    @Override
    public void pause() throws IllegalStateException {
        mDataLen = mPlayOffset;
        if (mLooperThread != null) {
            mLooperThread.clear();
        }
        if (mAudioTrack != null) {
            try {
                mAudioTrack.stop();
                mAudioTrack.release();
                mAudioTrack = null;
            } catch (Exception e) {
                e.printStackTrace();
                notifyOnError(PAUSE_ERROR, 0);
            }
        }
        playState = STATE_PAUSED;
    }

    @Override
    public void seekTo(int position) throws IllegalStateException {
        if (this.mAudioTrack != null) {
            if (position > 0L) {
                this.mPlayOffset = position;
            }
        }
    }

    @Override
    public void reset() {
        mDataLen = 0;
        mPlayOffset = 0;
        queue.clear();
        playState = STATE_INITED;
    }

    @Override
    public void release() {
        QLog.d(TAG, "release");
        isPlaying = false;
        stop();
    }

    @Override
    public long getDuration() {
        if (mAudioParam.mChannel == AudioFormat.CHANNEL_CONFIGURATION_MONO) {
            return 1000l * mDataLen / (mAudioParam.mFrequency * mAudioParam.mSampBit);
        }
        return mDataLen;
    }

    @Override
    public long getCurrentPosition() {
        if (mAudioParam.mChannel == AudioFormat.CHANNEL_CONFIGURATION_MONO) {
            return 1000l * mPlayOffset / (mAudioParam.mFrequency * mAudioParam.mSampBit);
        }
        return mPlayOffset;
    }

    @Override
    public void setVolume(float left, float right) {
        if (mAudioTrack != null) {
            mAudioTrack.setStereoVolume(left, right);
        }
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void setLooping(boolean isLooping) {

    }


    static class AudioParam {

        public int mChannel;
        public int mFrequency;
        public int mSampBit;
    }

}
