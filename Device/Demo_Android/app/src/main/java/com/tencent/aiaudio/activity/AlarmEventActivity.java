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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.aiaudio.activity.base.BaseActivity;
import com.tencent.aiaudio.adapter.AlarmEventAdapter;
import com.tencent.aiaudio.alarm.SkillAlarmBean;
import com.tencent.aiaudio.alarm.SkillAlarmManager;
import com.tencent.aiaudio.demo.R;
import com.tencent.aiaudio.view.SwipeRecyclerview;
import com.tencent.xiaowei.def.XWCommonDef;
import com.tencent.xiaowei.sdk.XWSDK;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * 闹钟列表
 */
public class AlarmEventActivity extends BaseActivity {
    static final String TAG = "AlarmEventActivity";

    SwipeRecyclerview mRecycler;
    List<SkillAlarmBean> mList = new ArrayList<>();
    AlarmEventAdapter mAdapter;
    private View mBack;

    private static final int REQUEST_CODE_EDIT_ALARM = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_event);
        initData();
        bindViews();
        bindListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            updateData();
        }
    }

    private void updateData() {
        mList = new ArrayList<>();
        initData();
        mAdapter.setItemList(mList);
    }

    private void initData() {
        ArrayList<SkillAlarmBean> list = SkillAlarmManager.instance().getAlarmList();
        if (list != null && list.size() > 0) {
            PriorityQueue<SkillAlarmBean> queue = new PriorityQueue(list.size(), new Comparator<SkillAlarmBean>() {
                @Override
                public int compare(SkillAlarmBean lhs, SkillAlarmBean rhs) {
                    return (int) (lhs.getAlarmTime() - rhs.getAlarmTime());
                }
            });
            queue.addAll(list);
            for (int i = 0; i < queue.size(); ) {
                mList.add(queue.poll());
            }
        }
    }

    private void bindViews() {
        mBack = findViewById(R.id.ll_back);
        TextView txtTitle = (TextView) findViewById(R.id.txt_title);
        txtTitle.setText("闹钟");

        mRecycler = (SwipeRecyclerview) findViewById(R.id.rcyl_alarm_event);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new AlarmEventAdapter(mList);
        mRecycler.setAdapter(mAdapter);
    }

    private void bindListener() {
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //设置打开/关闭事件提醒监听器
        mAdapter.setOnItemClickListener(new AlarmEventAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                SkillAlarmBean bean = mList.get(position);
                if (bean.getRepeatType() == 0 && System.currentTimeMillis() > bean.getAlarmTime()) {
                    bean.setAlarmTime(bean.getAlarmTime() + 24 * 60 * 60 * 1000l);
                }
                SkillAlarmManager.instance().updateAlarm(bean);
                mAdapter.notifyDataSetChanged();
            }
        });

        mRecycler.setOnListItemClickListener(new SwipeRecyclerview.OnListItemClickListener() {
            @Override
            public void onListItemClick(int position) {
                Intent intent = new Intent(AlarmEventActivity.this, EditAlarmActivity.class);
                intent.putExtra("clockInfo", mList.get(position));
                startActivityForResult(intent, REQUEST_CODE_EDIT_ALARM);
            }
        });

        //设置事件删除监听器
        mRecycler.setOnItemDeleteListener(new SwipeRecyclerview.OnItemDeleteListener() {
            @Override
            public void onDeleteClick(final int position) {

                String strClockJson = mList.get(position).toJsonString();
                Log.d(TAG, "delete alarm strClockJson: " + strClockJson);
                XWSDK.getInstance().setDeviceAlarmInfo(XWCommonDef.AlarmOptType.ALARM_OPT_TYPE_DELETE, strClockJson, new XWSDK.SetAlarmRspListener() {
                    @Override
                    public void onSetAlarmList(int errCode, String strVoiceID, int alarmId) {
                        if (errCode == XWCommonDef.ErrorCode.ERROR_NULL_SUCC) {

                            SkillAlarmBean bean = mAdapter.getItem(position);

                            mAdapter.removeItem(position);
                            SkillAlarmManager.instance().deleteAlarm(bean);
                        } else {
                            Toast.makeText(getApplicationContext(), "删除失败，返回码为" + errCode, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        findViewById(R.id.img_music_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlarmEventActivity.this, HelperActivity.class);
                ArrayList<String> helps = new ArrayList<>();
                intent.putExtra("title", "闹钟");
                helps.add("1小时后提醒我休息");
                helps.add("每天早上八点提醒我喝水");
                helps.add("下午三点提醒我开会");
                helps.add("每周六早上八点提醒我起床");
                intent.putExtra("helps", helps);
                startActivity(intent);
            }
        });

        ImageView addView = (ImageView) findViewById(R.id.img_alarm_add);
        addView.setVisibility(View.VISIBLE);
        addView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(AlarmEventActivity.this, EditAlarmActivity.class), REQUEST_CODE_EDIT_ALARM);
            }
        });
    }
}
