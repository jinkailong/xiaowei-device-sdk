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
#include <stdlib.h>

typedef int16_t opus_int16;
struct OpusDecoder;

class COpusDecoder {
public:
    COpusDecoder(size_t sample, size_t channel, size_t max_frames = 960 * 6);

    ~COpusDecoder();

    size_t Decode(const unsigned char *data, int data_len, char *outPcm);

private:
    size_t sample_;
    size_t channel_;
    size_t max_frames_;

    OpusDecoder *decoder_;
    opus_int16 *opus_buffer_;
    char *pcm_buffer_;
};
