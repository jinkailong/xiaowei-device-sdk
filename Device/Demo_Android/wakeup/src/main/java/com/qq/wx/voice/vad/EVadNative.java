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

public class EVadNative {
    static {
        System.loadLibrary("WXVoice");
    }

    /**
     * return value
     */
    public static final int VAD_SUCCESS = 0;
    public static final int VAD_ERROR = 1;

    public static final int VAD_SPEAK = 2;
    public static final int VAD_SILENCE = 3;
    public static final int VAD_UNKNOW = 4;

    /** MFE API */
    /**
     * 初始化VAD
     *
     * @param sample_rate 采样率
     * @param sil_time    静音末尾时间
     * @param s_n_ration  信噪比
     * @param bwin        判断窗长
     * @param bconfirm    确认窗长
     * @return
     */

    public native long Init(int sample_rate, int sil_time, float s_n_ration,
                            int bwin, int bconfirm);

    public native int Reset(long handle);

    public native int AddData(long handle, short[] data, int dsize);

    public native int Release(long handle);

}
