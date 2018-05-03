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
#ifndef AsyncTask_h
#define AsyncTask_h

//  async task
#define TXC_ASYN_TASK(CCLASS, F, ARG1_TYPE, ARG1_NAME)                    \
    struct tag_as_##CCLASS##F##_AsynTask_ : public TXCTask                \
    {                                                                     \
        tag_as_##CCLASS##F##_AsynTask_(CCLASS *obj, ARG1_TYPE &ARG1_NAME) \
            : __##ARG1_NAME(ARG1_NAME), __obj(obj){};                     \
        virtual void Run()                                                \
        {                                                                 \
            if (__obj)                                                    \
                __obj->F(__##ARG1_NAME);                                  \
        };                                                                \
        ARG1_TYPE __##ARG1_NAME;                                          \
        CCLASS *__obj;                                                    \
    };

#define TXC_ASYN_CALL(CCLASS, F, ARG1_NAME)                                                               \
    if (current_thread_id() != thread_id_)                                                                \
    {                                                                                                     \
        tag_as_##CCLASS##F##_AsynTask_ *_asyn_task = new tag_as_##CCLASS##F##_AsynTask_(this, ARG1_NAME); \
        TXCAutoPtr<TXCTask> spTask(_asyn_task);                                                           \
        PushTask(spTask);                                                                                 \
        return true;                                                                                      \
    }

#define TXC_ASYN_CALL_NR(CCLASS, F, ARG1_NAME)                                                            \
    if (current_thread_id() != thread_id_)                                                                \
    {                                                                                                     \
        tag_as_##CCLASS##F##_AsynTask_ *_asyn_task = new tag_as_##CCLASS##F##_AsynTask_(this, ARG1_NAME); \
        TXCAutoPtr<TXCTask> spTask(_asyn_task);                                                           \
        PushTask(spTask);                                                                                 \
        return;                                                                                           \
    }

//  sync task
#define TXC_SYNC_TASK4(CCLASS, F, RT, ARG1_TYPE, ARG1_NAME, ARG2_TYPE, ARG2_NAME, ARG3_TYPE, ARG3_NAME, ARG4_TYPE, ARG4_NAME)           \
    struct tag_as_##CCLASS##F##_AsynTask_ : public TXCTask                                                                              \
    {                                                                                                                                   \
        tag_as_##CCLASS##F##_AsynTask_(CCLASS *obj, ARG1_TYPE ARG1_NAME, ARG2_TYPE ARG2_NAME, ARG3_TYPE ARG3_NAME, ARG4_TYPE ARG4_NAME) \
            : __##ARG1_NAME(ARG1_NAME), __##ARG2_NAME(ARG2_NAME), __##ARG3_NAME(ARG3_NAME), __##ARG4_NAME(ARG4_NAME), __obj(obj){};     \
        virtual void Run()                                                                                                              \
        {                                                                                                                               \
            if (__obj)                                                                                                                  \
                __result = __obj->F(__##ARG1_NAME, __##ARG2_NAME, __##ARG3_NAME, __##ARG4_NAME);                                        \
            __sync__semaphore.Post();                                                                                                   \
            __sync__semaphore.Post();                                                                                                   \
        };                                                                                                                              \
        ARG1_TYPE __##ARG1_NAME;                                                                                                        \
        ARG2_TYPE __##ARG2_NAME;                                                                                                        \
        ARG3_TYPE __##ARG3_NAME;                                                                                                        \
        ARG4_TYPE __##ARG4_NAME;                                                                                                        \
        RT __result;                                                                                                                    \
        CCLASS *__obj;                                                                                                                  \
        TXCSemaphore __sync__semaphore;                                                                                                 \
    };

#define TXC_SYNC_CALL4(CCLASS, F, ARG1_NAME, ARG2_NAME, ARG3_NAME, ARG4_NAME)                                                              \
    if (current_thread_id() != thread_id_)                                                                                                 \
    {                                                                                                                                      \
        tag_as_##CCLASS##F##_AsynTask_ *_asyn_task = new tag_as_##CCLASS##F##_AsynTask_(this, ARG1_NAME, ARG2_NAME, ARG3_NAME, ARG4_NAME); \
        TXCAutoPtr<TXCTask> spTask(_asyn_task);                                                                                            \
        PushTask(spTask);                                                                                                                  \
        _asyn_task->__sync__semaphore.Wait();                                                                                              \
        return _asyn_task->__result;                                                                                                       \
    }

#define TXC_ASYN_PUSH_TASK(CCLASS, F, ARG1_NAME, ARG2_NAME, ARG3_NAME, ARG4_NAME)                                                          \
    {                                                                                                                                      \
        tag_as_##CCLASS##F##_AsynTask_ *_asyn_task = new tag_as_##CCLASS##F##_AsynTask_(this, ARG1_NAME, ARG2_NAME, ARG3_NAME, ARG4_NAME); \
        TXCAutoPtr<TXCTask> spTask(_asyn_task);                                                                                            \
        PushTask(spTask);                                                                                                                  \
        return true;                                                                                                                       \
    }
#define TXC_ASYN_CALL4(CCLASS, F, ARG1_NAME, ARG2_NAME, ARG3_NAME, ARG4_NAME) \
    if (current_thread_id() != thread_id_)                                    \
    TXC_ASYN_PUSH_TASK(CCLASS, F, ARG1_NAME, ARG2_NAME, ARG3_NAME, ARG4_NAME)

struct TXCTask
{
    //public:
    virtual ~TXCTask(){};

    virtual void Run() = 0;
};

#endif /* AsyncTask_h */
