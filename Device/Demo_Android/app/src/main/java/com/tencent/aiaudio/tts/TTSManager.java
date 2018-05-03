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
package com.tencent.aiaudio.tts;

import android.os.Handler;
import android.os.HandlerThread;

import com.tencent.xiaowei.info.XWEventLogInfo;
import com.tencent.xiaowei.info.XWTTSDataInfo;
import com.tencent.xiaowei.sdk.XWSDK;
import com.tencent.xiaowei.util.Singleton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class TTSManager {

    private static final String TAG = "TTSManager";
    private ConcurrentHashMap<String, TTSItem> mCache = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, ArrayList<String>> associateMap = new ConcurrentHashMap<>();
    private Handler mHandler;

    private TTSManager() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    private static final Singleton<TTSManager> sSingleton = new Singleton<TTSManager>() {
        @Override
        protected TTSManager createInstance() {
            return new TTSManager();
        }
    };

    public static TTSManager getInstance() {
        return sSingleton.getInstance();
    }

    /**
     * 将收到的数据写到cache
     *
     * @param data
     */
    public synchronized void write(XWTTSDataInfo data) {
        if (data == null) {
            return;
        }
        TTSItem item = mCache.get(data.resID);
        if (item == null) {
            item = new TTSItem();
            item.pcmSampleRate = data.pcmSampleRate;
            item.sampleRate = data.sampleRate;
            item.channel = data.channel;
            item.format = data.format;
            item.resID = data.resID;
            mCache.put(data.resID, item);
            XWEventLogInfo log = new XWEventLogInfo();
            log.event = XWEventLogInfo.EVENT_TTS_BEGIN;
            log.time = System.currentTimeMillis();
            XWSDK.getInstance().reportEvent(log);
        }
        item.isEnd = data.isEnd;
        item.data.add(data);
        if (data.isEnd) {
            item.length = data.seq + 1;
            XWEventLogInfo log = new XWEventLogInfo();
            log.event = XWEventLogInfo.EVENT_TTS_END;
            log.time = System.currentTimeMillis();
            XWSDK.getInstance().reportEvent(log);
        }
        notifyAll();
    }

    /**
     * 查询resId相关的TTS信息
     *
     * @param resID
     * @return
     */
    public synchronized TTSItem getInfo(String resID) {
        TTSItem item = mCache.get(resID);
        while (item == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            item = mCache.get(resID);
        }
        return item;
    }

    /**
     * 根据resId顺序读取TTS的opus数据
     *
     * @param resID
     * @return
     */
    public synchronized XWTTSDataInfo read(String resID) {
        TTSItem item = mCache.get(resID);
        while (item == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            item = mCache.get(resID);
        }
        if (item.curSeq > -1 && item.curSeq == item.length - 1 && item.isEnd) {
            XWTTSDataInfo data = new XWTTSDataInfo();
            data.pcmSampleRate = item.pcmSampleRate;
            data.sampleRate = item.sampleRate;
            data.channel = item.channel;
            data.format = item.format;
            data.data = item.allData;
            data.isEnd = item.isEnd;
            data.resID = item.resID;
            if (data.data == null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    for (XWTTSDataInfo tts : item.data) {
                        if (tts.data != null)
                            bos.write(tts.data);
                    }
                    bos.flush();
                    data.data = item.allData = bos.toByteArray();
                    item.data.clear();
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return data;
        } else {
            XWTTSDataInfo data = null;
            while (data == null) {
                for (XWTTSDataInfo tts : item.data) {
                    if (tts.seq == item.curSeq + 1) {
                        item.curSeq++;
                        data = tts;
                        break;
                    }
                }
                if (data == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return data;
        }
    }

    /**
     * reset之后read可以从头开始
     *
     * @param resID
     */
    public void reset(String resID) {
        TTSItem item = mCache.get(resID);
        if (item != null) {
            if (item.isEnd) {
                item.curSeq = item.length - 1;
            } else {
                item.curSeq = -1;
            }
        }
    }

    /**
     * 是否这个id相关的tts数据
     *
     * @param resId
     */
    public void release(final String resId) {
        if (resId != null) {
            TTSItem item = mCache.remove(resId);
            if (item != null && !item.isEnd) {
                //  如果resId还没接受完毕，需要cancel后台下发。
                XWSDK.getInstance().cancelTTS(resId);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCache.remove(resId);
                    }
                }, 5000);
            }
        }
    }

    /**
     * 释放该session相关的所有TTS
     *
     * @param sessionId
     */
    public void release(int sessionId) {
        ArrayList<String> resIds = associateMap.remove(sessionId);
        if (resIds == null) {
            return;
        }
        ArrayList<String> cancelResIds = new ArrayList<>(resIds.size());
        if (resIds != null) {
            for (String resId : resIds) {
                TTSItem item = mCache.remove(resId);
                if (item != null && !item.isEnd) {
                    cancelResIds.add(resId);
                }
            }
        }
        for (final String resId : cancelResIds) {
            //  如果resId还没接受完毕，需要cancel后台下发。
            XWSDK.getInstance().cancelTTS(resId);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCache.remove(resId);
                }
            }, 5000);
        }
    }

    /**
     * 关联session和resId
     *
     * @param sessionId
     * @param resId
     */
    public void associate(int sessionId, String resId) {
        ArrayList<String> resIds = associateMap.get(sessionId);
        if (resIds == null) {
            resIds = new ArrayList<>();
            associateMap.put(sessionId, resIds);
        }
        if (resId != null) {
            resIds.add(resId);
        }
    }

    public static class TTSItem {
        PriorityBlockingQueue<XWTTSDataInfo> data = new PriorityBlockingQueue<>();
        public byte[] allData;
        boolean isEnd;
        public int pcmSampleRate;
        public int sampleRate;
        public int channel;
        public int format;
        public String resID;
        int curSeq = -1;
        int length;
    }

}
