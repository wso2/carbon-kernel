/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.base.threads.watermark;

import org.apache.axis2.transport.base.threads.ThreadCleanupContainer;

import java.util.concurrent.*;

/**
 * An {@link ExecutorService} that executes each submitted task using
 * one of possibly several pooled threads, but the execution happens differently
 * from the {@link ThreadPoolExecutor}. In this executor after all the core pool threads
 * are used queuing happens until the water mark. If the more tasks are submitted after
 * the queue is filled up to the water mark the number of threads increases to max.
 * If the number of tasks continue to increase the Queue begins to fill up. If the queue
 * is a bounded queue and the queue is completely filled a {@link RejectedExecutionHandler}
 * is executed if one specified. Otherwise the task is rejected.
 */
public class WaterMarkExecutor extends ThreadPoolExecutor {
    public WaterMarkExecutor(int core, int max, long keepAlive,
                             TimeUnit timeUnit, WaterMarkQueue<Runnable> queue) {
        super(core, max, keepAlive, timeUnit, queue, new WaterMarkRejectionHandler(null));
    }

    public WaterMarkExecutor(int core, int max, long keepAlive,
                             TimeUnit timeUnit, WaterMarkQueue<Runnable> queue,
                             ThreadFactory threadFactory) {
        super(core, max, keepAlive,
                timeUnit, queue, threadFactory, new WaterMarkRejectionHandler(null));
    }

    public WaterMarkExecutor(int core, int max,
                             long keepAlive, TimeUnit timeUnit,
                             WaterMarkQueue<Runnable> queue,
                             RejectedExecutionHandler rejectedExecutionHandler) {

        super(core, max, keepAlive, timeUnit,
                queue, new WaterMarkRejectionHandler(rejectedExecutionHandler));
    }

    public WaterMarkExecutor(int core, int max, long keepAlive,
                             TimeUnit timeUnit, WaterMarkQueue<Runnable> queue,
                             ThreadFactory threadFactory,
                             RejectedExecutionHandler rejectedExecutionHandler) {
        super(core, max, keepAlive, timeUnit,
              queue, threadFactory, new WaterMarkRejectionHandler(rejectedExecutionHandler));
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        ThreadCleanupContainer.cleanupAll();
    }
}
