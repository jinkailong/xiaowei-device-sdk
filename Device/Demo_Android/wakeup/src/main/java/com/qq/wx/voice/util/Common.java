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
package com.qq.wx.voice.util;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Common {

    public static final String TYPE_PCM = "pcm";
    public static final String TYPE_WAV = "wav";

    /******************
     * <-- volumn --> *
     ******************/
    public static int calculateSum(byte[] buf, int len) {
        int sum = 0;
        for (int j = 0; j < len; j += 2) {
            short value = (short) (((buf[j + 1] & 0xff) << 8) | (buf[j] & 0xff));
            sum += Math.abs(value) / (len / 2);
        }

        return sum;
    }

    public static int calculateVolumn(int sum) {
        int PARAM_ENERGY_THRESHOLD_SP = 30;
        int SCALE = 64;
        double volume = 0;
        if (sum < PARAM_ENERGY_THRESHOLD_SP) {
            volume = 0;
        } else if (sum > 0x3FFF) {
            volume = SCALE;
        } else {
            volume = ((double) sum - (double) PARAM_ENERGY_THRESHOLD_SP)
                    / (12767.0 - (double) PARAM_ENERGY_THRESHOLD_SP)
                    * (double) SCALE;
        }

        return (int) volume;
    }

    public static int calculateVolumn(byte[] buf, int len) {
        int sum = calculateSum(buf, len);
        return calculateVolumn(sum);
    }

    public static void saveFile(byte[] buffer) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String str = dateFormat.format(date);
        saveFile(buffer, str, TYPE_PCM);
    }

    public static void saveFile(byte[] buffer, String typeName) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String str = dateFormat.format(date);
        saveFile(buffer, str, typeName);
    }

    /*********************
     * <-- save file --> *
     *********************/
    @SuppressLint("SimpleDateFormat")
    public static void saveFile(byte[] buffer, String fileName, String typeName) {
        if (buffer == null)
            return;
        String filepath = Environment.getExternalStorageDirectory().getPath()
                + "/tencent/xiaowei/voice/";
        File pcmFile = new File(filepath + fileName + "." + typeName);

        if (!pcmFile.getParentFile().exists()) {
            pcmFile.getParentFile().mkdirs();
        }
        if (!pcmFile.exists()) {
            try {
                pcmFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream pcmOutputStream = new FileOutputStream(pcmFile);
            BufferedOutputStream bos = new BufferedOutputStream(pcmOutputStream);
            if (typeName.equals(TYPE_WAV)) {
                //bos.write(new WaveHeader(buffer.length).getHeader());
            }
            bos.write(buffer);
            bos.flush();
            bos.close();
            pcmOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
