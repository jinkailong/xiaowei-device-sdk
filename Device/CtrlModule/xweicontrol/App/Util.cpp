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

#include "Util.hpp"
#include <sstream>
#include <map>
static int last_pid_ = 0;
static std::map<XWM_EVENT, std::string> XWM_EVENT_MAP;
static std::map<DURATION_HINT, std::string> DURATION_HINT_MAP;
static std::map<TXC_PLAYER_ACTION, std::string> TXC_PLAYER_ACTION_MAP;
static std::map<TXC_PLAYER_STATE, std::string> TXC_PLAYER_STATE_MAP;
static std::map<player_control, std::string> player_control_MAP;

bool Util::IsInvaild(const TXCA_PARAM_RESPONSE &cRsp)
{
     return (!cRsp.skill_info.id && !cRsp.skill_info.name && !cRsp.last_skill_info.id && !cRsp.last_skill_info.name && cRsp.resource_groups_size == 0 && cRsp.context.speak_timeout == 0);
}

bool Util::IsVaild(const TXCA_PARAM_RESPONSE &cRsp)
{
    bool invalid = (!cRsp.skill_info.id && !cRsp.skill_info.name && cRsp.resource_groups_size == 0 && cRsp.context.speak_timeout == 0);
    return !invalid;
}

// 还未确定意图的Rsp
bool Util::IsTempRsp(const TXCA_PARAM_RESPONSE &cRsp)
{
    return (!cRsp.skill_info.id && cRsp.resource_groups_size > 0);
}

bool Util::IsCommandRsp(const TXCA_PARAM_RESPONSE &cRsp)
{
    std::string skill_name = cRsp.skill_info.name ?  cRsp.skill_info.name : "";
    return  skill_name.find("通用控制") != std::string::npos && cRsp.resource_groups_size > 0 &&  cRsp.resource_groups[0].resources_size > 0 &&  cRsp.resource_groups[0].resources[0].format == txca_resource_command;
}

int Util::GetNewProcessId()
{
    return ++last_pid_;
}

std::string Util::ToString(const TXCA_PARAM_RESPONSE &cRsp)
{
    std::stringstream ss;
    if (cRsp.skill_info.id)
    {
        ss << "skillid:";
        ss << cRsp.skill_info.id;
    }
    if (cRsp.skill_info.name)
    {
        ss << " skillname:";
        ss << cRsp.skill_info.name;
    }
    if (cRsp.last_skill_info.id)
    {
        ss << "last_skill_id:";
        ss << cRsp.last_skill_info.id;
    }
    if (cRsp.last_skill_info.name)
    {
        ss << " last_skill_name:";
        ss << cRsp.last_skill_info.name;
    }
    ss << " error_code:";
    ss << cRsp.error_code;
    ss << " is_notify:";
    ss << cRsp.is_notify;
    if (cRsp.request_text)
    {
        ss << " request_text:";
        ss << cRsp.request_text;
    }
    ss << " resources_size:";
    ss << cRsp.resource_groups_size;
    ss << " is_recovery:";
    ss << cRsp.is_recovery;
    ss << " play_behavior:";
    ss << cRsp.play_behavior;
    return ss.str();
}


std::string Util::ToString(XWM_EVENT e)
{
    if(XWM_EVENT_MAP.size() == 0) {
        XWM_EVENT_MAP[XWM_NULL] = "XWM_NULL";
        XWM_EVENT_MAP[XWM_SUPPLEMENT_REQUEST] = "XWM_SUPPLEMENT_REQUEST";
        XWM_EVENT_MAP[XWM_ERROR_RESPONSE] = "XWM_ERROR_RESPONSE";
        XWM_EVENT_MAP[XWM_RESPONSE_DATA] = "XWM_RESPONSE_DATA";
        XWM_EVENT_MAP[XWM_SILENT] = "XWM_SILENT";
        XWM_EVENT_MAP[XWM_BEGIN_PLAYER_CONTROL] = "XWM_BEGIN_PLAYER_CONTROL";
        XWM_EVENT_MAP[XWM_STOP] = "XWM_STOP";
        XWM_EVENT_MAP[XWM_PLAY] = "XWM_PLAY";
        XWM_EVENT_MAP[XWM_PAUSE] = "XWM_PAUSE";
        XWM_EVENT_MAP[XWM_VOLUME] = "XWM_VOLUME";
        XWM_EVENT_MAP[XWM_BEGIN_NAVIGATE] = "XWM_BEGIN_NAVIGATE";
        XWM_EVENT_MAP[XWM_REPEAT] = "XWM_REPEAT";
        XWM_EVENT_MAP[XWM_NEXT] = "XWM_NEXT";
        XWM_EVENT_MAP[XWM_SKIP] = "XWM_SKIP";
        XWM_EVENT_MAP[XWM_SETFOCUS] = "XWM_SETFOCUS";
        XWM_EVENT_MAP[XWM_KILLFOCUS] = "XWM_KILLFOCUS";
        XWM_EVENT_MAP[XWM_REQUEST_AUDIO_FOCUS] = "XWM_REQUEST_AUDIO_FOCUS";
        XWM_EVENT_MAP[XWM_ABANDON_AUDIO_FOCUS] = "XWM_ABANDON_AUDIO_FOCUS";
        XWM_EVENT_MAP[XWM_SET_AUDIO_FOCUS] = "XWM_SET_AUDIO_FOCUS";
        XWM_EVENT_MAP[XWM_BEGIN_MEDIA] = "XWM_BEGIN_MEDIA";
        XWM_EVENT_MAP[XWM_ALBUM_ADDED] = "XWM_ALBUM_ADDED";
        XWM_EVENT_MAP[XWM_LIST_ADDED] = "XWM_LIST_ADDED";
        XWM_EVENT_MAP[XWM_LIST_REMOVED] = "XWM_LIST_REMOVED";
        XWM_EVENT_MAP[XWM_LIST_UPDATED] = "XWM_LIST_UPDATED";
        XWM_EVENT_MAP[XWM_MEDIA_ADDED] = "XWM_MEDIA_ADDED";
        XWM_EVENT_MAP[XWM_MEDIA_REMOVED] = "XWM_MEDIA_REMOVED";
        XWM_EVENT_MAP[XWM_PROGRESS] = "XWM_PROGRESS";
        XWM_EVENT_MAP[XWM_MEDIA_UPDATE] = "XWM_MEDIA_UPDATE";
        XWM_EVENT_MAP[XWM_BEGIN_UI_FEEDBACK] = "XWM_BEGIN_UI_FEEDBACK";
        XWM_EVENT_MAP[XWM_PLAYER_STATUS_CHANGED] = "XWM_PLAYER_STATUS_CHANGED";
        XWM_EVENT_MAP[XWM_PLAYER_STATUS_FINISH] = "XWM_PLAYER_STATUS_FINISH";
        XWM_EVENT_MAP[XWM_IM_MSG] = "XWM_IM_MSG";
        XWM_EVENT_MAP[XWM_SYSTEM] = "XWM_SYSTEM";
        XWM_EVENT_MAP[XWM_USER] = "XWM_USER";
    }
    return XWM_EVENT_MAP[e];
}

std::string Util::ToString(DURATION_HINT e)
{
    if(DURATION_HINT_MAP.size() == 0) {
        DURATION_HINT_MAP[AUDIOFOCUS_GAIN] = "AUDIOFOCUS_GAIN";
        DURATION_HINT_MAP[AUDIOFOCUS_GAIN_TRANSIENT] = "AUDIOFOCUS_GAIN_TRANSIENT";
        DURATION_HINT_MAP[AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK] = "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK";
        DURATION_HINT_MAP[AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE] = "AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE";
        DURATION_HINT_MAP[AUDIOFOCUS_LOSS] = "AUDIOFOCUS_LOSS";
        DURATION_HINT_MAP[AUDIOFOCUS_LOSS_TRANSIENT] = "AUDIOFOCUS_LOSS_TRANSIENT";
        DURATION_HINT_MAP[AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK] = "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
    }
    return DURATION_HINT_MAP[e];
}

std::string Util::ToString(TXC_PLAYER_ACTION e){
    if(TXC_PLAYER_ACTION_MAP.size() == 0) {
        TXC_PLAYER_ACTION_MAP[ACT_NULL] = "ACT_NULL";
        TXC_PLAYER_ACTION_MAP[ACT_MUSIC_PUSH_MEDIA] = "ACT_MUSIC_PUSH_MEDIA";
        TXC_PLAYER_ACTION_MAP[ACT_ADD_ALBUM] = "ACT_ADD_ALBUM";
        TXC_PLAYER_ACTION_MAP[ACT_PLAYLIST_ADD_ITEM] = "ACT_PLAYLIST_ADD_ITEM";
        TXC_PLAYER_ACTION_MAP[ACT_PLAYLIST_ADD_ITEM_FRONT] = "ACT_PLAYLIST_ADD_ITEM_FRONT";
        TXC_PLAYER_ACTION_MAP[ACT_PLAYLIST_REMOVE_ITEM] = "ACT_PLAYLIST_REMOVE_ITEM";
        TXC_PLAYER_ACTION_MAP[ACT_PLAYLIST_UPDATE_ITEM] = "ACT_PLAYLIST_UPDATE_ITEM";
        TXC_PLAYER_ACTION_MAP[ACT_PLAYER_STOP] = "ACT_PLAYER_STOP";
        TXC_PLAYER_ACTION_MAP[ACT_PLAYER_PAUSE] = "ACT_PLAYER_PAUSE";
        TXC_PLAYER_ACTION_MAP[ACT_PLAYER_SET_REPEAT_MODE] = "ACT_PLAYER_SET_REPEAT_MODE";
        TXC_PLAYER_ACTION_MAP[ACT_PLAYER_FINISH] = "ACT_PLAYER_FINISH";
        TXC_PLAYER_ACTION_MAP[ACT_RESPONSE_DATA] = "ACT_RESPONSE_DATA";
        TXC_PLAYER_ACTION_MAP[ACT_PROGRESS] = "ACT_PROGRESS";
        TXC_PLAYER_ACTION_MAP[ACT_NEED_SUPPLEMENT] = "ACT_NEED_SUPPLEMENT";
        TXC_PLAYER_ACTION_MAP[ACT_NEED_TIPS] = "ACT_NEED_TIPS";
        TXC_PLAYER_ACTION_MAP[ACT_CHANGE_VOLUME] = "ACT_CHANGE_VOLUME";
        TXC_PLAYER_ACTION_MAP[ACT_REPORT_PLAY_STATE] = "ACT_REPORT_PLAY_STATE";
        TXC_PLAYER_ACTION_MAP[ACT_DOWNLOAD_MSG] = "ACT_DOWNLOAD_MSG";
        TXC_PLAYER_ACTION_MAP[ACT_AUDIOMSG_RECORD] = "ACT_AUDIOMSG_RECORD";
        TXC_PLAYER_ACTION_MAP[ACT_AUDIOMSG_SEND] = "ACT_AUDIOMSG_SEND";
    }
    return TXC_PLAYER_ACTION_MAP[e];
}

std::string Util::ToString(TXC_PLAYER_STATE e){
    if(TXC_PLAYER_STATE_MAP.size() == 0) {
        TXC_PLAYER_STATE_MAP[TXC_PLAYER_STATE_START] = "TXC_PLAYER_STATE_START";
        TXC_PLAYER_STATE_MAP[TXC_PLAYER_STATE_STOP] = "TXC_PLAYER_STATE_STOP";
        TXC_PLAYER_STATE_MAP[TXC_PLAYER_STATE_COMPLETE] = "TXC_PLAYER_STATE_COMPLETE";
        TXC_PLAYER_STATE_MAP[TXC_PLAYER_STATE_PAUSE] = "TXC_PLAYER_STATE_PAUSE";
        TXC_PLAYER_STATE_MAP[TXC_PLAYER_STATE_CONTINUE] = "TXC_PLAYER_STATE_CONTINUE";
        TXC_PLAYER_STATE_MAP[TXC_PLAYER_STATE_ERR] = "TXC_PLAYER_STATE_ERR";
    }
    return TXC_PLAYER_STATE_MAP[e];
}

std::string Util::ToString(player_control e)
{
    if(player_control_MAP.size() == 0) {
        player_control_MAP[PLAYER_CONTROL_NULL] = "PLAYER_CONTROL_NULL";
        player_control_MAP[PLAYER_BEGIN_PLAYER_CONTROL] = "PLAYER_BEGIN_PLAYER_CONTROL";
        player_control_MAP[PLAYER_STOP] = "PLAYER_STOP";
        player_control_MAP[PLAYER_PLAY] = "PLAYER_PLAY";
        player_control_MAP[PLAYER_PAUSE] = "PLAYER_PAUSE";
        player_control_MAP[PLAYER_RESUME] = "PLAYER_RESUME";
        player_control_MAP[PLAYER_VOLUME] = "PLAYER_VOLUME";
        player_control_MAP[PLAYER_BEGIN_NAVIGATE] = "PLAYER_BEGIN_NAVIGATE";
        player_control_MAP[PLAYER_REPEAT] = "PLAYER_REPEAT";
        player_control_MAP[PLAYER_NEXT] = "PLAYER_NEXT";
        player_control_MAP[PLAYER_SKIP] = "PLAYER_SKIP";
    }
    return player_control_MAP[e];
}
