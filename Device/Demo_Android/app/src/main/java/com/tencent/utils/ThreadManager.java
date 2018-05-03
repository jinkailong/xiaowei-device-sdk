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
package com.tencent.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简单的线程池
 */
public class ThreadManager {
    static final int MAX_RUNNING_THREAD = 10;

    private static ThreadManager _instance;

    private ExecutorService executor;

    private ThreadManager(){
        try {
            executor = Executors.newFixedThreadPool(MAX_RUNNING_THREAD,	new NormalThreadFactory());
        } catch (Throwable t) {
            executor = Executors.newCachedThreadPool();
        }
    }

    public synchronized static ThreadManager getInstance(){
        if(_instance == null){
            _instance = new ThreadManager();
        }
        return _instance;
    }

    public void start(Runnable runnable){
        executor.submit(runnable);
    }

    public <T> Future<T> start(Callable<T> callable){
        return executor.submit(callable);
    }

    /**
     * 正常优先级的线程工厂
     */
    private static class NormalThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        NormalThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" +
                    poolNumber.getAndIncrement() +
                    "-temporary-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            return t;
        }
    };
}
