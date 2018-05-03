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
package com.tencent.aiaudio.wakeup;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.qq.wx.voice.WakeupManager;
import com.qq.wx.voice.recognizer.InfoRecorder;
import com.qq.wx.voice.util.Common;
import com.tencent.aiaudio.NotifyConstantDef;
import com.tencent.aiaudio.activity.MainActivity;
import com.tencent.aiaudio.msg.XWeiMsgTransfer;
import com.tencent.aiaudio.utils.DemoOnAudioFocusChangeListener;
import com.tencent.xiaowei.control.XWeiAudioFocusManager;
import com.tencent.xiaowei.control.XWeiControl;
import com.tencent.xiaowei.def.XWCommonDef;
import com.tencent.xiaowei.info.XWContextInfo;
import com.tencent.xiaowei.info.XWResponseInfo;
import com.tencent.xiaowei.sdk.XWSDK;
import com.tencent.xiaowei.util.QLog;
import com.tencent.xiaowei.util.Singleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.tencent.aiaudio.NotifyConstantDef.ActionDef.ACTION_DEF_ANIM_NOISE_CHANGED;
import static com.tencent.aiaudio.NotifyConstantDef.ActionDef.ACTION_DEF_ANIM_STOP;
import static com.tencent.aiaudio.NotifyConstantDef.ActionDef.ACTION_DEF_RECOGNIZE_TEXT;
import static com.tencent.aiaudio.NotifyConstantDef.ExtraKeyDef.EXTRA_KEY_DEF_MSG_NOISE_CHANGED;
import static com.tencent.aiaudio.NotifyConstantDef.ExtraKeyDef.EXTRA_KEY_DEF_RECOGNIZE_TEXT;
import static com.tencent.xiaowei.info.XWContextInfo.WAKEUP_TYPE_CLOUD_CHECK;
import static com.tencent.xiaowei.info.XWResponseInfo.WAKEUP_CHECK_RET_FAIL;
import static com.tencent.xiaowei.info.XWResponseInfo.WAKEUP_CHECK_RET_NOT;
import static com.tencent.xiaowei.info.XWResponseInfo.WAKEUP_CHECK_RET_SUC;
import static com.tencent.xiaowei.info.XWResponseInfo.WAKEUP_CHECK_RET_SUC_CONTINUE;
import static com.tencent.xiaowei.info.XWResponseInfo.WAKEUP_CHECK_RET_SUC_RSP;

/**
 * 录音数据处理
 */
public class RecordDataManager implements Runnable, XWSDK.AudioRequestListener {
    private static final Singleton<RecordDataManager> sSingleton = new Singleton<RecordDataManager>() {
        @Override
        protected RecordDataManager createInstance() {
            return new RecordDataManager();
        }
    };
    private static final String TAG = "RecordDataManager";
    private Context mContext;
    private String wakeupVoiceId;// 记录当前在唤醒校验的voiceId
    private String wakeupCheckingVoiceId;// 记录当前在唤醒校验并且还没唤醒结果的voiceId，用来开启一次动画
    private String recognizeVoiceId;// 记录当前在语音识别的voiceId
    private Handler mHandler = new Handler();
    private boolean isRecognizing;// 识别中，包括唤醒后连续说的时候
    private boolean isThinking;// 思考中，静音后到收到响应的这段时间

    public static RecordDataManager getInstance() {
        return sSingleton.getInstance();
    }

    private XWContextInfo wakeupContextInfo = new XWContextInfo();
    private XWContextInfo recognizeContextInfo = new XWContextInfo();
    private AudioManager mAudioManager = null;
    private boolean wakeupEnable;// 开启语音唤醒

    private boolean isRunning;
    public static final int STATE_IDLE = 0x0;
    public static final int STATE_WAKEUP = 0x1;
    public static final int STATE_RECOGNIZE = 0x2;

    private int mVoiceState = STATE_IDLE;
    private int mLastmVoiceState = STATE_IDLE;

    private static final Object CIRCLE_BUFFER = new Object();
    private ConcurrentLinkedQueue<byte[]> awakeCheckBuffer = new ConcurrentLinkedQueue<>();

    private boolean keepSilence;

    private XWeiAudioFocusManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new XWeiAudioFocusManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            QLog.d(TAG, "onAudioFocusChange " + focusChange);
            if (focusChange == XWeiAudioFocusManager.AUDIOFOCUS_LOSS || focusChange == XWeiAudioFocusManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                isRecognizing = false;
                keepSilence = false;
                changeVoiceState(STATE_WAKEUP, mVoiceState);
                wakeupVoiceId = null;
                sendBroadcast(ACTION_DEF_ANIM_STOP, null);
                XWSDK.getInstance().requestCancel("");// 通知SDK强制取消这次请求
            }
        }
    };


    @Override
    public boolean onRequest(String voiceId, int event, XWResponseInfo rspData, byte[] extendData) {
        QLog.e(TAG, "[" + voiceId + "]:" + event + " " + rspData);
        switch (event) {
            case XWCommonDef.XWEvent.ON_IDLE:
                keepSilence = false;
                MainActivity.setUITips("请求结束");
                if (voiceId.equals(wakeupVoiceId)) {
                    wakeupVoiceId = null;
                } else if (voiceId.equals(recognizeVoiceId)) {
                    isRecognizing = false;
                }
                isThinking = false;
                break;
            case XWCommonDef.XWEvent.ON_REQUEST_START:
                MainActivity.setUITips("请求开始：" + voiceId);
                XWeiControl.getInstance().processResponse(voiceId,
                                XWSDK.EVENT_REQUEST_START, rspData, extendData);
                break;
            case XWCommonDef.XWEvent.ON_SPEAK:
                MainActivity.setUITips("说话开始");
                XWeiControl.getInstance().processResponse(voiceId,
                                XWSDK.EVENT_SPEAK, rspData, extendData);
                break;
            case XWCommonDef.XWEvent.ON_SILENT:
                MainActivity.setUITips("说话结束");
                keepSilence = false;
                isThinking = true;
                // 如果是识别中
                if (voiceId.equals(wakeupVoiceId)) {
                    wakeupVoiceId = null;
                } else if (voiceId.equals(recognizeVoiceId)) {
                    isRecognizing = false;
                }
                changeVoiceState(STATE_WAKEUP, mVoiceState);
                XWeiControl.getInstance().processResponse(voiceId,
                                XWSDK.EVENT_SILENT, rspData, extendData);
                break;
            case XWCommonDef.XWEvent.ON_RECOGNIZE:
                String strRes = new String(extendData);
                try {
                    JSONObject jsonObject = new JSONObject(strRes);
                    String strEvent = jsonObject.getString("event");
                    if (strEvent.compareToIgnoreCase("Recognize") == 0) {
                        String strText = jsonObject.getString("text");

                        Bundle bundle = new Bundle();
                        bundle.putString(EXTRA_KEY_DEF_RECOGNIZE_TEXT, strText);
                        sendBroadcast(ACTION_DEF_RECOGNIZE_TEXT, bundle);

                    } else if (strEvent.compareToIgnoreCase("RecognizeEnd") == 0) {
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            case XWCommonDef.XWEvent.ON_RESPONSE:
                if (rspData.wakeupFlag != WAKEUP_CHECK_RET_NOT) {
                    // 唤醒类响应，直接处理掉
                    changeVoiceState(STATE_WAKEUP, mVoiceState);
                    if (rspData.wakeupFlag == WAKEUP_CHECK_RET_FAIL) {
                        wakeupVoiceId = null;
                        // 云端校验失败
                        QLog.d(TAG, "wakeup check fail voiceId:" + voiceId);
                    } else if (rspData.wakeupFlag == WAKEUP_CHECK_RET_SUC) {
                        wakeupVoiceId = null;
                        onWakeup();
                        // 唤醒成功，重新开启一次普通语音识别请求，并带上前300ms的数据(因为云端回来的结果经过网络有延迟，往前面拼一点数据避免中间的语音数据丢失了)
                    } else if (rspData.wakeupFlag == WAKEUP_CHECK_RET_SUC_RSP) {
                        wakeupVoiceId = null;
                        // 唤醒成功，收到最终结果了
                        dealRsp(voiceId, rspData, extendData);
                    } else if (rspData.wakeupFlag == WAKEUP_CHECK_RET_SUC_CONTINUE) {
                        // 如果需要 则开启动画
                        if (wakeupCheckingVoiceId != null) {
                            wakeupCheckingVoiceId = null;
                            isRecognizing = true;
                            onWakeupAnim();
                        }
                        // 唤醒成功。继续传语音
                        if (rspData.resources.length > 0) {
                            wakeupVoiceId = null;
                            // 收到最终结果了
                            dealRsp(voiceId, rspData, extendData);
                        }
                    }

                } else {
                    // 普通响应
                    dealRsp(voiceId, rspData, extendData);
                }
                break;

        }
        return true;
    }

    public void start(Context context) {
        mContext = context.getApplicationContext();
        isRunning = true;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        WakeupManager.getInstance().init(mContext);
        setWakeupEnable(true);
        changeVoiceState(STATE_WAKEUP, mVoiceState);
        XWSDK.getInstance().setAudioRequestListener(this);
        new Thread(this).start();

    }


    private void dealRsp(String voiceId, XWResponseInfo rspData, byte[] extendData) {
        QLog.d(TAG, "recognize end voiceId:" + voiceId + " text:" + rspData.requestText);
        MainActivity.setUITips("收到响应：" + voiceId + ((rspData.resultCode != 0) ? " 错误码：" + rspData.resultCode : "") + " skillName:" + rspData.appInfo.name);
        isRecognizing = false;
        isThinking = false;
        sendBroadcast(ACTION_DEF_ANIM_STOP, null);

        // 清理自定义设备状态
        XWSDK.getInstance().clearUserState();

        // 控制层处理ASR/NLP的数据
        XWeiControl.getInstance().processResponse(voiceId, rspData, extendData);

        // 收到了语音请求的结果，处理后，取消唤醒的焦点（如果已经被取消了，可以重复调用）。 延迟一点点，避免上一个焦点恢复的太快了。
        XWeiAudioFocusManager.getInstance().abandonAudioFocus(onAudioFocusChangeListener, 500);

        //  如果是不可恢复的，请求短期焦点，否则请求长期焦点。
        int duration = rspData.recoveryAble ? AudioManager.AUDIOFOCUS_GAIN : AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE;
        if (XWeiAudioFocusManager.getInstance().needRequestFocus(duration)) {
            int ret = mAudioManager.requestAudioFocus(DemoOnAudioFocusChangeListener.getInstance(), AudioManager.STREAM_MUSIC, duration);
            if (ret == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                XWeiAudioFocusManager.getInstance().setAudioFocusChange(duration);
            }
        }
    }

    public void stop() {
        isRunning = false;
        queue.clear();
        WakeupManager.getInstance().destroy();
    }

    private ConcurrentLinkedQueue<byte[]> queue = new ConcurrentLinkedQueue<>();

    private RecordDataManager() {
        wakeupContextInfo.voiceWakeupType = WAKEUP_TYPE_CLOUD_CHECK;
    }

    public synchronized void feedData(final byte[] data) {
        if (queue.size() > 100) {
            QLog.e(TAG, "record buffer size = 100.");
            queue.poll();
        }
        queue.add(data);
        notifyAll();
    }

    private synchronized byte[] getData() {
        long start = System.currentTimeMillis();
        while (queue.size() == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        long cost = end - start;
        if (cost > 100) {
            QLog.e(TAG, "getData wait time = " + cost + ".");
        }
        return queue.poll();
    }

    private boolean wakeupNeedVoice() {
        return wakeupEnable && (mVoiceState & STATE_WAKEUP) == STATE_WAKEUP && wakeupVoiceId == null;
    }

    private boolean wakeupCheckNeedVoice() {
        return wakeupEnable && (mVoiceState & STATE_WAKEUP) == STATE_WAKEUP && wakeupVoiceId != null;
    }

    private boolean xiaoweiNeedVoice() {
        return (mVoiceState & STATE_RECOGNIZE) == STATE_RECOGNIZE;
    }

    private void changeVoiceState(int add, int del) {
        int state = mVoiceState;
        state &= ~del;
        state |= add;
        if (mVoiceState == state) {
            // 免得外面重复调用
            return;
        }

        mLastmVoiceState = mVoiceState;
        mVoiceState = state;
        if (mVoiceState != mLastmVoiceState) {
            QLog.d(TAG, "changeVoiceState old:" + mLastmVoiceState + " new:" + mVoiceState);
        }
    }


    public void run() {
        while (isRunning) {
            byte[] pcmBuffer = RecordDataManager.getInstance().getData();
            if (wakeupNeedVoice()) {
                int offset = 0;
                while (offset < pcmBuffer.length) {
                    int count = Math.min(InfoRecorder.mRecordBufferSize, pcmBuffer.length - offset);
                    byte[] buffer = new byte[count];
                    System.arraycopy(pcmBuffer, offset, buffer, 0, count);
                    offset += count;
                    if (checkWakeup(buffer)) {
                        count = pcmBuffer.length - offset;
                        if (count > 0) {
                            buffer = new byte[count];
                            System.arraycopy(pcmBuffer, offset, buffer, 0, count);
                            // 把剩下的声音丢到识别里
                            XWSDK.getInstance().request(XWCommonDef.RequestType.WAKEUP_CHECK, buffer, wakeupContextInfo);
                            break;
                        }
                    }
                }
            }

            if (keepSilence) {// Demo使用，手机上没降噪和回声消除等，有时候周围太吵，需要传一段没声音数据让请求停下来。
                pcmBuffer = new byte[pcmBuffer.length];
            }
            if (wakeupCheckNeedVoice()) {
                wakeupVoiceId = XWSDK.getInstance().request(XWCommonDef.RequestType.WAKEUP_CHECK, pcmBuffer, wakeupContextInfo);
            }
            if (xiaoweiNeedVoice()) {
                isRecognizing = true;
                XWeiMsgTransfer.getInstance().setVoiceData(pcmBuffer);
                recognizeVoiceId = XWSDK.getInstance().request(XWCommonDef.RequestType.VOICE, pcmBuffer, recognizeContextInfo);
                if (TextUtils.isEmpty(recognizeVoiceId)) {
                    changeVoiceState(STATE_WAKEUP, mVoiceState);
                }
                recognizeContextInfo.voiceRequestBegin = false;
            }

            if (isRecognizing || isThinking) {
                Bundle bundle = new Bundle();
                if (!isThinking) {
                    int vol = Common.calculateVolumn(pcmBuffer, pcmBuffer.length);
                    float noise = vol / 64f;
                    bundle.putFloat(EXTRA_KEY_DEF_MSG_NOISE_CHANGED, noise);
                }
                sendBroadcast(ACTION_DEF_ANIM_NOISE_CHANGED, bundle);
            }
            // 缓存10k数据
            synchronized (CIRCLE_BUFFER) {
                awakeCheckBuffer.add(pcmBuffer);
                int size = 0;
                for (byte[] b : awakeCheckBuffer) {
                    size += b.length;
                }
                if (size > 10 * 1000) {
                    awakeCheckBuffer.poll();
                }
            }
        }

    }

    private boolean checkWakeup(byte[] pcmBuffer) {
        final WakeupManager.WakeupItem wakeupItem = WakeupManager.getInstance().checkWakeup(pcmBuffer);
        if (wakeupItem != null) {
            if (!TextUtils.isEmpty(wakeupItem.text)) {
                // 本地初步唤醒，是否唤醒成功需要等待后续的回调
                QLog.d(TAG, "onWakeup by " + wakeupItem.text + " " + wakeupItem.data.length);
                if (XWSDK.getInstance().isOnline()) {
                    wakeupContextInfo.voiceRequestBegin = true;
                    byte[] orig = wakeupItem.data;
                    if (orig.length <= 6400) {
                        wakeupCheckingVoiceId = wakeupVoiceId = XWSDK.getInstance().request(XWCommonDef.RequestType.WAKEUP_CHECK, orig, wakeupContextInfo);
                        wakeupContextInfo.voiceRequestBegin = false;
                    } else {
                        int off = 0;
                        while (orig.length > off) {

                            int count = Math.min(6400, orig.length - off);
                            byte[] input = new byte[count];
                            System.arraycopy(orig, off, input, 0, count);

                            wakeupCheckingVoiceId = wakeupVoiceId = XWSDK.getInstance().request(XWCommonDef.RequestType.WAKEUP_CHECK, input, wakeupContextInfo);
                            wakeupContextInfo.voiceRequestBegin = false;
                            off += count;
                        }
                    }

                }
                return true;
            }
        }
        return false;
    }

    private void onWakeup() {
        QLog.d(TAG, "onWakeup");
        recognizeContextInfo = new XWContextInfo();
        recognizeContextInfo.voiceRequestBegin = true;
        keepSilence = false;

        // wakeupFlag为WAKEUP_CHECK_RET_SUC 需要往前拼10k数据
        synchronized (CIRCLE_BUFFER) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                for (byte[] pcmBuffer : awakeCheckBuffer) {
                    bos.write(pcmBuffer);
                }
                bos.flush();
                awakeCheckBuffer.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] orig = bos.toByteArray();
            bos.reset();
            QLog.d(TAG, "fillData by circle buffer ." + orig.length);
            int off = 0;
            while (orig.length > off) {

                int count = Math.min(6400, orig.length - off);
                byte[] input = new byte[count];
                System.arraycopy(orig, off, input, 0, count);

                recognizeVoiceId = XWSDK.getInstance().request(XWCommonDef.RequestType.VOICE, input, recognizeContextInfo);
                recognizeContextInfo.voiceRequestBegin = false;
                off += count;
            }
        }
        changeVoiceState(STATE_RECOGNIZE | STATE_WAKEUP, mVoiceState);

        XWeiAudioFocusManager.getInstance().requestAudioFocus(onAudioFocusChangeListener, XWeiAudioFocusManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
        if (XWeiAudioFocusManager.getInstance().needRequestFocus(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)) {
            int ret = mAudioManager.requestAudioFocus(DemoOnAudioFocusChangeListener.getInstance(), AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
            if (ret == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                XWeiAudioFocusManager.getInstance().setAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
            }
        }

        sendBroadcast(NotifyConstantDef.ActionDef.ACTION_DEF_ANIM_START, null);
    }

    private void onWakeupAnim() {
        QLog.d(TAG, "onWakeupAnim");
        XWeiAudioFocusManager.getInstance().requestAudioFocus(onAudioFocusChangeListener, XWeiAudioFocusManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
        if (XWeiAudioFocusManager.getInstance().needRequestFocus(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)) {
            int ret = mAudioManager.requestAudioFocus(DemoOnAudioFocusChangeListener.getInstance(), AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
            if (ret == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                XWeiAudioFocusManager.getInstance().setAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
            }
        }
        sendBroadcast(NotifyConstantDef.ActionDef.ACTION_DEF_ANIM_START, null);
    }

    public void onWakeup(XWContextInfo contextInfo) {
        QLog.d(TAG, "onWakeup " + contextInfo);
        recognizeContextInfo = contextInfo;
        recognizeContextInfo.voiceRequestBegin = true;
        keepSilence = false;

        // 按钮唤醒延迟200ms录音
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                changeVoiceState(STATE_RECOGNIZE | STATE_WAKEUP, mVoiceState);
            }
        }, 200);// 没有回声消除，也没有播放同步，先这样规避一下录到播放器声音的问题

        XWeiAudioFocusManager.getInstance().requestAudioFocus(onAudioFocusChangeListener, XWeiAudioFocusManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
        if (XWeiAudioFocusManager.getInstance().needRequestFocus(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)) {
            int ret = mAudioManager.requestAudioFocus(DemoOnAudioFocusChangeListener.getInstance(), AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
            if (ret == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                XWeiAudioFocusManager.getInstance().setAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
            }
        }

        sendBroadcast(NotifyConstantDef.ActionDef.ACTION_DEF_ANIM_START, null);
    }

    public void onSleep() {
        XWeiAudioFocusManager.getInstance().abandonAudioFocus(onAudioFocusChangeListener);

        changeVoiceState(STATE_WAKEUP, mVoiceState);
        XWSDK.getInstance().requestCancel("");// 通知SDK强制取消这次请求
    }

    public void setWakeupEnable(boolean enable) {
        if (wakeupEnable == enable) {
            return;
        }
        wakeupEnable = enable;
        if (wakeupEnable) {
            WakeupManager.getInstance().start();
        } else {
            WakeupManager.getInstance().stop();
        }
    }

    private void sendBroadcast(String action, Bundle extra) {
        Intent intent = new Intent(action);
        if (extra != null)
            intent.putExtras(extra);
        mContext.sendBroadcast(intent);
    }

    public void setHalfWordsCheck(boolean enable) {
        WakeupManager.getInstance().setHalfWordsCheck(enable);
    }

    public void keepSilence(boolean localVad) {
        if(localVad){
            recognizeContextInfo.voiceRequestEnd = true;
            recognizeVoiceId = XWSDK.getInstance().request(XWCommonDef.RequestType.VOICE, null, recognizeContextInfo);
        }
        keepSilence = true;
    }

}
