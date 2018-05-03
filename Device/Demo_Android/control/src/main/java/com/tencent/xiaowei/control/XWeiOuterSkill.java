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

import android.text.TextUtils;

import com.tencent.xiaowei.info.XWResponseInfo;
import com.tencent.xiaowei.util.QLog;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 第三方Skill处理，例如闹钟，本地Skill等
 */
public class XWeiOuterSkill {
    private static final String TAG = XWeiOuterSkill.class.getSimpleName();

    private ConcurrentHashMap<String, OuterSkillHandler> outerSkillHandlers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, WeakReference<OuterSkillHandler>> session2HandlerMap = new ConcurrentHashMap();

    public native void nativeInit();

    public native void nativeUninit();

    /**
     * 注册SkillId或SkillName
     *
     * @param skillIdOrName 场景ID或场景名
     * @param handler       场景处理回调
     */
    public void registerSkillIdOrSkillName(String skillIdOrName, OuterSkillHandler handler) {
        if (!TextUtils.isEmpty(skillIdOrName)) {
            outerSkillHandlers.put(skillIdOrName, handler);
        }
    }

    /**
     * 反注册SkillId或SkillName
     *
     * @param skillIdOrName 场景ID或场景名
     */
    public void unRegisterSkillIdOrSkillName(String skillIdOrName) {
        outerSkillHandlers.remove(skillIdOrName);
    }

    /**
     * 启动第三方Skill（JNI回调）
     *
     * @param sessionId 场景sessionId
     * @param skillName 场景名
     * @param skillId   场景ID
     * @return true 应用层可以处理，false 应用层不可以处理
     */
    private boolean onStartOuterSkill(int sessionId, String skillName, String skillId) {
        QLog.d(TAG, "onStartOuterSkill sessionId=" + sessionId + " skillName=" + skillName + " skillId=" + skillId);
        OuterSkillHandler handler = outerSkillHandlers.get(skillId);
        if (handler == null) {
            handler = outerSkillHandlers.get(skillName);
        }
        if (handler == null) {
            return false;
        }
        session2HandlerMap.put(sessionId, new WeakReference<>(handler));
        return true;
    }

    /**
     * 处理第三方Skill响应数据（JNI回调）
     *
     * @param sessionId    场景sessionId
     * @param responseInfo 场景响应数据
     * @return
     */
    private boolean onSendResponse(int sessionId, XWResponseInfo responseInfo) {
        QLog.d(TAG, "onSendResponse sessionId=" + sessionId);
        OuterSkillHandler handler = outerSkillHandlers.get(responseInfo.appInfo.ID);
        if (handler == null) {
            handler = outerSkillHandlers.get(responseInfo.appInfo.name);
        }
        if (handler == null) {
            WeakReference<OuterSkillHandler> wHandler = session2HandlerMap.get(sessionId);
            if (wHandler != null) {
                handler = wHandler.get();
            }
        }

        return handler != null && handler.handleResponse(sessionId, responseInfo);
    }

    /**
     * 场景处理回调函数
     */
    public interface OuterSkillHandler {
        boolean handleResponse(int sessionId, XWResponseInfo responseInfo);
    }
}
