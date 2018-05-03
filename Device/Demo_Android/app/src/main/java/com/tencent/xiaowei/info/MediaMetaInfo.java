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
package com.tencent.xiaowei.info;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.tencent.xiaowei.util.JsonUtil;

/**
 * 媒体meta信息
 */
public class MediaMetaInfo implements Parcelable {
    public String name;
    public String artist;
    public String album;
    public String cover;
    public int duration;
    public boolean favorite;
    public String lyric;
    public int quality;
    public String playId;

    public MediaMetaInfo() {
    }

    protected MediaMetaInfo(Parcel in) {
        name = in.readString();
        artist = in.readString();
        album = in.readString();
        cover = in.readString();
        playId = in.readString();
        duration = in.readInt();
        favorite = in.readByte() != 0;
        lyric = in.readString();
        quality = in.readInt();
    }

    public static final Creator<MediaMetaInfo> CREATOR = new Creator<MediaMetaInfo>() {
        @Override
        public MediaMetaInfo createFromParcel(Parcel in) {
            return new MediaMetaInfo(in);
        }

        @Override
        public MediaMetaInfo[] newArray(int size) {
            return new MediaMetaInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeString(cover);
        dest.writeString(playId);
        dest.writeInt(duration);
        dest.writeByte((byte) (favorite ? 1 : 0));
        dest.writeString(lyric);
        dest.writeInt(quality);
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MediaMetaInfo) {
            return comparePlayId(playId, ((MediaMetaInfo) o).playId);
        }
        return false;
    }

    public String getUniqueId() {
        return getUniqueId(playId);
    }

    public static String getUniqueId(String playId) {
        if (TextUtils.isEmpty(playId)) {
            return "";
        }
        String[] split = playId.split("&");
        String value[] = new String[1];
        for (String s : split) {
            if (s.contains("unique_id")) {
                value = s.split("=");
            }
        }
        if (value.length >= 2) {
            return value[1];
        }
        return playId;
    }

    private boolean comparePlayId(String a, String b) {
        return TextUtils.equals((a), (b));
    }
}
