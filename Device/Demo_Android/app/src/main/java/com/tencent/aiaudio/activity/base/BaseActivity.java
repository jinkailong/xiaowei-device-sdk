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
package com.tencent.aiaudio.activity.base;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.tencent.aiaudio.activity.ActivityManager;
import com.tencent.aiaudio.demo.R;
import com.tencent.aiaudio.utils.UIUtils;

import static com.tencent.aiaudio.service.ControlService.EXTRA_KEY_START_SKILL_ID;
import static com.tencent.aiaudio.service.ControlService.EXTRA_KEY_START_SKILL_NAME;
import static com.tencent.aiaudio.service.ControlService.EXTRA_KEY_START_SKILL_SESSION_ID;

public class BaseActivity extends FragmentActivity {
    protected Bundle animBundle;
    protected boolean mShowStatus = false;

    protected Handler mHandler = new Handler();

    protected String skillName;
    protected String skillId;
    protected SharedPreferences sp;
    protected SharedPreferences.Editor editor;

    protected int sessionId;
    public static BaseActivity activity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        animBundle = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(),
                R.anim.anim_activity_enter, R.anim.anim_activity_exit).toBundle();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Translucent status bar
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            if (mShowStatus) {
                ViewGroup decor = (ViewGroup) getWindow().getDecorView();
                View statusView = new View(this);
                statusView.setBackgroundColor(getResources().getColor(R.color.statu_bar_bg));
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.height = UIUtils.getStatusBarHeight(this);
                statusView.setLayoutParams(lp);
                decor.addView(statusView);
            }
        }
        sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        editor = sp.edit();

        sessionId = getIntent().getIntExtra(EXTRA_KEY_START_SKILL_SESSION_ID, -1);

        String skillName = getIntent().getStringExtra(EXTRA_KEY_START_SKILL_NAME);
        String skillId = getIntent().getStringExtra(EXTRA_KEY_START_SKILL_ID);
        if (!TextUtils.isEmpty(skillName)) {
            this.skillName = skillName;
        }
        if (!TextUtils.isEmpty(skillId)) {
            this.skillId = skillId;
        }

        ActivityManager.getInstance().put(sessionId, this);
    }

    public void onSkillIdle() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        ActivityManager.getInstance().remove(sessionId);
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_activity_finish_enter, R.anim.anim_activity_finish_exit);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.anim_activity_enter, R.anim.anim_activity_exit);
    }

    public void startActivitySuper(Intent intent) {
        super.startActivity(intent);
    }

    public void onBack(View view) {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activity = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        activity = null;
    }
}
