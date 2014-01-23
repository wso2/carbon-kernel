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

package org.apache.axis2.transport.base.threads;

import org.apache.axis2.transport.base.threads.watermark.DefaultWaterMarkQueue;
import org.apache.axis2.transport.base.threads.watermark.WaterMarkExecutor;
import org.apache.axis2.transport.base.threads.watermark.WaterMarkQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.*;

/**
 * Worker pool implementation based on java.util.concurrent in JDK 1.5 or later.
 */
public class NativeWorkerPool implements WorkerPool {

    static final Log log = LogFactory.getLog(NativeWorkerPool.class);

    private final ThreadPoolExecutor executor;
    private final BlockingQueue<Runnable> blockingQueue;

    public NativeWorkerPool(int core, int max, int keepAlive,
        int queueLength, String threadGroupName, String threadGroupId) {

        if (log.isDebugEnabled()) {
            log.debug("Using native util.concurrent package..");
        }
        blockingQueue =
            (queueLength == -1 ? new LinkedBlockingQueue<Runnable>()
                               : new LinkedBlockingQueue<Runnable>(queueLength));
        executor = new Axis2ThreadPoolExecutor(core, max, keepAlive, TimeUnit.SECONDS,
                                               blockingQueue,
                                               new NativeThreadFactory(new ThreadGroup(threadGroupName),
                                                                       threadGroupId));
    }

    public NativeWorkerPool(int core, int max, int keepAlive,
                            int queueLength, String threadGroupName,
                            String threadGroupId, BlockingQueue<Runnable> queue) {

        if (log.isDebugEnabled()) {
            log.debug("Using native util.concurrent package..");
        }

        if (queue == null) {
            blockingQueue =
                    (queueLength == -1 ? new LinkedBlockingQueue<Runnable>()
                            : new LinkedBlockingQueue<Runnable>(queueLength));
        } else {
            blockingQueue = queue;
        }

        executor = new Axis2ThreadPoolExecutor(
                core, max, keepAlive,
                TimeUnit.SECONDS,
                blockingQueue,
                new NativeThreadFactory(new ThreadGroup(threadGroupName), threadGroupId));
    }

    public NativeWorkerPool(int core, int max, int keepAlive,
                            int queueLength, String threadGroupName,
                            String threadGroupId, BlockingQueue<Runnable> queue,
                            RejectedExecutionHandler rejectedExecutionHandler) {

        if (log.isDebugEnabled()) {
            log.debug("Using native util.concurrent package..");
        }

        if (queue == null) {
            blockingQueue =
                    (queueLength == -1 ? new LinkedBlockingQueue<Runnable>()
                            : new LinkedBlockingQueue<Runnable>(queueLength));
        } else {
            blockingQueue = queue;
        }

        executor = new Axis2ThreadPoolExecutor(
                core, max, keepAlive,
                TimeUnit.SECONDS,
                blockingQueue,
                new NativeThreadFactory(new ThreadGroup(threadGroupName), threadGroupId),
                rejectedExecutionHandler);
    }

    public NativeWorkerPool(int core, int max, int keepAlive,
                            int queueLength, int waterMark, String threadGroupName,
                            String threadGroupId) {

        if (log.isDebugEnabled()) {
            log.debug("Using native util.concurrent package..");
        }


        blockingQueue =
                (queueLength == -1 ? new DefaultWaterMarkQueue<Runnable>(waterMark)
                        : new DefaultWaterMarkQueue<Runnable>(waterMark, queueLength));

        executor = new WaterMarkExecutor(
                core, max, keepAlive,
                TimeUnit.SECONDS,
                (WaterMarkQueue<Runnable>) blockingQueue,
                new NativeThreadFactory(new ThreadGroup(threadGroupName), threadGroupId));
    }

    public NativeWorkerPool(int core, int max, int keepAlive,
                            int queueLength, int waterMark, String threadGroupName,
                            String threadGroupId, WaterMarkQueue<Runnable> queue) {

        if (log.isDebugEnabled()) {
            log.debug("Using native util.concurrent package..");
        }

        if (queue == null) {
            blockingQueue =
                    (queueLength == -1 ? new DefaultWaterMarkQueue<Runnable>(waterMark)
                            : new DefaultWaterMarkQueue<Runnable>(waterMark, queueLength));
        } else {
            blockingQueue = queue;
        }

        executor = new WaterMarkExecutor(
                core, max, keepAlive,
                TimeUnit.SECONDS,
                (WaterMarkQueue<Runnable>) blockingQueue,
                new NativeThreadFactory(new ThreadGroup(threadGroupName), threadGroupId));
    }

    public NativeWorkerPool(int core, int max, int keepAlive,
                            int queueLength, int waterMark, String threadGroupName,
                            String threadGroupId,
                            RejectedExecutionHandler rejectedExecutionHandler) {

        if (log.isDebugEnabled()) {
            log.debug("Using native util.concurrent package..");
        }


        blockingQueue =
                (queueLength == -1 ? new DefaultWaterMarkQueue<Runnable>(waterMark)
                        : new DefaultWaterMarkQueue<Runnable>(waterMark, queueLength));

        executor = new WaterMarkExecutor(
                core, max, keepAlive,
                TimeUnit.SECONDS,
                (WaterMarkQueue<Runnable>) blockingQueue,
                new NativeThreadFactory(new ThreadGroup(threadGroupName), threadGroupId),
                rejectedExecutionHandler);
    }

    public void execute(final Runnable task) {
        executor.execute(new Runnable() {
            public void run() {
                try {
                    task.run();
                } catch (Throwable t) {
                    log.error("Uncaught exception", t);
                }
            }
        });
    }

    public int getActiveCount() {
        return executor.getActiveCount();
    }

    public int getQueueSize() {
        return blockingQueue.size();
    }

    public void shutdown(int timeout) throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(timeout, TimeUnit.MILLISECONDS);
    }
}
