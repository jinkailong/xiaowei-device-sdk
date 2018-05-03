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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tencent.aiaudio.activity.base.BaseActivity;
import com.tencent.aiaudio.alarm.SkillAlarmBean;
import com.tencent.aiaudio.alarm.SkillAlarmManager;
import com.tencent.aiaudio.demo.R;
import com.tencent.aiaudio.view.XiaoweiCircleView;
import com.tencent.utils.MusicPlayer;
import com.tencent.xiaowei.control.Constants;
import com.tencent.xiaowei.control.XWeiAudioFocusManager;
import com.tencent.xiaowei.def.XWCommonDef;
import com.tencent.xiaowei.info.XWAppInfo;
import com.tencent.xiaowei.info.XWContextInfo;
import com.tencent.xiaowei.info.XWPlayStateInfo;
import com.tencent.xiaowei.info.XWResponseInfo;
import com.tencent.xiaowei.sdk.XWSDK;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.tencent.aiaudio.alarm.AlarmSkillHandler.EXTRA_KEY_CLOSE_ALARM;

/**
 * 闹钟触发界面，闹钟场景可恢复
 */
public class AlarmActivity extends BaseActivity {

    private static final String TAG = "AlarmActivity";
    private SkillAlarmBean mCurSkillAlarmBean;
    private TextView tvName;
    private XiaoweiCircleView[] xcvs = new XiaoweiCircleView[3];
    private TextView tvTime;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            tvTime.setText(df.format(new Date(System.currentTimeMillis())));
            mHandler.postDelayed(this, 200);
        }
    };
    private volatile boolean isFinishing = false;

    private Runnable mTimeToStopTask = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case EXTRA_KEY_CLOSE_ALARM:
                    isFinishing = true;
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_alarm);
        initView();
        startAlarm(getIntent());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(EXTRA_KEY_CLOSE_ALARM);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
    }

    private void initView() {
        xcvs[0] = (XiaoweiCircleView) findViewById(R.id.xcv1);
        xcvs[1] = (XiaoweiCircleView) findViewById(R.id.xcv2);
        xcvs[2] = (XiaoweiCircleView) findViewById(R.id.xcv3);
        tvName = (TextView) findViewById(R.id.txt_alarm_event_name);
        tvTime = (TextView) findViewById(R.id.txt_alarm_event_time);
    }

    private boolean isFirst = true;
    private XWeiAudioFocusManager.OnAudioFocusChangeListener listener = new XWeiAudioFocusManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.e(TAG, "onFocusChange " + focusChange);
            if (focusChange == XWeiAudioFocusManager.AUDIOFOCUS_GAIN || focusChange == XWeiAudioFocusManager.AUDIOFOCUS_GAIN_TRANSIENT) {
                if (isFinishing) {
                    return;
                }

                XWPlayStateInfo stateInfo = new XWPlayStateInfo();
                stateInfo.appInfo = new XWAppInfo();
                stateInfo.appInfo.ID = Constants.SkillIdDef.SKILL_ID_TRIGGER_ALARM;
                stateInfo.appInfo.name = Constants.SKILL_NAME.SKILL_NAME_TRIGGER_ALARM;
                stateInfo.playContent = mCurSkillAlarmBean.getEvent();
                stateInfo.state = XWCommonDef.PlayState.RESUME;
                XWSDK.getInstance().reportPlayState(stateInfo);

                MusicPlayer.getInstance2().playMediaInfo("http://qzonestyle.gtimg.cn/qzone/vas/opensns/res/doc/alarm_sound.mp3", new MusicPlayer.OnPlayListener() {
                    @Override
                    public void onCompletion(int what) {
                    }

                }, true);
                MusicPlayer.getInstance().setVolume(100);
                MusicPlayer.getInstance2().setVolume(100);

                if (isFirst) {
                    isFirst = false;
                    if (!TextUtils.isEmpty(mCurSkillAlarmBean.getEvent())) {
                        XWSDK.getInstance().requestTTS(("提醒 " + mCurSkillAlarmBean.getEvent()).getBytes(), new XWContextInfo(), new XWSDK.RequestListener() {
                            @Override
                            public boolean onRequest(int event, XWResponseInfo rspData, byte[] extendData) {
                                if (rspData.resources == null || rspData.resources.length == 0) {
                                    return true;
                                }
                                MusicPlayer.getInstance().playMediaInfo(rspData.resources[0].resources[0], new MusicPlayer.OnPlayListener() {
                                    @Override
                                    public void onCompletion(int what) {
                                        MusicPlayer.getInstance2().setVolume(100);
                                    }

                                });
                                MusicPlayer.getInstance2().setVolume(20);
                                return true;
                            }
                        });
                    }
                }

            } else if (focusChange == XWeiAudioFocusManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                MusicPlayer.getInstance().setVolume(20);
                MusicPlayer.getInstance2().setVolume(20);
            } else if (focusChange == XWeiAudioFocusManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                XWPlayStateInfo stateInfo = new XWPlayStateInfo();
                stateInfo.appInfo = new XWAppInfo();
                stateInfo.appInfo.ID = Constants.SkillIdDef.SKILL_ID_TRIGGER_ALARM;
                stateInfo.appInfo.name = Constants.SKILL_NAME.SKILL_NAME_TRIGGER_ALARM;
                stateInfo.playContent = mCurSkillAlarmBean.getEvent();
                stateInfo.state = XWCommonDef.PlayState.PAUSE;
                XWSDK.getInstance().reportPlayState(stateInfo);

                MusicPlayer.getInstance().stop();
                MusicPlayer.getInstance2().stop();
            } else if (focusChange == XWeiAudioFocusManager.AUDIOFOCUS_LOSS) {
                finish();
            }
        }
    };

    private void startPlayRing() {
        MusicPlayer.getInstance().stop();
        MusicPlayer.getInstance2().stop();
        mHandler.removeCallbacksAndMessages(null);

        mHandler.post(mRunnable);
        mHandler.postDelayed(mTimeToStopTask, 2 * 60 * 1000);
        XWeiAudioFocusManager.getInstance().requestAudioFocus(listener, XWeiAudioFocusManager.AUDIOFOCUS_GAIN);

        XWPlayStateInfo stateInfo = new XWPlayStateInfo();
        stateInfo.appInfo = new XWAppInfo();
        stateInfo.appInfo.ID = Constants.SkillIdDef.SKILL_ID_TRIGGER_ALARM;
        stateInfo.appInfo.name = Constants.SKILL_NAME.SKILL_NAME_TRIGGER_ALARM;
        stateInfo.playContent = mCurSkillAlarmBean.getEvent();
        stateInfo.state = XWCommonDef.PlayState.START;
        XWSDK.getInstance().reportPlayState(stateInfo);
    }

    private void stopPlayRing() {
        MusicPlayer.getInstance().stop();
        MusicPlayer.getInstance2().stop();
        mHandler.removeCallbacksAndMessages(null);

        XWPlayStateInfo stateInfo = new XWPlayStateInfo();
        stateInfo.appInfo = new XWAppInfo();
        stateInfo.appInfo.ID = Constants.SkillIdDef.SKILL_ID_TRIGGER_ALARM;
        stateInfo.appInfo.name = Constants.SKILL_NAME.SKILL_NAME_TRIGGER_ALARM;
        stateInfo.state = XWCommonDef.PlayState.FINISH;
        XWSDK.getInstance().reportPlayState(stateInfo);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        isFirst = true;
        startAlarm(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        XWeiAudioFocusManager.getInstance().abandonAudioFocus(listener);
        stopPlayRing();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    /**
     * 首次进入Activity，初始化数据
     *
     * @param intent
     */
    private void startAlarm(Intent intent) {
        List<SkillAlarmBean> beans = intent.getParcelableArrayListExtra("alarms");
        if (beans == null || beans.size() <= 0) {
            finish();
            return;
        }

        mCurSkillAlarmBean = beans.get(0);

        if (TextUtils.isEmpty(mCurSkillAlarmBean.getEvent())) {
            tvName.setText("提醒");
        } else {
            tvName.setText(mCurSkillAlarmBean.getEvent());
        }

        startPlayRing();
    }

    /**
     * 延迟闹钟
     *
     * @param view
     */
    public void delay(View view) {
        SkillAlarmBean bean = new SkillAlarmBean();
        bean.setAlarmTime(System.currentTimeMillis() + 5 * 60 * 1000);// 5min后再次提醒
        bean.setEvent(mCurSkillAlarmBean.getEvent());
        bean.setKey("-100");
        SkillAlarmManager.instance().startAlarm(bean);

        finish();
    }

    /**
     * 停止闹钟
     *
     * @param view
     */
    public void close(View view) {
        finish();
    }
}
