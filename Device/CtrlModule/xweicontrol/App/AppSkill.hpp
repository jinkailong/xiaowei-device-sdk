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
#ifndef _AIAUDIO_APPKIT_HPP_
#define _AIAUDIO_APPKIT_HPP_

#include "Media.hpp"
#include "AudioApp.hpp"

#include "AudioFocusManager.hpp"

typedef struct _txca_param_response TXCA_PARAM_RESPONSE;
typedef struct _txca_param_resource TXCA_PARAM_RESOURCE;

namespace AIAudio
{
namespace Global
{
class CResponse;
}
}

class AppSkill
{
  public:
    AppSkill(SESSION id);
    virtual ~AppSkill(){};

    // 收到一个响应，在这里处理它。（核心处理流程在这里，如果有一个新的场景需要自定义，可以重写它来实现。）
    virtual bool OnAiAudioRsp(const TXCA_PARAM_RESPONSE &cRsp) = 0;

    // 收到一个消息
    virtual bool OnMessage(xwm_event event, XWPARAM arg1, XWPARAM arg2) = 0;
    
    virtual std::string GetClassName() = 0;
    
    virtual SESSION GetSessionId() = 0;

    // sessionid
    int app_id_;

  protected:
};

class TXCPlayer;
class CPlayerControl;
class CPlaylist;

class PlayerKit : public AppSkill, public OnAudioFocusChangeListener
{
  public:
    PlayerKit(int app_id);
    PlayerKit(int app_id, REPEAT_MODE repeat_mode);
    ~PlayerKit();

    virtual bool OnAiAudioRsp(const TXCA_PARAM_RESPONSE &cRsp);
    virtual bool OnMessage(XWM_EVENT event, XWPARAM arg1, XWPARAM arg2);
    virtual std::string GetClassName();
    
    SESSION GetSessionId();

  protected:
    virtual bool PreProcessResourceCommand(const TXCA_PARAM_RES_GROUP *v_groups, size_t count);
    virtual bool CanProcessNotify(const TXCA_PARAM_RESPONSE &cRsp);
    virtual void OnAudioFocusChange(int focus_change);
    
    virtual void ProcessPlayList(const TXCA_PARAM_RESPONSE &cRsp, bool& bHandled);
    virtual void OnSupplementRequest(const TXCA_PARAM_RESPONSE &cRsp, bool& bHandled);
    
    // 添加播放资源
    virtual size_t AddList(const TXCA_PARAM_RESPONSE &cRsp, _Out_ PtrMedia &album, _Out_ int &album_index);
    
    // 检查这个media是否可以当做UI显示
    virtual bool CanBeAlbum(const PtrMedia media);

    // 更新播放资源
    virtual size_t UpdateListItem(const TXCA_PARAM_RESPONSE &cRsp, _Out_ std::vector<PtrMedia> &list);

    // 替换播放资源的部分字段
    virtual size_t ReplaceListItem(const TXCA_PARAM_RESPONSE &cRsp, _Out_ std::vector<PtrMedia> &list);
    
    virtual size_t RemoveListItem(const TXCA_PARAM_RESPONSE &cRsp, _Out_ std::vector<PtrMedia> &list);

    // 翻译成Media对象
    virtual bool ResToMedia(const TXCA_PARAM_RESOURCE *item, _Out_ PtrMedia &_media);
    void InitList(bool hasMore);

  protected:
    TXCAutoPtr<TXCPlayer> player_;
    TXCAutoPtr<CPlayerControl> control_;
    TXCAutoPtr<CPlaylist> playlist_;
    bool m_is_need_play;

  private:
    bool m_isRecovery;
};

class AppKitMusic : public PlayerKit
{
  public:
    AppKitMusic(int app_id);
    virtual std::string GetClassName();

  protected:
    bool CanBeAlbum(const PtrMedia media);
};

class AppKitFM : public PlayerKit
{
  public:
    AppKitFM(int app_id);
    virtual std::string GetClassName();

  protected:
    bool CanBeAlbum(const PtrMedia media);
};

class AppKitNew : public PlayerKit
{
  public:
    AppKitNew(int app_id);
    virtual std::string GetClassName();

  protected:
    size_t AddList(const TXCA_PARAM_RESPONSE &cRsp, _Out_ PtrMedia &album, _Out_ int &album_index);
    bool ResToMedia(const TXCA_PARAM_RESOURCE *item, const std::string &description, _Out_ PtrMedia &_media);
};

class AppKitCommon : public PlayerKit
{
  public:
    AppKitCommon(int app_id);
    virtual std::string GetClassName();
};

class AppKitNotify : public PlayerKit
{
  public:
    AppKitNotify(int app_id);
    virtual std::string GetClassName();

  protected:
    virtual bool PreProcessResourceCommand(const TXCA_PARAM_RES_GROUP *v_groups, size_t count);
    virtual bool CanProcessNotify(const TXCA_PARAM_RESPONSE &cRsp);
    virtual bool OnAiAudioRsp(const TXCA_PARAM_RESPONSE &cRsp);
};

class AppKitGlobal : public PlayerKit
{
  public:
    AppKitGlobal(int app_id);
    virtual std::string GetClassName();
    virtual bool OnAiAudioRsp(const TXCA_PARAM_RESPONSE &cRsp);

  protected:
    virtual bool PreProcessResourceCommand(const TXCA_PARAM_RES_GROUP *v_groups, size_t count);
};

class OuterSkill : public AppSkill
{
  public:
    OuterSkill(int app_id);
    virtual std::string GetClassName();

    virtual bool OnAiAudioRsp(const TXCA_PARAM_RESPONSE &cRsp);
    virtual bool OnMessage(XWM_EVENT event, XWPARAM arg1, XWPARAM arg2);
    SESSION GetSessionId();

  protected:
    virtual bool PreProcessResourceCommand(const TXCA_PARAM_RES_GROUP *v_groups, size_t count);
    virtual bool CanProcessNotify(const TXCA_PARAM_RESPONSE &cRsp);
};

#endif /* _AIAUDIO_APPKIT_HPP_ */
