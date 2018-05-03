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
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.tencent.aiaudio.activity.base.BaseActivity;
import com.tencent.aiaudio.alarm.SkillAlarmBean;
import com.tencent.aiaudio.alarm.SkillAlarmManager;
import com.tencent.aiaudio.demo.R;
import com.tencent.aiaudio.view.SettingItemView;
import com.tencent.xiaowei.def.XWCommonDef;
import com.tencent.xiaowei.sdk.XWSDK;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditAlarmActivity extends BaseActivity {
    private static final String TAG = EditAlarmActivity.class.getSimpleName();

    private SkillAlarmBean bean = null;

    boolean isAddNew = false;
    boolean isNeedSave = false;

    private SettingItemView timeView;
    private SettingItemView dateView;
    private SettingItemView eventView;
    private Spinner repeatView;
    private View repeatExpandView;
    private EditText edtRepeatInternal;

    private Calendar mCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_edit_alarm);
        Intent intent = getIntent();

        bean = intent.getParcelableExtra("clockInfo");
        if (bean == null) {
            bean = new SkillAlarmBean();
            isAddNew = true;
        }

        mCalendar = Calendar.getInstance();
        bindViews(isAddNew);
        bindListener();
    }

    /**
     * 设置UI
     *
     * @param isAddNew 是否为新增闹钟
     */
    private void bindViews(boolean isAddNew) {

        if (isAddNew) {
            ((TextView) findViewById(R.id.txt_title)).setText("新增闹钟");
        } else {
            ((TextView) findViewById(R.id.txt_title)).setText("闹钟详情");
        }

        findViewById(R.id.img_music_help).setVisibility(View.GONE);
        findViewById(R.id.txt_save).setVisibility(View.VISIBLE);

        timeView = (SettingItemView) findViewById(R.id.setting_item_time);
        dateView = (SettingItemView) findViewById(R.id.setting_item_date);
        eventView = (SettingItemView) findViewById(R.id.setting_item_event);
        repeatView = (Spinner) findViewById(R.id.setting_item_repeat_type);
        edtRepeatInternal = (EditText) findViewById(R.id.repeat_internal);

        repeatExpandView = findViewById(R.id.repeat_type_expand_content);

        String[] repeatTypeArray = getResources().getStringArray(R.array.repeat_type);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(EditAlarmActivity.this, R.layout.repeat_type_item, repeatTypeArray);
        repeatView.setAdapter(arrayAdapter);
        repeatView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // 不循环
                    bean.setType(SkillAlarmBean.TYPE_ALARM_PROMPT);
                } else if (position == 1) { // 按天循环
                    bean.setType(SkillAlarmBean.TYPE_ALARM_LOOP);
                    bean.setRepeatType(SkillAlarmBean.CLOCK_REPEAT_TYPE.CLOCK_REPEAT_TYPE_DAY);
                } else if (position == 2) { // 按周循环
                    bean.setType(SkillAlarmBean.TYPE_ALARM_LOOP);
                    bean.setRepeatType(SkillAlarmBean.CLOCK_REPEAT_TYPE.CLOCK_REPEAT_TYPE_WEEK);
                }
                updateView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        updateView();
    }

    private void updateView() {
        if (!isNeedSave) {
            isNeedSave = true;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale.getDefault());

        String currTime = dateFormat.format(new Date(bean.getAlarmTime() > 0 ? bean.getAlarmTime() : System.currentTimeMillis()));
        String[] data = currTime.split(" ");
        timeView.setDescription(data[data.length - 1]);
        dateView.setDescription(data[0]);
        eventView.setDescription(TextUtils.isEmpty(bean.getEvent()) ? "可设置提醒事件" : bean.getEvent());
        if (bean.getType() == SkillAlarmBean.TYPE_ALARM_LOOP) {
            if (bean.getRepeatType() == SkillAlarmBean.CLOCK_REPEAT_TYPE.CLOCK_REPEAT_TYPE_DAY) {
                repeatView.setSelection(1);
            } else if (bean.getRepeatType() == SkillAlarmBean.CLOCK_REPEAT_TYPE.CLOCK_REPEAT_TYPE_WEEK) {
                repeatView.setSelection(2);
            }
            repeatExpandView.setVisibility(View.VISIBLE);
        } else {
            repeatView.setSelection(0);
            repeatExpandView.setVisibility(View.GONE);
        }
    }

    /**
     * 绑定时间监听器
     */
    private void bindListener() {

        findViewById(R.id.ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });


        findViewById(R.id.txt_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isNeedSave) {
                    Toast.makeText(getApplicationContext(), "没有任何改动", Toast.LENGTH_LONG).show();
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                    return;
                }

                if (isAddNew) {
                    if (bean.getType() != SkillAlarmBean.TYPE_ALARM_LOOP
                            && bean.getAlarmTime() > 0 && bean.getAlarmTime() < System.currentTimeMillis()) {
                        Toast.makeText(getApplicationContext(), "请检查提醒/闹钟的触发时间是否已经过期", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (bean.getAlarmTime() == 0) {
                        bean.setAlarmTime(System.currentTimeMillis());
                    }

                    if (bean.getType() == SkillAlarmBean.TYPE_ALARM_LOOP) {
                        String repeatInternal = edtRepeatInternal.getText().toString();
                        bean.setRepeatInterval(TextUtils.isEmpty(repeatInternal) ? "1" : repeatInternal);
                    }

                    XWSDK.getInstance().setDeviceAlarmInfo(XWCommonDef.AlarmOptType.ALARM_OPT_TYPE_ADD, bean.toJsonString(), new XWSDK.SetAlarmRspListener() {
                        @Override
                        public void onSetAlarmList(int errCode, String strVoiceID, int alarmId) {
                            Log.d(TAG, "add alarm onSetAlarmList errCode: " + errCode + " clockId: " + alarmId);
                            if (errCode == XWCommonDef.ErrorCode.ERROR_NULL_SUCC) {
                                bean.setKey(String.valueOf(alarmId));
                                SkillAlarmManager.instance().startAlarm(bean);
                                setResult(Activity.RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "创建闹钟失败，返回码：" + errCode, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    XWSDK.getInstance().setDeviceAlarmInfo(XWCommonDef.AlarmOptType.ALARM_OPT_TYPE_UPDATE, bean.toJsonString(), new XWSDK.SetAlarmRspListener() {
                        @Override
                        public void onSetAlarmList(int errCode, String strVoiceID, int alarmId) {
                            if (errCode == XWCommonDef.ErrorCode.ERROR_NULL_SUCC) {
                                SkillAlarmManager.instance().updateAlarm(bean);
                                setResult(Activity.RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "更新闹钟失败，返回码：" + errCode, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        timeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(EditAlarmActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        mCalendar.set(Calendar.MINUTE, minute);
                        bean.setAlarmTime(mCalendar.getTimeInMillis());
                        updateView();
                    }
                }, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), true);
                dialog.show();
            }
        });

        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(EditAlarmActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        mCalendar.set(Calendar.YEAR, year);
                        mCalendar.set(Calendar.MONTH, monthOfYear);
                        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        bean.setAlarmTime(mCalendar.getTimeInMillis());
                        updateView();
                    }

                }, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        eventView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(EditAlarmActivity.this);
                alertDialog.setTitle("请输入提醒事件");

                final EditText input = new EditText(EditAlarmActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("确认",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String event = input.getText().toString();
                                if (!TextUtils.isEmpty(event)) {
                                    bean.setEvent(event);
                                    updateView();
                                }
                            }
                        });

                alertDialog.setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                alertDialog.show();
            }
        });
    }
}
