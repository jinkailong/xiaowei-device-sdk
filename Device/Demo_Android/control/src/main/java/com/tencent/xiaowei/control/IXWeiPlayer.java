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

import com.tencent.xiaowei.control.info.XWeiMediaInfo;
import com.tencent.xiaowei.control.info.XWeiPlayState;

/**
 * 播放器接口
 */
public interface IXWeiPlayer {

    /**
     * 停止播放
     *
     * @param sessionId
     * @return
     */
    boolean stop(int sessionId);

    /**
     * 暂停播放
     *
     * @param sessionId
     * @return
     */
    boolean pause(int sessionId);

    /**
     * 继续播放
     *
     * @param sessionId
     * @return
     */
    boolean resume(int sessionId);

    /**
     * 改变音量
     *
     * @param sessionId
     * @param volume
     * @return
     */
    boolean changeVolume(int sessionId, int volume);

    /**
     * 播放媒体资源
     *
     * @param sessionId
     * @param mediaInfo
     * @return
     */
    boolean playMediaInfo(int sessionId, XWeiMediaInfo mediaInfo, boolean needReleaseRes);

    /**
     * 获得当前播放位置
     *
     * @return 当前播放位置
     */
    int getCurrentPosition();

    /**
     * 是的总共时长
     *
     * @return 当前播放资源的时长
     */
    int getDuration();

    /**
     * 快进到指定进度
     *
     * @param position 进度
     */
    void seekTo(int position);

    /**
     * 是否正在播放
     *
     * @return 是否正在播放中
     */
    boolean isPlaying();

    /**
     * 需要上报状态，不正常上报会影响后台之后的下发逻辑和小微App的显示记录
     *
     * @param sessionId
     * @param playState
     */
    void onNeedReportPlayState(int sessionId, XWeiPlayState playState);
}
