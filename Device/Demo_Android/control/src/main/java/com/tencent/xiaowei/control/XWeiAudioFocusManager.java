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
package com.tencent.xiaowei.control;

import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;

import com.tencent.xiaowei.util.QLog;
import com.tencent.xiaowei.util.Singleton;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 声音焦点管理
 */
public class XWeiAudioFocusManager {

    /**
     * 希望之前播放的资源暂停，比如音乐、闹钟等应该申请它,之后可以收到{@link #AUDIOFOCUS_LOSS}、{@link #AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK}、{@link #AUDIOFOCUS_LOSS_TRANSIENT}
     */
    public static final int AUDIOFOCUS_GAIN = 1;

    /**
     * 希望播放完毕后就销毁资源，比如天气等应该申请它。之后可以收到{@link #AUDIOFOCUS_LOSS}、{@link #AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK}
     */
    public static final int AUDIOFOCUS_GAIN_TRANSIENT = 2;
    /**
     * 不希望之前播放的暂停但是需要降低音量，比如唤醒、导航等应该申请它。之后可以收到{@link #AUDIOFOCUS_LOSS}、{@link #AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK}
     */
    public static final int AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK = 3;

    /**
     * 临时请求一下焦点，这个焦点不能传递给别的场景，其余的和AUDIOFOCUS_GAIN_TRANSIENT一致。唤醒应该申请它。避免在其他APP的时候唤醒一次就一直播放我们APP的资源。
     */
    public static final int AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE = 4;

    /**
     * 收到后没机会再播放了，应该释放资源
     */
    public static final int AUDIOFOCUS_LOSS = -1;
    /**
     * 收到后可以暂停播放，但是待会儿有机会恢复
     */
    public static final int AUDIOFOCUS_LOSS_TRANSIENT = -2;
    /**
     * 收到后应该降低音量播放
     */
    public static final int AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK = -3;
    private static final String TAG = "XWeiAudioFocusManager";

    private static Singleton<XWeiAudioFocusManager> sSingleton = new Singleton<XWeiAudioFocusManager>() {
        @Override
        protected XWeiAudioFocusManager createInstance() {
            return new XWeiAudioFocusManager();
        }
    };
    private int mFocusChange;

    private ConcurrentHashMap<OnAudioFocusChangeListener, Integer> map = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, OnAudioFocusChangeListener> i2lMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Integer> cookieMap = new ConcurrentHashMap<>();

    private Handler mHandler;


    public static XWeiAudioFocusManager getInstance() {
        if (sSingleton == null) {
            sSingleton = new Singleton<XWeiAudioFocusManager>() {
                @Override
                protected XWeiAudioFocusManager createInstance() {
                    return new XWeiAudioFocusManager();
                }
            };
        }
        return sSingleton.getInstance();
    }

    private XWeiAudioFocusManager() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    public boolean needRequestFocus(int duration) {
        if (duration < AUDIOFOCUS_GAIN || duration > AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE) {
            return false;
        }
        if (mFocusChange <= 0) {
            return true;
        }
        if (mFocusChange > duration && duration == AUDIOFOCUS_GAIN) {
            return true;
        }
        return false;
    }

    public interface OnAudioFocusChangeListener {
        void onAudioFocusChange(int focusChange);
    }

    /**
     * 应用层申请一个焦点
     *
     * @param listener
     * @param duration
     */
    public void requestAudioFocus(final OnAudioFocusChangeListener listener, final int duration) {
        if (duration < AUDIOFOCUS_GAIN || duration > AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE || listener == null) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int cookie = -1;
                if (map.containsKey(listener)) {
                    cookie = map.get(listener);
                } else if (cookieMap.containsKey(listener.hashCode())) {
                    cookie = cookieMap.get(listener.hashCode());
                }
                QLog.d(TAG, "requestAudioFocus nativeRequestAudioFocus cookie:" + cookie + " duration:" + duration + " " + listener);
                cookie = XWeiControl.getInstance().nativeRequestAudioFocus(cookie, duration);
                map.put(listener, cookie);
                i2lMap.put(cookie, listener);
                cookieMap.remove(listener.hashCode());
            }
        });
    }

    void onFocusChange(final int cookie, final int duration) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                QLog.d(TAG, "onFocusChange cookie:" + cookie + " duration:" + duration + " " + i2lMap);
                OnAudioFocusChangeListener listener = i2lMap.get(cookie);
                if (listener != null) {
                    listener.onAudioFocusChange(duration);
                }
                if (duration == AUDIOFOCUS_LOSS) {
                    i2lMap.remove(cookie);
                    if (listener != null) {
                        map.remove(listener);
                        cookieMap.put(listener.hashCode(), cookie);
                    }
                }
            }
        });
    }

    /**
     * 应用层释放某个焦点
     *
     * @param listener
     */
    public void abandonAudioFocus(final OnAudioFocusChangeListener listener) {
        abandonAudioFocus(listener, 0);
    }

    /**
     * 应用层释放某个焦点
     *
     * @param listener
     * @param delayMillis
     */
    public void abandonAudioFocus(final OnAudioFocusChangeListener listener, int delayMillis) {
        if (listener == null) {
            return;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (map.containsKey(listener)) {
                    int cookie = map.get(listener);
                    XWeiControl.getInstance().nativeAbandonAudioFocus(cookie);
                }
            }
        }, delayMillis);
    }

    /**
     * 释放所有焦点，会停止所有的播放资源，适合在解绑的时候使用
     */
    public void abandonAllAudioFocus() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                XWeiControl.getInstance().nativeAbandonAllAudioFocus();
            }
        });
    }

    /**
     * 设置当前App获得的焦点，Android向系统申请焦点的时候会获得对应的focusChange，通知播放控制焦点管理器进行管理。
     *
     * @param focusChange 可以是{@link AudioManager#AUDIOFOCUS_GAIN}、{@link AudioManager#AUDIOFOCUS_LOSS}、{@link AudioManager#AUDIOFOCUS_LOSS_TRANSIENT}、{@link AudioManager#AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK}
     */
    public void setAudioFocusChange(final int focusChange) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                QLog.d(TAG, "setAudioFocusChange " + focusChange);
                mFocusChange = focusChange;
                XWeiControl.getInstance().nativeSetAudioFocus(focusChange);// 1 3 内部焦点可传递，2  内部焦点只可以给一个APP，之后置为0， < 0,focus置为0  AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK focus置为1。
            }
        });
    }

}
