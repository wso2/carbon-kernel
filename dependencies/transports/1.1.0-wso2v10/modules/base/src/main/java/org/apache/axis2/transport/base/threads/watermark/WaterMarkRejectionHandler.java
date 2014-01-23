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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * This class implements the {@link RejectedExecutionHandler} and provide a mechanism for
 * having the water mark in the {@link WaterMarkExecutor}. This is an internal class used by
 * the {@link WaterMarkExecutor}.
 */
class WaterMarkRejectionHandler implements RejectedExecutionHandler {
    RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();

    public WaterMarkRejectionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
        if (rejectedExecutionHandler != null) {
            this.rejectedExecutionHandler = rejectedExecutionHandler;
        }
    }

    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
        BlockingQueue q = threadPoolExecutor.getQueue();
        if (q instanceof WaterMarkQueue) {
            WaterMarkQueue wq = (WaterMarkQueue) q;

            if (!wq.offerAfter(runnable)) {
                if (rejectedExecutionHandler != null) {
                    rejectedExecutionHandler.rejectedExecution(runnable, threadPoolExecutor);
                }
            }
        }
    }
}
