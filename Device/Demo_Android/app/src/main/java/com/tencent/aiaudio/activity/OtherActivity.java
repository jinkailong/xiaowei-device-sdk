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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tencent.aiaudio.activity.base.BaseActivity;
import com.tencent.aiaudio.demo.R;

import static com.tencent.aiaudio.service.ControlService.EXTRA_KEY_START_SKILL_ANSWER;
import static com.tencent.aiaudio.service.ControlService.EXTRA_KEY_START_SKILL_NAME;
import static com.tencent.aiaudio.service.ControlService.EXTRA_KEY_START_SKILL_SESSION_ID;

/**
 * 一些通用的场景，只有SkillName, Answer内容
 */
public class OtherActivity extends BaseActivity {

    private TextView mTvQuestion;
    private TextView mTvAnswer;
    private ImageView mIv;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encyclopedia);
        mIv = (ImageView) findViewById(R.id.txt_pedia_pic);
        mTvQuestion = (TextView) findViewById(R.id.txt_pedia_title);
        mTvAnswer = (TextView) findViewById(R.id.txt_pedia_desctribe);
        initData(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initData(intent);
    }

    private void initData(Intent intent) {
        sessionId = intent.getIntExtra(EXTRA_KEY_START_SKILL_SESSION_ID, 0);
        String skillName = intent.getStringExtra(EXTRA_KEY_START_SKILL_NAME);
        mTvQuestion.setText(skillName);
        String answer = intent.getStringExtra(EXTRA_KEY_START_SKILL_ANSWER);
        mTvAnswer.setText(answer);
        String icon = intent.getStringExtra("pic_url");
        if (TextUtils.isEmpty(icon)) {
            mIv.setVisibility(View.GONE);
        } else {
            if (!TextUtils.isEmpty(icon)) {
                Picasso.with(getApplicationContext()).load(icon).into(mIv);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public void onSkillIdle() {
        super.onSkillIdle();
        finish();
    }
}
