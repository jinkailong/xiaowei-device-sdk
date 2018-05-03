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
package com.tencent.aiaudio.activity;

import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tencent.aiaudio.activity.base.BaseActivity;
import com.tencent.aiaudio.demo.R;
import com.tencent.xiaowei.sdk.XWVoiceLinkManager;

public class WifiDecodeActivity extends BaseActivity {
    private static final int samplerate = 44100;
    private static final int channel = AudioFormat.CHANNEL_IN_MONO;
    private static final int format = AudioFormat.ENCODING_PCM_16BIT;

    private TextView mwifiinfo;

    private int bufferSizeInBytes = 0;

    private AudioRecord audioRecord;

    private int isRecording = 0;

    private int ip = 0;
    private int port = 0;

    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated constructor stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifidecode);
        findViewById(R.id.ackAppBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XWVoiceLinkManager.ackApp(ip, port);
            }
        });
        mwifiinfo = (TextView) findViewById(R.id.wifiinfo);

        XWVoiceLinkManager.startWifiDecoder("TXTEST-axewang-7", samplerate, 3, new XWVoiceLinkManager.OnStartWifiDecoderListener() {
            @Override
            public void onReceiveWifiInfo(String ssid, String pwd, int ip, int port) {
                String info = "ssid:" + ssid + "\npassword:" + pwd + "\nip:" + ip + "\nport:" + port;

                mwifiinfo.setText(info);

                stopRecord();

                XWVoiceLinkManager.stopWifiDecoder();
                SharedPreferences.Editor editor = getSharedPreferences("XWVoiceLink", 0).edit();
                editor.putBoolean("NetworkSetted", true);
                editor.commit();

                WifiDecodeActivity.this.ip = ip;
                WifiDecodeActivity.this.port = port;
            }
        });
        // 创建audiorecoder

        createAudioRecord();
        try {
            startRecord();
        } catch (Exception e) {
            // 没有麦克风权限
            audioRecord = null;
            finish();
        }
    }

    private void createAudioRecord() {
        bufferSizeInBytes = AudioRecord.getMinBufferSize(samplerate, channel, format);
        if (bufferSizeInBytes % (441 * 2 * 2) == 0) {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, samplerate, channel, format, bufferSizeInBytes);

        } else {
            bufferSizeInBytes = (bufferSizeInBytes / (441 * 2 * 2) + 2) * (441 * 2 * 2);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, samplerate, channel, format, bufferSizeInBytes);
        }
    }

    private void startRecord() {
        if (audioRecord != null) {
            audioRecord.startRecording();
            isRecording = 1;
            new Thread(new AudioRecordThread()).start();
        }
    }

    private void stopRecord() {
        if (audioRecord != null) {
            isRecording = 0;
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    class AudioRecordThread implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            int readsize = 0;
            byte[] audiodata = new byte[bufferSizeInBytes];
            byte[] block = new byte[(441 * 2 * 2)];
            byte[] tail_block = null;
            while (isRecording == 1) {
                readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);

                if (readsize == bufferSizeInBytes) {
                    int i = 0;
                    while (readsize > (441 * 2 * 2)) //20ms
                    {
                        System.arraycopy(audiodata, i * (441 * 2 * 2), block, 0, (441 * 2 * 2));
                        XWVoiceLinkManager.fillVoiceWavData(block);
                        i = i + 1;
                        readsize = readsize - (441 * 2 * 2);
                    }

                    if (readsize > 0) {
                        tail_block = new byte[readsize];
                        System.arraycopy(audiodata, i * (441 * 2 * 2), tail_block, 0, readsize);
                        XWVoiceLinkManager.fillVoiceWavData(tail_block);
                    }
                } else {
                    Log.i("TAG_WifiDecode", "size error");
                }
            }
        }

    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onDestroy() {
        stopRecord();
        super.onDestroy();
    }

}
