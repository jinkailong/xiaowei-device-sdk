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
package com.tencent.aiaudio.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tencent.aiaudio.CommonApplication;
import com.tencent.aiaudio.activity.base.BaseActivity;
import com.tencent.aiaudio.demo.IAIAudioService;
import com.tencent.aiaudio.demo.R;
import com.tencent.aiaudio.utils.UIUtils;
import com.tencent.aiaudio.view.WakeupAnimatorView;
import com.tencent.xiaowei.util.QLog;

import static com.tencent.aiaudio.NotifyConstantDef.ActionDef.ACTION_DEF_ANIM_NOISE_CHANGED;
import static com.tencent.aiaudio.NotifyConstantDef.ActionDef.ACTION_DEF_ANIM_START;
import static com.tencent.aiaudio.NotifyConstantDef.ActionDef.ACTION_DEF_ANIM_STOP;
import static com.tencent.aiaudio.NotifyConstantDef.ActionDef.ACTION_DEF_RECOGNIZE_TEXT;
import static com.tencent.aiaudio.NotifyConstantDef.ExtraKeyDef.EXTRA_KEY_DEF_MSG_NOISE_CHANGED;
import static com.tencent.aiaudio.NotifyConstantDef.ExtraKeyDef.EXTRA_KEY_DEF_RECOGNIZE_TEXT;

public class WakeupAnimatorService extends Service {

    private static final String TAG = "WakeupAnimatorService";

    public static int FLAG_NONE = 0;
    public static int FLAG_MAIN_ACTIVITY = 1;
    public static int FLAG_FLOAT_SERVICE = 2;

    private static int showFlag = FLAG_NONE;

    private static WakeupAnimatorService service;
    private static boolean isShowing;
    private WakeupAnimatorView mWakeupAnimatorView;
    private FrameLayout mFloatView;
    private TextView mTvText;
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
    private BroadcastHandler mBroadcastHandler;
    private View mTestBtns;// 测试使用的button
    private TextView tvNet;

    public static WakeupAnimatorService getInstance() {
        return service;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        service = this;
        Intent intent = new Intent(this, AIAudioService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

        mBroadcastHandler = new BroadcastHandler();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DEF_ANIM_NOISE_CHANGED);
        filter.addAction(ACTION_DEF_ANIM_START);
        filter.addAction(ACTION_DEF_ANIM_STOP);
        filter.addAction(ACTION_DEF_RECOGNIZE_TEXT);
        registerReceiver(mBroadcastHandler, filter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createFloatView();
        createBackFloatView();
        createNetStateFloatView();
        return START_STICKY;
    }

    private class BroadcastHandler extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }

            switch (intent.getAction()) {
                case ACTION_DEF_ANIM_START:
                    onVisible();
                    break;
                case ACTION_DEF_ANIM_STOP:
                    onGone();
                    break;
                case ACTION_DEF_ANIM_NOISE_CHANGED:
                    float noise = intent.getExtras().getFloat(EXTRA_KEY_DEF_MSG_NOISE_CHANGED, -1);
                    if (noise == -1) {
                        // 思考
                        mWakeupAnimatorView.setState(WakeupAnimatorView.State.THINK, null);
                    } else {
                        mWakeupAnimatorView.setNoise(noise);
                    }
                    break;
                case ACTION_DEF_RECOGNIZE_TEXT:
                    String text = intent.getStringExtra(EXTRA_KEY_DEF_RECOGNIZE_TEXT);

                    mTvText.setText(text);

                    if (text.length() > 0)
                        mTvText.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    private void createFloatView() {
        mFloatView = new FrameLayout(this);
        View view = LayoutInflater.from(this).inflate(R.layout.float_recording, mFloatView, false);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mFloatView.addView(view, lp);
        mWakeupAnimatorView = (WakeupAnimatorView) mFloatView.findViewById(R.id.waveform_view);
        mWakeupAnimatorView.setZOrderOnTop(true);
        mWakeupAnimatorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onWakeup();
            }
        });
        mWakeupAnimatorView.setNoise(0f);
        mTvText = (TextView) mFloatView.findViewById(R.id.tv_text);
        mTestBtns = mFloatView.findViewById(R.id.btns);
        mFloatView.findViewById(R.id.btn_silence).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAIAudioService != null) {
                    try {
                        mAIAudioService.keepSilence();// 给sdk静音的数据，周围声音噪杂的时候使用
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mFloatView.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAIAudioService != null) {
                    try {
                        mAIAudioService.cancelEmbed();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                onGone();
            }
        });


    }

    public void onWakeup() {
        if (mWakeupAnimatorView.getState() == WakeupAnimatorView.State.WAITE) {
            if (mAIAudioService != null) {
                try {
                    mAIAudioService.wakeup();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void createBackFloatView() {
        FrameLayout mFloatView = new FrameLayout(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        TextView tv = new TextView(this);
        tv.setText("返回");
        mFloatView.addView(tv, lp);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BaseActivity.activity != null) {
                    BaseActivity.activity.finish();
                }
            }
        });
        UIUtils.createFloatView(mFloatView, 0, 0, UIUtils.dip2px(CommonApplication.mApplication, 100), UIUtils.dip2px(CommonApplication.mApplication, 40), 0, false);

    }

    private void createNetStateFloatView() {
        FrameLayout mFloatView = new FrameLayout(this);
        tvNet = new TextView(this);
        tvNet.setBackgroundColor(getResources().getColor(R.color.white40));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mFloatView.addView(tvNet, lp);
        UIUtils.createFloatView(mFloatView, (int) (UIUtils.getScreenWidth(CommonApplication.mApplication) - UIUtils.dip2px(CommonApplication.mApplication, 100)), 0, UIUtils.dip2px(CommonApplication.mApplication, 50), UIUtils.dip2px(CommonApplication.mApplication, 20), 0, false);

    }

    private void onVisible() {
        showFloat(FLAG_FLOAT_SERVICE);
        mWakeupAnimatorView.setState(WakeupAnimatorView.State.IN, new WakeupAnimatorView.OnCompletionListener() {
            @Override
            public void onCompletion(boolean cancel, WakeupAnimatorView.State oldState, WakeupAnimatorView.State newState) {
                mWakeupAnimatorView.setState(WakeupAnimatorView.State.LISTEN, null);
            }
        });
        mTestBtns.setVisibility(View.VISIBLE);
    }

    private void onGone() {
        // 出场
        mTvText.setVisibility(View.GONE);
        mWakeupAnimatorView.setState(WakeupAnimatorView.State.OUT, new WakeupAnimatorView.OnCompletionListener() {
            @Override
            public void onCompletion(boolean cancel, WakeupAnimatorView.State oldState, WakeupAnimatorView.State newState) {
                dismissFloat(FLAG_FLOAT_SERVICE);
                mWakeupAnimatorView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mWakeupAnimatorView.setState(WakeupAnimatorView.State.WAITE, null);
                        mTvText.setText("");
                        mTvText.setVisibility(View.GONE);
                    }
                }, 200);// 延迟一会儿恢复成圆
            }
        });
        mTestBtns.setVisibility(View.GONE);
    }


    @Override
    public void onDestroy() {
        mWakeupAnimatorView = null;
        mFloatView = null;
        super.onDestroy();
        if (mFloatView != null) {
            dismissFloat(0);
        }
        if (mBroadcastHandler != null) {
            unregisterReceiver(mBroadcastHandler);
        }
        unbindService(mServiceConnection);
        service = null;
    }

    public void setNetText(final String text) {
        if (tvNet != null) {
            tvNet.post(new Runnable() {
                @Override
                public void run() {
                    tvNet.setText(text);
                }
            });
        }
    }

    public void showFloat(int flag) {
        QLog.d(TAG, "showFloat start " + flag);
        showFlag |= flag;
        if (showFlag > 0) {
            if (!isShowing) {
                // 每次显示悬浮窗的时候，都重新创建
                createFloatView();
                UIUtils.createFloatView(mFloatView, (int) (UIUtils.getScreenWidth(CommonApplication.mApplication) / 2 - UIUtils.dip2px(CommonApplication.mApplication, 250) / 2), (int) (UIUtils.getScreenHeight(CommonApplication.mApplication) - UIUtils.dip2px(CommonApplication.mApplication, 160)), UIUtils.dip2px(CommonApplication.mApplication, 250), UIUtils.dip2px(CommonApplication.mApplication, 160), 0, false);
                isShowing = true;
                if (mWakeupAnimatorView != null) {
                    mWakeupAnimatorView.startAnimator();
                    mWakeupAnimatorView.setState(WakeupAnimatorView.State.WAITE, null);
                }
            }
        }
        QLog.d(TAG, "showFloat " + Integer.toBinaryString(showFlag));
    }

    public void dismissFloat(int flag) {
        QLog.d(TAG, "dismissFloat start " + flag);
        showFlag &= ~flag;
        if (showFlag == 0) {
            if (isShowing) {
                if (mWakeupAnimatorView != null) {
                    mWakeupAnimatorView.stopAnimator();
                }
                UIUtils.removeFloatView(mFloatView);
                mFloatView = null;
                isShowing = false;
            }
        }
        QLog.d(TAG, "dismissFloat " + Integer.toBinaryString(showFlag));
    }
}
