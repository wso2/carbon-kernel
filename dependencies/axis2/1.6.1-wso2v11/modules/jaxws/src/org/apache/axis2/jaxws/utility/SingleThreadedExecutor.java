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

import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.Executor;

/**
 * A simple Executor implementation that does not create a new thread 
 * for processing work, but just borrows the current thread.
 */
public class SingleThreadedExecutor implements Executor {

    public static final Log log = LogFactory.getLog(SingleThreadedExecutor.class);
    
    public void execute(Runnable command) {
        if (log.isDebugEnabled()) {
            log.debug("JAX-WS work on SingleThreadedExector started.");
        }
        
        if (command == null) {
            throw ExceptionFactory.makeWebServiceException(
                      Messages.getMessage("singleThreadedExecutorErr1"));
                                                           
        }
        
        command.run();
        
        if (log.isDebugEnabled()) {
            log.debug("JAX-WS work on SingleThreadedExectuor complete.");
        }
    }

}
