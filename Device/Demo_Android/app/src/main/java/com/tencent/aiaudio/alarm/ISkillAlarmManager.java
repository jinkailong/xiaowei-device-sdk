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

import java.util.ArrayList;

public interface ISkillAlarmManager {
    /**
     * 初始化application对象
     */
    void init(Application application);

    /**
     * 启动所有闹钟
     */
    void startAllAlarm();

    /**
     * 启动一个特定的闹钟
     *
     * @param bean 闹钟
     */
    void startAlarm(SkillAlarmBean bean);

    /**
     * 更新闹钟实体
     *
     * @param bean 闹钟实体
     */
    void updateAlarm(SkillAlarmBean bean);

    /**
     * 删除闹钟实体
     *
     * @param bean 闹钟实体
     */
    void deleteAlarm(SkillAlarmBean bean);

    /**
     * 针对闹钟实体执行动作
     *
     * @param bean      闹钟实体
     * @param operation 当前处理动作
     *                  {@link SkillAlarmManagerOperation#CLOCK_OPT_ADD} 添加闹钟项
     *                  {@link SkillAlarmManagerOperation#CLOCK_OPT_UPDATE} 更新闹钟项
     *                  {@link SkillAlarmManagerOperation#CLOCK_OPT_DEL} 删除闹钟项
     *                  {@link SkillAlarmManagerOperation#CLOCK_OPT_MODIFY_TYPE} 修改闹钟类型
     * @param listener  操作完成监听器
     */
    void executeAlarm(SkillAlarmBean bean, int operation, OnOperationFinishListener listener);

    /**
     * 设置定时skill配置
     *
     * @param config 定时skill配置对象
     */
    void setSkillAlarmConfig(ISkillAlarmConfig config);

    /**
     * 因为打电话而导致设置的闹钟延迟了
     *
     * @param bean 延迟的闹钟
     */
    void delayAlarm(SkillAlarmBean bean);

    /**
     * 拉取服务器的闹钟列表
     */
    void updateAlarmList();

    /**
     * 如果有延迟的闹钟就进行触发
     */
    void triggerDelayAlarm();

    /**
     * 清除本地所有闹钟
     */
    void eraseAllAlarms();

    /**
     * 释放闹钟使用资源
     */
    void release();

    ArrayList<SkillAlarmBean> getAlarmList();

    /**
     * 闹钟管理操作类型
     */
    class SkillAlarmManagerOperation {
        /**
         * 不需要任何操作
         */
        public static final int CLOCK_OPT_NONE = 0;
        /**
         * 添加闹钟项
         */
        public static final int CLOCK_OPT_ADD = 1;
        /**
         * 更新闹钟项
         */
        public static final int CLOCK_OPT_UPDATE = 2;
        /**
         * 删除闹钟项
         */
        public static final int CLOCK_OPT_DEL = 3;
        /**
         * 修改闹钟类型
         */
        public static final int CLOCK_OPT_MODIFY_TYPE = 4;
    }

    /**
     * 定时skill配置接口
     */
    interface ISkillAlarmConfig {
        /**
         * 得到服务器时间
         *
         * @return 返回后台时间值
         */
        long getServerTime();
    }
}
