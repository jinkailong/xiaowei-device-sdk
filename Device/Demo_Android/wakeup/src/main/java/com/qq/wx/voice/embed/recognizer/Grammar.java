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
package com.qq.wx.voice.embed.recognizer;


import android.content.Context;

import com.tencent.xiaowei.util.Singleton;

import java.io.UnsupportedEncodingException;

public class Grammar {
    // private final String TAG = "Grammar";
    private GrammarResource mInnerGrammar = null;

    private Context mContext;

    private GrammarResult mResult = new GrammarResult();

    private int mSoVer = 0;

    private int mBinVer = 0;

    /**
     * -1: no init; 0: init or end; 1: begin or recognize. no init -> init ->
     * begin -> recognize -> end -> begin
     */
    private int mGrammerState = -1;

    private static Singleton<Grammar> sSingleton = new Singleton<Grammar>() {
        @Override
        protected Grammar createInstance() {
            return new Grammar();
        }
    };

    /**
     * 获得唤醒词管理器实例
     */
    public static Grammar getInstance() {
        if (sSingleton == null) {
            sSingleton = new Singleton<Grammar>() {
                @Override
                protected Grammar createInstance() {
                    return new Grammar();
                }
            };
        }
        return sSingleton.getInstance();
    }

    private Grammar() {
        mInnerGrammar = new GrammarResource();
    }

    /**
     * 初始化,利用app绝对路径给GrammarNative载入模型
     *
     * @return [-1,0] -1:失败，一般为路径错误 ;0:成功.
     */
    public int init(Context context, String assetsName) {
        mContext = context;
        int ret = 0;
        ret = mInnerGrammar.init(mContext, assetsName);
        if (ret < 0)
            return -1;

        try {
            ret = GrammarNative.init(mInnerGrammar.getPath().getBytes(),
                    mInnerGrammar.getData().getBytes(), null);
            if (ret < 0)
                return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        mGrammerState = 0;
        return 0;
    }

    /**
     * 设置编号为nKeywordSetIndex的集合
     *
     * @param nKeywordSetIndex
     * @return [-1,0] -1:失败 ;0:成功.
     */
    public int setKeywordSetIndex(int nKeywordSetIndex) {
        return GrammarNative.setKeywordSetIndex(nKeywordSetIndex);
    }

    /**
     * GrammarNative告知引擎新一轮识别要开始了
     *
     * @return [-1,0] -1:失败 ;0:成功.
     */
    public int begin() {
        if (mGrammerState == 0) {
            mGrammerState = 1;
            try {
                int ret = GrammarNative.begin();
                if (ret < 0)
                    return -1;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }
        return 0;
    }

    /**
     * GrammarNative传入语音流
     *
     * @return [-1,0,1,2] -1:失败 ;0:成功;1:唤醒;2:发现半词
     */
    public int recognize(byte[] wav, int len) {
        int ret = 0;
        if (mGrammerState == 1) {
            try {
                ret = GrammarNative.recognize(wav, len);
                if (ret < 0)
                    return -1;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }

            if (ret == 1) {
                mGrammerState = 0;
                return 1;
            }
        }
        return ret;
    }

    /**
     * 如果尚未唤醒，语音传完了，告知GrammarNative引擎没得传了
     *
     * @return [-1,0] -1:失败 ;0:成功.
     */
    public int end() {
        if (mGrammerState == 1) {
            mGrammerState = 0;
            try {
                int ret = GrammarNative.end();
                if (ret < 0)
                    return -1;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }
        return 0;
    }

    /**
     * 获取识别结果，取走回调传给mResult的值
     *
     * @param result
     * @return [-1,0] -1:失败 ;0:成功.
     */
    public int getResult(GrammarResult result) {
        try {
            int ret = GrammarNative.getResult(this);
            if (ret < 0)
                return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        result.text = mResult.text;
        result.name = mResult.name;
        result.action = mResult.action;
        result.type = mResult.type;
        return 0;
    }

    public void onGetResult(int type, byte[] text, byte[] action, byte[] name) {

        mResult.type = type;

        try {
            if (text != null) {
                mResult.text = new String(text, "GBK");

                if (mResult.type != 0)
                    mResult.text = null;
            } else {
                mResult.text = null;
            }

            if (action != null) {
                mResult.action = new String(action, "GBK");
            } else {
                mResult.action = null;
            }
            if (name != null) {
                mResult.name = new String(name, "GBK");
            } else {
                mResult.name = null;
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 销毁语音识别
     *
     * @return [-1,0] -1:失败;0:成功.
     */
    public int destroy() {
        sSingleton = null;
        try {
            int ret = GrammarNative.destroy();
            if (ret < 0)
                return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }


    /**
     * 获取版本号，取走回调传回版本号的值
     *
     * @return [-1,0] -1:失败 ;0:成功.
     */
    public int getVersion(SDKVersion sdkVersion) {
        try {
            int ret = GrammarNative.getVersion(this);
            if (ret < 0)
                return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        sdkVersion.soVer = mSoVer;
        sdkVersion.binVer = mBinVer;
        return 0;
    }

    public void onGetVersion(int soVer, int binVer) {
        mSoVer = soVer;
        mBinVer = binVer;
    }
}