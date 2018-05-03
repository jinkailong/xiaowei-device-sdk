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
package com.tencent.aiaudio.msg;

import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;

import com.tencent.xiaowei.control.XWeiControl;
import com.tencent.xiaowei.control.info.XWeiMsgInfo;
import com.tencent.xiaowei.info.XWFileTransferInfo;
import com.tencent.xiaowei.info.XWeiMessageInfo;
import com.tencent.xiaowei.sdk.XWSDK;
import com.tencent.xiaowei.util.QLog;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.kvh.media.amr.AmrEncoder;

public class XWeiMsgTransfer {

    final String TAG = "XWeiMsgTransfer";

    private boolean bEnableRecord = false;

    private long lDurations = 0;

    private List<Byte> arrayVoiceData = new ArrayList<Byte>();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    private static volatile XWeiMsgTransfer mInstance;

    public XWeiMsgTransfer() {

    }

    public static synchronized XWeiMsgTransfer getInstance() {
        if (mInstance == null) {
            mInstance = new XWeiMsgTransfer();
        }
        return mInstance;
    }

    public void onDownloadMsgFile(int sessionId, final long tinyId, int channel, final int type, String key1,
                                  String key2, final int duration, final int timestamp) {
        //demo 实现消息的下载，此处app可以控制是否下载消息文件
        XWSDK.getInstance().downloadMiniFile(key1, type, key2, new XWSDK.OnFileTransferListener(){

            @Override
            public void onProgress(long transferProgress, long maxTransferProgress) {

            }

            @Override
            public void onComplete(XWFileTransferInfo info, int errorCode) {
                if (errorCode == 0) {
                    QLog.d(TAG, "downloadMiniFile success. local file: " + info.filePath);
                    XWeiMsgInfo msgInfo = new XWeiMsgInfo();
                    msgInfo.tinyId = tinyId;
                    msgInfo.type = type;
                    msgInfo.duration = duration;
                    msgInfo.content = info.filePath;
                    msgInfo.timestamp = timestamp;
                    XWeiControl.getInstance().addMsgToMsgbox(msgInfo);
                } else {
                    QLog.e(TAG, "downloadMiniFile failed. errCode: " + errorCode);
                }
            }
        });
    }


    public void setVoiceData(byte[] voiceData) {
        if (bEnableRecord) {
            QLog.d(TAG, "setVoiceData " + voiceData.length);
            baos.write(voiceData, 0, voiceData.length);
        }
    }

    public void onAudioMsgRecord() {
        QLog.d(TAG, "onAudioMsgRecord start");
        bEnableRecord = true;
        baos.reset();

        //记录录音开始时间
        lDurations = System.currentTimeMillis();
    }

    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    public void onAudioMsgSend(final long tinyId) {
        QLog.d(TAG, "onAudioMsgSend tinyId: " + tinyId);
        bEnableRecord = false;
        //计算录音时长
        lDurations = System.currentTimeMillis() - lDurations;

        //编码语音文件amr
        final String amrPath = Environment.getExternalStoragePublicDirectory("tencent") + "/device/file/" + "qqmsg_send_" + lDurations + ".amr";
        encodeVoiceData2Amr(baos.toByteArray(), amrPath);

        baos.reset();

        //发送文件
        final XWeiMessageInfo msg = new XWeiMessageInfo();
        msg.type = XWeiMessageInfo.TYPE_AUDIO;
        msg.receiver = new ArrayList<>();
        msg.receiver.add(String.valueOf(tinyId));
        msg.content = amrPath;

        XWSDK.getInstance().sendMessage(msg, new XWSDK.OnSendMessageListener() {

            @Override
            public void onProgress(long transferProgress, long maxTransferProgress) {

            }

            @Override
            public void onComplete(int errCode) {
                QLog.d(TAG, "sendMessage errCode " + errCode);
                if (errCode == 0) {
                    //add to msgbox
                    XWeiMsgInfo msgInfo = new XWeiMsgInfo();
                    msgInfo.tinyId = tinyId;
                    msgInfo.type = XWeiMessageInfo.TYPE_AUDIO;
                    msgInfo.duration = (int)lDurations;
                    msgInfo.content = amrPath;
                    msgInfo.timestamp = (int)System.currentTimeMillis();
                    msgInfo.isRecv = false;
                    XWeiControl.getInstance().addMsgToMsgbox(msgInfo);
                }
            }
        });
    }

    private void encodeVoiceData2Amr(byte[] pcm, String amrPath) {

        AmrEncoder.init(0);
        int mode = AmrEncoder.Mode.MR515.ordinal();

        QLog.e(TAG, "AmrEncoder.encode Begin.");

        try {
            OutputStream out = new FileOutputStream(amrPath);
            //下面的AMR的文件头,缺少这几个字节是不行的
            out.write(0x23);
            out.write(0x21);
            out.write(0x41);
            out.write(0x4D);
            out.write(0x52);
            out.write(0x0A);

            int voiceLength = pcm.length;

            if (pcm.length > 0) {
                String tmpFile = Environment.getExternalStoragePublicDirectory("tencent") + "/device/file/" + "pcm_" + voiceLength + ".pcm";
                OutputStream out2 = new FileOutputStream(tmpFile);
                out2.write(pcm, 0, pcm.length);
                out2.close();
            }

            int pcmSize = 640;
            int amrSize = 320;

            int voiceOffset = 0;
            while (voiceLength > 0 && voiceLength >= pcmSize)
            {
                //pcmSize = (voiceLength > pcmSize ? pcmSize : voiceLength);

                byte[] range = Arrays.copyOfRange(pcm, voiceOffset, voiceOffset + pcmSize);
                QLog.e(TAG, "copyOfRange voiceOffset: " + voiceOffset + " pcmSize: " + pcmSize + " left pcm array: " + voiceLength);

                byte[] newPcm = new byte[range.length/2];
                for (int i = 0; i < newPcm.length; i+=2)
                {
                    if (i >= range.length/2)
                        break;

                    System.arraycopy(range, 2*i, newPcm, i, 2);
                }

                short[] shortin = new short[newPcm.length/2];
                ByteBuffer.wrap(newPcm).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortin);

//                short[] in = new short[amrSize];
//                int rangeLen = range.length;
//                for (int nPos = 0; nPos < amrSize; nPos+=2)
//                {
//                    if (nPos >= pcmSize/2) {
//                        break;
//                    }
//
//                    in[nPos] = (short)(range[nPos*2] << 8 | (range[nPos*2+1]));
//                }

                byte[] outData = new byte[pcmSize/2];
                int len = AmrEncoder.encode(mode, shortin, outData);
                QLog.e(TAG, "AmrEncoder.encode pcmSize: " + pcmSize + " to amr: " + len);
                if (len > 0) {
                    out.write(outData, 0, len);
                }

                voiceOffset += pcmSize;
                voiceLength -= pcmSize;
            }

            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        QLog.e(TAG, "AmrEncoder.encode End.");

        AmrEncoder.exit();

    }
}
