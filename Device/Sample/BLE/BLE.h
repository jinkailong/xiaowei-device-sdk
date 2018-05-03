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
#ifndef __BLE_H__
#define __BLE_H__

#include <string>

CXX_EXTERN_BEGIN

/**
 * 蓝牙设备的状态
 */
enum ble_state {
    state_bond_none  = 10,// 未配对的设备
    state_bonding  = 11,// 配对中的设备
    state_bonded  = 12,// 已配对的设备
    state_connected  = 13,// 已连接的设备
    state_connecting  = 14,// 连接中的设备
};

/**
 * 通知给App的蓝牙结果
 */
typedef struct _ble_device_info
{
    /**
     * 蓝牙设备名字
     */
    const char* name;
    
    /**
     * 蓝牙设备地址
     */
    const char* address;
    
    /**
     * 蓝牙设备状态
     */
    unsigned int state;
    
    /**
     * 蓝牙设备支持的模式
     */
    unsigned int major;
}ble_device_info;


/**
 * 通知给App的蓝牙结果
 */
typedef struct _ble_result_info
{
    /**
     * 蓝牙事件
     */
	const char* event;
    
    /**
     * 查询结果 或者 操作结果
     */
	unsigned int result;
    
    /**
     * 相关的蓝牙设备 json格式
     */
	const char* device;
}ble_result_info;

/**
 * 通知App蓝牙已打开
 */
void on_ble_open();

/**
 * 通知App蓝牙已关闭
 */
void on_ble_close();

/**
 * 通知App蓝牙配对结果
 */
void on_ble_bond(ble_device_info* device);

/**
 * 通知App蓝牙配对失败或解除配对结果
 */
void on_ble_unbond(ble_device_info* device);

/**
 * 通知App蓝牙连接的相关事件
 * connected true表示device已经连接  false表示device断开连接
 */
void on_ble_connected(bool connected, ble_device_info* device);

/**
 * 通知App蓝牙扫描结果
 */
void on_ble_discovery(ble_device_info* device, unsigned int count);

/**
 * App控制设备蓝牙的相关事件
 */
typedef struct _tx_ai_ble_notify
{
    /**
     * 查询蓝牙是否打开
     */
	bool (*is_ble_open)();
    
    /**
     * 查询当前蓝牙已连接的设备，没有连接返回null
     */
    ble_device_info* (*on_get_current_connected_ble_device)();
    
    /**
     * 控制设备打开蓝牙
     */
	bool (*open_ble)();
    
    /**
     * 控制设备关闭蓝牙
     */
    bool (*close_ble)();
    
    /**
     * 控制设备扫描蓝牙
     */
    bool (*start_discovery)();
    
    /**
     * 控制设备蓝牙配对 成功后调用on_ble_bond回传结果，失败后调用on_ble_unbond回传结果
     */
    bool (*bond)(const char* address);
    
    /**
     * 控制设备蓝牙解除配对 成功后调用on_ble_unbond回传结果
     */
    bool (*un_bond)(const char* address);
    
    /**
     * 控制设备蓝牙连接设备 成功后调用on_ble_connected(true,device)回传结果，失败后调用on_ble_connected(false,device)回传结果
     */
    bool (*connect)(const char* address);

}ble_notify;

void config_ble_notify(ble_notify* callback);
///////////////////////////////////////////////////

CXX_EXTERN_END

#endif // __BLE_H__
