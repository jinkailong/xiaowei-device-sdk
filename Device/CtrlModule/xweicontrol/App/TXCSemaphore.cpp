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
#include "txctypedef.h"
#include "TXCSemaphore.hpp"

#include <stdio.h>
#include <string.h>
#include <cassert>
#include <sys/cdefs.h>

TXCSemaphore::TXCSemaphore()
{
#ifdef OS_MAC
    static volatile unsigned int sem_id = 0;
    psem_ = SEM_FAILED;

    while (SEM_FAILED == psem_)
    {
        unsigned int cur_id = ++sem_id;
        memset(sem_name_, 0, sizeof(sem_name_));
        sprintf(sem_name_, "_tmp_txcsem_%u", cur_id);
        psem_ = sem_open(sem_name_, O_CREAT | O_EXCL, S_IRUSR | S_IWUSR, 0);
    }
    assert(psem_ != SEM_FAILED);
#else
    psem_ = &sem_;
    int err = sem_init(psem_, 0, 0);
    assert(err == 0);
#endif
}
TXCSemaphore::~TXCSemaphore()
{
#ifdef OS_MAC
    if (SEM_FAILED != psem_)
    {
        sem_close(psem_);
        sem_unlink(sem_name_);
        psem_ = NULL;
    }
#else
    sem_destroy(psem_);
#endif
}
int TXCSemaphore::Wait()
{
    int err = -1;
    if (SEM_FAILED != psem_)
    {
        err = sem_wait(psem_);
    }
    //    assert(0 == err);
    return err;
}
int TXCSemaphore::Try()
{
    int err = -1;
    if (SEM_FAILED != psem_)
    {
        err = sem_trywait(psem_);
    }
    return err;
}
int TXCSemaphore::Post()
{
    int err = -1;
    if (SEM_FAILED != psem_)
    {
        err = sem_post(psem_);
    }
    assert(0 == err);
    return err;
}
