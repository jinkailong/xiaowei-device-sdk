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
package com.tencent.aiaudio.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.aiaudio.CommonApplication;
import com.tencent.aiaudio.activity.MainActivity;
import com.tencent.aiaudio.chat.AVChatManager;
import com.tencent.aiaudio.demo.IAIAudioService;
import com.tencent.aiaudio.player.XWeiPlayerMgr;
import com.tencent.aiaudio.utils.AssetsUtil;
import com.tencent.aiaudio.utils.DemoOnAudioFocusChangeListener;
import com.tencent.aiaudio.wakeup.RecordDataManager;
import com.tencent.utils.MusicPlayer;
import com.tencent.utils.ThreadManager;
import com.tencent.xiaowei.control.XWeiAudioFocusManager;
import com.tencent.xiaowei.control.XWeiCommon;
import com.tencent.xiaowei.control.XWeiControl;
import com.tencent.xiaowei.def.XWCommonDef;
import com.tencent.xiaowei.info.XWContextInfo;
import com.tencent.xiaowei.info.XWResponseInfo;
import com.tencent.xiaowei.sdk.XWDeviceBaseManager;
import com.tencent.xiaowei.sdk.XWSDK;
import com.tencent.xiaowei.util.QLog;

import java.io.File;
import java.io.FileInputStream;


/**
 * 负责录音，语音请求相关的逻辑
 */
public class AIAudioService extends Service {
    private static final String TAG = AIAudioService.class.getSimpleName();

    private int audioSource = MediaRecorder.AudioSource.DEFAULT;
    private int sampleRateInHz = 16000;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);

    public static boolean localVad;
    /**
     * 查询语音场景信息
     */

    public static final String QUERY = "com.ktcp.voice.QUERY";
    /**
     * 查询语音场景信息
     */
    public static final String COMMIT = "com.ktcp.voice.COMMIT";
    /**
     * 执行语音命令
     */
    public static final String EXECUTE = "com.ktcp.voice.EXECUTE";
    /**
     * 语音搜索
     */
    public static final String SEARCH = "com.ktcp.voice.SEARCH";
    /**
     * 语音执行结果
     */
    public static final String EXERESULT = "com.ktcp.voice.EXERESULT";

    static AIAudioService service;
    private XWeiAudioFocusManager.OnAudioFocusChangeListener listener;

    public static AIAudioService getInstance() {
        return service;
    }

    private AudioRecord audioRecorder = new AudioRecord(audioSource,
            sampleRateInHz,
            channelConfig,
            audioFormat,
            bufferSizeInBytes);

    private AudioManager mAudioManager = null;

    private AcousticEchoCanceler canceler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new AIAudioServer();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        service = this;

        if (AudioRecord.STATE_INITIALIZED != audioRecorder.getState()) {
            audioRecorder.release();
            audioRecorder = new AudioRecord(audioSource,
                    sampleRateInHz,
                    channelConfig,
                    audioFormat,
                    bufferSizeInBytes);
        }

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        requestFocus();

        SharedPreferences sp = getSharedPreferences("wakeup", Context.MODE_PRIVATE);
        RecordDataManager.getInstance().setWakeupEnable(sp.getBoolean("use", true));

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            if (AcousticEchoCanceler.isAvailable()) {
                canceler = AcousticEchoCanceler.create(audioRecorder.getAudioSessionId());
                if (canceler != null) {
                    canceler.setEnabled(true);
                }
            }

            XWeiPlayerMgr.setAudioSessionId(audioRecorder.getAudioSessionId());
        }

        RecordDataManager.getInstance().start(AIAudioService.this);
        startRecord();
        AssetsUtil.init(this);


        XWeiControl.getInstance().getCommonTool().setOnVolumeChangeListener(new XWeiCommon.OnVolumeChangeListener() {
            @Override
            public void onSilence(boolean silence) {
                if (silence) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI);
                } else {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, AudioManager.FLAG_SHOW_UI);
                }
            }

            @Override
            public void onChangeVolume(boolean isIncrement, double volume) {
                int tag = AudioManager.STREAM_MUSIC;
                int max = mAudioManager.getStreamMaxVolume(tag);
                int current = mAudioManager.getStreamVolume(tag);
                int vol;
                double percent;
                if (volume <= 1 && volume >= -1) {
                    percent = volume;
                } else {
                    percent = volume / 100f;
                }

                if (isIncrement) {
                    vol = (int) (current + percent * max);
                } else {
                    vol = (int) (percent * max);
                }
                if (vol < 0) {
                    vol = 0;
                }
                if (vol > max) {
                    vol = max;
                }
                mAudioManager.setStreamVolume(tag, vol, AudioManager.FLAG_SHOW_UI);
            }

        });
        XWeiControl.getInstance().getCommonTool().setOnFetchDeviceInfoListener(new XWeiCommon.OnFetchDeviceInfoListener() {
            @Override
            public void onFetch(String type) {
                switch (type) {
                    case "MAC":
                        String mac = getLocalMacAddress(AIAudioService.this);
                        if (TextUtils.isEmpty(mac)) {
                            return;
                        }
                        mac = mac.replaceAll(":", "");
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < mac.length(); i++) {
                            sb.append(mac.charAt(i));
                            if (i % 2 == 1 && i != mac.length() - 1) {
                                sb.append(" 冒号 ");
                            } else {
                                sb.append(" ");
                            }
                        }
                        playTTS("我的mac地址是" + sb.toString());
                        break;
                    case "IP":
                        String ip = getWIFILocalIpAdress(AIAudioService.this);
                        playTTS("我的IP地址是" + ip);
                        break;
                    case "DIN":
                        playTTS("我的设备编号是" + XWDeviceBaseManager.getSelfDin());
                        break;
                    case "SN":
                        playTTS("我的序列号是" + getFormatTTSText(XWDeviceBaseManager.getLoginInfo().serialNumber));
                        break;
                }
            }
        });

        XWSDK.getInstance().setNetworkDelayListener(new XWSDK.NetworkDelayListener() {
            @Override
            public void onDelay(String voiceId, long time) {
                if (WakeupAnimatorService.getInstance() != null)
                    WakeupAnimatorService.getInstance().setNetText(time + "ms");
            }
        });
    }

    private void playTTS(String text) {
        XWSDK.getInstance().requestTTS(text.getBytes(), new XWContextInfo(), new XWSDK.RequestListener() {
            @Override
            public boolean onRequest(int event, final XWResponseInfo rspData, byte[] extendData) {
                listener = new XWeiAudioFocusManager.OnAudioFocusChangeListener() {
                    @Override
                    public void onAudioFocusChange(int focusChange) {
                        if (focusChange == XWeiAudioFocusManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK) {
                            MusicPlayer.getInstance().playMediaInfo(rspData.resources[0].resources[0], new MusicPlayer.OnPlayListener() {
                                @Override
                                public void onCompletion(int error) {
                                    XWeiAudioFocusManager.getInstance().abandonAudioFocus(listener);
                                }

                            });
                        } else {
                            MusicPlayer.getInstance().stop();
                        }
                    }
                };
                XWeiAudioFocusManager.getInstance().requestAudioFocus(listener, XWeiAudioFocusManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
                return true;
            }
        });
    }

    private void requestFocus() {
        if (XWeiAudioFocusManager.getInstance().needRequestFocus(AudioManager.AUDIOFOCUS_GAIN)) {
            int ret = mAudioManager.requestAudioFocus(DemoOnAudioFocusChangeListener.getInstance(), AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (ret == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                XWeiAudioFocusManager.getInstance().setAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);
            }
        }
    }

    // 只给查设备信息的长文本使用，让TTS能正确念出来这个格式
    private String getFormatTTSText(String text) {
        StringBuffer sb = new StringBuffer();
        try {
            Long.valueOf(text);
            for (int i = 0; i < text.length(); i++) {
                sb.append(text.charAt(i));
                sb.append(" ");
            }
        } catch (Exception e) {
            for (int i = 0; i < text.length(); i++) {
                if (':' == text.charAt(i)) {
                    sb.append("冒号");
                } else {
                    sb.append(text.charAt(i));
                }
                sb.append(",");
            }
        }

        return sb.toString();
    }

    private void wakeup() {
        Log.e(TAG, "context.voiceRequestBegin true wakup");
        XWContextInfo contextInfo = new XWContextInfo();
        if (localVad) {
            contextInfo.requestParam |= XWContextInfo.REQUEST_PARAM_USE_LOCAL_VAD;
        }
        RecordDataManager.getInstance().onWakeup(contextInfo);
    }

    public void wakeup(String contextId, int speakTimeout, int silentTimeout, long requestParam) {
        XWContextInfo contextInfo = new XWContextInfo();
        contextInfo.ID = contextId;
        contextInfo.silentTimeout = silentTimeout;
        contextInfo.speakTimeout = speakTimeout;
        contextInfo.requestParam = requestParam;

        RecordDataManager.getInstance().onWakeup(contextInfo);
    }

    public void setVolume(int value) {
        int tag = AudioManager.STREAM_MUSIC;
        if (mAudioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
            tag = AudioManager.MODE_IN_COMMUNICATION;
        }
        int max = mAudioManager.getStreamMaxVolume(tag);
        int current = mAudioManager.getStreamVolume(tag);
        int vol;
        float percent;
        if (value <= 1 && value >= -1) {
            percent = value;
        } else {
            percent = value / 100f;
        }

        vol = (int) (percent * max);
        if (vol < 0) {
            vol = 0;
        }
        if (vol > max) {
            vol = max;
        }
        mAudioManager.setStreamVolume(tag, vol, AudioManager.FLAG_SHOW_UI);
        QLog.d(TAG, "setVolume old: " + current + ", new: " + vol);
    }

    public int getVolume() {
        int tag = AudioManager.STREAM_MUSIC;
        if (mAudioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
            tag = AudioManager.MODE_IN_COMMUNICATION;
        }
        int max = mAudioManager.getStreamMaxVolume(tag);
        int current = mAudioManager.getStreamVolume(tag);
        int vol = (int) (current * 100.0f / max);
        QLog.d(TAG, "getVolume current: " + current + ", max: " + max + ", vol: " + vol);
        return vol;
    }

    private void startRecord() {
        startRecordThread();
    }

    private void startRecordThread() {
        // 开始录音
        try {
            audioRecorder.startRecording();
        } catch (Exception e) {
            e.printStackTrace();
            CommonApplication.showToast("请打开权限中的录音再重启APP");
            return;
        }
        mRecordTask.startRecording();
        ThreadManager.getInstance().start(mRecordTask);
    }

    private RecordTask mRecordTask = new RecordTask();


    // 录音工作线程，需要自行适配
    private class RecordTask implements Runnable {

        private volatile boolean isRecording = false;
        private final Object recordObject = new Object();

        public boolean isRecording() {
            synchronized (recordObject) {
                return isRecording;
            }
        }

        public void startRecording() {
            synchronized (recordObject) {
                this.isRecording = true;
            }
        }

        public void stopRecording() {
            synchronized (recordObject) {
                this.isRecording = false;
            }
        }

        @Override
        public void run() {

            while (isRecording()) {
                byte buffer[] = new byte[bufferSizeInBytes / 2];
                int read_len = audioRecorder.read(buffer, 0, buffer.length);
                if (read_len <= 0) {
                    // 录音出错了,一般是被别的程序占用了麦克风
                    break;
                }
                if (AVChatManager.putAudioData(buffer, read_len)) {
//                    continue;
                }
                RecordDataManager.getInstance().feedData(buffer);
            }
            audioRecorder.stop();
            RecordDataManager.getInstance().stop();
        }
    }


    private void stopRecording() {
        if (null != audioRecorder) {
            mRecordTask.stopRecording();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        service = null;
        stopRecording();
        mAudioManager.abandonAudioFocus(DemoOnAudioFocusChangeListener.getInstance());

        if (android.os.Build.VERSION.SDK_INT >= 16 && AcousticEchoCanceler.isAvailable()) {
            if (canceler != null) {
                canceler.setEnabled(false);
                canceler.release();
            }
        }

    }

    public class AIAudioServer extends IAIAudioService.Stub {

        @Override
        public void setUseWakeup(boolean use) throws RemoteException {
            RecordDataManager.getInstance().setWakeupEnable(use);
        }

        @Override
        public void wakeup() throws RemoteException {
            AIAudioService.this.wakeup();
            MainActivity.setUITips("按钮唤醒");
        }

        @Override
        public String startRequest(String text) throws RemoteException {
            return XWSDK.getInstance().request(XWCommonDef.RequestType.TEXT, text.getBytes(), new XWContextInfo());
        }

        @Override
        public void cancelEmbed() throws RemoteException {
            RecordDataManager.getInstance().onSleep();
        }

        @Override
        public void keepSilence() throws RemoteException {
            Log.e(TAG, "keepSilence");
            RecordDataManager.getInstance().keepSilence(localVad);
        }

        @Override
        public void enableV2A(boolean enable) {
            XWSDK.getInstance().enableV2A(enable);
        }

    }


    //////////////////////////////////////////////////////////////

    private static String getWIFILocalIpAdress(Context context) {

        //获取wifi服务
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = formatIpAddress(ipAddress);
        return ip;
    }

    private static String formatIpAddress(int ipAdress) {

        return (ipAdress & 0xFF) + "." +
                ((ipAdress >> 8) & 0xFF) + "." +
                ((ipAdress >> 16) & 0xFF) + "." +
                (ipAdress >> 24 & 0xFF);
    }

    /**
     * 获得mac
     *
     * @return
     */
    private static String getLocalMacAddress(Context context) {
        String Mac = null;
        try {
            String path = "sys/class/net/wlan0/address";
            if ((new File(path)).exists()) {
                FileInputStream fis = new FileInputStream(path);
                byte[] buffer = new byte[8192];
                int byteCount = fis.read(buffer);
                if (byteCount > 0) {
                    Mac = new String(buffer, 0, byteCount, "utf-8");
                }
                fis.close();
            }

            if (Mac == null || Mac.length() == 0) {
                path = "sys/class/net/eth0/address";
                FileInputStream fis = new FileInputStream(path);
                byte[] buffer_name = new byte[8192];
                int byteCount_name = fis.read(buffer_name);
                if (byteCount_name > 0) {
                    Mac = new String(buffer_name, 0, byteCount_name, "utf-8");
                }
                fis.close();
            }

            if (!TextUtils.isEmpty(Mac)) {
                Mac = Mac.substring(0, Mac.length() - 1);
            }
        } catch (Exception io) {
        }

        if (TextUtils.isEmpty(Mac)) {
            WifiManager wifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo.getMacAddress() != null) {
                return wifiInfo.getMacAddress();// MAC地址
            }
        }
        return TextUtils.isEmpty(Mac) ? "" : Mac;
    }
}
