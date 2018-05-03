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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tencent.aiaudio.CommonApplication;
import com.tencent.aiaudio.activity.base.BaseActivity;
import com.tencent.aiaudio.adapter.CommonListAdapter;
import com.tencent.aiaudio.chat.AVChatManager;
import com.tencent.aiaudio.demo.R;
import com.tencent.xiaowei.info.XWAIAudioFriendInfo;
import com.tencent.xiaowei.info.XWContactInfo;
import com.tencent.xiaowei.sdk.XWDeviceBaseManager;

public class ContactActivity extends BaseActivity {
    static final String TAG = "ContactActivity";

    private GridView mGridView;
    protected CommonListAdapter<XWContactInfo> mAdapter;
    private boolean mIs264Mode;
    private boolean mIsAudioMode;
    private Button mBtnSwitch;
    private final String AUDIO_MODE_TEXT = "语音电话";
    private final String VIDEO_MODE_TEXT = "视频电话";
    private final String H264_MODE_TEXT = "264测试";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contact);
        initViews();
        initAdapter();
        initDatas();
        initEvens();
    }

    private void initDatas() {
        mIs264Mode = sp.getBoolean("is_264_mode", false);//先判断是否是264的测试
        mIsAudioMode = sp.getBoolean("is_audio", false);
        updateData();
    }

    private void initEvens() {
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!AVChatManager.getInstance().isCalling()) {
                    AVChatManager.getInstance().setThirdManageCamera(ifThirdManageCamera());//是否由用户管理摄像头
                    AVChatManager.getInstance().startAudioVideoChat(mAdapter.getItem(position).tinyID);// 启动AVSDK需要时间大概1-3s，根据性能确定
                    CommonApplication.showToast("正在启动QQ电话");
                } else {
                    CommonApplication.showToast("已经在通话中了");
                }

            }
        });

        updateData();
        XWDeviceBaseManager.setOnFriendListChangeListener(new XWDeviceBaseManager.OnFriendListChangeListener() {
            @Override
            public void onResult(XWAIAudioFriendInfo[] friendList) {
                if (friendList != null && friendList.length > 0) {
                    mAdapter.clear();
                    mAdapter.addAll(friendList);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });


        mBtnSwitch.setText(getBtnModeText());
        mBtnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBtnMode();
                setChatParam();
            }
        });

        setChatParam();
    }

    private void setChatParam() {
        AVChatManager.getInstance().setAudioMode(mIsAudioMode);
    }

    private void initAdapter() {
        mAdapter = new CommonListAdapter<XWContactInfo>() {
            @Override
            protected View initListCell(int position, View convertView, ViewGroup parent) {
                convertView = getLayoutInflater().inflate(R.layout.item_contact, parent, false);
                ((TextView) convertView.findViewById(R.id.tv)).setText(mAdapter.getItem(position).remark);
                if (!TextUtils.isEmpty(mAdapter.getItem(position).headUrl)) {
                    Picasso.with(convertView.getContext()).load(mAdapter.getItem(position).headUrl).into((ImageView) convertView.findViewById(R.id.iv));
                }
                return convertView;
            }
        };
    }

    private void initViews() {
        mGridView = (GridView) findViewById(R.id.gv);
        mBtnSwitch = (Button) findViewById(R.id.btn_switch);
    }

    protected void updateData() {
        XWDeviceBaseManager.getAIAudioFriendList(new XWDeviceBaseManager.GetFriendListRspListener() {
            @Override
            public void onResult(int errCode, XWAIAudioFriendInfo[] friendList) {
                if (friendList != null && friendList.length > 0) {
                    mAdapter.clear();
                    mAdapter.addAll(friendList);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        XWDeviceBaseManager.setOnFriendListChangeListener(null);
    }

    private String getBtnModeText() {
        String btnText = "";
        if (mIs264Mode) {
            btnText = H264_MODE_TEXT;
        } else {
            if (mIsAudioMode) {
                btnText = AUDIO_MODE_TEXT;
            } else {
                btnText = VIDEO_MODE_TEXT;
            }
        }
        return btnText;
    }


    private void changeBtnMode() {
        if (mBtnSwitch != null) {
            String curText = mBtnSwitch.getText().toString();
            if (!TextUtils.isEmpty(curText)) {
                String nextText = "";
                if (H264_MODE_TEXT.equals(curText)) {//当前是264切换成 音频
                    nextText = AUDIO_MODE_TEXT;
                    mIs264Mode = false;
                    mIsAudioMode = true;
                } else if (AUDIO_MODE_TEXT.equals(curText)) {//当前是音频切换成 视频
                    nextText = VIDEO_MODE_TEXT;
                    mIs264Mode = false;
                    mIsAudioMode = false;
                } else if (VIDEO_MODE_TEXT.equals(curText)) {//当前是视频切换成 264
                    nextText = H264_MODE_TEXT;
                    mIs264Mode = true;
                    mIsAudioMode = false;
                }
                editor.putBoolean("is_264_mode", mIs264Mode);
                editor.commit();
                editor.putBoolean("is_audio", mIsAudioMode);
                editor.commit();
                mBtnSwitch.setText(nextText);
            }
        }
    }

    private boolean ifThirdManageCamera() {
        boolean ifThirdManage = false;
        if (mBtnSwitch != null) {
            String curText = mBtnSwitch.getText().toString();
            if (!TextUtils.isEmpty(curText)) {
                if (H264_MODE_TEXT.equals(curText)) {
                    ifThirdManage = true;
                }
            }
        }
        return ifThirdManage;
    }
}
