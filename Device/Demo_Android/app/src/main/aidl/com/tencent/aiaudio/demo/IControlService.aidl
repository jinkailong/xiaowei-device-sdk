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
package com.tencent.aiaudio.demo;

import com.tencent.xiaowei.info.MediaMetaInfo;
// Declare any non-default types here with import statements

interface IControlService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
    //        double aDouble, String aString);


    int getCurrentPlayMode(int sessionId);

    boolean isPlaying(int sessionId);

    int getCurrentPosition(int sessionId);

    int getDuration(int sessionId);

    void seekTo(int sessionId, int position);

    List<MediaMetaInfo> getCurrentMediaList(int sessionId);

    String[] getCurrentPlayIdList(int sessionId);

    MediaMetaInfo getCurrentMediaInfo(int sessionId);

    void getMoreList(int sessionId);

    void refreshPlayList(int sessionId);
}
