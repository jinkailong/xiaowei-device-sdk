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

#ifndef __TX_FILE_TRANSFER_H__
#define __TX_FILE_TRANSFER_H__

#include "TXSDKCommonDef.h"
#include "TXCAudioType.h"

////////////////////////////////////////////////////////////////////////////
//  文件传输接口
////////////////////////////////////////////////////////////////////////////

CXX_EXTERN_BEGIN

/**
 * 目前已经在使用的业务名称
 */
#define BUSINESS_NAME_IMAGE_MSG "ImgMsg"   // 来自手机QQ的图片消息
#define BUSINESS_NAME_AUDIO_MSG "AudioMsg" // 来自手机QQ的语音留言
#define BUSINESS_NAME_VIDEO_MSG "VideoMsg" // 来自手机QQ的视频留言
#define BUSINESS_NAME_FILE_MSG  "FileMsg"  // 来自手机QQ的文件消息

/**
 * 传输任务类型 （txc_file_transfer_info -> transfer_type）
 */
enum TXCA_FILE_TRANSFER_TYPE
{
    transfet_type_none     = 0,
    transfer_type_upload   = 1,   // 当前任务是上传任务
    transfer_type_download = 2,   // 当前任务是下载任务
    transfer_type_c2c_in   = 3,   // 供 SDK 内部使用
    transfer_type_c2c_out  = 4,   // 供 SDK 内部使用
};

/**
 * 传输文件类型 （txc_file_transfer_info -> file_type）
 */
enum TXCA_FILE_TRANSFER_FILETYPE
{
    transfer_filetype_image = 1, // 图片文件
    transfer_filetype_video = 2, // 视频文件
    transfer_filetype_audio = 3, // 语音文件
    transfer_filetype_other = 4, // 其它文件
};

/**
 * 传输通道类型 （txc_file_transfer_info -> channel_type）
 */
enum TXCA_FILE_TRANSFER_CHANNELTYPE
{
    // FTN 文件传输通道, 该通道特点如下：
    //（1）安全性极高：带高强度安全校验，无登录状态无法访问
    //（2）文件容量大：最大支持文件大小为 4G
    //（3）有上传限制：同一账号 7 天内最多上传 5000 个文件，多于此会被服务器拒绝
    transfer_channeltype_FTN = 1,

    // 小文件传输通道, 该通道特点如下：
    //（1）专为PPT语音短信，图片等小文件场景服务，最大支持文件大小25M
    //（2）无权限校验，有url就可以下载，安全强度较低
    transfer_channeltype_MINI = 2,

    //ptt类型数据, 暂未开放
    transfer_channeltype_PTT = 3,
};

/**
 * 任务信息
 */
typedef struct tag_txc_file_transfer_info
{
    char file_path[1024]; // 文件本地路径                                    （通用字段）
    char file_key[512];   // 文件的后台索引                                  （通用字段）
    int key_length;

    char *buffer_raw; // 下载到的Buffer
    unsigned long long buffer_raw_len;
    char buffer_key[512]; // 上传Buffer后得到的后台索引
    int buffer_key_len;

    char *buff_with_file; // C2C发送文件附带的自定义BUFFER
    int buff_length;

    char bussiness_name[64]; // 用于确定发送 or 接收的文件的业务场景            （通用字段）
                             // 比如收到一个文件时，这个字段用来指明文件是语音留言，还是给打印机的待打印文件
                             // 已经分配的 business name 见此头文件最开始的宏定义

    unsigned long long file_size; // 文件大小                                        （通用字段）
    int channel_type;             // 通道类型：TXCA_FILE_TRANSFER_CHANNELTYPE          （通用字段）
    int file_type;                // 文件类型：TXCA_FILE_TRANSFER_FILETYPE             （通用字段）
    int transfer_type;            // upload  download  c2c_in  c2c_out

    char mini_token[512]; // 文件验证token，用于检查文件的完整性和合法性，
                        // 避免传输文件在DNS挟持攻击下被篡改或者文件下载不完整时即被交给UI层处理的问题
                        // 仅用于适用于小文件通道的文件传输（transfer_channeltype_MINI）
                        // *** 可见字符 : length = strlen(mini_token)
} TXCA_FILE_TRANSFER_INFO;

/**
* 通知，回调
*/
typedef struct tag_tx_file_transfer_notify
{
    // 传输进度
    // transfer_progress ： 上传 下载进度
    // max_transfer_progress ： 进度的最大值， transfer_progress/max_transfer_progress 计算传输百分比
    void (*on_transfer_progress)(unsigned long long transfer_cookie, unsigned long long transfer_progress, unsigned long long max_transfer_progress);

    // 传输结果
    // txc_file_transfer_info结构体里包含文件本地路径，下载就是文件的保存路径
    void (*on_transfer_complete)(unsigned long long transfer_cookie, int err_code, TXCA_FILE_TRANSFER_INFO *tran_info);

    // 收到C2C transfer通知
    void (*on_file_in_come)(unsigned long long transfer_cookie, const TXCA_CCMSG_INST_INFO *inst_info, const TXCA_FILE_TRANSFER_INFO *tran_info);

} TXCA_FILE_TRANSFER_NOTIFY;

/**
* 初始化传文件
*   notify : 回调
*   path_recv_file : 接收文件的目录
*/
SDK_API int txca_init_file_transfer(TXCA_FILE_TRANSFER_NOTIFY notify, char *path_recv_file);

/**
* 上传文件
* channeltype : 传输通道类型，取值范围见TXCA_FILE_TRANSFER_CHANNELTYPE。
* filetype : 传输文件类型，取值范围见TXCA_FILE_TRANSFER_FILETYPE
* file_path: 上传的文件路径
* transfer_cookie：返回任务cookie
*/
SDK_API int txca_upload_file(int channel_type, int file_type, char *file_path, unsigned long long *transfer_cookie);

/**
* 下载文件
* channeltype : 传输通道类型，取值范围见TXCA_FILE_TRANSFER_CHANNELTYPE。
* filetype : 传输文件类型，取值范围见TXCA_FILE_TRANSFER_FILETYPE
* file_key：要下载的文件的key
* key_length：file_key的长度
* mini_token是手机QQ 6.3开始引入的一种文件安全校验码，如果收到的文件信息里有mini_token请带上这个字段。
* mini_token如果不为空，SDK会检查文件的完整性和合法性，避免传输文件在DNS挟持攻击下被篡改或者文件下载不完整时即被交给UI层处理的问题
* transfer_cookie：返回任务cookie
*/
SDK_API int txca_download_file(int channel_type, int file_type, char *file_key, int key_length, const char *mini_token, unsigned long long *transfer_cookie);

/**
* 取消传送文件的任务
* transfer_cookie：要取消的任务cookie，由txc_upload_file, txca_download_file 所返回
*/
SDK_API int txca_cancel_transfer(unsigned long long transfer_cookie);

/**
* 获取小文件通道下载url
* fileId：通过小文件通道上传源文件得到的文件索引。对端发送过来。
* fileType：文件类型，也是对端发送过来的。
* downloadUrl：出参。使用者分配空间，字节大小设置为400，用于保存标准http下载文件的链接地址。
*/
SDK_API int txca_get_minidownload_url(char *file_id, int file_type, char *download_url);

/**
* 自动下载文件控制的回调
* uFileSize: 文件大小
* uChannelType: 文件传输通道, 1: NFC-局域网传输通道 2: FTN传输通道 3: MINI-小文件传输通道
* 返回值: 返回0则自动下载，其他则不触发下载
*/
typedef int (*TXCA_AUTO_DOWNLOAD_CONTROL_CALLBACK)(unsigned long long file_size, unsigned int channel_type);

/**
* 设置自动下载文件控制的回调
* FTN文件传输通道默认是自动下载文件的，调用者可以通过设置回调来决定是否要自动下载
*/
SDK_API void txca_set_auto_download_callbak(TXCA_AUTO_DOWNLOAD_CONTROL_CALLBACK cb);

CXX_EXTERN_END

#endif // __TX_FILE_TRANSFER_H__
