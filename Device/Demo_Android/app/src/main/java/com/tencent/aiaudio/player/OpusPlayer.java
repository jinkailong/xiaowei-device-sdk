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

import com.tencent.aiaudio.tts.TTSManager;
import com.tencent.xiaowei.control.OpusDecoder;
import com.tencent.xiaowei.info.XWTTSDataInfo;
import com.tencent.xiaowei.util.QLog;

import java.io.IOException;
import java.util.ArrayList;

public class OpusPlayer extends BasePlayer {
    private final static String TAG = "OpusPlayer";

    private boolean mLogAble = true;

    private String resId;
    private int mSessionId;
    private OpusDecoder mOpusDecoder;
    private PcmPlayer mPcmPlayer;
    private float mLeftVolume;
    private float mRightVolume;

    private ArrayList<byte[]> prepareData = new ArrayList<>(5);
    private boolean isPlaying;
    private static int _s_id;
    private int id;

    public OpusPlayer() {
    }

    @Override
    public void setDataSource(String resId) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException, UnsupportedOperationException {
        this.resId = resId;
    }

    @Override
    public int getAudioSessionId() {
        return mSessionId;
    }

    @Override
    @Deprecated
    public void setAudioStreamType(int i) {

    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        if (resId == null) {
            throw new IllegalStateException("resId is null.");
        }
        Runnable runnable = new Runnable() {
            public void run() {
                TTSManager.TTSItem item = TTSManager.getInstance().getInfo(resId);
                mOpusDecoder = new OpusDecoder();
                id = _s_id++;
                mOpusDecoder.init(id, item.sampleRate, item.channel);
                initPcmPlayer();
                XWTTSDataInfo data = null;
                int count = 0;// 缓冲5包

                while ((data == null || !data.isEnd) && count < 5) {
                    data = TTSManager.getInstance().read(resId);
                    if (data.data != null) {
                        byte[] buffer = mOpusDecoder.decoder(id, data.data);
                        if (buffer != null) {
                            prepareData.add(buffer);
                        }
                        count++;
                    } else {
                        // TODO: check data.data why is null
                        QLog.e(TAG, "data.data is null");
                    }

                }
                // 准备好了，可以开始播放了
                mPcmPlayer.prepareAsync();
            }
        };
        runInNewThread(runnable);
    }

    private void initPcmPlayer() {
        mPcmPlayer = new PcmPlayer();
        mPcmPlayer.setAudioSessionId(mSessionId);
        mPcmPlayer.setVolume(mLeftVolume, mRightVolume);
        mPcmPlayer.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(BasePlayer player) {
                OpusPlayer.this.notifyOnPrepared();
            }
        });
        mPcmPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(BasePlayer player) {
                OpusPlayer.this.notifyOnCompletion();
            }
        });
        mPcmPlayer.setOnErrorListener(new OnErrorListener() {
            @Override
            public void onError(BasePlayer player, int what, int extra) {
                OpusPlayer.this.notifyOnError(what, extra);
            }
        });
        mPcmPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(BasePlayer player) {
                OpusPlayer.this.notifyOnSeek();
            }
        });
    }

    private Runnable playThread = new Runnable() {
        @Override
        public void run() {
            for (byte[] data : prepareData) {
                mPcmPlayer.write(data);
            }
            prepareData.clear();
            XWTTSDataInfo data = null;
            while (isPlaying && (data == null || !data.isEnd)) {
                data = TTSManager.getInstance().read(resId);
                if (!data.isEnd) {
                    byte[] buffer = mOpusDecoder.decoder(id, data.data);
                    if (buffer != null) {
                        mPcmPlayer.write(buffer);
                    }
                } else {
                    mPcmPlayer.write(null);
                }
            }
        }
    };

    @Override
    public void start() throws IllegalStateException {
        if (isPlaying) {
            return;
        }
        isPlaying = true;
        if (mPcmPlayer != null) {
            mPcmPlayer.start();
        }

        runInNewThread(playThread);
    }

    private void runInNewThread(final Runnable runnable) {
        new Thread() {
            public void run() {
                runnable.run();
            }
        }.start();
    }

    private byte[] short2byte(short[] data) {
        if (data == null) {
            return null;
        }
        byte[] buffer = new byte[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            buffer[i * 2] = (byte) (data[i] & 0xff);
            buffer[i * 2 + 1] = (byte) (data[i] >> 8 & 0xff);
        }
        return buffer;
    }

    @Override
    public void stop() throws IllegalStateException {
        reset();
        if (mPcmPlayer != null) {
            mPcmPlayer.stop();
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        isPlaying = false;
        if (mPcmPlayer != null) {
            mPcmPlayer.pause();
        }
    }

    @Override
    public void seekTo(int position) throws IllegalStateException {

    }

    @Override
    @Deprecated
    public void reset() {
        pause();
        prepareData.clear();
        if (mOpusDecoder != null)
            mOpusDecoder.unInit(id);
    }

    @Override
    public void release() {
        stop();
        if (mPcmPlayer != null) {
            mPcmPlayer.release();
            mPcmPlayer.clearListener();
        }
        clearListener();
    }

    @Override
    public long getDuration() {
        if (mPcmPlayer != null) {
            return mPcmPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public long getCurrentPosition() {
        if (mPcmPlayer != null) {
            return mPcmPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void setVolume(float l, float r) {
        mLeftVolume = l;
        mRightVolume = r;
        if (mPcmPlayer != null) {
            mPcmPlayer.setVolume(l, r);
        }
    }

    @Override
    public boolean isPlaying() {
        if (mPcmPlayer != null) {
            return mPcmPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public void setLooping(boolean isLooping) {

    }


    public void setAudioSessionId(int audioSessionId) {
        mSessionId = audioSessionId;
    }
}
