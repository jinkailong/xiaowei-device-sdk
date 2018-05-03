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

import com.tencent.xiaowei.sdk.XWSDK;
import com.tencent.xiaowei.util.JsonUtil;
import com.tencent.xiaowei.util.QLog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * JNI接口：通用控制
 */
public class XWeiCommon {

    private final static int TYPE_COMMON_CONTROL_VOLUME_SET = 1;
    private final static int TYPE_COMMON_CONTROL_VOLUME_SILENCE = 2;
    private final static int TYPE_COMMON_CONTROL_UPLOAD_LOG = 3;
    private final static int TYPE_COMMON_CONTROL_FETCH_DEVICE_INFO = 4;
    private final static int TYPE_COMMON_CONTROL_FEED_BACK_ERROR = 5;
    private final static int TYPE_COMMON_CONTROL_SET_FAVORITE = 6;
    private OnVolumeChangeListener mOnVolumeChangeListener;
    private OnFetchDeviceInfoListener mOnFetchDeviceInfoListener;

    public native void nativeInit();

    public native void nativeUninit();

    /**
     * 收到了需要应用层处理的通用控制
     *
     * @param type
     * @param json
     */
    private void onCommonControl(int type, String json) {
        QLog.i("XWeiCommon", "onCommonControl " + type + " " + json);
        switch (type) {
            case TYPE_COMMON_CONTROL_VOLUME_SET:
                if (mOnVolumeChangeListener != null) {
                    mOnVolumeChangeListener.onChangeVolume(Boolean.parseBoolean(JsonUtil.getValue(json, "isIncrement")), Double.valueOf(JsonUtil.getValue(json, "value")));
                }
                break;
            case TYPE_COMMON_CONTROL_VOLUME_SILENCE:
                if (mOnVolumeChangeListener != null) {
                    mOnVolumeChangeListener.onSilence(Boolean.parseBoolean(JsonUtil.getValue(json, "silence")));
                }
                break;
            case TYPE_COMMON_CONTROL_UPLOAD_LOG:
                XWSDK.getInstance().uploadLogs(JsonUtil.getValue(json, "start"), JsonUtil.getValue(json, "end"));
                break;
            case TYPE_COMMON_CONTROL_FETCH_DEVICE_INFO:
                if (mOnFetchDeviceInfoListener != null) {
                    mOnFetchDeviceInfoListener.onFetch(JsonUtil.getValue(json, "type"));
                }
                break;
            case TYPE_COMMON_CONTROL_FEED_BACK_ERROR:
                XWSDK.getInstance().errorFeedBack();
                break;
            case TYPE_COMMON_CONTROL_SET_FAVORITE:

                try {
                    JSONObject jsonObject = new JSONObject(json);
                    String event = jsonObject.getString("event");
                    String playId = jsonObject.getString("playId");

                    if (XWeiControl.getInstance().getXWeiPlayerMgr() != null) {
                        XWeiControl.getInstance().getXWeiPlayerMgr().OnFavoriteEvent(event, playId);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
        }
    }

    public interface OnVolumeChangeListener {

        /**
         * 设置为静音或者取消静音
         *
         * @param silence
         */
        void onSilence(boolean silence);

        /**
         * 直接设置，例如"音量设置为50%"，"音量设置为20"   在原来的基础上修改，例如"音量增大50%"，"音量减小20"
         *
         * @param isIncrement 表示在原来的基础上修改(true)或者直接设置(false)
         * @param volume
         */
        void onChangeVolume(boolean isIncrement, double volume);
    }

    public void setOnVolumeChangeListener(OnVolumeChangeListener listener) {
        mOnVolumeChangeListener = listener;
    }

    public interface OnFetchDeviceInfoListener {
        /**
         * 用户说"mac地址"等
         *
         * @param type IP、MAC、DIN、SN、PID
         */
        void onFetch(String type);
    }

    /**
     * 设置查询设备信息的监听
     *
     * @param listener
     */
    public void setOnFetchDeviceInfoListener(OnFetchDeviceInfoListener listener) {
        mOnFetchDeviceInfoListener = listener;
    }

}
