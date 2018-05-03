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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.tencent.aiaudio.CommonApplication;
import com.tencent.aiaudio.PidInfoConfig;
import com.tencent.aiaudio.demo.R;
import com.tencent.aiaudio.utils.UIUtils;
import com.tencent.xiaowei.sdk.XWDeviceBaseManager;
import com.tencent.xiaowei.util.QLog;

import static com.tencent.aiaudio.CommonApplication.ACTION_LOGIN_FAILED;
import static com.tencent.aiaudio.CommonApplication.ACTION_LOGIN_SUCCESS;
import static com.tencent.aiaudio.CommonApplication.ACTION_ON_BINDER_LIST_CHANGE;


/**
 * 这个Activity是用确定已经登录的
 */
public class LoginActivity extends Activity {

    private static final String TAG = "LoginActivity";

    private BroadcastReceiver mBroadcastReceiver;
    private ImageView mIv;
    private TextView mTv;
    private boolean needRetry;
    private boolean isFirst = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!this.isTaskRoot()) {
            String action = getIntent().getAction();
            if (getIntent().hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                Log.e(TAG, "重复打开LoginActivity");
                finish();
                return;
            }
        }
        if (CommonApplication.isLogined) {
            gotoMain();
            return;
        }
        setContentView(R.layout.activity_login);

        final Button btnEnv = (Button) findViewById(R.id.btn_test_env);
        btnEnv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
                int test = sp.getInt("TEST", 0);
                if (test == 0) {
                    test = 1;
                } else {
                    test = 0;
                }
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("TEST", test);
                editor.commit();
                if (test == 0) {
                    btnEnv.setText("切换为测试环境");
                } else {
                    btnEnv.setText("切换为正式环境");
                }
                CommonApplication.showToast("重启生效");
            }
        });

        findViewById(R.id.btn_goto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoMain();
            }
        });

        findViewById(R.id.btn_voice_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, WifiDecodeActivity.class));
            }
        });
        {
            SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
            int test = sp.getInt("TEST", 0);
            if (test == 0) {
                btnEnv.setText("切换为测试环境");
            } else {
                btnEnv.setText("切换为正式环境");
            }
        }

        mIv = (ImageView) findViewById(R.id.iv_err);
        mTv = (TextView) findViewById(R.id.tv_login);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_LOGIN_SUCCESS);
        filter.addAction(ACTION_ON_BINDER_LIST_CHANGE);
        filter.addAction(ACTION_LOGIN_FAILED);
        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case ACTION_LOGIN_FAILED:
                        mTv.setText("登录失败，点击屏幕重试");
                        needRetry = true;
                        break;
                    case ACTION_LOGIN_SUCCESS:
                        needRetry = false;
                        break;
                    case ACTION_ON_BINDER_LIST_CHANGE:
                        if (XWDeviceBaseManager.getBinderList().size() > 0) {
                            gotoMain();
                            return;
                        }
                        if (CommonApplication.isLogined) {
                            showErcode();
                        }
                        break;
                }

            }
        };

        registerReceiver(mBroadcastReceiver, filter);

        if (!UIUtils.isNetworkAvailable(this)) {
            mTv.setText("网络已断开");
        }
    }

    private void showErcode() {
        mTv.setText("请先使用腾讯云小微APP扫描二维码绑定");

//        if (isFirst) {
//            isFirst = false;
//            PcmBytesPlayer.getInstance().playSync(AssetsUtil.getRing("bind_tips.pcm"), null);
//        }

        try {
            Bitmap qrcodeBitmap;
            BitMatrix result;
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            QLog.e(TAG, XWDeviceBaseManager.getQRCodeUrl());
            String url;
//            url = "http://iot.qq.com/add?pid=" + CommonApplication.pid + "&sn=" + CommonApplication.sn;
            url = XWDeviceBaseManager.getQRCodeUrl();

            result = multiFormatWriter.encode(url, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            qrcodeBitmap = barcodeEncoder.createBitmap(result);
            mIv.setImageBitmap(qrcodeBitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException iae) {
        }
    }


    @Override
    protected void onDestroy() {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        super.onDestroy();
    }

    public void startActivity(Intent intent, boolean needFinish) {
        super.startActivity(intent);
        if (needFinish) {
            finish();
        }
    }

    private void gotoMain() {
        Intent in = new Intent(this, MainActivity.class);
        startActivity(in, true);
    }

    public void reLogin(View view) {
        if (needRetry) {
            needRetry = false;
            mTv.setText("登录中...");
            XWDeviceBaseManager.deviceReconnect();
        }
    }

    public void onEnter(View view) {
        TextView tv = (TextView) findViewById(R.id.tv_tips);
        if (PidInfoConfig.pid == 0 || TextUtils.isEmpty(PidInfoConfig.sn)) {
            tv.setText("请先运行sn生成工具，再重新打开Demo");
        } else {
            tv.setVisibility(View.GONE);
            view.setVisibility(View.GONE);
        }
    }

}
