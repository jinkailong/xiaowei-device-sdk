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

#include "CmdManager.h"
#include "XWeiDevice.h"


bool CmdManager::ParseCmd(std::string& strCmd)
{
    if(strCmd == "quit" || strCmd == "QUIT" || strCmd == "q" || strCmd == "Q") {
        return false;
    }
    if(strCmd == "w" || strCmd == "W") {
        CXWeiApp::instance().AudioEngine().OnWakeup();
    }
    if(strCmd == "c" || strCmd == "C") {
        CXWeiApp::instance().AudioEngine().OnCancel();
    }
    if(strCmd == "t" || strCmd == "T") {
        CXWeiApp::instance().AudioEngine().OnTextQuery();
    }
    return true;
}
