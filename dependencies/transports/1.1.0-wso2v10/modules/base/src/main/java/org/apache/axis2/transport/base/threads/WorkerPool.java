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

public interface WorkerPool {
    /**
     * Asynchronously execute the given task using one of the threads of the worker pool.
     * The task is expected to terminate gracefully, i.e. {@link Runnable#run()} should not
     * throw an exception. Any uncaught exceptions should be logged by the worker pool
     * implementation.
     * 
     * @param task the task to execute
     */
    public void execute(Runnable task);
    
    public int getActiveCount();
    public int getQueueSize();
    
    /**
     * Destroy the worker pool. The pool will immediately stop
     * accepting new tasks. All previously submitted tasks will
     * be executed. The method blocks until all tasks have
     * completed execution, or the timeout occurs, or the current
     * thread is interrupted, whichever happens first.
     * 
     * @param timeout the timeout value in milliseconds
     * @throws InterruptedException if the current thread was
     *         interrupted while waiting for pending tasks to
     *         finish execution
     */
    public void shutdown(int timeout) throws InterruptedException;
}
