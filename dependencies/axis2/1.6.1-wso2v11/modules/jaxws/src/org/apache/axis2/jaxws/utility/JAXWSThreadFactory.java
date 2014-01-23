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

package org.apache.axis2.jaxws.utility;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.ThreadFactory;

/**
 * Factory to create threads in the ThreadPool Executor.  We provide a factory so 
 * the threads can be set as daemon threads so that they do not prevent the JVM from 
 * exiting normally when the main client thread is finished.
 *
 */
public class JAXWSThreadFactory implements ThreadFactory {
	private static final Log log = LogFactory.getLog(JAXWSThreadFactory.class);
    private static int groupNumber = 0;
    private int threadNumber = 0;
    // We put the threads into a unique thread group only for ease of identifying them
    private ThreadGroup threadGroup = null;

    public Thread newThread(final Runnable r) {
        if (threadGroup == null) {
            try {
                threadGroup =
                        (ThreadGroup) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                            public Object run() {
                                return new ThreadGroup("JAX-WS Default Executor Group "
                                        + groupNumber++);
                            }
                        });
            } catch (PrivilegedActionException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Exception thrown from AccessController: " + e);
                }
                throw ExceptionFactory.makeWebServiceException(e.getException());
            }
        }

        threadNumber++;
        Thread returnThread = null;
        try {
            returnThread = (Thread) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() {
                    Thread newThread = new Thread(threadGroup, r);
                    newThread.setDaemon(true);
                    return newThread;
                }
            });
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw ExceptionFactory.makeWebServiceException(e.getException());
        }

        return returnThread;
    }
}
