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
package com.tencent.aiaudio.alarm;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class ClockListBean implements Parcelable {


    private List<ClockInfoBean> clock_info;


    protected ClockListBean(Parcel in) {
        clock_info = in.createTypedArrayList(ClockInfoBean.CREATOR);
    }

    public static final Creator<ClockListBean> CREATOR = new Creator<ClockListBean>() {
        @Override
        public ClockListBean createFromParcel(Parcel in) {
            return new ClockListBean(in);
        }

        @Override
        public ClockListBean[] newArray(int size) {
            return new ClockListBean[size];
        }
    };

    public List<ClockInfoBean> getClock_info() {
        return clock_info;
    }

    public void setClock_info(List<ClockInfoBean> clock_info) {
        this.clock_info = clock_info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public static class ClockInfoBean implements  Parcelable{
        /**
         * clock_id : 598
         * clock_type : 0
         * event : 喝水
         * opt : 1
         * repeat_interval :
         * repeat_type : 0
         * service_type : 0
         * trig_time : 1502193827
         * snooze : false
         */

        private String clock_id;
        private int clock_type;
        private String event;
        private int opt;
        private String repeat_interval;
        private int repeat_type;
        private int service_type;
        private String trig_time;
        private boolean snooze;

        protected ClockInfoBean(Parcel in) {
            clock_id = in.readString();
            clock_type = in.readInt();
            event = in.readString();
            opt = in.readInt();
            repeat_interval = in.readString();
            repeat_type = in.readInt();
            service_type = in.readInt();
            trig_time = in.readString();
            snooze = in.readByte() > 0;
        }

        public static final Creator<ClockInfoBean> CREATOR = new Creator<ClockInfoBean>() {
            @Override
            public ClockInfoBean createFromParcel(Parcel in) {
                return new ClockInfoBean(in);
            }

            @Override
            public ClockInfoBean[] newArray(int size) {
                return new ClockInfoBean[size];
            }
        };

        public String getClock_id() {
            return clock_id;
        }

        public void setClock_id(String clock_id) {
            this.clock_id = clock_id;
        }

        public int getClock_type() {
            return clock_type;
        }

        public void setClock_type(int clock_type) {
            this.clock_type = clock_type;
        }

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public int getOpt() {
            return opt;
        }

        public void setOpt(int opt) {
            this.opt = opt;
        }

        public String getRepeat_interval() {
            return repeat_interval;
        }

        public void setRepeat_interval(String repeat_interval) {
            this.repeat_interval = repeat_interval;
        }

        public int getRepeat_type() {
            return repeat_type;
        }

        public void setRepeat_type(int repeat_type) {
            this.repeat_type = repeat_type;
        }

        public int getService_type() {
            return service_type;
        }

        public void setService_type(int service_type) {
            this.service_type = service_type;
        }

        public String getTrig_time() {
            return trig_time;
        }

        public void setTrig_time(String trig_time) {
            this.trig_time = trig_time;
        }

        public boolean isSnooze() { return snooze; }

        public void setSnooze(boolean snooze) {
            this.snooze = snooze;
        }

        @Override
        public String toString() {
            return "ClockInfoBean{" +
                    "event='" + event + '\'' +
                    ", clock_id='" + clock_id + '\'' +
                    ", opt=" + opt +
                    ", repeat_interval='" + repeat_interval + '\'' +
                    ", repeat_type=" + repeat_type +
                    ", service_type=" + service_type +
                    ", trig_time=" + trig_time +
                    ", clock_type=" + clock_type +
                    ", snooze=" + snooze +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(clock_id);
            dest.writeInt(clock_type);
            dest.writeString(event);
            dest.writeInt(opt);
            dest.writeString(repeat_interval);
            dest.writeInt(repeat_type);
            dest.writeInt(service_type);
            dest.writeString(trig_time);
            dest.writeByte((byte) (snooze ? 1 : 0));
        }
    }
}
