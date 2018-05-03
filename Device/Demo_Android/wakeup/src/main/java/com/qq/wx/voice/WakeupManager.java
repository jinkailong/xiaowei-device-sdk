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
package com.qq.wx.voice;

import android.content.Context;
import android.text.TextUtils;

import com.qq.wx.voice.embed.recognizer.Grammar;
import com.qq.wx.voice.embed.recognizer.GrammarResult;
import com.qq.wx.voice.embed.recognizer.SDKVersion;
import com.qq.wx.voice.util.ErrorCode;
import com.qq.wx.voice.vad.EVadUtil;
import com.tencent.xiaowei.util.QLog;
import com.tencent.xiaowei.util.Singleton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 唤醒词管理器,这一块逻辑由静音检测EVadUtil和Grammar组成，在vad检测到开始说话后，将声音传入Grammar进行分析，如果检测到本地唤醒词，就开始新的vad检测。如果再vad检测到静音还没识别到唤醒词，就重新开始新的vad检测。
 * <p>
 * <p>
 * Grammar中init和destroy 只全局调用一次；begin和end为每次检测调用；recognize为填充需要检测的语音数据，可以再begin和end中间重复调用，当返回1或者2的时候可以使用getResult获得结果。
 * <p>
 * 先将声音经过EVadUtil处理：
 * mEVadUtil = new EVadUtil(500, 0);
 * mEVadUtil.setVADListener(mVADListener);
 * mEVadUtil.start();
 * <p>
 * 在mVADListener#onSpeak的时候调用：
 * mGrammar.begin();
 * 如果返回值为0，说明调用成功，开始将声音传给唤醒模块处理：
 * int ret = mGrammar.recognize(pcmBuffer, pcmBuffer.length);
 * 如果ret为1，说明检查到了唤醒词，如果ret为2，说明检测到了四个字以上唤醒词的前两位（这时候提前发起云端校验，便于快速响应）。
 * 这时候可以获得唤醒词的信息：
 * mGrammar.getResult(result);
 * 之后需要调用
 * mGrammar.end();
 * mEVadUtil.release();
 * 结束这轮检测。
 * <p>
 * 如果一直没检测到唤醒，说完一句话后会触发mVADListener#onSilence，这时候需要调用
 * mGrammar.end();
 * mEVadUtil.release();
 * 结束这轮检测。
 * <p>
 * 然后开始新的一轮静音检测和唤醒检测。
 * <p>
 * <p>
 * 这里需要特别注意，声音给唤醒模块检测之后，发送到云端的识别声音需要是连续的，所以需要保证语音数据分发的时候不丢失。其中feedDataError就是因为多个线程之间处理数据，所以需要将多余的数据回调给外层，让外面有机会处理。每个Buffer都有个index，便于外面重新排序。
 */

public class WakeupManager {

    private static final String TAG = "WakeupManager";
    private Context mContext;
    private Grammar mGrammar;
    private ConcurrentLinkedQueue<byte[]> mDataQueue = new ConcurrentLinkedQueue<>();
    private int mDataCurrentLength = 0;
    private static int MAX_TEMP_DATA_LENGTH = 24 * 1024;
    private boolean mHalfWordsCheckEnable = true;// 检测到一半就可以返回成功

    private WakeupManager() {

    }

    private static final Singleton<WakeupManager> sSingleton = new Singleton<WakeupManager>() {
        @Override
        protected WakeupManager createInstance() {
            return new WakeupManager();
        }
    };

    /**
     * 获得唤醒词管理器实例
     *
     * @return {@link WakeupManager}
     */
    public static WakeupManager getInstance() {
        return sSingleton.getInstance();
    }

    private EVadUtil mEVadUtil;
    public static boolean isSpeaking;
    private ByteArrayOutputStream mTempPcmData = new ByteArrayOutputStream();

    private enum State {
        UnInited, Inited, Started, Finish, Stoped
    }

    private State mCurrentState = State.UnInited;

    /**
     * 初始化模块
     *
     * @param context 上下文对象
     * @return
     */
    public int init(Context context) {
        QLog.v(TAG, "init");
        mContext = context;
        mGrammar = Grammar.getInstance();
        String assetsName = getAssetsName();
        if (mGrammar.init(mContext, assetsName) < 0) {
            QLog.d(TAG, "init failed, 检查so和bin版本号，部分系统对assets做缓存，需要想办法替换bin文件。");
            return ErrorCode.ERROR_GRAMMAR_INIT;
        }
        mCurrentState = State.Inited;
        SDKVersion version = new SDKVersion();
        mGrammar.getVersion(version);
        QLog.d(TAG, "init success,version is " + version + ", assets is " + assetsName);
        return 0;
    }

    private String getAssetsName() {
        return "libwxvoiceembed.bin";
    }

    /**
     * 开始检测
     */
    public void start() {
        if (mCurrentState == State.Started) {
            return;
        }
        mDataQueue.clear();
        mTempPcmData.reset();
        mDataCurrentLength = 0;
        if (mEVadUtil != null) {
            mEVadUtil.release();
        }
        mEVadUtil = new EVadUtil(500, 0);
        mEVadUtil.start();
        mCurrentState = State.Started;

        QLog.v(TAG, "start");
    }

    /**
     * 填充pcm数据
     *
     * @param pcmBuffer 录音的pcm数据
     * @return {@link WakeupItem}
     */
    public WakeupItem checkWakeup(byte[] pcmBuffer) {
        WakeupItem item = new WakeupItem();
        if (pcmBuffer == null || pcmBuffer.length == 0) {
            return null;
        }
        final byte[] data = new byte[pcmBuffer.length];
        System.arraycopy(pcmBuffer, 0, data, 0, pcmBuffer.length);
        if (mEVadUtil != null) {
            EVadUtil.VadResult result = mEVadUtil.addData(data, data.length);
            if (result != null) {
                item.vadResult = result.ret;
                if (result.ret == 1) {
                    QLog.d(TAG, "onSpeak.");
                    isSpeaking = true;
                    try {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        for (byte[] b : result.preData) {
                            bos.write(b);
                        }
                        bos.flush();
                        item.data = bos.toByteArray();
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    int ret = mGrammar.begin();
                    if (ret < 0) {
                        QLog.e(TAG, "vad begin failed.");
                        stop();
                        return item;
                    }
//                    TXAIAudioSDK.getInstance().dumpBytes(null, null, TXAIAudioSDK.DUMP_TYPE_FILL_OTHER, TXAIAudioSDK.DUMP_OPTYPE_RESET);

                    feedGrammar(item, item.data);
                    if (!TextUtils.isEmpty(item.text)) {
                        item.vadResult = result.ret;
                        QLog.d(TAG, "wakeup in onSpeak.");
                        return item;
                    }
                } else if (result.ret == 2) {
                    QLog.d(TAG, "onSilence.");
                    if (isSpeaking) {
                        isSpeaking = false;
//                        TXAIAudioSDK.getInstance().dumpBytes(null, null, TXAIAudioSDK.DUMP_TYPE_FILL_OTHER, TXAIAudioSDK.DUMP_OPTYPE_COMMIT);
//                        TXAIAudioSDK.getInstance().dumpBytes(null, null, TXAIAudioSDK.DUMP_TYPE_FILL_OTHER, TXAIAudioSDK.DUMP_OPTYPE_RESET);
                        mGrammar.end();
                        onResult(item, true);
                        mDataQueue.clear();
                        mTempPcmData.reset();
                        mDataCurrentLength = 0;
                    }
                }
            }
        }
        if (isSpeaking) {
            feedGrammar(item, data);
            if (!TextUtils.isEmpty(item.text)) {
                QLog.d(TAG, "wakeup in feedGrammar.");
            }
        }
        return item;
    }

    /**
     * 唤醒结果
     */
    public static class WakeupItem {
        public static final int NEED_CLOUD_CHECK = 2;
        /**
         * 唤醒词
         */
        public String text;

        /**
         * 唤醒相关的声音
         */
        public byte[] data;
        public int vadResult;
        public int wakeupRet;// 1 不需要云端校验，2 需要云端校验

        @Override
        public String toString() {
            return text;
        }
    }

    private void feedGrammar(WakeupItem item, byte[] pcmBuffer) {
        if (pcmBuffer.length > 1280) {
            int off = 0;
            while (pcmBuffer.length > off) {
                int count = Math.min(1280, pcmBuffer.length - off);
                byte[] input = new byte[count];
                System.arraycopy(pcmBuffer, off, input, 0, count);
                addTempData(input);
                off += count;
            }
        } else {
            addTempData(pcmBuffer);
        }
        int ret = mGrammar.recognize(pcmBuffer, pcmBuffer.length);
        if (ret == 1 || (ret == 2 && mHalfWordsCheckEnable)) {
            QLog.d(TAG, "recognize ret is " + ret);
            isSpeaking = false;
            item.wakeupRet = ret;
            onResult(item);
            mGrammar.end();
            // 2 表示 识别到了半个唤醒词，需要去云端校验
        }
    }

    private void addTempData(byte[] pcmBuffer) {
        mDataQueue.add(pcmBuffer);
        mDataCurrentLength += pcmBuffer.length;
        if (mDataCurrentLength > MAX_TEMP_DATA_LENGTH) {
            mDataCurrentLength -= mDataQueue.poll().length;
        }
    }

    private void onResult(WakeupItem item) {
        onResult(item, false);
    }

    private void onResult(WakeupItem item, boolean isCannot) {
        final GrammarResult result = new GrammarResult();
        mGrammar.getResult(result);
        if (!isCannot) {
            try {
                for (byte[] data : mDataQueue) {
                    mTempPcmData.write(data);
                }
                mTempPcmData.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            item.data = mTempPcmData.toByteArray();
            QLog.d(TAG, "wakeup by " + result.text + " " + item.data.length);
            item.text = result.text;

        }
        mCurrentState = State.Finish;
        start();// 没有stop就重新开始检测唤醒
    }

    /**
     * 结束检测
     */
    public void stop() {
        if (mCurrentState == State.Started) {
            mGrammar.end();
            mEVadUtil.release();
            mEVadUtil = null;
        }
        isSpeaking = false;
        mCurrentState = State.Stoped;
        QLog.v(TAG, "stop");
//        TXAIAudioSDK.getInstance().dumpBytes(null, null, TXAIAudioSDK.DUMP_TYPE_FILL_OTHER, TXAIAudioSDK.DUMP_OPTYPE_RESET);
    }

    /**
     * 销毁
     *
     * @return 0为成功
     */
    public int destroy() {
        return mGrammar.destroy();
    }

    public void setHalfWordsCheck(boolean enable) {
        mHalfWordsCheckEnable = enable;
    }
}
