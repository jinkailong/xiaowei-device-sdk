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

#include <sstream>
#include "XWeiTTSManager.h"

XWTTSDataInfo::XWTTSDataInfo()
    : seq(0),
      is_end(false),
      pcm_sample_rate(0),
      sample_rate(0),
      channel(1),
      format(0)
{
}

XWTTSDataInfo::~XWTTSDataInfo()
{
}

std::string XWTTSDataInfo::ToString()
{
    std::stringstream ss;
    ss << "[XWTTSDataInfo]";
    ss << "res_id:";
    ss << res_id;
    ss << "seq:";
    ss << seq;
    ss << "is_end:";
    ss << is_end;
    ss << "data.length:";
    ss << data.length();

    return ss.str();
}

TTSItem::TTSItem()
    : is_end(false),
      pcm_sample_rate(0),
      sample_rate(0),
      channel(1),
      format(0),
      recoverable(false),
      cur_seq(-1),
      length(0)
{
}

TTSItem::~TTSItem()
{
    for (std::list<XWTTSDataInfo *>::iterator iter = data.begin();
         iter != data.end();)
    {
        XWTTSDataInfo *info = *iter;
        delete info;
        data.erase(iter++);
    }
}

std::string TTSItem::ToString()
{
    std::stringstream ss;
    ss << "[TTSItem]";
    ss << "res_id:";
    ss << res_id;
    ss << "cur_seq:";
    ss << cur_seq;
    ss << "is_end:";
    ss << is_end;
    ss << "data.length:";
    ss << all_data.size();

    return ss.str();
}

CXWeiTTSManager::CXWeiTTSManager()
{
}

CXWeiTTSManager::~CXWeiTTSManager()
{
    for (std::map<std::string, TTSItem *>::iterator iter = mCache.begin();
         iter != mCache.end();)
    {
        TTSItem *item = iter->second;
        delete item;
        mCache.erase(iter++);
    }
    associateMap.clear();
}

void CXWeiTTSManager::write(TXCA_PARAM_AUDIO_DATA *data)
{
    if (data == NULL)
    {
        return;
    }
    std::string resId = data->id;
    std::map<std::string, TTSItem *>::iterator iter = mCache.find(resId);
    TTSItem *item = NULL;
    if (iter != mCache.end())
    {
        item = iter->second;
    }
    if (item == NULL)
    {
        item = new TTSItem;
        item->pcm_sample_rate = data->pcm_sample_rate;
        item->sample_rate = data->sample_rate;
        item->channel = data->channel;
        item->format = data->format;
        item->res_id = resId;

        mCache[resId] = item;

        // TODO 上报 TXCA_PARAM_LOG_EVENT_TTS_BEGIN
    }

    item->is_end = data->is_end;

    XWTTSDataInfo *ttsInfo = new XWTTSDataInfo;
    ttsInfo->pcm_sample_rate = data->pcm_sample_rate;
    ttsInfo->sample_rate = data->sample_rate;
    ttsInfo->channel = data->channel;
    ttsInfo->format = data->format;
    ttsInfo->res_id = resId;
    ttsInfo->seq = data->seq;
    ttsInfo->is_end = data->is_end;
    ttsInfo->data.assign((const char *)data->raw_data, (size_t)data->raw_data_len);

    // TODO 根据seq排序
    item->data.push_back(ttsInfo);
    if (item->is_end)
    {
        item->length = data->seq + 1;

        // TODO 上报 TXCA_PARAM_LOG_EVENT_TTS_END
    }
    std::map<std::string, OnTTSPushListener *>::iterator itor = mTTSListeners.find(resId);
    OnTTSPushListener *listener = NULL;
    if (itor != mTTSListeners.end())
    {
        listener = itor->second;
        read(resId, listener);
    }
}

TTSItem *CXWeiTTSManager::getInfo(std::string resId)
{
    std::map<std::string, TTSItem *>::iterator iter = mCache.find(resId);
    TTSItem *item = NULL;
    if (iter != mCache.end())
    {
        item = iter->second;
    }
    return item;
}

void CXWeiTTSManager::read(std::string resId, OnTTSPushListener *listener)
{
    if (listener != NULL)
    {
        mTTSListeners[resId] = listener;

        TTSItem *item = getInfo(resId);
        if (item != NULL)
        {
            // 已经接收完毕了
            if (item->cur_seq > -1 && item->cur_seq == item->length - 1 && item->is_end)
            {
                listener->OnTTSInfo(item);
                XWTTSDataInfo data;
                data.pcm_sample_rate = item->pcm_sample_rate;
                data.sample_rate = item->sample_rate;
                data.channel = item->channel;
                data.format = item->format;
                data.is_end = item->is_end;
                data.res_id = item->res_id;
                if (item->all_data.length() == 0)
                {
                    std::stringstream ss;
                    for (std::list<XWTTSDataInfo *>::iterator iter = item->data.begin();
                         iter != item->data.end(); iter++)
                    {
                        XWTTSDataInfo *info = *iter;
                        ss << info->data;
                        delete info;
                    }
                    item->all_data = ss.str();
                    item->data.clear();
                }
                data.data = item->all_data;
                listener->OnTTSData(&data);
            }
            else
            {
                // 先把已经接收了的全回调出去
                for (std::list<XWTTSDataInfo *>::iterator iter = item->data.begin();
                     iter != item->data.end(); iter++)
                {
                    if ((*iter)->seq == item->cur_seq + 1 && (item->data.size() >= 5 || item->is_end))
                    {
                        if ((*iter)->seq == 0)
                        {
                            listener->OnTTSInfo(item);
                        }
                        item->cur_seq++;
                        XWTTSDataInfo *info = *iter;
                        listener->OnTTSData(info);
                        if (item->is_end && !item->recoverable)
                        {
                            mTTSListeners.erase(resId);
                            release(resId);
                            break;
                        }
                    }
                }
            }
        }
    }
}

void CXWeiTTSManager::reset(std::string resId)
{
    std::map<std::string, TTSItem *>::iterator iter = mCache.find(resId);
    TTSItem *item = NULL;
    if (iter != mCache.end())
    {
        item = iter->second;
        if (item->is_end)
        {
            item->cur_seq = item->length - 1;
        }
        else
        {
            item->cur_seq = -1;
        }
    }
}

void CXWeiTTSManager::release(std::string resId)
{
    std::map<std::string, TTSItem *>::iterator iter = mCache.find(resId);
    TTSItem *item = NULL;
    if (iter != mCache.end())
    {
        item = iter->second;
        if (!item->is_end)
        {
            // TODO 取消接收TTS

            // TODO 延迟一会儿从mCache中移除，之所以延迟，是需要等取消TTS生效。

            delete item;
            mCache.erase(iter);
        }
        else
        {
            delete item;
            mCache.erase(iter);
        }
    }
}

void CXWeiTTSManager::associate(int sessionId, std::string resId)
{
    if (resId.length() == 0)
    {
        return;
    }
    std::map<int, std::list<std::string>>::iterator iter = associateMap.find(sessionId);
    std::list<std::string> list;
    if (iter != associateMap.end())
    {
        list = iter->second;
    }
    list.push_back(resId);
    associateMap[sessionId] = list;
}

void CXWeiTTSManager::release(int sessionId)
{
    std::map<int, std::list<std::string>>::iterator iter = associateMap.find(sessionId);
    if (iter != associateMap.end())
    {
        std::list<std::string> list = iter->second;
        for (std::list<std::string>::iterator iter = list.begin();
             iter != list.end(); iter++)
        {
            std::string resId = *iter;
            release(resId);
        }
        list.clear();
        associateMap.erase(iter);
    }
}
