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
#include <jni.h>
#include <android/log.h>

#include "XWeiJNIDef.h"
#include "OpusDecoder.h"
#include "opus.h"

#include <map>
#include <string.h>

COpusDecoder::COpusDecoder(size_t sample, size_t channel, size_t max_frames)
        : sample_(sample), channel_(channel), max_frames_(max_frames), decoder_(NULL),
          opus_buffer_(NULL), pcm_buffer_(NULL) {

}

COpusDecoder::~COpusDecoder() {
    if (decoder_)
        opus_decoder_destroy(decoder_);
    if (opus_buffer_)
        delete[] opus_buffer_;
    if (pcm_buffer_)
        delete[] pcm_buffer_;
}

size_t COpusDecoder::Decode(const unsigned char *data, int data_len, char *outPcm) {

    if (data == NULL) {
        return -1;
    }
    if (outPcm == NULL) {
        return -1;
    }
    int total_frames = 0;
    if (!decoder_) {
        int err = 0;
        decoder_ = opus_decoder_create(sample_, channel_, &err);
    }
    if (!opus_buffer_) {
        opus_buffer_ = new opus_int16[max_frames_ * channel_];
    }
    if (!pcm_buffer_) {
        pcm_buffer_ = new char[max_frames_ * channel_ * 2];
    }
    int outLength = 0;

    if (decoder_ && opus_buffer_ && pcm_buffer_) {

        int frames = 0;
        do {
            frames = opus_decode(decoder_, data, data_len, opus_buffer_, max_frames_, 0);
            if (frames <= 0) {
                __android_log_print(ANDROID_LOG_ERROR, LOGFILTER,
                                    "COpusDecoder::onTTSPush opus decode error=%d, origin len=%d",
                                    frames, data_len);
                break;
            }
            for (int i = 0; i < frames; i++) {
                pcm_buffer_[i * 2] = (char) ((opus_buffer_[i]) & 0xFF);
                pcm_buffer_[i * 2 + 1] = (char) ((opus_buffer_[i] >> 8) & 0xFF);
            }

            total_frames += frames;
            int length = frames * channel_ * 2;
            memcpy(outPcm + outLength, pcm_buffer_, length);
            outLength += length;
        } while (frames == max_frames_);
    }

    return outLength;
}


class CDecoderMgr {
public:
    bool Start(int id, int sample, int channel, int max_frames);

    int Decode(int id, const unsigned char *data, int data_length, char *outPcm);

    bool Stop(int id);

private:
    std::map<int, COpusDecoder *> map_;
};

bool CDecoderMgr::Start(int id, int sample, int channel, int max_frames) {
    std::map<int, COpusDecoder *>::iterator itr = map_.find(id);
    if (map_.end() != itr) {
        delete (itr->second);
    }
    COpusDecoder *decoder = new COpusDecoder(sample, channel, max_frames);
    map_[id] = decoder;

    return true;
}

int CDecoderMgr::Decode(int id, const unsigned char *data, int data_length, char *outPcm) {

    int result = 0;
    std::map<int, COpusDecoder *>::iterator itr = map_.find(id);
    if (map_.end() != itr) {
        COpusDecoder *decoder = itr->second;
        if (decoder) {
            result = decoder->Decode(data, data_length, outPcm);
        }
    }

    return result;
}

bool CDecoderMgr::Stop(int id) {
    bool result = false;
    std::map<int, COpusDecoder *>::iterator itr = map_.find(id);
    if (map_.end() != itr) {
        COpusDecoder *decoder = itr->second;
        if (decoder) {
            delete decoder;
        }

        map_.erase(itr);
        result = true;
    }

    return result;
}

CDecoderMgr g_decoderMgr;
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL
Java_com_tencent_xiaowei_control_OpusDecoder_init(JNIEnv *env, jclass service, jint id, jint sample,
                                          jint channel) {
    g_decoderMgr.Start(id, sample, channel, 960 * 6);
}

JNIEXPORT jbyteArray JNICALL
Java_com_tencent_xiaowei_control_OpusDecoder_decoder(JNIEnv *env, jclass service, jint id,
                                             jbyteArray data) {
    int nBufLen = 0;
    unsigned char *pBuffer = NULL;
    char *pOutBuffer = NULL;
    if (NULL != data) {
        nBufLen = env->GetArrayLength(data);
        if (nBufLen > 0) {
            pBuffer = new unsigned char[nBufLen];
            memset(pBuffer, 0, nBufLen);
            env->GetByteArrayRegion(data, 0, nBufLen, (jbyte *) pBuffer);
            pOutBuffer = new char[960 * 6 * 2];
            memset(pOutBuffer, 0, 960 * 6 * 2);
            int length = g_decoderMgr.Decode(id, reinterpret_cast<const unsigned char *>(pBuffer),
                                             nBufLen, pOutBuffer);
            jbyteArray jbuf = env->NewByteArray(length);
            env->SetByteArrayRegion(jbuf, 0, length, (jbyte *) pOutBuffer);

            delete[]pOutBuffer;
            delete[]pBuffer;
            return jbuf;
        }
    }
    return NULL;
}

JNIEXPORT void JNICALL
Java_com_tencent_xiaowei_control_OpusDecoder_unInit(JNIEnv *env, jclass service, jint id) {
    g_decoderMgr.Stop(id);
}

#ifdef __cplusplus
}
#endif
