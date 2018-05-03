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
package com.tencent.av;
import com.tencent.xiaowei.info.XWContactInfo;
import com.tencent.xiaowei.info.XWAudioFrameInfo;

interface IXWAVChatAIDLService
{
    long getSelfDin();
    boolean isContact(long uin);
    XWContactInfo getXWContactInfo(String uin);
    List getBinderList();
    byte[] getVideoChatSignature();
    void notifyVideoServiceStarted();
    void sendVideoCall(long peerUin, int uinType, in byte[] msg);
    void sendVideoCallM2M(long peerUin, int uinType, in byte[] msg);
    void sendVideoCMD(long peerUin, int uinType, in byte[] msg);
    void setVideoPID(int pid, String videoService);
    XWAudioFrameInfo readAudioData(int length);
    void startQQCallSkill(long uin);
    void cancelAIAudioRequest();
    int sendQQCallRequest(int uinType, long tinyId, in byte[] msg, int length);
    long getBindedQQUin();
    void statisticsPoint(String compassName, String event, String param, long time);
}