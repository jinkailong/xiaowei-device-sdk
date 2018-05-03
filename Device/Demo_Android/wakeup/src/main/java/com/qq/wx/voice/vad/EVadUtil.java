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
package com.qq.wx.voice.vad;

import android.os.Handler;
import android.os.Looper;

import com.qq.wx.voice.recognizer.InfoRecorder;
import com.tencent.xiaowei.util.QLog;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EVadUtil {

    private static final String TAG = "EVadUtil";
    private final int preAudioMax;
    private int mTimeOut;
    private EVad mEvad = null;
    private int mState = EVadNative.VAD_SILENCE;

    private int mLastState = EVadNative.VAD_SILENCE;
    private boolean isSpeaking;
    private boolean isNeverSpeak = true;

    private ConcurrentLinkedQueue<byte[]> preAudio = new ConcurrentLinkedQueue<byte[]>();
    private VADListener mVADListener;

    private Handler mHandler = new Handler(Looper.getMainLooper());
//    private long time1;// 开始的时间，如果是900ms后才开始onSpeak，就不用走容错逻辑
//    private long time2;// 第一次onSpeak时间，如果在750ms内调用onSilence。就忽略onSilence和下一次onSpeak。并且开始5000-750ms内的计时，时间一到就调用onSilence。但是如果在这段计时内收到了onSpeak，就忽略这次计时。
//
//    private int mIgnoreState = STATE.STATE_OK;

    public EVadUtil() {
        this(InfoRecorder.mSilTime);
    }

    public EVadUtil(int silenceTime) {
        this(silenceTime, InfoRecorder.mTimeout);
    }

    public EVadUtil(int silenceTime, int timeOut) {
        mEvad = new EVad();
        preAudioMax = InfoRecorder.mPreAudioByteSize
                / InfoRecorder.mRecordBufferSize;
        mTimeOut = timeOut;
        int ret = mEvad.Init(InfoRecorder.mFrequency, silenceTime,
                InfoRecorder.mSNRation, InfoRecorder.mBwin,
                InfoRecorder.mBconfirm);
        if (ret != 0) {
            QLog.e(TAG, QLog.CLR, "init error " + ret);
        }
        QLog.v(TAG, QLog.CLR, "init success " + silenceTime + " " + timeOut);
    }

    private void reset() {
        stop();
        preAudio.clear();
        mState = EVadNative.VAD_SILENCE;
        mLastState = EVadNative.VAD_SILENCE;
        isNeverSpeak = true;
        isSpeaking = false;
//        time1 = 0;
//        time2 = 0;
        QLog.v(TAG, QLog.CLR, "reset");
    }

    public void start() {
//        time1 = System.currentTimeMillis();
        reset();
        if (mTimeOut > 0) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isNeverSpeak) {
                        QLog.v(TAG, QLog.CLR, "timeout");
                        reset();
                        if (mVADListener != null) {
                            mVADListener.onTimeOut();
                        }
                    }
                }
            }, mTimeOut);
        }

        QLog.v(TAG, QLog.CLR, "start");
    }

    public void stop() {
        mHandler.removeCallbacksAndMessages(null);
        QLog.v(TAG, QLog.CLR, "stop");
    }

    /**
     * @param buffer        语音数据
     * @param pcmBufferSize 传InfoRecorder.mRecordBufferSize大小的数据，保证检测的最小力度
     * @return 0 无状态变化 1 开始说话了 2 静音了
     */
    public VadResult addData(byte[] buffer, int pcmBufferSize) {
        byte[] buffer2 = Arrays.copyOf(buffer, buffer.length);
        int offset = 0;
        VadResult ret = null;
        while (offset < pcmBufferSize) {
            int count = Math.min(InfoRecorder.mRecordBufferSize, pcmBufferSize - offset);
            byte[] pcmBuffer = new byte[count];
            System.arraycopy(buffer2, offset, pcmBuffer, 0, count);
            offset += count;
            VadResult result = addData320(pcmBuffer, count);
            if (result != null) {
                ret = result;
            }
        }
        return ret;
    }

    private VadResult addData320(byte[] pcmBuffer, int pcmBufferSize) {
//        byte[] pcmBuffer = Arrays.copyOf(buffer, buffer.length);
        VadResult ret = null;
        mState = mEvad.AddData(pcmBuffer, pcmBufferSize);

        if (!isSpeaking) {
            if (mState == EVadNative.VAD_SPEAK
                    && mLastState == EVadNative.VAD_SILENCE) {
                isSpeaking = true;
                isNeverSpeak = false;
                QLog.v(TAG, QLog.CLR, "speaking start: preAudio size = "
                        + preAudio.size());

                // 把preAudio发出去
                final LinkedList<byte[]> list = new LinkedList<>();
                for (byte[] bt : preAudio) {
                    list.add(Arrays.copyOf(bt, bt.length));
                }
                ret = new VadResult();
                ret.ret = 1;
                ret.preData = list;
//                    mVADListener.onSpeak(list);

                QLog.d(TAG, QLog.CLR, "onSpeak");
            }
        } else {
            if (mState == EVadNative.VAD_SILENCE
                    && mLastState == EVadNative.VAD_SPEAK) {
                isSpeaking = false;
                isNeverSpeak = true;
                preAudio.clear();
                reset();
                ret = new VadResult();
                ret.ret = 2;
//                    mVADListener.onSilence();
                QLog.d(TAG, QLog.CLR, "onSilence");
            }
        }

        mLastState = mState;
        preAudio.add(pcmBuffer);
        if (preAudio.size() > preAudioMax)
            preAudio.poll();

        return ret;
    }

    public void release() {
        mHandler.removeCallbacksAndMessages(null);
        mEvad.Release();
        QLog.v(TAG, QLog.CLR, "release");
    }

    public void setVADListener(VADListener listener) {
        mVADListener = listener;
    }


    public static class VadResult {
        public int ret;
        public LinkedList<byte[]> preData;
    }

    public interface VADListener {

/*        *//**
         * 开始说话了
         *
         * @param firstData 之前的需要发送给服务器的数据
         *//*
        void onSpeak(LinkedList<byte[]> firstData);

        *//**
         * 停止说话了
         *//*
        void onSilence();*/

        /**
         * 超时了
         */
        void onTimeOut();
    }

//    public static class STATE {
//        public static final int STATE_OK = 0;
//        public static final int STATE_FIRST_SPEAK = 1;
//        public static final int STATE_FIRST_SILENCE = 2;
//        public static final int STATE_SECOND_SPEAK = 3;
//    }
}
