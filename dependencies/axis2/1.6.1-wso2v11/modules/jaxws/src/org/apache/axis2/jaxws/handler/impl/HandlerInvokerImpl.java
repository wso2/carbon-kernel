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

package org.apache.axis2.jaxws.handler.impl;

import org.apache.axis2.jaxws.handler.HandlerInvocationContext;
import org.apache.axis2.jaxws.handler.HandlerInvoker;
import org.apache.axis2.jaxws.handler.HandlerInvokerUtils;

/**
 * This class will be responsible for driving both inbound and
 * outbound handler chains for an endpoint invocation.
 *
 */
public class HandlerInvokerImpl implements HandlerInvoker {

    /**
     * This invokes the inbound handlers for the invocation.
     */
    public boolean invokeInboundHandlers(HandlerInvocationContext context) {
        return HandlerInvokerUtils.invokeInboundHandlers(context.getMessageContext().
                                                  getMEPContext(), context.getHandlers(), 
                                                  context.getMEP(), context.isOneWay());
    }

    /**
     * This invokes the outbound handlers for the invocation.
     */
    public boolean invokeOutboundHandlers(HandlerInvocationContext context) {
        return HandlerInvokerUtils.invokeOutboundHandlers(context.getMessageContext().
                                                         getMEPContext(), context.getHandlers(), 
                                                         context.getMEP(), context.isOneWay());
    }
    
}
