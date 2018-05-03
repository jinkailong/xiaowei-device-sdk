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
package com.tencent.xiaowei.control;


/**
 * 常量值定义
 */
public class Constants {

    public interface XWeiPlayerState {
        int tx_ai_audio_player_state_start = 1,            //开始播放: 新播放一个资源
                tx_ai_audio_player_state_abort = 2,            //退出播放: 终止播放, 调用tx_ai_audio_player_control_code_stop后触发
                tx_ai_audio_player_state_complete = 3,            //完成播放: 完成一首, 包括调用下一首上一首等
                tx_ai_audio_player_state_pause = 4,            //暂停播放: 暂停, 调用tx_ai_audio_player_control_code_pause后触发
                tx_ai_audio_player_state_continue = 5,            //继续播放: 继续播放, 调用tx_ai_audio_player_control_code_resume后触发
                tx_ai_audio_player_state_err = 6,            //播放错误: 包括无法下载url，资源异常等
                tx_ai_audio_player_state_seek = 7;            //通知sdk播放器进行了定位操作。要使用tx_ai_audio_player_statechangeEx接口，data给offset的值（秒）

    }

    public interface XWeiControlCode {
        int PLAYER_CONTROL_NULL = 0,
                PLAYER_BEGIN_PLAYER_CONTROL = 1,
                PLAYER_STOP = 2,
                PLAYER_PLAY = 3,    //  arg1: index
                PLAYER_PAUSE = 4,   //
                PLAYER_RESUME = 5,  //
                PLAYER_VOLUME = 6,  //  arg1: value of volume

        PLAYER_BEGIN_NAVIGATE = 0x100,
                PLAYER_REPEAT = 0x101,  //  arg1: repeat_mode
                PLAYER_NEXT = 0x102,   //  arg1: +n|-n indexes
                PLAYER_SKIP = 0x103,   //  arg1: skip +n|-n milliseconds

        PLAYER_SETFOCUS = 0x104,  // arg1: session id lost focus
                PLAYER_KILLFOCUS = 0x105;  // arg1: session id receives focus
    }

    public interface XWeiInnerPlayerStatus {
        int STATUS_STOP = 0,
                STATUS_PLAY = 1,
                STATUS_PAUSE = 2;
    }

    interface XWeiRepeatMode {
        int REPEAT_CURRENT = 0,
                REPEAT_SEQUENCE = 1,
                REPEAT_LOOP = 2,
                REPEAT_RANDOM = 3,
                REPEAT_SINGLE = 4;

    }

    interface XWMEvent {
        int XWM_NULL = 0,

        XWM_SUPPLEMENT_REQUEST = 1, // arg1: speak_timeout; response   = (const TXCA_PARAM_RESPONSE*)(arg2);
                XWM_ERROR_RESPONSE = 2,     // arg1: error_code; response   = (const TXCA_PARAM_RESPONSE*)(arg2);
                XWM_RESPONSE_DATA = 3,      // arg1:

        XWM_BEGIN_PLAYER_CONTROL = 0x100,
                XWM_STOP = 0x101,
                XWM_PLAY = 0x102,   //  arg1: index
                XWM_PAUSE = 0x103,  //  arg1: 1 for pause, 0 for resume
                XWM_VOLUME = 0x104, //  arg1: value of volume

        XWM_BEGIN_NAVIGATE = 0x200,
                XWM_REPEAT = 0x201, //  arg1: repeat_mode
                XWM_NEXT = 0x202,   //  arg1: +n indexes
                XWM_SKIP = 0x203,   //  arg1: skip n milliseconds

        XWM_SETFOCUS = 0x204,   // arg1: session id lost focus
                XWM_KILLFOCUS = 0x205,  // arg1: session id receives focus

        XWM_BEGIN_MEDIA = 0x300,
                XWM_ALBUM_ADDED = 0x301,  // arg1: media index of album description;
                XWM_LIST_ADDED = 0x302, // arg1: start item index; arg2: added item count
                XWM_LIST_REMOVED = 0x303,   // arg1: start item index; arg2: end item index
                XWM_MEDIA_ADDED = 0x304, // arg1: start item index; arg2: added item count
                XWM_MEDIA_REMOVED = 0x305,   // arg1: start item index; arg2: end item index

        XWM_PROGRESS = 0x306,   // progress = reinterpret_cast<const txc_progress_t *>(arg1)
                XWM_MEDIA_UPDATE = 0x307,   //  arg1: const char *resID

        XWM_BEGIN_UI_FEEDBACK = 0x400,
                XWM_PLAYER_STATUS_CHANGED = 0x401,  // arg1: tx_ai_audio_player_state

        XWM_IM_MSG = 0x900,


        XWM_SYSTEM = 0X1000,
                XWM_SYS_DP_DELAY = 0X1001,

        XWM_USER = 0X2000;
    }


    public interface TXPlayerTipsType {
        int PLAYER_TIPS_NEXT_FAILURE = 0;
        int PLAYER_TIPS_PREV_FAILURE = 1;
    }

    /**
            * 播放模式
     */
    public interface RepeatMode {

        /**
         * 随机播放
         */
        int REPEAT_MODE_RANDOM = 0;

        /**
         * 单曲循环
         */
        int REPEAT_SINGLE = 1;

        /**
         * 循环播放
         */
        int REPEAT_MODE_LOOP = 2;

        /**
         * 顺序播放
         */
        int REPEAT_MODE_SEQUENCE = 3;

    }

    /**
     * 场景（技能）名定义
     */
    public interface SKILL_NAME {
        String SKILL_NAME_WEATHER = "天气服务";
        String SKILL_NAME_MUSIC = "音乐";
        String SKILL_NAME_XIAOWEI_CHAT = "小微闲聊";
        String SKILL_NAME_ALARM = "提醒类";
        String SKILL_NAME_TRIGGER_ALARM = "闹钟触发场景";
        String SKILL_NAME_QQCall = "通讯-QQ通话";
    }

    /**
     * 场景（技能）ID定义
     */
    public interface SkillIdDef {


        /**
         * 通用控制
         */
        String SKILL_ID_GLOBAL = "8dab4796-fa37-4114-0000-7637fa2b0000";
        /**
         * 天气
         */
        String SKILL_ID_WEATHER = "8dab4796-fa37-4114-0012-7637fa2b0003";

        /**
         * 音乐
         */
        String SKILL_ID_MUSIC = "8dab4796-fa37-4114-0011-7637fa2b0001";

        /**
         * FM
         */
        String SKILL_ID_FM = "8dab4796-fa37-4114-0024-7637fa2b0001";

        /**
         * 新闻
         */
        String SKILL_ID_New = "8dab4796-fa37-4114-0019-7637fa2b0001";

        /**
         * 搜狗百科-历史上的今天
         */
        String SKILL_ID_WIKI_HISTORY = "8dab4796-fa37-4114-0027-7637fa2b0001";

        /**
         * AI-LAB百科
         */
        String SKILL_ID_WIKI_AI_LAB = "8dab4796-fa37-4114-0020-7637fa2b0001";

        /**
         * 搜狗百科-当前时间
         */
        String SKILL_ID_WIKI_Time = "8dab4796-fa37-4114-0028-7637fa2b0001";

        /**
         * 搜狗-计算器
         */
        String SKILL_ID_WIKI_Calculator = "8dab4796-fa37-4114-0018-7637fa2b0001";

        /**
         * 微信闲聊
         */
        String SKILL_ID_WX_Chat = "8dab4796-fa37-4114-0029-7637fa2b0001";

        /**
         * 闹钟
         */
        String SKILL_ID_ALARM = "8dab4796-fa37-4114-0012-7637fa2b0001";

        /**
         * 闹钟触发场景
         */
        String SKILL_ID_TRIGGER_ALARM = "8dab4796-fa37-4114-0036-7637fa2b0001";

        /**
         * 翻译
         */
        String SKILL_ID_TRANSLATE = "8dab4796-fa37-4114-0030-7637fa2b0001";

        /**
         * QQ电话
         */
        String SKILL_ID_QQ_CALL = "8dab4796-fa37-4114-0001-7637fa2b0001";

        /**
         * QQ消息
         */
        String SKILL_ID_QQ_MSG = "8dab4796-fa37-4114-0002-7637fa2b0001";

        /**
         * 消息盒子
         */
        String SKILL_ID_MSGBOX = "8dab4796-fa37-4114-0012-7637fa2b0002";

        /**
         * 导航
         */
        String SKILL_ID_NAV = "8dab4796-fa37-4114-0015-7637fa2b0001";

        /**
         * 视频
         */
        String SKILL_ID_VIDEO = "8dab4796-fa37-4114-0026-7637fa2b0001";

        /**
         * 对不起，我没有明白你的意思
         */
        String SKILL_ID_Unknown = "8dab4796-fa37-4114-ffff-ffffffffffff";
    }

    public interface PROPERTY_ID {

        int RESUME = 700003;
        int PAUSE = 700004;
        int STOP = 700100;
        int PREV = 700005;
        int NEXT = 700006;
        int RANDOM = 700103;
        int ORDER = 700104;
        int LOOP = 700137;
        int SINGLE = 700113;
        int REPEAT = 700108;
        int SHARE = 700126;

        // 下面是视频使用
        int SPEED = 700134; // 快进
        int REWIND = 700135;     // 快退
        int DEFINITION = 700138;   // 分辨率调整
        int POSITION = 700139;   // 查询已播放时长
        int DURATION = 700140;  // 查询剩余播放时长

        //下面只导航使用
        int ENSURE = 700142; // 确定
        int CANCEL = 700143;   // 取消
        int BACK = 700144;// 返回

        // 下面是视频和导航使用
        int SELECT = 700145; // 选项确认 第几集
        int PREV_PAGE = 700148; // 上一页
        int NEXT_PAGE = 700149;// 下一页

        // 下面是视频使用
        int SPEED_TO = 700152;// 进度调节，快进到，快退到
        int EXIT = 700153;// 退出
        int REWIND_TO = 700155;// 快退到

    }
}
