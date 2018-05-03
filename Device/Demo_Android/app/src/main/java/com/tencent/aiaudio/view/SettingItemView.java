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
package com.tencent.aiaudio.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.aiaudio.demo.R;

public class SettingItemView extends RelativeLayout {

    private TextView itemTxtView;
    private TextView itemDesTxtView;
    private View divider;

    public SettingItemView(Context context) {
        this(context, null);
    }

    public SettingItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
        init(context, attrs);
    }

    private void initViews(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.setting_item_view, this);
        itemTxtView = (TextView) view.findViewById(R.id.item_title);
        itemDesTxtView = (TextView) view.findViewById(R.id.item_description);
        divider = view.findViewById(R.id.item_divider);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.setting_item_attrs);
        String text = ta.getString(R.styleable.setting_item_attrs_item_text);
        String description = ta.getString(R.styleable.setting_item_attrs_item_text);
        boolean hasDivider = ta.getBoolean(R.styleable.setting_item_attrs_has_divider, true);
        ta.recycle();

        if (!TextUtils.isEmpty(text)) {
            itemTxtView.setText(text);
        }
        if (!TextUtils.isEmpty(description)) {

            itemDesTxtView.setText(description);
        }

        if (!hasDivider) {
            divider.setVisibility(View.GONE);
        }
    }

    public void setDividerVisibility(boolean isVisible) {
        if (isVisible) {
            divider.setVisibility(VISIBLE);
        } else {
            divider.setVisibility(GONE);
        }
    }

    public void setTitle(String text) {
        if (!TextUtils.isEmpty(text)) {
            itemTxtView.setText(text);
        }
    }

    public void setDescription(String text) {
        if (!TextUtils.isEmpty(text)) {
            itemDesTxtView.setText(text);
        }
    }

}
