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
#ifndef _AIAUDIO_AUDIOFOCUS_H_
#define _AIAUDIO_AUDIOFOCUS_H_

// 提供给OuterSkill需要使用声音焦点的时候调用
CXX_EXTERN_BEGIN

#include "txctypedef.h"

// 申请焦点的类型
enum DURATION_HINT
{
    AUDIOFOCUS_GAIN = 1,                    // 暂停上一个，并且自己是可恢复的
    AUDIOFOCUS_GAIN_TRANSIENT = 2,          // 暂停上一个，并且自己是不可恢复的，适合短资源播放
    AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK = 3, // 降低上一个音量
    AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE = 4,// 临时请求一下焦点，这个焦点不能传递给别的场景，其余的和AUDIOFOCUS_GAIN_TRANSIENT一致。唤醒应该申请它。避免在其他APP的时候唤醒一次就一直播放我们APP的资源。

    AUDIOFOCUS_LOSS = -1,                    //应该释放资源了
    AUDIOFOCUS_LOSS_TRANSIENT = -2,          // 应该暂停，待会儿还有机会恢复
    AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK = -3, // 应该降低音量

};

// 焦点改变的回调，所有OuterSkill公用一个焦点回调，使用cookie区分
typedef void (*audio_focus_change_function)(int cookie, int focus_change);

/**
 * 接口说明：设置声音焦点回调接口，外部Skill申请焦点，例如唤醒时，需要申请焦点
 *
 * @param fuc 焦点改变的回调
 */
SDK_API void txc_set_audio_focus_change_callback(audio_focus_change_function fuc);

/**
 * 接口说明：外部Skill请求焦点，例如闹钟Skill，所有OuterSkill公用一个焦点监听
 *
 * @param cookie 所有OuterSkill公用一个焦点回调，使用cookie区分
 * @param duration 焦点类型
 */
SDK_API void txc_request_audio_focus(int& cookie, DURATION_HINT duration);

/**
 * 接口说明：根据cookie释放OuterSkill的焦点监听
 *
 * @param cookie 请求焦点时返回的
 */
SDK_API void txc_abandon_audio_focus(int cookie);

/**
 * 接口说明：释放所有的焦点监听
 */
SDK_API void txc_abandon_all_audio_focus();

/**
 * 接口说明：设置当前App(或者说控制层)可获得的焦点
 *
 * @param focus 仅可以是DURATION_HINT
 */
SDK_API void txc_set_audio_focus(DURATION_HINT focus);

CXX_EXTERN_END

#endif /* _AIAUDIO_AUDIOFOCUS_H_ */
