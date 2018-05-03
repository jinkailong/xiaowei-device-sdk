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
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.aiaudio.activity.base.BaseActivity;
import com.tencent.aiaudio.adapter.CommonListAdapter;
import com.tencent.aiaudio.demo.R;
import com.tencent.aiaudio.service.ControlService;
import com.tencent.xiaowei.util.JsonUtil;

import java.util.ArrayList;

public class WeatherActivity extends BaseActivity {

    private ImageView mIvWeather;
    private TextView mTvCurrentTemperature;
    private TextView mTvTemperature;
    private TextView mTvCity;
    private GridView mGridView;
    private CommonListAdapter<WeatherItem> mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_weather);

        TextView txtTitle = (TextView) findViewById(R.id.txt_title);
        txtTitle.setText("天气");
        mIvWeather = (ImageView) findViewById(R.id.img_weather);
        mTvCurrentTemperature = (TextView) findViewById(R.id.txt_current_temperature);
        mTvTemperature = (TextView) findViewById(R.id.txt_temp_lowandhigh);
        mTvCity = (TextView) findViewById(R.id.txt_location);
        mGridView = (GridView) findViewById(R.id.gv);
        mAdapter = new CommonListAdapter<WeatherItem>() {
            @Override
            protected View initListCell(int position, View convertView, ViewGroup parent) {
                Holder holder;
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.item_weather, parent, false);
                    holder = new Holder();
                    holder.tvTp = (TextView) convertView.findViewById(R.id.tp);
                    holder.ivIcon = (ImageView) convertView.findViewById(R.id.icon);
                    holder.tvDay = (TextView) convertView.findViewById(R.id.day);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }

                WeatherItem item = getItem(position);
                holder.tvTp.setText(item.min_tp + "°~" + item.max_tp + "°");
                holder.ivIcon.setImageResource(getWeatherResourceId(item.condition));
                holder.tvDay.setText(item.date);
                return convertView;
            }

            class Holder {
                TextView tvTp;
                ImageView ivIcon;
                TextView tvDay;
            }
        };
        mGridView.setAdapter(mAdapter);
        findViewById(R.id.ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.img_music_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeatherActivity.this, HelperActivity.class);
                ArrayList<String> helps = new ArrayList<>();
                intent.putExtra("title", "天气");
                helps.add("明天深圳天气怎么样");
                helps.add("今天北京的空气质量如何");
                helps.add("后天杭州热不热");
                intent.putExtra("helps", helps);
                startActivity(intent);
            }
        });

        initData(getIntent());
    }

    private void initData(Intent intent) {
        String json = intent.getStringExtra(ControlService.EXTRA_KEY_START_SKILL_DATA);
        if (TextUtils.isEmpty(json)) {
            finish();
            return;
        }
        Weather weather = JsonUtil.getObject(json, Weather.class);

        if (weather == null || weather.data == null) {
            finish();
            return;
        }
        mTvCity.setText(weather.loc);
        WeatherItem item = null;
        for (int i = 0; i < weather.data.size(); i++) {
            if ("1".equals(weather.data.get(i).is_asked)) {
                item = weather.data.get(i);
            } else {
                mAdapter.add(weather.data.get(i));
            }
        }
        mAdapter.notifyDataSetChanged();
        if (item != null) {
            mTvTemperature.setText(item.min_tp + "°~" + item.max_tp + "°");
            mTvCurrentTemperature.setText(item.tp + "°");
            mIvWeather.setImageResource(getWeatherResourceId(item.condition));
        }
    }

    private int getWeatherResourceId(String condition) {
        if (condition.contains("阴转晴")) {
            return R.drawable.ic_weather_cloudy;
        }
        if (condition.contains("晴转阴")) {
            return R.drawable.ic_weather_cloudy;
        }
        if (condition.contains("冰雹")) {
            return R.drawable.ic_weather_rainy;
        }
        if (condition.contains("雷阵雨")) {
            return R.drawable.ic_weather_thunderstorm;
        }
        if (condition.contains("阵雨")) {
            return R.drawable.ic_weather_rainy;
        }
        if (condition.contains("雨夹雪")) {
            return R.drawable.ic_weather_sleet;
        }
        if (condition.contains("雪")) {
            return R.drawable.ic_weather_snowy;
        }
        if (condition.contains("雨")) {
            return R.drawable.ic_weather_rainy;
        }
        if (condition.contains("晴")) {
            return R.drawable.ic_weather_sunny;
        }
        if (condition.contains("霾") || condition.contains("雾") || condition.contains("浮尘") || condition.contains("扬沙") || condition.contains("沙尘暴") || condition.contains("霜")) {
            return R.drawable.ic_weather_haze;
        }
        if (condition.contains("多云")) {
            return R.drawable.ic_weather_cloudy;
        }
        if (condition.contains("阴")) {
            return R.drawable.ic_weather_overcast;
        }
        return R.drawable.ic_weather_cloudy;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initData(intent);
    }

    static class Weather {
        String loc;
        ArrayList<WeatherItem> data;
    }

    static class WeatherItem {
        String condition;
        String date;
        String max_tp;
        String min_tp;
        String pm25;
        String quality;
        String tp;
        String wind_direct;
        String wind_lv;
        String is_asked;
    }

    @Override
    public void onSkillIdle() {
        super.onSkillIdle();
        finish();
    }
}
