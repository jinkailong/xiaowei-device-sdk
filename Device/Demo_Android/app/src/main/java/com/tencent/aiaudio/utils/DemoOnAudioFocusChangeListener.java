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
package com.tencent.aiaudio.utils;

import android.media.AudioManager;
import android.util.Log;

import com.tencent.xiaowei.control.XWeiAudioFocusManager;

public class DemoOnAudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {

    private static DemoOnAudioFocusChangeListener listener;

    public static DemoOnAudioFocusChangeListener getInstance() {
        // 这里单例可能触发系统的bug，所以每次创建一个，在回调的时候进行判断
        listener = new DemoOnAudioFocusChangeListener();
        return listener;
    }

    private DemoOnAudioFocusChangeListener() {

    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (listener == this) {
            XWeiAudioFocusManager.getInstance().setAudioFocusChange(focusChange);
        }
    }
}
