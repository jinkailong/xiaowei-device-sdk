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
package com.tencent.aiaudio.utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.ArrayList;


public class PcmBytesPlayer {

    private static final String TAG = "PcmBytesPlayer";
    private AudioTrack mAudioTrack = null;

    private AudioParam mAudioParam;

    private ArrayList<Task> list = new ArrayList<>();

    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private Task mCurrentTask;

    private Handler mHandlerPlay;
    private HandlerThread mHandlerThreadPlay;

    public void setAudioParam(AudioParam audioParam) {
        this.mAudioParam = audioParam;
    }


    private static PcmBytesPlayer mPcmBytesPlayer;

    public static PcmBytesPlayer getInstance() {
        if (mPcmBytesPlayer == null) {
            mPcmBytesPlayer = new PcmBytesPlayer();
        }
        return mPcmBytesPlayer;
    }

    private PcmBytesPlayer() {
        mHandlerThread = new HandlerThread("PcmPlayer");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mHandlerThreadPlay = new HandlerThread("PcmPlayer2");
        mHandlerThreadPlay.start();
        mHandlerPlay = new Handler(mHandlerThreadPlay.getLooper());
        PcmBytesPlayer.AudioParam audioParam = new PcmBytesPlayer.AudioParam();
        audioParam.mFrequency = 16000;
        audioParam.mChannel = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        audioParam.mSampBit = AudioFormat.ENCODING_PCM_16BIT;
        setAudioParam(audioParam);
    }

    private void prepareAsync() {
        release();
        int minBufSize = AudioTrack.getMinBufferSize(mAudioParam.mFrequency,
                mAudioParam.mChannel, mAudioParam.mSampBit);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mAudioParam.mFrequency,
                mAudioParam.mChannel, mAudioParam.mSampBit,
                minBufSize, AudioTrack.MODE_STREAM);

        if (AudioTrack.STATE_INITIALIZED != mAudioTrack.getState()) {
            Log.e(TAG, "AudioTrack state is not AudioTrack.STATE_INITIALIZED. the state is" + mAudioTrack.getState());
        }

        try {
            mAudioTrack.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playSync(final byte[] data, final OnCompletionListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Task task = new Task();
                task.canInterrupt = false;
                task.data = data;
                task.listener = listener;


                if (mCurrentTask == null || mCurrentTask.canInterrupt) {
                    Log.d(TAG, "playSync start to play.");
                    play(task);
                } else {
                    list.add(task);
                    Log.d(TAG, "playSync add to list");
                }

            }
        });

    }

    public void play(final byte[] data, final OnCompletionListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Task task = new Task();
                task.canInterrupt = true;
                task.data = data;
                task.listener = listener;
                if (mCurrentTask == null || mCurrentTask.canInterrupt) {
                    Log.d(TAG, "play start to play.");
                    play(task);
                } else {
                    list.add(task);
                    Log.d(TAG, "play add to list");
                }


            }
        });
    }

    private void play(final Task task) {
        if (task == null) {
            return;
        }
        if (mCurrentTask != null && mCurrentTask.listener != null) {
            mCurrentTask.listener.onCompletion();
            mCurrentTask = null;
        }
        mCurrentTask = task;

        Log.d(TAG, "play prepareAsync");
        prepareAsync();
        if (task.data == null) {
            if (task.listener != null) {
                task.listener.onCompletion();
            }
            mCurrentTask = null;
            playNext();
            return;
        }

        mHandlerPlay.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "play start");
                    // 计算时间，时间过后再回调OnComplete
                    int duration = getDuration(task.data.length);
                    mHandlerPlay.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "play over");
                            if (task.listener != null) {
                                task.listener.onCompletion();
                            }
                            mCurrentTask = null;
                            playNext();
                        }
                    }, duration);
                    mAudioTrack.write(task.data, 0, task.data.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void playNext() {
        Log.d(TAG, "playNext " + list.size());
        if (list.size() > 0) {
            Task task = list.remove(0);
            play(task);
        }
    }

    private void release() {
        Log.d(TAG, "release");
        if (mAudioTrack != null) {
            try {
                mAudioTrack.stop();
                mAudioTrack.release();
                mAudioTrack = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void destroy() {
        release();
        mHandlerThread.quit();
        mHandler.removeCallbacksAndMessages(null);
    }


    static class AudioParam {
        public int mChannel;
        public int mFrequency;
        public int mSampBit;
    }

    public interface OnCompletionListener {
        void onCompletion();
    }

    static class Task {
        byte[] data;
        OnCompletionListener listener;
        boolean canInterrupt;
    }


    private int getDuration(int byteLength) {
        int bytePerSecond = mAudioParam.mFrequency * mAudioParam.mSampBit;
        if (mAudioParam.mChannel != AudioFormat.CHANNEL_CONFIGURATION_MONO) {
            return 0;// 其他的要用的时候自行换算
        }
        int ms = byteLength * 1000 / bytePerSecond;
        return ms;
    }
}
