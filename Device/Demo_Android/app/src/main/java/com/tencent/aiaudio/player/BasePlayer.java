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
package com.tencent.aiaudio.player;

import java.io.IOException;

public abstract class BasePlayer {

    private OnPreparedListener mOnPreparedListener;
    private OnCompletionListener mOnCompletionListener;
    private OnErrorListener mOnErrorListener;
    private OnSeekCompleteListener mOnSeekCompleteListener;
    private Object tag;

    public abstract void setAudioSessionId(int sessionId);

    public abstract void setDataSource(String url) throws IOException, UnsupportedOperationException;

    public abstract int getAudioSessionId();

    public abstract void setAudioStreamType(int type);

    public abstract void prepareAsync() throws IllegalStateException;

    public abstract void start();

    public abstract void stop();

    public abstract void pause();

    public abstract void seekTo(int i);

    public abstract void reset();

    public abstract void release();

    public abstract long getDuration();

    public abstract long getCurrentPosition();

    public abstract void setVolume(float left, float right);

    public abstract boolean isPlaying();

    public abstract void setLooping(boolean isLooping);

    public void notifyOnPrepared() {
        if (mOnPreparedListener != null) {
            mOnPreparedListener.onPrepared(this);
        }
    }

    public void notifyOnCompletion() {
        if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(this);
        }
    }

    public void notifyOnError(int what, int extra) {
        if (mOnErrorListener != null) {
            mOnErrorListener.onError(this, what, extra);
        }
    }

    public void notifyOnSeek() {
        if (mOnSeekCompleteListener != null) {
            mOnSeekCompleteListener.onSeekComplete(this);
        }
    }

    public interface OnPreparedListener {
        void onPrepared(BasePlayer player);
    }

    public void setOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    public interface OnCompletionListener {
        void onCompletion(BasePlayer player);
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    public interface OnErrorListener {
        void onError(BasePlayer player, int what, int extra);
    }

    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    public interface OnSeekCompleteListener {
        void onSeekComplete(BasePlayer player);
    }

    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        mOnSeekCompleteListener = listener;
    }

    public void clearListener() {
        mOnPreparedListener = null;
        mOnCompletionListener = null;
        mOnErrorListener = null;
        mOnSeekCompleteListener = null;
    }

    public void setTag(Object obj) {
        tag = obj;
    }

    public Object getTag() {
        return tag;
    }
}
