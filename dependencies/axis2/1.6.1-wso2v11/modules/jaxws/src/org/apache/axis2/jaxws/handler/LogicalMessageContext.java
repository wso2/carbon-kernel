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

package org.apache.axis2.jaxws.handler;

import org.apache.axis2.jaxws.core.MessageContext;

import javax.xml.ws.LogicalMessage;

/**
 * The LogicalMessageContext is a JAX-WS interface that is given to Logical handlers to provide
 * access to the message and its associated properties.
 */
public class LogicalMessageContext extends BaseMessageContext implements
        javax.xml.ws.handler.LogicalMessageContext {

    public LogicalMessageContext(MessageContext messageCtx) {
        super(messageCtx);
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.ws.handler.LogicalMessageContext#getMessage()
     */
    public LogicalMessage getMessage() {
        return new LogicalMessageImpl(messageCtx.getMEPContext());
    }

}
