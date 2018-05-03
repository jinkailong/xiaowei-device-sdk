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
package com.tencent.aiaudio.activity;

import com.tencent.aiaudio.activity.base.BaseActivity;
import com.tencent.xiaowei.util.Singleton;

import java.util.HashMap;

public class ActivityManager {
    private static Singleton<ActivityManager> sSingleton = new Singleton<ActivityManager>() {
        @Override
        protected ActivityManager createInstance() {
            return new ActivityManager();
        }
    };

    public static ActivityManager getInstance() {
        if (sSingleton == null) {
            sSingleton = new Singleton<ActivityManager>() {
                @Override
                protected ActivityManager createInstance() {
                    return new ActivityManager();
                }
            };
        }
        return sSingleton.getInstance();
    }

    private HashMap<Integer, BaseActivity> map = new HashMap<>();

    public void put(int sessionId, BaseActivity activity) {
        map.put(sessionId, activity);
    }


    public void remove(int sessionId) {
        map.remove(sessionId);
    }

    public void finish(int sessionId) {
        BaseActivity activity = map.get(sessionId);
        if (activity != null) {
            activity.onSkillIdle();
        }
    }

}
