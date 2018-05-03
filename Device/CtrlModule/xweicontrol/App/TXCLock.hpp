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
#ifndef TXCLock_hpp
#define TXCLock_hpp

#include <pthread.h>

class TXCLock
{
  public:
    TXCLock();
    ~TXCLock();

    void Lock();
    void Unlock();

    bool Try(void);

  private:
    pthread_mutex_t mutex_;
};

class TXCAutoLock
{
  public:
    TXCAutoLock(TXCLock &lock);
    ~TXCAutoLock();

  private:
    TXCLock &lock_;
};

#endif /* TXCLock_hpp */
