/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.axis2.util.threadpool;

import org.apache.axis2.AxisFault;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.java.security.AccessController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This the thread pool for axis2. This class will be used a singleton
 * across axis2 engine. <code>ThreadPool</code> is accepts <code>AxisWorkers</code> which has
 * run method on them and execute this method, using one of the threads
 * in the thread pool.
 */
public class ThreadPool implements ThreadFactory {
    private static final Log log = LogFactory.getLog(ThreadPool.class);
    protected static long SLEEP_INTERVAL = 1000;
    private static boolean shutDown;
    protected ThreadPoolExecutor executor;

    //integers that define the pool size, with the default values set.
    private int corePoolSize = 5;
    // Limiting the max thread pool size to 100
    private int maxPoolSize = 100;
//    private int maxPoolSize = Integer.MAX_VALUE;

    public ThreadPool() {
        setExecutor(createDefaultExecutor("Axis2 Task", Thread.NORM_PRIORITY, true));
    }

    public ThreadPool(int corePoolSize, int maxPoolSize) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        setExecutor(createDefaultExecutor("Axis2 Task", Thread.NORM_PRIORITY, true));
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public void execute(Runnable worker) {
        if (shutDown) {
            throw new RuntimeException(Messages.getMessage("threadpoolshutdown"));
        }
        executor.execute(worker);
    }

    /**
     * A forceful shutdown mechanism for thread pool.
     */
    public void forceShutDown() {
        if (log.isDebugEnabled()) {
            log.debug("forceShutDown called. Thread workers will be stopped");
        }
        executor.shutdownNow();
    }

    /**
     * This is the recommended shutdown method for the thread pool
     * This will wait till all the workers that are already handed over to the
     * thread pool get executed.
     *
     * @throws org.apache.axis2.AxisFault
     */
    public void safeShutDown() throws AxisFault {
        synchronized (this) {
            shutDown = true;
        }

        executor.shutdown();
    }

    protected ThreadPoolExecutor createDefaultExecutor(final String name,
                                                       final int priority,
                                                       final boolean daemon) {
        ThreadPoolExecutor rc;
        if (maxPoolSize == Integer.MAX_VALUE) {
            rc = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 10,
                    TimeUnit.SECONDS, new SynchronousQueue(),
                    new DefaultThreadFactory(name, daemon, priority));
        } else {
            rc = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 10,
                    TimeUnit.SECONDS, new LinkedBlockingQueue(),
                    new DefaultThreadFactory(name, daemon, priority));
        }
// FIXME: This API is only in JDK 1.6 - Use reflection?        
//        rc.allowCoreThreadTimeOut(true);
        return rc;
    }

    private static class DefaultThreadFactory implements java.util.concurrent.ThreadFactory {
        private final String name;
        private final boolean daemon;
        private final int priority;

        public DefaultThreadFactory(String name, boolean daemon, int priority) {
            this.name = name;
            this.daemon = daemon;
            this.priority = priority;
        }

        public Thread newThread(
                final Runnable runnable) {
            // do the following section as privileged
            // so that it will work even when java2 security
            // has been enabled
            Thread returnThread = null;
            try {
                returnThread = (Thread)
                        AccessController.doPrivileged(new PrivilegedExceptionAction<Thread>() {
                            public Thread run() {
                                Thread newThread =
                                        new Thread(runnable, name);
                                newThread.setDaemon(daemon);
                                newThread.setPriority(priority);
                                return newThread;
                            }
                        }
                        );
            }
            catch (PrivilegedActionException e) {
                // note: inner class can't have its own static log variable
                if (log.isDebugEnabled()) {
                    log.debug("ThreadPoolExecutor.newThread():   Exception from AccessController [" + e.getClass()
                            .getName() + "]  for [" + e.getMessage() + "]", e);
                }
            }
            return returnThread;

        }
    }
}
