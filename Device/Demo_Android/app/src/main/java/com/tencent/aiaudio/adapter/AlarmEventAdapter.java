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
package com.tencent.aiaudio.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tencent.aiaudio.alarm.SkillAlarmBean;
import com.tencent.aiaudio.demo.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class AlarmEventAdapter extends RecyclerView.Adapter {
    private List<SkillAlarmBean> mDataList;
    private OnItemClickListener mListener;
    private OnListItemClickListener mListItemClickListener;

    public AlarmEventAdapter(List<SkillAlarmBean> mDataList) {
        this.mDataList = mDataList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm_event, parent, false);
        return new EventHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final SkillAlarmBean event = mDataList.get(position);
        EventHolder eventHolder = (EventHolder) holder;
        if (TextUtils.isEmpty(event.getEvent())) {
            eventHolder.mTxtEvent.setText("提醒");
        } else {
            eventHolder.mTxtEvent.setText(event.getEvent());
        }
        eventHolder.mTxtTime.setText(getAlarmTime(event.getAlarmTime()));
        eventHolder.mTxtDay.setText(getAlarmData(event.getAlarmTime()));
        eventHolder.mImgSwitch.setOnClickListener(new MyOnClickListener(eventHolder, event.ismIsOpen()));
//        eventHolder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mListItemClickListener != null) {
//                    mListItemClickListener.onListItemClick(position);
//                }
//            }
//        });
        if (event.ismIsOpen()) {
            if (System.currentTimeMillis() > event.getAlarmTime()) {
                event.setmIsOpen(false);// 已过期
            }
        }
        if (event.ismIsOpen()) {
            eventHolder.mTxtEvent.setTextColor(Color.WHITE);
            eventHolder.mTxtTime.setTextColor(Color.WHITE);
            eventHolder.mTxtDay.setTextColor(Color.WHITE);
            eventHolder.mImgSwitch.setImageResource(R.drawable.ic_circle_blue);
        }
    }

    public String getAlarmTime(long alarmTime) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return df.format(new Date(alarmTime));
    }

    private String getAlarmData(long alarmTime) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        long alarm = Long.valueOf(df.format(new Date(alarmTime)));
        long current = Long.valueOf(df.format(new Date(System.currentTimeMillis())));
        switch ((int) (alarm - current)) {
            case 0:
                return "今天";
            case 1:
                return "明天";
            case 2:
                return "后天";
            default:
                df = new SimpleDateFormat("yyyy-MM-dd");
                return df.format(new Date(alarmTime));
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void setOnListItemClickListener(OnListItemClickListener listener) {
        mListItemClickListener = listener;
    }

    public class EventHolder extends RecyclerView.ViewHolder {
        public LinearLayout mLayout;
        public TextView mTxtDelete;
        TextView mTxtEvent;
        TextView mTxtTime;
        TextView mTxtDay;
        ImageView mImgSwitch;

        EventHolder(View itemView) {
            super(itemView);
            mTxtEvent = (TextView) itemView.findViewById(R.id.txt_alarm_event);
            mTxtTime = (TextView) itemView.findViewById(R.id.txt_alarm_time);
            mTxtDay = (TextView) itemView.findViewById(R.id.txt_alarm_day);
            mImgSwitch = (ImageView) itemView.findViewById(R.id.img_alarm_switch);
            mTxtDelete = (TextView) itemView.findViewById(R.id.txt_alarm_delete);
            mLayout = (LinearLayout) itemView.findViewById(R.id.layout_alarm_root);
        }
    }

    public interface OnListItemClickListener {
        void onListItemClick(int position);
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private class MyOnClickListener implements View.OnClickListener {
        EventHolder holder;
        boolean mIsSwitchOn;

        MyOnClickListener(EventHolder holder, boolean warning) {
            this.holder = holder;
            mIsSwitchOn = warning;
        }

        @Override
        public void onClick(View v) {
            mIsSwitchOn = !mIsSwitchOn;
            if (mIsSwitchOn) {
                holder.mTxtEvent.setTextColor(Color.WHITE);
                holder.mTxtTime.setTextColor(Color.WHITE);
                holder.mTxtDay.setTextColor(Color.WHITE);
                holder.mImgSwitch.setImageResource(R.drawable.ic_circle_blue);
            } else {
                int color = Color.parseColor("#66ffffff");
                holder.mTxtEvent.setTextColor(color);
                holder.mTxtTime.setTextColor(color);
                holder.mTxtDay.setTextColor(color);
                holder.mImgSwitch.setImageResource(R.drawable.ic_circle_white);
            }
            SkillAlarmBean event = mDataList.get(holder.getAdapterPosition());
            event.setmIsOpen(mIsSwitchOn);
            if (mListener != null)
                mListener.onItemClick(holder.getAdapterPosition());
        }
    }

    public void setItemList(List<SkillAlarmBean> mDataList) {
        this.mDataList = mDataList;
        notifyDataSetChanged();
    }

    public SkillAlarmBean removeItem(int position) {
        SkillAlarmBean bean = mDataList.remove(position);
        notifyDataSetChanged();
        return bean;
    }

    public SkillAlarmBean getItem(int position) {
        return mDataList.get(position);
    }
}