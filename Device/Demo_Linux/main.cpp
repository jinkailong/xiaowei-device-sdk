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
#include "CmdManager.h"
#include <stdio.h>
#include <string.h>
#include <unistd.h>


/****************************************************************
*  测试代码：
*
*  （1）while循环的作用仅仅是使 Demo进程不会退出，实际使用SDK时一般不需要
*
*  （2） 输入 "quit" 将会退出当前进程，这段逻辑存在的原因在于：
*                       在某些芯片上，直接用Ctrl+C 退出易产生僵尸进程
*
*  （3）while循环里面的sleep(1)在这里是必须的，因为如果Demo进程后台运行，scanf没有阻塞作用，会导致当前线程跑满CPU
*
*****************************************************************/
int main(int argc, char* argv[])
{
    if ( !CXWeiApp::instance().Device().Init() ) {
        return -1;
    }
    
    char input[100];
    while (scanf("%s", input)) {
        std::string strCmd = input;
        if (!CmdManager::ParseCmd(strCmd)) {
            CXWeiApp::instance().Device().Uninit();
            printf(">>quit\n");
            break;
        }
        sleep(1);
    }
    
    return 0;
}
