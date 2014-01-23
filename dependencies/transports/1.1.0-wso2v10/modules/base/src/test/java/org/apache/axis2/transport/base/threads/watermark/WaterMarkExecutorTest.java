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

import junit.framework.TestCase;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WaterMarkExecutorTest extends TestCase {

    private WaterMarkExecutor executor = null;

    private WaterMarkExecutor executor2 = null;

    private final int TASKS = 1000;

    private volatile int runTasks = 0;

    private volatile int[] tasksSubmitted = new int[TASKS];

    private Lock lock = new ReentrantLock();

    @Override
    protected void setUp() throws Exception {
        executor = new WaterMarkExecutor(10, 100, 10,
            TimeUnit.SECONDS, new DefaultWaterMarkQueue<Runnable>(100, 500),
            new ThreadPoolExecutor.CallerRunsPolicy());

        executor2 = new WaterMarkExecutor(10, 100, 10,
            TimeUnit.SECONDS, new DefaultWaterMarkQueue<Runnable>(100),
            new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void testExecutor() {
        for (int i = 0; i < TASKS; i++) {
            tasksSubmitted[i] = i + 1;
        }

        for (int i = 0; i < TASKS; i++) {
            executor.execute(new Test(i + 1, lock));
        }

        // this is an best effort number so we wait another 1 second for
        // the executor to finish the tasks
        while (executor.getActiveCount() > 0) {}
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }

        int tasks = 0;
        for (int aTasksSubmitted : tasksSubmitted) {
            if (aTasksSubmitted != 0) {
                tasks++;
            }
        }

        assertEquals(TASKS, runTasks);
        assertEquals(tasks, 0);

        executor.shutdown();

    }

    public void testExecutor2() {
        for (int i = 0; i < TASKS; i++) {
            tasksSubmitted[i] = i + 1;
        }

        for (int i = 0; i < TASKS; i++) {
            executor2.execute(new Test(i + 1, lock));
        }

        // this is an best effort number so we wait another 1 second for
        // the executor to finish the tasks
        while (executor2.getActiveCount() > 0) {}
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }

        int tasks = 0;
        for (int aTasksSubmitted : tasksSubmitted) {
            if (aTasksSubmitted != 0) {
                tasks++;
            }
        }

        assertEquals(TASKS, runTasks);
        assertEquals(tasks, 0);

        executor2.shutdown();

    }

    private class Test implements Runnable {
        long taskId;
        Lock tLock;

        private Test(long taskId, Lock lock) {
            this.taskId = taskId;
            tLock = lock;
        }

        public void run() {
                tLock.lock();
                try {
                    runTasks++;
                    for (int i = 0; i < TASKS; i++) {
                        if (taskId == tasksSubmitted[i]) {
                            tasksSubmitted[i] = 0;
                        }
                    }
                } finally {
                    tLock.unlock();
                }

        }
    }
}
