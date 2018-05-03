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
#ifndef RespnseHolder_hpp
#define RespnseHolder_hpp
#include <string>
#include <vector>
#include "ThirdLib.h"
#include "AutoBuffer.hpp"

typedef struct _txca_param_response TXCA_PARAM_RESPONSE;
typedef struct _txca_param_res_group TXCA_PARAM_RES_GROUP;
typedef struct _txca_param_resource TXCA_PARAM_RESOURCE;

class CResponseHolder
{
  public:
    //  swap data
    void Swap(CResponseHolder &other);

    void Copy(const TXCA_PARAM_RESPONSE *response);

    TXCA_PARAM_RESPONSE &response();

  private:
    void Free();

    tpl::shared_ptr<TXCA_PARAM_RESPONSE> respex_;

    tpl::shared_ptr<CAutoBuffer<TXCA_PARAM_RES_GROUP> > resource_groups_;
    typedef tpl::shared_ptr<CAutoBuffer<TXCA_PARAM_RESOURCE> > ResourcesPtr;
    std::vector<ResourcesPtr> vec_resources_;
    tpl::shared_ptr<CStringHolder> res_holder_;
};

#endif /* RespnseHolder_hpp */
