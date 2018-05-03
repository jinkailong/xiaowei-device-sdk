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

#ifndef MediaFile_hpp
#define MediaFile_hpp

#include "Media.hpp"

//本地文件类型的Media
class CMediaFile : public CMedia
{
public:
    void Init(MEDIA_TYPE type, const char* content);
    
    int Read(_Out_ const void **data, _Out_ size_t *data_size, _In_ size_t offset);
    
protected:
    CMediaFile(const std::string& res_id);
    
private:
    friend class TXCMediaCenter;
    
private:
    bool inited_;
};

#endif /* MediaFile_hpp */
