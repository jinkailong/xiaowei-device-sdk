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

#pragma once

namespace Util {
    /**
     * 辅助函数: 从文件读取buffer
     * 这里用于读取 license 和 guid
     * 这样做的好处是不用频繁修改代码就可以更新license和guid
     */

    bool readBufferFromFile(const char *pPath, char *pBuffer, int nInSize, int *pSizeUsed);

    /**
     * 辅助函数：SDK的log输出回调
     * SDK内部调用改log输出函数，有助于开发者调试程序
     */
    void log_func(int level, const char* module, int line, const char* message);
};
