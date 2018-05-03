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
#include "ResponseHolder.hpp"
#include "TXCAudioType.h"
#include <memory.h>

#include "logger.h"

void CResponseHolder::Copy(const TXCA_PARAM_RESPONSE *response)
{
    if (response)
    {
        respex_.reset(new TXCA_PARAM_RESPONSE);
        res_holder_.reset(new CStringHolder);
        memcpy(respex_.get(), response, sizeof(TXCA_PARAM_RESPONSE));

        {
            // response_.*
            respex_->request_text = res_holder_->Hold(response->request_text);
            respex_->response_data = res_holder_->Hold(response->response_data);
        }

        {
            // response_->skill_info
            respex_->skill_info.name = res_holder_->Hold(response->skill_info.name);
            respex_->skill_info.id = res_holder_->Hold(response->skill_info.id);
        }
        
        {
            // response_->last_skill_info
            respex_->last_skill_info.name = res_holder_->Hold(response->last_skill_info.name);
            respex_->last_skill_info.id = res_holder_->Hold(response->last_skill_info.id);
        }

        {
            //  resp_this.context
            respex_->context.id = res_holder_->Hold(response->context.id);
        }
        respex_->is_notify = response->is_notify;

        {
            if (response->resource_groups && response->resource_groups_size > 0)
            {
                resource_groups_.reset(new CAutoBuffer<TXCA_PARAM_RES_GROUP>(response->resource_groups_size));
                respex_->resource_groups = resource_groups_->Get();
                memset(respex_->resource_groups, 0, sizeof(TXCA_PARAM_RES_GROUP) * response->resource_groups_size);
                for (size_t i = 0; i < response->resource_groups_size; ++i)
                {
                    TXCA_PARAM_RES_GROUP *orgGroup = response->resource_groups + i;
                    TXCA_PARAM_RES_GROUP &tmpGroup = (*resource_groups_)[i];

                    ResourcesPtr resources_;
                    resources_.reset(new CAutoBuffer<TXCA_PARAM_RESOURCE>(response->resource_groups[i].resources_size));

                    tmpGroup.resources = resources_->Get();
                    tmpGroup.resources_size = response->resource_groups[i].resources_size;
                    memset(tmpGroup.resources, 0, sizeof(TXCA_PARAM_RESOURCE) * tmpGroup.resources_size);

                    for (size_t j = 0; j < response->resource_groups[i].resources_size; j++)
                    {
                        TXCA_PARAM_RESOURCE *orgResource = orgGroup->resources + j;
                        TXCA_PARAM_RESOURCE &tmpResource = (*resources_)[j];

                        tmpResource.format = orgResource->format;
                        tmpResource.offset = orgResource->offset;
                        tmpResource.play_count = orgResource->play_count;

                        if (orgResource->id && orgResource->id[0])
                        {
                            tmpResource.id = const_cast<char *>(res_holder_->Hold(orgResource->id));
                        }
                        if (orgResource->content && orgResource->content[0])
                        {
                            tmpResource.content = const_cast<char *>(res_holder_->Hold(orgResource->content));
                        }
                        if (orgResource->extend_buffer && orgResource->extend_buffer[0])
                        {
                            tmpResource.extend_buffer = const_cast<char *>(res_holder_->Hold(orgResource->extend_buffer));
                        }
                    }

                    vec_resources_.push_back(resources_);
                }
            }
            else
            {
                respex_->resource_groups = NULL;
                respex_->resource_groups_size = 0;
            }
        }
    }
}

TXCA_PARAM_RESPONSE &CResponseHolder::response()
{
    return *(respex_.get());
}

void CResponseHolder::Swap(CResponseHolder &other)
{
    respex_.swap(other.respex_);
    resource_groups_.swap(other.resource_groups_);
    vec_resources_.swap(other.vec_resources_);
}
