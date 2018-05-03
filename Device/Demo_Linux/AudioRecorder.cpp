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

#include "AudioRecorder.h"
#define ALSA_PCM_NEW_HW_PARAMS_API
#include <alsa/asoundlib.h>

CAudioRecorder::CAudioRecorder(IAudioRecorderDataRecver *pRecv)
    : m_pRecv(pRecv)
{
}

CAudioRecorder::~CAudioRecorder()
{
}

bool CAudioRecorder::ReadData(int recordTime)
{
    if (NULL == m_pRecv)
    {
        return false;
    }
    if (recordTime <= 0)
    {
        return false;
    }

    long loops;
    int rc;
    int size;
    snd_pcm_t *handle;
    snd_pcm_hw_params_t *params;
    unsigned int val;
    int dir;
    snd_pcm_uframes_t frames;
    char *buffer;

    rc = snd_pcm_open(&handle, "default", SND_PCM_STREAM_CAPTURE, 0);
    if (rc < 0)
    {
        fprintf(stderr, "unable to open pcm device: %s\n", snd_strerror(rc));
        return false;
    }
    /* Allocate a hardware parameters object. */
    snd_pcm_hw_params_alloca(&params);
    /* Fill it in with default values. */
    snd_pcm_hw_params_any(handle, params);
    /* Set the desired hardware parameters. */
    /* Interleaved mode */
    snd_pcm_hw_params_set_access(handle, params, SND_PCM_ACCESS_RW_INTERLEAVED);
    /* Signed 16-bit little-endian format */
    snd_pcm_hw_params_set_format(handle, params, SND_PCM_FORMAT_S16_LE);
    /* Two channels (stereo) */
    snd_pcm_hw_params_set_channels(handle, params, 1);
    /* 44100 bits/second sampling rate (CD quality) */
    val = 16000;
    snd_pcm_hw_params_set_rate_near(handle, params, &val, &dir);
    /* Set period size to 32 frames. */
    frames = 320;
    snd_pcm_hw_params_set_period_size_near(handle, params, &frames, &dir);
    /* Write the parameters to the driver */
    rc = snd_pcm_hw_params(handle, params);
    if (rc < 0)
    {
        fprintf(stderr, "unable to set hw parameters: %s\n", snd_strerror(rc));
        return false;
    }
    /* Use a buffer large enough to hold one period */
    snd_pcm_hw_params_get_period_size(params, &frames, &dir);
    size = frames * 2; /* 2 bytes/sample, 2 channels */
    buffer = (char *)malloc(size);
    /* We want to loop for 5 seconds */
    snd_pcm_hw_params_get_period_time(params, &val, &dir);
    loops = recordTime / val;

    printf("CAudioRecoder::ReadData begin\n");
    while (loops > 0)
    {
        loops--;
        rc = snd_pcm_readi(handle, buffer, frames);
        if (rc == -EPIPE)
        {
            /* EPIPE means overrun */
            fprintf(stderr, "overrun occurred\n");
            snd_pcm_prepare(handle);
        }
        else if (rc < 0)
        {
            fprintf(stderr, "error from read: %s\n", snd_strerror(rc));
        }
        else if (rc != (int)frames)
        {
            fprintf(stderr, "short read, read %d frames\n", rc);
        }
        //printf("CAudioRecoder::ReadData snd_pcm_readi, size %d rc %d\n", size, rc);
        if (m_pRecv)
        {
            if (m_pRecv->IsStopRecorder())
            {
                m_pRecv->OnRecorderData((char *)buffer, (size_t)size);
                break;
            }
            else
            {
                m_pRecv->OnRecorderData((char *)buffer, (size_t)size);
            }
        }
    }
    printf("CAudioRecoder::ReadData end\n");

    snd_pcm_drain(handle);
    snd_pcm_close(handle);
    free(buffer);

    return true;
}
