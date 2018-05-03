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
#ifndef __TX_CLOUD_SKILL_DEFINE_H__
#define __TX_CLOUD_SKILL_DEFINE_H__

#include "TXSDKCommonDef.h"

CXX_EXTERN_BEGIN

#define DEF_TXCA_SKILL_ID_UNKNOWN "8dab4796-fa37-4114-ffff-ffffffffffff"

#define DEF_TXCA_SKILL_ID_MUSIC "8dab4796-fa37-4114-0011-7637fa2b0001"     //skill name: 音乐
#define DEF_TXCA_SKILL_ID_FM "8dab4796-fa37-4114-0024-7637fa2b0001"        //skill name: FM-笑话/FM-电台/FM-小说/FM-相声/FM-评书/FM-故事/FM-杂烩
#define DEF_TXCA_SKILL_ID_WEATHER "8dab4796-fa37-4114-0012-7637fa2b0003"   //skill name: 天气服务
#define DEF_TXCA_SKILL_ID_NEWS "8dab4796-fa37-4114-0019-7637fa2b0001"      //skill name: 新闻
#define DEF_TXCA_SKILL_ID_WIKI "8dab4796-fa37-4114-0020-7637fa2b0001"      //skill name: 百科
#define DEF_TXCA_SKILL_ID_HISTORY "8dab4796-fa37-4114-0027-7637fa2b0001"   //skill name: 历史上的今天
#define DEF_TXCA_SKILL_ID_DATETIME "8dab4796-fa37-4114-0028-7637fa2b0001"  //skill name: 当前时间
#define DEF_TXCA_SKILL_ID_CALC "8dab4796-fa37-4114-0018-7637fa2b0001"      //skill name: 计算器
#define DEF_TXCA_SKILL_ID_TRANSLATE "8dab4796-fa37-4114-0030-7637fa2b0001" //skill name: 翻译
#define DEF_TXCA_SKILL_ID_CHAT "8dab4796-fa37-4114-0029-7637fa2b0001"      //skill name: 闲聊

#define DEF_TXCA_SKILL_ID_IOTCTRL "8dab4796-fa37-4114-0016-7637fa2b0001" //skill name: 物联-物联设备控制

#define DEF_TXCA_SKILL_ID_ALARM "8dab4796-fa37-4114-0012-7637fa2b0001"    //skill name: 提醒类
#define DEF_TXCA_SKILL_ID_QQTEL "8dab4796-fa37-4114-0001-7637fa2b0001"    //skill name: 通讯-QQ通话
#define DEF_TXCA_SKILL_ID_QQMSG "8dab4796-fa37-4114-0002-7637fa2b0001"    //skill name: 通讯-QQ消息
#define DEF_TXCA_SKILL_ID_MSGBOX "8dab4796-fa37-4114-0012-7637fa2b0002"   //skill name: 消息盒子
#define DEF_TXCA_SKILL_ID_NAVIGATE "8dab4796-fa37-4114-0015-7637fa2b0001" //skill name: 导航
#define DEF_TXCA_SKILL_ID_VOD "8dab4796-fa37-4114-0026-7637fa2b0001"      //skill name: 视频
#define DEF_TXCA_SKILL_ID_TRIGGER_ALARM "8dab4796-fa37-4114-0036-7637fa2b0001"      //skill name: 闹钟触发场景

#define DEF_TXCA_SKILL_ID_GLOBAL "8dab4796-fa37-4114-0000-7637fa2b0000"   //skill name: 通用控制

#define DEF_TXCA_SKILL_NAME_CHAT_EX "小微闲聊"

// 播放控制
#define PROPERTY_ID_PLAY 700003            // 播放
#define PROPERTY_ID_PAUSE 700004           // 暂停
#define PROPERTY_ID_PREV 700005            // 上一首
#define PROPERTY_ID_NEXT 700006            // 下一首
#define PROPERTY_ID_STOP 700100            // 停止播放
#define PROPERTY_ID_PLAYMODE_RANDOM 700103 // 随机播放
#define PROPERTY_ID_PLAYMODE_ORDER 700104  // 顺序播放
#define PROPERTY_ID_PLAYMODE_LOOP 700137   // 循环播放
#define PROPERTY_ID_PLAYMODE_SINGLE 700113 // 单曲循环
#define PROPERTY_ID_REPEAT 700108          // 重播

// 通用控制
#define PROPERTY_ID_VOLUME_SET 700101        // 设置音量
#define PROPERTY_ID_VOLUME_INC 700001        // 音量大
#define PROPERTY_ID_VOLUME_DEC 700002        // 音量小
#define PROPERTY_ID_KEEP_SHARE 700126        // 收藏 分享
#define PROPERTY_ID_KEEP_SILENCE 700128      // 静音 取消静音
#define PROPERTY_ID_UPLOAD_LOG 700129        // 上报日志
#define PROPERTY_ID_FETCH_DEVICE_INFO 700130 // 查询设备基础信息（pid,sn,din,mac,ip等）
#define PROPERTY_ID_ERROR_FEEDBACK 700141    // 错误反馈，将上一次请求上报到后台
#define PROPERTY_ID_VOLUME_MAX 700150        // 音量最大
#define PROPERTY_ID_VOLUME_MIN 700151        // 音量最小

// 下面是视频使用
#define PROPERTY_ID_SPEED 700134              // 快进
#define PROPERTY_ID_REWIND 700135             // 快退
#define PROPERTY_ID_DEFINITION 700138         // 分辨率调整
#define PROPERTY_ID_CURRENT_POSITION 700139   // 查询已播放时长
#define PROPERTY_ID_REMAINDER_DURATION 700140 // 查询剩余播放时长

//下面只导航使用
#define PROPERTY_ID_ENSURE 700142 // 确定
#define PROPERTY_ID_CANCEL 700143 // 取消
#define PROPERTY_ID_BACK 700144   // 返回

// 下面是视频和导航使用
#define PROPERTY_ID_SELECT 700145    // 选项确认 第几集
#define PROPERTY_ID_OPEN_DEBUG_INFO 700146 // 打开调试信息，收到后可以在屏幕上显示一些关键信息，便于查问题
#define PROPERTY_ID_CLOSE_DEBUG_INFO 700147 // 关闭调试信息
#define PROPERTY_ID_PREV_PAGE 700148 // 上一页
#define PROPERTY_ID_NEXT_PAGE 700149 // 下一页

// 下面是视频使用
#define PROPERTY_ID_SPEED_TO 700152  // 进度调节，快进到，快退到
#define PROPERTY_ID_EXIT 700153      // 退出
#define PROPERTY_ID_REWIND_TO 700155 // 快退到

// 下面是通话
#define PROPERTY_ID_QQCALL_REQUEST 11020 //
#define PROPERTY_ID_QQCALL_REQUEST_NEW 11052
#define PROPERTY_ID_QQCALL_CANCEL 11021 //11055
#define PROPERTY_ID_QQCALL_ACCEPT 11022 //11056
#define PROPERTY_ID_QQCALL_REJECT 11023 //11057
#define PROPERTY_ID_QQCALL_INVITE 666666
#define PROPERTY_ID_QQCALL_UI_REQUEST 666667
#define PROPERTY_ID_QQCALL_PROMPT 11027   //11058
#define PROPERTY_ID_QQCALL_CONTACTS 11061 //打电话给手机联系人

// 下面是消息盒子的
#define PROPERTY_ID_START 700125
#define PROPERTY_ID_START_BY_ID 700127
#define PROPERTY_ID_MSG_READED 700128
#define PROPERTY_ID_IMAGENT 11054
#define PROPERTY_ID_SEND_IOT_AUDIO_MSG  11018   //发送物联语音消息给手Q

#define PROPERTY_ID_IOT_TEXT    10000   //物联文本消息
#define PROPERTY_ID_IOT_VIDEO   10001   //群聊视频消息（小文件通道）
#define PROPERTY_ID_IOT_IMAGE   10002   //群聊图片消息（小文件通道）
#define PROPERTY_ID_IOT_AUDIO   10003   //群聊音频消息（小文件通道）
#define PROPERTY_ID_IOT_OTHER   11001   //通过小文件通道接收的任意类型的文件
#define PROPERTY_ID_IOT_CACHE   72010   //本地指令控制添加下载完的消息到消息盒子

//etc...

CXX_EXTERN_END

#endif // __TX_CLOUD_SKILL_DEFINE_H__
