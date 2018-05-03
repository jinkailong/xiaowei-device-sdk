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
package com.tencent.aiaudio.alarm;

import android.app.Application;

import com.tencent.xiaowei.info.XWResponseInfo;

/**
 * 闹钟相关管理：将后台下发的闹钟事件
 */
public interface IDeviceSkillAlarmManager {
    /**
     * 初始化application对象
     */
    void init(Application application);

    /**
     * 启动设备所有闹钟
     */
    void startDeviceAllAlarm();

    /**
     * 判断是否设置闹钟操作
     *
     * @param responseInfo 当前后台下发资源实体
     * @return true表示闹钟Skill
     */
    boolean isSetAlarmOperation(XWResponseInfo responseInfo);

    /**
     * 判断是否为"稍后提醒我"指令设置的闹钟
     *
     * @param responseInfo  当前后台下发资源实体
     * @return true表示闹钟为"稍后提醒我"设置的闹钟
     */
    boolean isSnoozeAlarm(XWResponseInfo responseInfo);

    /**
     * 处理闹钟/定时Skill场景数据
     *
     * @param responseInfo 当前后台下发资源实体
     */
    boolean operationAlarmSkill(XWResponseInfo responseInfo);

    /**
     * 拉取服务器的闹钟列表
     */
    void updateAlarmList();

    /**
     * 触发延迟播放的
     */
    void triggerDelayAlarm();

    /**
     * 清除并取消本地的所有闹钟
     */
    void eraseAllAlarms();

    /**
     * 释放闹钟使用资源
     */
    void release();
}
