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

package org.apache.axis2.handlers;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;

/**
 * This abstract class differentiates the concern of the conditional check to see whether this
 * particular message needs to be handled by the handler implementation and the actual invocation
 * logic.
 *
 * @see org.apache.axis2.handlers.AbstractHandler
 */
public abstract class AbstractTemplatedHandler extends AbstractHandler {

    /**
     * Implements the separation of the conditional check and the actual logic
     * 
     * @param msgContext the <code>MessageContext</code> to process with this <code>Handler</code>.
     * @return CONTINUE if the handler implementation says 'should not be invoked'
     * or the result of the {@link #doInvoke(org.apache.axis2.context.MessageContext)}
     * @throws AxisFault if the {@link #doInvoke(org.apache.axis2.context.MessageContext)}
     * throws the same
     */
    public final InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        return shouldInvoke(msgContext) ? doInvoke(msgContext) : InvocationResponse.CONTINUE;
    }

    /**
     * This method should implement the conditional check of the handler to decide whether this 
     * particular message needs to be handled by me
     *
     * @param msgCtx current <code>MessageContext</code> to be evaluated
     * @return boolean <code>true<code>, if this handler needs to be further invoked,
     * <code>false</code> if this handler has nothing to do with this specific message
     * and want the flow to be continued
     * @throws AxisFault in an error in evaluating the decision
     */
    public abstract boolean shouldInvoke(MessageContext msgCtx) throws AxisFault;

    /**
     * This should implement the actual handler invocation logic.
     * 
     * @param msgCtx current message to be handled by this handler
     * @return flow completion decision, should be one of {@link InvocationResponse#CONTINUE},
     * {@link InvocationResponse#ABORT}, {@link InvocationResponse#SUSPEND}
     * @throws AxisFault in an error in invoking the handler
     */
    public abstract InvocationResponse doInvoke(MessageContext msgCtx) throws AxisFault;
}
