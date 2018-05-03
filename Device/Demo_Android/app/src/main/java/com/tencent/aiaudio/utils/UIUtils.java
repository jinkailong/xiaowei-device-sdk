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

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.aiaudio.CommonApplication;
import com.tencent.aiaudio.demo.R;

import java.lang.reflect.Field;

public class UIUtils {

    private static Toast mToast;


    /**
     * 获取版本号
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
        }
        return 0;
    }

    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变
     *
     * @param pxValue （DisplayMetrics类中属性density）
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue （DisplayMetrics类中属性density）
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


    /**
     * 获取通知栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    /**
     * 获取标题栏高度
     *
     * @param context
     * @return
     */
    public static int getTitleBarHeight(Activity context) {
        int contentTop = context.getWindow()
                .findViewById(Window.ID_ANDROID_CONTENT).getTop();
        return contentTop - getStatusBarHeight(context);
    }

    /**
     * 获取屏幕宽度，px
     *
     * @param context
     * @return
     */
    public static float getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度，px
     *
     * @param context
     * @return
     */
    public static float getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    /**
     * 获取屏幕像素密度
     *
     * @param context
     * @return
     */
    public static float getDensity(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.density;
    }

    /**
     * 获取scaledDensity
     *
     * @param context
     * @return
     */
    public static float getScaledDensity(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.scaledDensity;
    }


    /**
     * 是否安装了sdcard。
     *
     * @return true表示有，false表示没有
     */
    public static boolean hasSDCard() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }


    /**
     * 检测当的网络（WLAN、3G/2G）状态
     *
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }


    // 悬浮窗相关

    private static WindowManager mWindowManager;
    private static Handler mHandler;

    public static void removeFloatView(View view) {
        if (view == null) {
            return;
        }
        try {
            mWindowManager.removeView(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createFloatView(final View rootView, int x, int y, int width, int height, long duration, boolean focusable) {
        if (rootView == null) {
            return;
        }
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) CommonApplication.mApplication.getSystemService(Context.WINDOW_SERVICE);
        if (focusable) {
            wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            wmParams.flags = WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        } else {
//            wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

        }
        wmParams.format = PixelFormat.RGBA_8888;

        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.x = x;
        wmParams.y = y;

        wmParams.width = width;
        wmParams.height = height;

        if (rootView.getParent() != null) {
            removeFloatView(rootView);
        }

        mWindowManager.addView(rootView, wmParams);
        rootView.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        if (duration > 0) {
            if (mHandler == null) {
                mHandler = new Handler(Looper.getMainLooper());
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    removeFloatView(rootView);
                }
            }, duration);

        }
    }

    public static void createFloatView(final View rootView, int x, int y, int width, int height, long duration) {
        createFloatView(rootView, x, y, width, height, duration, true);
    }


    /**
     * 使用土司的方式弹收藏成功,可以将其写到工具类中
     *
     * @param message
     */
    public static void showToast(String message) {
        //加载Toast布局
        showToast(R.drawable.ic_music_liked, message);
    }

    /**
     * 使用土司的方式弹收藏成功,可以将其写到工具类中
     *
     * @param message
     */
    public static void showToast(int res, String message) {
        //加载Toast布局
        View toastRoot = LayoutInflater.from(CommonApplication.mApplication).inflate(R.layout.custom_toast, null);
        //初始化布局控件
        ImageView iv = (ImageView) toastRoot.findViewById(R.id.img_toast_like);
        iv.setImageResource(res);
        TextView mTextView = (TextView) toastRoot.findViewById(R.id.txt_toast_msg);
        //为控件设置属性
        mTextView.setText(message);
        mTextView.setTextColor(Color.WHITE);
        //Toast的初始化
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = new Toast(CommonApplication.mApplication);
        //获取屏幕高度
        WindowManager wm = (WindowManager) CommonApplication.mApplication.getSystemService(Context.WINDOW_SERVICE);
        int height = wm.getDefaultDisplay().getHeight();

        //Toast的Y坐标是屏幕高度的1/3，不会出现不适配的问题
        mToast.setGravity(Gravity.TOP, 0, height / 3);
        mToast.setDuration(Toast.LENGTH_LONG);

        mToast.setView(toastRoot);
        mToast.show();
    }

    /**
     * 修该Viev的margin属性,单位为px
     */
    public static void changeMargins(View v, int left, int top, int right, int bottom) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            v.requestLayout();
        }
    }


}
