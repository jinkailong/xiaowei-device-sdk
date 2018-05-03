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
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.tencent.aiaudio.activity.base.BaseActivity;
import com.tencent.aiaudio.adapter.CommonListAdapter;
import com.tencent.xiaowei.def.XWCommonDef;
import com.tencent.aiaudio.demo.R;
import com.tencent.xiaowei.sdk.XWSDK;
import com.tencent.xiaowei.info.XWContextInfo;

import java.util.ArrayList;

public class HelperActivity extends BaseActivity {

    private TextView tvTitle;
    private ListView mListView;
    private CommonListAdapter<String> mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mShowStatus = false;
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_help);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        mListView = (ListView) findViewById(R.id.lv_help);
        mAdapter = new CommonListAdapter<String>() {
            @Override
            protected View initListCell(int position, View convertView, ViewGroup parent) {
                Holder holder;
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.item_help, parent, false);
                    holder = new Holder();
                    holder.tv = (TextView) convertView.findViewById(R.id.tv);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                holder.tv.setText(mAdapter.getItem(position));
                return convertView;
            }

            class Holder {
                TextView tv;
            }
        };
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = mAdapter.getItem(position);
                try {
                    text = text.substring(text.indexOf(",") + 1);
                    XWSDK.getInstance().request(XWCommonDef.RequestType.TEXT, text.getBytes(), new XWContextInfo());
                } catch (Exception e) {

                }
            }
        });

        initData(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initData(intent);
    }

    private void initData(Intent intent) {
        String title = intent.getStringExtra("title");
        tvTitle.setText(title);
        ArrayList<String> list = intent.getStringArrayListExtra("helps");
        mAdapter.addAll(list);
        mAdapter.notifyDataSetChanged();
    }

}
