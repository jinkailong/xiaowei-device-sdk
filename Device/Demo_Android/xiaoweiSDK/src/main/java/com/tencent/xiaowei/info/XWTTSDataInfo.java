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
package com.tencent.xiaowei.info;

import android.support.annotation.NonNull;

/**
 * TTS数据
 */
public class XWTTSDataInfo implements Comparable<XWTTSDataInfo> {

    public static final int FORMAT_PCM = 0;
    public static final int FORMAT_SILK = 1;
    public static final int FORMAT_OPUS = 2;

    /**
     * 资源ID
     */
    public String resID;
    /**
     * 序号
     */
    public int seq;
    /**
     * 标记这个resID的TTS是否接收完毕
     */
    public boolean isEnd;
    /**
     * pcm采样率
     */
    public int pcmSampleRate;
    /**
     * opus采样率
     */
    public int sampleRate;
    /**
     * 声道
     */
    public int channel;
    /**
     * 格式
     */
    public int format;
    /**
     * 数据
     */
    public byte[] data;

    @Override
    public String toString() {
        return "resID:" + resID + " seq:" + seq + " isEnd:" + isEnd + " data.length:" + (data == null ? 0 : data.length);
    }

    @Override
    public int compareTo(@NonNull XWTTSDataInfo another) {
        return seq - another.seq;
    }
}
