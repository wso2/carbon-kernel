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

package org.apache.axis2.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;

/**
 * This is the interface for a piece of code that will plug into the user
 * programming model impl (e.g. JAX-WS impl) and will be invoked while on the
 * ultimate thread of execution.  It is intended to provide a mechanism to
 * allow information to be migrated between the Axis2 contexts and thread
 * local storage.
 * <p/>
 * Note: It is up to each particular programming model impl to decide whether
 * or not they wish to make use of the ThreadContextMigrators.
 * <p/>
 * For each general MEP, here is the invocation pattern:
 * <p/>
 * [one-way inbound]
 * migrateContextToThread(req)
 * cleanupThread(req)
 * <p/>
 * [req/rsp inbound]
 * migrateContextToThread(req)
 * migrateThreadToContext(rsp)
 * cleanupContext(rsp)
 * cleanupThread(req)
 * <p/>
 * [one-way outbound]
 * migrateThreadToContext(req)
 * cleanupContext(req)
 * <p/>
 * [req/rsp outbound (both sync and async)]
 * migrateThreadToContext(req)
 * cleanupContext(req)
 * migrateContextToThread(rsp)
 * Note: there is no corresponding cleanupThread(rsp); one of the inbound
 * cases would need to handle this
 * <p/>
 * If a fault occurs during execution of one of the migrators, it will be
 * treated like any other service fault (i.e. like what will happen if we can't
 * deliver the message to a service or if a handler fails.
 * <p/>
 * The cleanup* methods can be expected to be invoked after any exeception
 * that occurs within the scope of the migration that would cause that scope
 * to be left so that the thread and/or context may be cleaned up properly.
 */
public interface ThreadContextMigrator {
    /**
     * This method will be invoked when the processing of the message is
     * guaranteed to be on the thread of execution that will be used in
     * user space.  It will be invoked for incoming messages.
     * Implementations of this interface can use the information found in the
     * MessageContext to determine whether a request or response is being
     * processed.
     * (e.g. MessageContext.getAxisOperation().getMessageExchangePattern())
     *
     * @param messageContext
     * @throws AxisFault
     */
    void migrateContextToThread(MessageContext messageContext) throws AxisFault;

    /**
     * This method will be invoked when the processing of the message is
     * guaranteed to still be on the thread of execution that was used in user
     * space, after all processing has completed (i.e. when the particular
     * processing of a message is unwinding.)  It provides a mechanism which can
     * be used to clean up the TLS.
     *
     * @param messageContext
     */
    void cleanupThread(MessageContext messageContext);

    /**
     * This method will be invoked when the processing of the message is
     * guaranteed to still be on the thread of execution that was used in
     * user space.  It will be invoked for both outgoing messages.
     * Implementations of this interface can use the information found in the
     * MessageContext to determine whether a request or response is being
     * processed.
     * (e.g. MessageContext.getAxisOperation().getMessageExchangePattern())
     *
     * @param messageContext
     * @throws AxisFault
     */
    void migrateThreadToContext(MessageContext messageContext) throws AxisFault;

    /**
     * This method will be invoked when the processing of the message is
     * guaranteed to be on the thread of execution that will be used in user
     * space, after all processing has completed (i.e. when the particular
     * processing of a message is unwinding.)  It provides a mechanism which can
     * be used to clean up the MessageContext or restore TLS.
     *
     * @param messageContext
     */
    void cleanupContext(MessageContext messageContext);
}
