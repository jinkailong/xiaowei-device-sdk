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

#include "Util.h"
#include <stdio.h>

/**
 * 辅助函数: 从文件读取buffer
 * 这里用于读取 license 和 guid
 * 这样做的好处是不用频繁修改代码就可以更新license和guid
 */
bool Util::readBufferFromFile(const char *pPath, char *pBuffer, int nInSize, int *pSizeUsed) {
    if (!pPath || !pBuffer) {
        return false;
    }

    int uLen = 0;
    FILE * file = fopen(pPath, "rb");
    if (!file) {
        return false;
    }

    fseek(file, 0L, SEEK_END);
    uLen = ftell(file);
    fseek(file, 0L, SEEK_SET);

    if (0 == uLen || nInSize < uLen) {
        printf("invalide file or buffer size is too small...\n");
        fclose(file);
        return false;
    }

    *pSizeUsed = fread(pBuffer, 1, uLen, file);
    // bugfix: 0x0a is a lineend char, no use.
    if (pBuffer[uLen-1] == 0x0a)
    {
        *pSizeUsed = uLen - 1;
        pBuffer[uLen-1] = '\0';
    }

    printf("len:%d, ulen:%d\n",uLen, *pSizeUsed);
    fclose(file);
    return true;
}

/**
 * 辅助函数：SDK的log输出回调
 * SDK内部调用改log输出函数，有助于开发者调试程序
 * 回调函数参数说明：
 *         level   log级别 取值有 0 严重错误；1 错误；2 警告；3 提示；4 调试；5 冗余信息
 *         module  模块
 *         line    行号
 *         message log内容
 */
void Util::log_func(int level, const char* module, int line, const char* message)
{
    // 建议输出调试级别以上的日志
    if (level < 5) {
        printf("%s\n", message);
    }  
}
