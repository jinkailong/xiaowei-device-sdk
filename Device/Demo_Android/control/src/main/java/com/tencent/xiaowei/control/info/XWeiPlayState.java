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
package com.tencent.xiaowei.control.info;

import com.tencent.xiaowei.util.JsonUtil;

/**
 * 上报状态结构定义
 */
public class XWeiPlayState {
    /**
     * 场景名称
     */
    public String skillName;

    /**
     * (Skill)的唯一ID
     */
    public String skillId;
    /**
     * 播放状态 {@link com.tencent.xiaowei.def.XWCommonDef.PlayState}
     */
    public int playState;

    /**
     * 资源id
     */
    public String resId;

    /**
     * 播放内容
     */
    public String content;

    /**
     * 播放偏移量,单位是s
     */
    public long position;

    /**
     * 播放模式
     */
    public int playMode;

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
