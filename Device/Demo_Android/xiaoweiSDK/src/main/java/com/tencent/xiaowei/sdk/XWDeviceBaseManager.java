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
package com.tencent.xiaowei.sdk;

import android.text.TextUtils;
import android.util.Log;

import com.tencent.xiaowei.info.XWAIAudioFriendInfo;
import com.tencent.xiaowei.info.XWBinderInfo;
import com.tencent.xiaowei.info.XWBinderRemark;
import com.tencent.xiaowei.info.XWContactInfo;
import com.tencent.xiaowei.info.XWLoginInfo;
import com.tencent.xiaowei.util.QLog;

import java.net.URI;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xw on 2016/11/25.
 * SDK基础功能
 */
public class XWDeviceBaseManager {
    public static final String TAG = "XWDeviceBaseManager";
    // 绑定者相关
    private static OnGetBinderListListener mOnGetBinderListListener;
    private static OnBinderEventListener mOnBinderEventListener;
    private static OnUnBindListener mOnEraseAllBinderListener;
    private static Runnable binderOnlineStatusRunnable = null;
    private static OnGetSDKLogListener mOnGetSDKLogListener;

    protected final static HashMap<Long, XWContactInfo> mAllFriendListCache = new HashMap<>();// 所有的XWContactInfo

    public static long getSelfDin() {
        return XWSDKJNI.getSelfDin();
    }

    /**
     * 获取绑定者列表的结果
     */
    public interface OnGetBinderListListener {
        void onResult(int error, ArrayList<XWBinderInfo> mBinderList);
    }

    /**
     * 网络ping相关，ping不通会自动重试
     *
     * @param ret 0 连接成功，1 网络不通， 2 ping不通服务器
     */
    static void onConnectedServer(int ret) {
        QLog.d(TAG, "onConnectedServer " + ret);
        if (mOnDeviceRegisterEventListener != null) {
            mOnDeviceRegisterEventListener.onConnectedServer(ret);
        }
    }

    /**
     * 注册相关，注册失败后会自动重试
     *
     * @param ret    0 注册成功，1 信息不对， 2 未知错误
     * @param subRet 对应的内部错误码
     */
    static void onRegister(int ret, int subRet) {
        QLog.d(TAG, "onRegister " + ret + " " + subRet);
        if (mOnDeviceRegisterEventListener != null) {
            mOnDeviceRegisterEventListener.onRegister(ret, subRet);
        }
    }


    // 初始化注册相关
    private static OnDeviceRegisterEventListener mOnDeviceRegisterEventListener;

    public interface OnDeviceRegisterEventListener {

        void onConnectedServer(int errorCode);

        void onRegister(int errorCode, int subCode);// 10*n秒重试一次。n从1开始累加
    }

    public static void setOnDeviceRegisterEventListener(OnDeviceRegisterEventListener listener) {
        mOnDeviceRegisterEventListener = listener;
    }


    /**
     * 获取绑定列表
     *
     * @param listener
     */
    public static void getBinderList(OnGetBinderListListener listener) {
        XWBinderInfo[] list = XWSDKJNI.fetchBinderList();
        if (list != null && listener != null) {
            listener.onResult(0, XWSDKJNI.getBinderList());
        } else {
            mOnGetBinderListListener = listener;
        }
        QLog.d(TAG, "getBinderList");
    }

    /**
     * 更新绑定者在线状态, 结果通过setOnBinderEventListener设置的listener通知
     *
     * @param intervalSec - 刷新的时间间隔(单位秒)，最小支持10秒, 如果传0则结束刷新
     */
    public static void updateBinderListOnlineStatus(final int intervalSec) {

        if (intervalSec == 0 && binderOnlineStatusRunnable != null) {
            XWSDKJNI.getInstance().removeMainRunnable(binderOnlineStatusRunnable);
            binderOnlineStatusRunnable = null;
        }

        if (intervalSec >= 10 && binderOnlineStatusRunnable == null) {

            binderOnlineStatusRunnable = new Runnable() {
                @Override
                public void run() {

                    XWBinderInfo[] infos = XWSDKJNI.fetchBinderList();
                    if (infos != null && infos.length > 0) {
                        XWSDKJNI.updateBinderOnlineStatus();
                    }

                    XWSDKJNI.getInstance().postMainDelay(binderOnlineStatusRunnable, intervalSec * 1000);
                }
            };

            XWSDKJNI.updateBinderOnlineStatus();
            XWSDKJNI.getInstance().postMainDelay(binderOnlineStatusRunnable, intervalSec * 1000);
        }
    }

    static void onBinderListChange(final int error, final ArrayList<XWBinderInfo> mBinderList) {
        postMain(new Runnable() {
            @Override
            public void run() {
                if (mOnGetBinderListListener != null) {
                    mOnGetBinderListListener.onResult(error, mBinderList);
                    mOnGetBinderListListener = null;
                } else {
                    // 被动改变的
                    if (mOnBinderEventListener != null) {
                        mOnBinderEventListener.onBinderListChange(error, mBinderList);
                    }
                }
            }
        });
        QLog.d(TAG, "onBinderListChange " + error);
    }

    /**
     * 解除所有绑定者的结果
     */
    public interface OnUnBindListener {
        void onResult(int error);
    }


    /**
     * 解绑所有绑定者（须在登录成功之后调用）
     */
    public static void unBind(OnUnBindListener listener) {
        XWSDKJNI.unBind();
        mOnEraseAllBinderListener = listener;
        QLog.d(TAG, "eraseAllBinders ");
    }

    static void onUnBind(final int error) {
        postMain(new Runnable() {
            @Override
            public void run() {
                if (mOnEraseAllBinderListener != null) {
                    mOnEraseAllBinderListener.onResult(error);
                    mOnEraseAllBinderListener = null;
                }
            }
        });
        QLog.d(TAG, "onEraseAllBinders " + error);
    }

    /**
     * 绑定者相关的事件监听器
     */
    public interface OnBinderEventListener {

        void onBinderListChange(int error, ArrayList<XWBinderInfo> mBinderList);
    }

    /**
     * 设置绑定者相关的事件监听器
     *
     * @param listener
     */
    public static void setOnBinderEventListener(OnBinderEventListener listener) {
        mOnBinderEventListener = listener;
    }

    // 基础接口

    /**
     * 获得SDK版本
     *
     * @return
     */
    public static int[] getSDKVersion() {
        return XWSDKJNI.getInstance().getSDKVersion();
    }

    /**
     * 生成二维码url(根据init时传入的pid,sn,license生成)
     *
     * @return
     */
    public static String getQRCodeUrl() {
        return XWSDKJNI.getQRCodeUrl();
    }

    /**
     * 生成支持QQ绑定的二维码
     *
     * @return
     */
    public static String getQRCodeUrl4QQ() {
        return appendUri(getQRCodeUrl(), "sqq=1");
    }


    public static boolean isSupportMusicPay() {
        long[] adminInfo = XWSDKJNI.getBinderAdminInfo();
        return adminInfo != null && adminInfo.length == 3 && adminInfo[0] != 0 && adminInfo[1] == 0 && adminInfo[2] != 0;
    }

    public static String getMusicPayQRCode(String key) {
        if (isSupportMusicPay()) {
            long[] adminInfo = XWSDKJNI.getBinderAdminInfo();
            String oriUrl = "https://y.qq.com/m/act/cloudvip/index.html";
            String url = appendUri(oriUrl, "partner=" + key);
            url = appendUri(url, "check_uin=1");
            String uin = MD5(String.valueOf(adminInfo[2]));
            url = appendUri(url, "uin=" + uin);
            return url;
        } else {
            Log.d(TAG, "getMusicPayQRCode not support!");
            return "";
        }
    }

    public static String MD5(String path) {
        byte[] buffer = path.getBytes();
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] md5Bytes = md5.digest(buffer);
            StringBuffer hexValue = new StringBuffer();
            for (int i = 0; i < md5Bytes.length; i++) {
                int val = ((int) md5Bytes[i]) & 0xff;
                if (val < 16)
                    hexValue.append("0");
                hexValue.append(Integer.toHexString(val));
            }
            return hexValue.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String appendUri(String uri, String appendQuery) {

        try {
            URI oldUri = new URI(uri);

            String newQuery = oldUri.getQuery();
            if (newQuery == null) {
                newQuery = appendQuery;
            } else {
                newQuery += "&" + appendQuery;
            }

            URI newUri = new URI(oldUri.getScheme(), oldUri.getAuthority(),
                    oldUri.getPath(), newQuery, oldUri.getFragment());

            return newUri.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return uri;
    }

    /**
     * 获取绑定者的账户类型
     *
     * @return
     */
    public static int getBinderTinyIDType() {
        long[] adminInfo = XWSDKJNI.getBinderAdminInfo();
        if (adminInfo != null && adminInfo.length == 3 && 1 == adminInfo[0]) {
            if (0 == adminInfo[1]) {
                return XWBinderInfo.BINDER_TINYID_TYPE_QQ_NOM;
            } else if (1 == adminInfo[1]) {
                return XWBinderInfo.BINDER_TINYID_TYPE_QQ;
            } else if (2 == adminInfo[1]) {
                return XWBinderInfo.BINDER_TINYID_TYPE_WX;
            }
        }
        return XWBinderInfo.BINDER_TINYID_TYPE_UNKNOWN;
    }

    /**
     * 恢复网络后调用这个接口立马重新连接
     */
    public static void deviceReconnect() {
        XWSDKJNI.deviceReconnect();
    }

    /**
     * 获取腾讯云服务器标准校时时间(s)
     */
    public static int getServerTime() {
        return XWSDKJNI.getServerTime();
    }

    // 登录相关
    private static OnDeviceLoginEventListener mOnDeviceLoginEventListener;

    /**
     * 登录相关的监听器
     */
    public interface OnDeviceLoginEventListener {
        /**
         * 登录完成
         *
         * @param error 0表示成功
         */
        void onLoginComplete(int error);

        /**
         * 上线
         */
        void onOnlineSuccess();

        /**
         * 离线
         */
        void onOfflineSuccess();

        /**
         * 上传注册信息成功，在此时可以获得带正确token的绑定二维码
         */
        void onUploadRegInfo(int error);


    }

    /**
     * 获取绑定者列表的结果
     */
    public interface OnGetSDKLogListener {
        /**
         * 厂商日志获取
         *
         * @param level   log级别 取值有 0 严重错误；1 错误；2 警告；3 提示；4 调试
         * @param module  模块
         * @param line    行号
         * @param message log内容
         */
        void onGetSDKLog(int level, String module, int line, String message);
    }

    static void onUploadRegInfoSuccess(final int error) {
        if (mOnDeviceLoginEventListener != null) {
            postMain(new Runnable() {
                @Override
                public void run() {
                    mOnDeviceLoginEventListener.onUploadRegInfo(error);
                }
            });
        }
    }

    public static void setOnDeviceSDKEventListener(OnDeviceLoginEventListener listener) {
        mOnDeviceLoginEventListener = listener;
    }

    static void onLoginComplete(final int error) {
        if (mOnDeviceLoginEventListener != null) {
            postMain(new Runnable() {
                @Override
                public void run() {
                    mOnDeviceLoginEventListener.onLoginComplete(error);
                }
            });
        }
    }

    static void onOnlineSuccess() {
        if (mOnDeviceLoginEventListener != null) {
            postMain(new Runnable() {
                @Override
                public void run() {
                    mOnDeviceLoginEventListener.onOnlineSuccess();
                }
            });
        }
    }

    static void onOfflineSuccess() {
        if (mOnDeviceLoginEventListener != null) {
            postMain(new Runnable() {
                @Override
                public void run() {
                    mOnDeviceLoginEventListener.onOfflineSuccess();
                }
            });
        }
    }

    public static void setOnGetSDKLogListener(OnGetSDKLogListener listener) {
        mOnGetSDKLogListener = listener;
    }

    static void onGetSDKLog(int level, String module, int line, String message) {
        if (mOnGetSDKLogListener != null) {
            mOnGetSDKLogListener.onGetSDKLog(level, module, line, message);
        }
    }

    //绑定者备注相关 begin
    public interface IGetBinderRemarkListCallback {
        void onResult(XWBinderRemark[] binderRemarks);
    }

    public interface ISetBinderRemarkCallback {
        void OnResult(int error);
    }

    private static HashMap<Integer, IGetBinderRemarkListCallback> mGetBinderRemarkListCallbacks = new HashMap();
    private static HashMap<Integer, ISetBinderRemarkCallback> mSetBinderRemarkCallbacks = new HashMap();
    private static IGetBinderRemarkListCallback mBinderRemarkListener = null;

    public static void registerBinderRemarkChangeListener(final IGetBinderRemarkListCallback callback) {
        if (callback != null)
            mBinderRemarkListener = callback;
    }

    public static void getBinderRemarkList(final IGetBinderRemarkListCallback callback) {
        postMain(new Runnable() {
            @Override
            public void run() {
//                int sid = XWSDKJNI.getBinderRemarkList();
//                long[] adminInfo = XWSDKJNI.getBinderAdminInfo();
                mGetBinderRemarkListCallbacks.put(XWSDKJNI.getBinderRemarkList(), callback);
            }
        });
    }

    public static void setBinderRemark(final XWBinderRemark binderRemark, final ISetBinderRemarkCallback callback) {
        postMain(new Runnable() {
            @Override
            public void run() {
                mSetBinderRemarkCallbacks.put(XWSDKJNI.setBinderRemark(binderRemark.tinyid, binderRemark.remark), callback);
            }
        });
    }

    static void onGetBinderRemarkList(final int cookie, final XWBinderRemark[] binderRemarks) {
        postMain(new Runnable() {
            @Override
            public void run() {
                IGetBinderRemarkListCallback callback = mGetBinderRemarkListCallbacks.get(cookie);
                if (callback != null) {
                    callback.onResult(binderRemarks);
                }

                if (mBinderRemarkListener != null)
                    mBinderRemarkListener.onResult(binderRemarks);
            }
        });
    }

    static void onSetBinderRemark(final int cookie, final int error) {
        postMain(new Runnable() {
            @Override
            public void run() {
                ISetBinderRemarkCallback callback = mSetBinderRemarkCallbacks.get(cookie);
                if (callback != null) {
                    callback.OnResult(error);
                }
            }
        });
    }

    static void postMain(Runnable runnable) {
        XWSDKJNI.getInstance().postMain(runnable);
    }


    public static ArrayList<XWBinderInfo> getBinderList() {
        return XWSDKJNI.getBinderList();
    }

    public static XWLoginInfo getLoginInfo() {
        return XWCoreService.mXWLoginInfo;
    }

    /**
     * 判断一个uin是不是绑定者分享者或设备好友
     *
     * @param uin
     * @return
     */
    public static boolean isContact(long uin) {
        return mAllFriendListCache.containsKey(uin);
    }


    /**
     * 根据uin查询XWContactInfo
     *
     * @param uin
     * @return 不会为null，如果没找到对应的XWContactInfo，会new一个新的并赋值uin
     */
    public static XWContactInfo getXWContactInfo(String uin) {
        XWContactInfo contact = mAllFriendListCache.get(Long.valueOf(uin));
        //TXDeviceSDK.getMsgProxyFlag() == 1 &&
        if (contact == null) {
            contact = mAllFriendListCache.get(0);
        }

        if (contact == null) {
            contact = new XWContactInfo(Long.valueOf(uin));
        }
        return contact;
    }

    private static ConcurrentHashMap<String, GetFriendListRspListener> mGetFriendListListeners = new ConcurrentHashMap<>();

    private static OnFriendListChangeListener mOnFriendListChangeListener;

    public interface GetFriendListRspListener {
        void onResult(int errCode, XWAIAudioFriendInfo[] friendList);
    }

    public interface OnFriendListChangeListener {
        void onResult(XWAIAudioFriendInfo[] friendList);
    }


    /**
     * alarm_sound
     * 设置代收联系人信息通知回调
     *
     * @param listener
     */
    public static void setOnFriendListChangeListener(OnFriendListChangeListener listener) {
        mOnFriendListChangeListener = listener;
    }


    public static int getAIAudioFriendList(GetFriendListRspListener listener) {
        String sessionID = XWSDKJNI.getAIAudioFriendList();
        QLog.d(TAG, "getAIAudioFriendList sessionID: " + sessionID);

        if (!TextUtils.isEmpty(sessionID)) {
            mGetFriendListListeners.put(sessionID, listener);
        } else {
            listener.onResult(-1, null);
            QLog.d(TAG, "getAIAudioFriendList failed session is null");
        }

        return (sessionID == null ? -1 : 0);
    }

}
