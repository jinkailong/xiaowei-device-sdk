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

#pragma once

#include <stdio.h>
#include <list>
#include <map>
#include <string>
#include "TXCAudioType.h"

template <typename T>
class Singleton
{
public:
    static T *instance()
    {
        if (!instance_)
        {
            instance_ = new T;
        }
        return instance_;
    }

private:
    static T *instance_;
};
template <typename T>
T *Singleton<T>::instance_ = NULL;

class XWTTSDataInfo
{
public:
    XWTTSDataInfo();
    ~XWTTSDataInfo();

    std::string res_id;
    int seq;
    bool is_end;
    int pcm_sample_rate;
    int sample_rate;
    int channel;
    int format;
    std::string data;

    std::string ToString();
};

class TTSItem
{
public:
    TTSItem();
    ~TTSItem();

    std::list<XWTTSDataInfo *> data;
    std::string all_data;
    bool is_end;
    int pcm_sample_rate;
    int sample_rate;
    int channel;
    int format;
    std::string res_id;
    bool recoverable;
    int cur_seq;
    int length;

    std::string ToString();
};

// TTS监听
class OnTTSPushListener
{
public:
    virtual void OnTTSInfo(TTSItem *ttsInfo) = 0;
    virtual void OnTTSData(XWTTSDataInfo *ttsInfo) = 0;
};

class CXWeiTTSManager : public Singleton<CXWeiTTSManager>
{
public:
    void write(TXCA_PARAM_AUDIO_DATA *data);
    TTSItem *getInfo(std::string resId);
    void read(std::string resId, OnTTSPushListener *listener);
    void reset(std::string resId);
    void release(std::string resId);

    void associate(int sessionId, std::string resId);
    void release(int sessionId);

private:
    friend class Singleton<CXWeiTTSManager>;

    CXWeiTTSManager();
    ~CXWeiTTSManager();

private:
    std::map<std::string, TTSItem *> mCache;
    std::map<int, std::list<std::string>> associateMap;
    std::map<std::string, OnTTSPushListener *> mTTSListeners;
};
