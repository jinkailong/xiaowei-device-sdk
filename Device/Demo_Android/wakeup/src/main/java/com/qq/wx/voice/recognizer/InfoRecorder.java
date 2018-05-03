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
package com.qq.wx.voice.recognizer;

public class InfoRecorder {
    // frequency
    public static int mFrequency = 16000;

    // buffer size 2048
//	public static int mRecordBufferSize = (1 << 11);
    public static int mRecordBufferSize = 640;

    // 末尾静音
    public static int mSilTime = 500;

    // 录音超时时间
    public static int mTimeout = 5000;

    // 信噪比
    public static float mSNRation = (float) 2.5;

    // 判断窗长
    public static int mBwin = 200;

    // 确认窗长
    public static int mBconfirm = 150;

    // 前置语音长度
    public static int mPreAudioByteSize = 20000;
}
