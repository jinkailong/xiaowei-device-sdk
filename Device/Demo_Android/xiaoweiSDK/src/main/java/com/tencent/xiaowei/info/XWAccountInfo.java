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

import android.os.Parcel;
import android.os.Parcelable;

import com.tencent.xiaowei.util.JsonUtil;


/**
 * 账户(绑定者)信息
 * 用于不使用小微App对接时的绑定信息同步
 * 使用小微App绑定时，无需设置，默认值即可
 */
public class XWAccountInfo implements Parcelable {

	public static final int AccountNull       		= 0;    //默认值，使用小微App时使用
    public static final int AccountQQ               = 1;    //QQ账户绑定
    public static final int AccountWX            	= 2;    //WX账户绑定
    public static final int Account3rd              = 3;    //第三方账户云接口绑定

    /**
     * 账户类型
     */
    public int         type;

    /**
     * 账户名，如果type是QQ/WX登录，则表示openid；如果type是3rd，表示使用云绑定接口时传入的App自有账户名
     */
    public String      account;

    /**
     * 账户token，如果type是QQ/WX登录表示accessToken，其他类型传空即可
     */
    public String      token;

    /**
     * 账户名和token对应的appid，在QQ/WX登录时使用，其他类型传空即可
     */
    public String 	   appid;

    /**
     * 扩展参数
     */
    public byte[]      extendBuffer;


    public XWAccountInfo() {}

    protected XWAccountInfo(Parcel in) {
    	type = in.readInt();
        account = in.readString();
        token = in.readString();
        appid = in.readString();
        extendBuffer = in.createByteArray();
    }

    public static final Parcelable.Creator<XWAccountInfo> CREATOR = new Parcelable.Creator<XWAccountInfo>() {
        @Override
        public XWAccountInfo createFromParcel(Parcel in) {
            return new XWAccountInfo(in);
        }

        @Override
        public XWAccountInfo[] newArray(int size) {
            return new XWAccountInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    	dest.writeInt(type);
        dest.writeString(account);
        dest.writeString(token);
        dest.writeString(appid);
        dest.writeByteArray(extendBuffer);
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}