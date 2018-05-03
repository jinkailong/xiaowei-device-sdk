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
package com.tencent.xiaowei.info;

import com.tencent.xiaowei.util.JsonUtil;

/**
 * 登录状态信息
 */
public class XWLoginStatusInfo {

    public static final int QQ = 1;
    public static final int WX = 2;

    /**
     * appId，场景id
     */
    public String skillId;

    /**
     * 登录态类型 {QQ, WX}
     */
    public int type;

    /**
     * 登录的appid
     */
    public String appID;

    /**
     * 登录的openId
     */
    public String openID;

    /**
     * 登录的accessToken
     */
    public String accessToken;

    /**
     * 登录的refreshToken
     */
    public String refreshToken;

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
