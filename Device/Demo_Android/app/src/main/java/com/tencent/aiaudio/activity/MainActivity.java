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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.tencent.aiaudio.CommonApplication;
import com.tencent.aiaudio.activity.base.BaseActivity;
import com.tencent.aiaudio.adapter.CommonListAdapter;
import com.tencent.aiaudio.bledemo.BLEActivity;
import com.tencent.aiaudio.demo.BuildConfig;
import com.tencent.aiaudio.demo.IAIAudioService;
import com.tencent.aiaudio.demo.R;
import com.tencent.aiaudio.service.AIAudioService;
import com.tencent.aiaudio.service.ControlService;
import com.tencent.aiaudio.service.WakeupAnimatorService;
import com.tencent.aiaudio.utils.DemoOnAudioFocusChangeListener;
import com.tencent.aiaudio.utils.XiaomiUtil;
import com.tencent.xiaowei.control.XWeiAudioFocusManager;
import com.tencent.xiaowei.sdk.XWDeviceBaseManager;
import com.tencent.xiaowei.util.QLog;

import static com.tencent.aiaudio.CommonApplication.ACTION_ON_BINDER_LIST_CHANGE;


public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private boolean useWakeupDemo;

    private BroadcastHandler mBroadcastHandler;
    private ImageView ivErcode;
    private View vGray;

    private GridView mGridView;
    private CommonListAdapter<String> mAdapter;
    private static TextView mTvUITips;
    private static ScrollView mTvUITipsScrollView;

    private IAIAudioService mAIAudioService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAIAudioService = (IAIAudioService) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAIAudioService = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        Intent intent = new Intent(this, AIAudioService.class);
        startService(intent);
        if (!BuildConfig.IS_NEED_VOICE_LINK) {
            startService(intent);
            bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        }

        intent = new Intent(this, ControlService.class);
        startService(intent);

        mBroadcastHandler = new BroadcastHandler();
        IntentFilter filter = new IntentFilter();
        filter.addAction("ONLINE");
        filter.addAction("OFFLINE");
        filter.addAction(ACTION_ON_BINDER_LIST_CHANGE);
        registerReceiver(mBroadcastHandler, filter);

        if (Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")) {
            if (!XiaomiUtil.checkFloatWindowPermission(this)) {
                XiaomiUtil.applyMiuiPermission(this);
                CommonApplication.showToast("请先去设置开启悬浮窗权限");
            }
        }
    }

    private class BroadcastHandler extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "ONLINE":
                    vGray.setVisibility(View.GONE);
                    break;
                case "OFFLINE":
                    vGray.setVisibility(View.VISIBLE);
                    break;
                case ACTION_ON_BINDER_LIST_CHANGE:
                    if (XWDeviceBaseManager.getBinderList().size() == 0) {
                        showErcode();
                    } else {
                        dissmissErcode();
                    }
                    break;

            }
        }
    }

    private void initView() {
        vGray = findViewById(R.id.v_gray);
        vGray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XWDeviceBaseManager.deviceReconnect();
            }
        });

        mGridView = (GridView) findViewById(R.id.gv);
        mAdapter = new CommonListAdapter<String>() {
            @Override
            protected View initListCell(int position, View convertView, ViewGroup parent) {
                convertView = getLayoutInflater().inflate(R.layout.item_help, parent, false);
                ((TextView) convertView.findViewById(R.id.tv)).setText(mAdapter.getItem(position));
                return convertView;
            }
        };

        mTvUITips = (TextView) findViewById(R.id.tv_wakeup_tips);
        mTvUITipsScrollView = (ScrollView) findViewById(R.id.tv_wakeup_tips_scroll);
        initAdapterData();
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                switch (position) {
                    case 0:
                        if (mAIAudioService != null) {
                            try {

                                mAIAudioService.setUseWakeup(!useWakeupDemo);
                                useWakeupDemo = !useWakeupDemo;
                                mAdapter.remove(0);
                                mAdapter.addFirst(useWakeupDemo ? "语音唤醒已打开" : "语音唤醒已关闭");
                                mAdapter.notifyDataSetChanged();
                                editor.putBoolean("use", useWakeupDemo);
                                editor.commit();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case 1:
                        XWDeviceBaseManager.unBind(null);
                        break;
                    case 2:
                        requestText("播放收藏的音乐");
                        break;
                    case 3:
                        startActivity(new Intent(MainActivity.this, MusicActivity.class));
                        break;
                    case 4:  // 进入配网
                        startActivity(new Intent(MainActivity.this, WifiDecodeActivity.class));
                        break;
                    case 5:
                        requestText("今天天气怎么样");
                        break;
                    case 6:
                        requestText("进入正式环境");
                        break;
                    case 7: //蓝牙
                        startActivity(new Intent(MainActivity.this, BLEActivity.class));
                        break;
                    case 8: //绑定者
                        startActivity(new Intent(MainActivity.this, BinderActivity.class));
                        break;
                    case 9:
                        startActivity(new Intent(MainActivity.this, FMActivity.class));
                        break;
                    case 10:
                        startActivity(new Intent(MainActivity.this, NewsActivity.class));
                        break;
                    case 11:
                        requestText("五秒后提醒我喝水");
                        break;
                    case 12:
                        AIAudioService.localVad = !AIAudioService.localVad;
                        CommonApplication.showToast("使用" + (AIAudioService.localVad ? "本地" : "后台") + "vad");
                        break;
                }
            }
        });


        final EditText et = (EditText) findViewById(R.id.et_main);
        et.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_COMMA) {
                    String text = et.getText().toString();
                    if (!TextUtils.isEmpty(text)) {
                        requestText(text);
                        et.setText("");
                    }
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.btn_request).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = et.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    requestText(text);
                    et.setText("");
                }
            }
        });

        ((TextView) findViewById(R.id.tv_din)).setText("din:" + XWDeviceBaseManager.getSelfDin());


        ivErcode = (ImageView) findViewById(R.id.iv_ercode);
        if (XWDeviceBaseManager.getBinderList().size() == 0) {
            showErcode();
        }

    }

    private void requestText(String text) {
        if (mAIAudioService != null) {
            try {
                mAIAudioService.startRequest(text);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void initAdapterData() {
        sp = getSharedPreferences("wakeup", Context.MODE_PRIVATE);
        editor = sp.edit();
        useWakeupDemo = sp.getBoolean("use", true);
        mAdapter.add(useWakeupDemo ? "语音唤醒已打开" : "语音唤醒已关闭");
        mAdapter.add("解绑");
        mAdapter.add("播放收藏的音乐");
        mAdapter.add("音乐");
        mAdapter.add("进入配网模式");
        mAdapter.add("天气");
        mAdapter.add("进入语音正式环境");
        mAdapter.add("蓝牙"); //7
        mAdapter.add("绑定者");
        mAdapter.add("FM");
        mAdapter.add("新闻");
        mAdapter.add("5s后喝水");
        mAdapter.add("vad切换");

        mAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (WakeupAnimatorService.getInstance() != null)
            WakeupAnimatorService.getInstance().showFloat(WakeupAnimatorService.FLAG_MAIN_ACTIVITY);
        if (XWeiAudioFocusManager.getInstance().needRequestFocus(AudioManager.AUDIOFOCUS_GAIN)) {
            AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int ret = mAudioManager.requestAudioFocus(DemoOnAudioFocusChangeListener.getInstance(), AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (ret == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                XWeiAudioFocusManager.getInstance().setAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (WakeupAnimatorService.getInstance() != null)
            WakeupAnimatorService.getInstance().dismissFloat(WakeupAnimatorService.FLAG_MAIN_ACTIVITY);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        if (mBroadcastHandler != null) {
            unregisterReceiver(mBroadcastHandler);
        }
        unbindService(mServiceConnection);
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    // 显示QQ扫码绑定的二维码
    private void showErcode() {
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
            ivErcode.setImageBitmap(qrcodeBitmap);
            ivErcode.setVisibility(View.VISIBLE);
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException iae) {
        }
    }

    private void dissmissErcode() {
        ivErcode.setVisibility(View.GONE);
    }

    public static void setUITips(final String text) {
        if (mTvUITips != null) {
            mTvUITips.post(new Runnable() {
                @Override
                public void run() {
                    mTvUITips.append(text);
                    mTvUITips.append("\r\n");
                    if (mTvUITipsScrollView != null)
                        mTvUITipsScrollView.post(new Runnable() {
                            @Override
                            public void run() {
                                mTvUITipsScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
                }
            });
        }
    }

    public void onWakeup(View v) {
        WakeupAnimatorService service = WakeupAnimatorService.getInstance();
        if (service != null) {
            service.onWakeup();
        }
    }
}
