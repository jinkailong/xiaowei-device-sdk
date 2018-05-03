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
#include "BLE.h"
#include "TXCAudioMsg.h"
#include "json.h"

#include <sstream>

unsigned long long m_id = 0;

std::string ConvertBLEInfoToJson(ble_device_info* info)
{
    std::string strBLE = "";
    
    json::Object jsonValue;
    
    if(info->name !=NULL) {
        jsonValue["name"] = info->name;
    }
    if(info->address !=NULL) {
        jsonValue["address"] = info->address;
    }
    jsonValue["state"] = (int)info->state;
    jsonValue["major"] = (int)info->major;
    
    strBLE = json::Serialize(jsonValue);
    
    return strBLE;
}

std::string ConvertBLEInfoListToJson(ble_device_info* info, unsigned int count)
{
    std::string strBLE = "";
    json::Array devices;
    for(int i = 0;i<count;i++)
    {
        json::Object jsonValue;
        
        if(info[i].name !=NULL) {
            jsonValue["name"] = info[i].name;
        }
        if(info[i].address !=NULL) {
            jsonValue["address"] = info[i].address;
        }
        jsonValue["state"] = (int)info[i].state;
        jsonValue["major"] = (int)info[i].major;
        devices.push_back(jsonValue);
    }
    strBLE = json::Serialize(devices);
    return strBLE;
}

// ble_result_info
void send_ble_result_2_app(std::string event, unsigned int result, std::string device)
{
    if(m_id == 0) {
        return;
    }
    std::string msg ;
    json::Object jsonValue;
    
    jsonValue["event"] = event;
    jsonValue["result"] = (int)result;
    jsonValue["time"] = eventTime;
    if(device.length()>0) {
        jsonValue["device"] = device;
    }
    
    msg = json::Serialize(jsonValue);
    
    log_notice("send_ble_result_2_app %s",msg.c_str());
    unsigned int cookie;
    
    TXCA_PARAM_CC_MSG cc_msg = {0};
    cc_msg.business_name = "蓝牙";
    cc_msg.msg = msg.c_str();
    cc_msg.msg_len = msg.length();
    txca_send_c2c_msg(m_id, &cc_msg, &cookie);
}


void on_ble_open()
{
    send_ble_result_2_app("BLE_OPEN", 0, "");
}

void on_ble_close()
{
    send_ble_result_2_app("BLE_CLOSE", 0, "");
}

void on_ble_bond(ble_device_info* device)
{
    send_ble_result_2_app("BLE_BOND", 0, ConvertBLEInfoToJson(device));
}

void on_ble_unbond(ble_device_info* device)
{
    send_ble_result_2_app("BLE_UNBOND", 0, ConvertBLEInfoToJson(device));
}

void on_ble_connected(bool connected, ble_device_info* device)
{
    send_ble_result_2_app("BLE_CONNECT", connected ? 0 : 1, ConvertBLEInfoToJson(device));
}

void on_ble_discovery(ble_device_info* device, unsigned int count)
{
    send_ble_result_2_app("BLE_DISCOVERY", 0, ConvertBLEInfoListToJson(device, count));
}



ble_notify m_callback;

// 在 on_cc_msg_notify 收到消息的时候，根据tx_ai_cc_msg的 business_name 分发，如果是"蓝牙"，将其放到这里处理
void on_cc_msg(unsigned long long from, const char* msg, unsigned int msg_len){
    m_id = from;
    std::string ble_event = msg;
    json::Value jsonValue = json::Deserialize(ble_event);
    json::Object jsonObject = jsonValue.ToObject();
    std::string event;
    std::string address;
    Util::JsonUtil::JsonGetString(event, jsonObject, "event");
    Util::JsonUtil::JsonGetString(address, jsonObject, "address");
    
    if(event == "BLE_OPEN") {
        if(m_callback.open_ble){
            bool ret = m_callback.open_ble();
            if(!ret) {
                send_ble_result_2_app(event, 1, "");
            }
        }
    } else if(event == "BLE_CLOSE") {
        if(m_callback.close_ble){
            bool ret = m_callback.close_ble();
            if(!ret) {
                send_ble_result_2_app(event, 1, "");
            }
        }
    } else if(event == "BLE_GET_STATE") {
        if(m_callback.is_ble_open && m_callback.on_get_current_connected_ble_device){
            bool open = m_callback.is_ble_open();
            ble_device_info* device = m_callback.on_get_current_connected_ble_device();
            send_ble_result_2_app(event, open ? 0 : 1, ConvertBLEInfoToJson(device));
        }
    } else if(event == "BLE_DISCOVERY") {
        if(m_callback.start_discovery){
            bool ret = m_callback.start_discovery();
            if(!ret) {
                send_ble_result_2_app(event, 1, "");
            }
        }
    } else if(event == "BLE_BOND") {
        if(m_callback.bond){
            bool ret = m_callback.bond(address.c_str());
            if(!ret) {
                ble_device_info device = {0};
                device.address = address.c_str();
                send_ble_result_2_app(event, 1, ConvertBLEInfoToJson(&device));
            }
        }
    } else if(event == "BLE_UNBOND") {
        if(m_callback.un_bond){
            bool ret = m_callback.un_bond(address.c_str());
            if(!ret) {
                ble_device_info device = {0};
                device.address = address.c_str();
                send_ble_result_2_app(event, 1, ConvertBLEInfoToJson(&device));
            }
        }
    } else if(event == "BLE_CONNECT") {
        if(m_callback.connect){
            bool ret = m_callback.connect(address.c_str());
            if(!ret) {
                ble_device_info device = {0};
                device.address = address.c_str();
                send_ble_result_2_app(event, 1, ConvertBLEInfoToJson(&device));
            }
        }
    }
}

void config_ble_notify(ble_notify* callback)
{
    if(callback != NULL){
        memcpy(&m_callback, callback, sizeof(m_callback));
    } else {
        memset(&m_callback, 0, sizeof(m_callback));
    }
}
