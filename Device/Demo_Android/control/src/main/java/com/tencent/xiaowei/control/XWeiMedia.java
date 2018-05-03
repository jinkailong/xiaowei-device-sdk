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
import com.tencent.xiaowei.control.info.XWeiPlayerInfo;
import com.tencent.xiaowei.control.info.XWeiPlaylistInfo;

/**
 * 控制层媒体信息获取
 */
public class XWeiMedia {
    public native void nativeInit();

    public native void nativeUninit();

    /**
     * 播放状态同步
     *
     * @param sessionId 场景sessionId
     * @param stateCode 状态码，请参考 {@link Constants.XWeiPlayerState}
     */
    public native void txcPlayerStateChange(int sessionId, int stateCode);

    /**
     * 播放控制接口
     *
     * @param sessionId   场景sessionId
     * @param controlCode 控制码，请参考 {@link Constants.XWeiControlCode}
     * @param arg1        参数1，请参考 {@link Constants.XWeiControlCode}中的说明
     * @param arg2        参数2，请参考 {@link Constants.XWeiControlCode}中的说明
     */
    public native boolean txcPlayerControl(int sessionId, int controlCode, int arg1, int arg2);

    /**
     * 获取播放器信息
     *
     * @param sessionId 场景sessionId
     * @return 播放器信息
     */
    public native XWeiPlayerInfo txcGetPlayerInfo(int sessionId);

    /**
     * 获取播放列表信息
     *
     * @param sessionId 场景sessionId
     * @return 播放列表信息
     */
    public native XWeiPlaylistInfo txcGetPlaylistInfo(int sessionId);

    /**
     * 获取播放列表中某个资源信息
     *
     * @param playlistId 播放列表标识
     * @param index      获取资源的索引
     * @return 资源信息
     */
    public native XWeiMediaInfo txcGetMedia(int playlistId, long index);
}
