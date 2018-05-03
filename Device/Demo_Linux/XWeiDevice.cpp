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
#include "XWeiDevice.h"
#include "Util.h"
#include "TXDeviceSDK.h"
#include <stdio.h>

void on_binder_list_change(int error_code, TX_BINDER_INFO * pBinderList, int nCount)
{
    printf("on_binder_list_change nCount: %d\n", nCount);
    CXWeiApp::instance().Device().OnBind(nCount > 0);
}

/**
 * 登录完成的通知，errcode为0表示登录成功，其余请参考全局的错误码表
 */
void on_login_complete(int errcode) {
    printf("on_login_complete code:%d\n", errcode);
    CXWeiApp::instance().Device().OnLogin(err_null == errcode);
}

/**
 * 在线状态变化通知， 状态（status）取值为 11 表示 在线， 取值为 21 表示  离线
 * old是前一个状态，new是变化后的状态（当前）
 */
void on_online_status(int oldState, int newState) {
    printf("online status: %s\n", 11 == newState ? "true" : "false");
    CXWeiApp::instance().Device().OnState(11 == newState);
}


CXWeiDevice::CXWeiDevice()
{

}

CXWeiDevice::~CXWeiDevice()
{

}

/**
 * 设备是否被绑定
 */
void CXWeiDevice::OnBind(bool isBinded)
{

}

/**
 * 设备登录是否成功
 */
void CXWeiDevice::OnLogin(bool isSuccessed)
{

}

/**
 * 当设备上线后，初始化语音引擎
 */
void CXWeiDevice::OnState(bool isOnline)
{
    if(isOnline) {
        m_objEngine.Init();
    }
}

/**
 * SDK初始化
 * 例如：
 * （1）填写设备基本信息；
 * （2）打算监听哪些事件，事件监听的原理实际上就是设置各类消息的回调函数；
 * （3）调用tx_init_device执行初始化
 */
bool CXWeiDevice::Init()
{
    // 重要信息：
    // 1.如何获取设备的基本信息，请参考文档：https://xiaowei.qcloud.com/wiki/#hardware_product_intro 和 https://xiaowei.qcloud.com/wiki/#OpenSrc_Linux_Demo_Guide
    // 2. 设备License，与设备SN(guid)一一对应，SN和License的生成方法和规则，请参考文档：https://xiaowei.qcloud.com/wiki/#TechMisc_license_calc

    // 读取License信息
    char license[256] = {0};
    int nLicenseSize = 0;
    if (!Util::readBufferFromFile("./DeviceData/licence.sign.file.txt", license, sizeof(license), &nLicenseSize)) {
        printf("[error]get license from file failed...\n");
        return false;
    }

    // 读取guid(SN)信息
    char guid[32] = {0};
    int nGUIDSize = 0;
    if(!Util::readBufferFromFile("./DeviceData/GUID_file.txt", guid, sizeof(guid), &nGUIDSize)) {
        printf("[error]get guid from file failed...\n");
        return false;
    }

    // 读取服务器公钥信息
    char svrPubkey[256] = {0};
    int nPubkeySize = 0;
    if (!Util::readBufferFromFile("./DeviceData/1700004669.pem", svrPubkey, sizeof(svrPubkey), &nPubkeySize))
    {
        printf("[error]get svrPubkey from file faileds..\n");
        return NULL;
    }

    // 设备的基本信息
    TX_DEVICE_INFO info = {0};
    info.os_platform            = "Linux";           // 平台名
    info.device_name            = "demo1";           // 设备名称
    info.device_serial_number   = guid;              // 设备SN，由厂商自行生成，
    info.device_license         = license;           // 设备License，与SN一一对应，SN和License的生成方法和规则，请参考文档：https://xiaowei.qcloud.com/wiki/#TechMisc_license_calc
    info.product_version        = 1;                 // 固件版本号，与OTA升级相关，设备应保存该版本号
    info.network_type           = network_type_wifi; // 网络类型
    info.product_id             = 1700004669;        // 设备Pid信息，通过小微硬件开放平台上的配置平台注册一个新设备后会分配pid信息，请自行替换
    info.server_pub_key         = svrPubkey;         // 服务器公钥，通过小微硬件开放平台的配置平台下载，与某个设备是关联的

    // 设备登录、在线状态、消息等相关的事件通知
    // 注意事项：
    // 如下的这些notify回调函数，都是来自硬件SDK内部的一个线程，所以在这些回调函数内部的代码一定要注意线程安全问题
    // 比如在on_login_complete操作某个全局变量时，一定要考虑是不是您自己的线程也有可能操作这个变量
    TX_DEVICE_NOTIFY notify      = {0};
    notify.on_login_complete     = on_login_complete;
    notify.on_online_status      = on_online_status;
    notify.on_binder_list_change = on_binder_list_change;

    // SDK初始化目录，写入配置、Log输出等信息
    // 为了了解设备的运行状况，存在上传异常错误日志 到 服务器的必要
    // system_path：SDK会在该目录下写入保证正常运行必需的配置信息
    // system_path_capicity：是允许SDK在该目录下最多写入多少字节的数据（最小大小：10K，建议大小：100K）
    // app_path：用于保存运行中产生的log或者crash堆栈
    // app_path_capicity：同上，（最小大小：300K，建议大小：1M）
    // temp_path：可能会在该目录下写入临时文件
    // temp_path_capicity：这个参数实际没有用的，可以忽略
    TX_INIT_PATH init_path = {0};
    init_path.system_path = "./";
    init_path.system_path_capicity = 100 * 1024;
    init_path.app_path = "./";
    init_path.app_path_capicity = 1024 * 1024;
    init_path.temp_path = "./";
    init_path.temp_path_capicity = 10 * 1024;

    // 设置log输出函数，如果不想打印log，则无需设置。
    // 建议开发在开发调试阶段开启log，在产品发布的时候禁用log。
    tx_set_log_func(Util::log_func, 1, 1);

    // 初始化SDK，若初始化成功，则内部会启动一个线程去执行相关逻辑，该线程会持续运行，直到收到 exit 调用
    int ret = tx_init_device(&info, &notify, &init_path);
    if (err_null == ret) {
        printf(" >>> tx_init_device success\n");
    }
    else {
        printf(" >>> tx_init_device failed [%d]\n", ret);
        return false;
    }

    return true;
}

void CXWeiDevice::Uninit()
{
    m_objEngine.Uninit();
    tx_exit_device();
    return;
}

CXWeiAudioEngine& CXWeiDevice::GetAudioEngine()
{
    return m_objEngine;
}


CXWeiApp::CXWeiApp()
{
    
}

CXWeiApp::~CXWeiApp()
{
    m_objDevice.Uninit();
}

CXWeiApp& CXWeiApp::instance()
{
    static CXWeiApp app;
    return app;
}

CXWeiDevice& CXWeiApp::Device()
{
    return m_objDevice;
}

CXWeiAudioEngine& CXWeiApp::AudioEngine()
{
    return m_objDevice.GetAudioEngine();
}
