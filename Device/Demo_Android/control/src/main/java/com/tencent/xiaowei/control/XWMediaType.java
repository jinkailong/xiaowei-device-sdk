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


/**
 * XWeiMediaInfo {@link com.tencent.xiaowei.control.info.XWeiMediaInfo} 类型定义
 */
public class XWMediaType {
    /**
     * 未知类型
     */
    public final static int TYPE_UNKNOW = 0;
    /**
     * TTS资源起始定义，未使用
     */
    public final static int TYPE_BEGIN_TTS = 1;
    /**
     * 文本类型的TTS播放资源
     * resId: 该TTS的文本内容
     * content: 该TTS的文本内容
     */
    public final static int TYPE_TTS_TEXT = 2;
    /**
     * 文本类型的TTS播放资源, 提示音类型，只播放一次或几次
     * resId: 该TTS的文本内容
     * content: 该TTS的文本内容
     */
    public final static int TYPE_TTS_TEXT_TIP = 3;
    /**
     * OPUS编码类型TTS播放流
     * resId: 资源唯一标识
     * content: 该TTS的文本内容
     */
    public final static int TYPE_TTS_OPUS = 4;

    /**
     * 消息前缀tts的请求数据
     * description: 时间戳
     * content: tinyid
     */
    public final static int TYPE_TTS_MSGPROMPT = 5;

    /**
     * 文本类资源起始定义，未使用
     */
    public final static int TYPE_BEGIN_TEXT = 0x100;
    public final static int TYPE_URL = 0x101;
    public final static int TYPE_JSON = 0x102;

    /**
     * 音乐类型资源起始定义，未使用
     */
    public final static int TYPE_BEGIN_MUSIC = 0x200;
    /**
     * 音乐URL资源
     * resId: 该资源的唯一标识
     * content: URL
     * description: 音乐的meta信息
     */
    public final static int TYPE_MUSIC_URL = 0x201;
    /**
     * 播放URL类型提示音
     * resId: 该资源的唯一标识
     * content: URL
     */
    public final static int TYPE_MUSIC_URL_TIP = 0x202;

    /**
     * 本地文件
     */
    public final static int TYPE_LOCAL_FILE = 0x401;
    /**
     * 多媒体资源的其实定义，未使用
     */
    public final static int TYPE_BEGIN_MEDIA = 0x600;
    public final static int TYPE_H264 = 0x601;
    public final static int TYPE_JPEG = 0x602;
    public final static int TYPE_LYRICS = 0x603;
    /**
     * 混合资源类型定义，未使用
     */
    public final static int TYPE_BEGIN_MISC = 0x800;
    /**
     * 天气信息
     * description：天气信息的JSON描述
     */
    public final static int TYPE_INFO_WEATHER = 0x801;

    public final static int TYPE_USER_DEFINED = 0x1000;
}
