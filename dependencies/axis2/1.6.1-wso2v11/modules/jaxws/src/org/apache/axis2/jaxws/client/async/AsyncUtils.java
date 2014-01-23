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

package org.apache.axis2.jaxws.client.async;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.util.Constants;
import org.apache.axis2.util.ThreadContextMigratorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.WebServiceException;

public class AsyncUtils {

    private static final Log log = LogFactory.getLog(AsyncUtils.class);
    private static final boolean debug = log.isDebugEnabled();

    public static MessageContext createJAXWSMessageContext(
            org.apache.axis2.context.MessageContext mc) throws WebServiceException {
        MessageContext response = null;

        if (debug) {
            log.debug("Creating response MessageContext");
        }

        // Create the JAX-WS response MessageContext from the Axis2 response
        response = new MessageContext(mc);
        // don't set the response.setMEPContext() here.

        // REVIEW: Are we on the final thread of execution here or does this get handed off to the executor?
        // TODO: Remove workaround for WS-Addressing running in thin client (non-server) environment
        try {
            ThreadContextMigratorUtil
                    .performMigrationToThread(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, mc);
        }
        catch (Throwable t) {
            if (debug) {
                log.debug(mc.getLogIDString() +
                        " An error occurred in the ThreadContextMigratorUtil " + t);
                log.debug("...caused by " + t.getCause());
            }
            throw ExceptionFactory.makeWebServiceException(t);
        }

        return response;
    }
}
