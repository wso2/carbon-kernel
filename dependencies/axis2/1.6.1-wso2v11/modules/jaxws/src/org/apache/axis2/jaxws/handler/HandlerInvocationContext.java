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

import javax.xml.ws.handler.Handler;
import java.util.List;

/**
 * This data bean will be passed to the HandlerInvoker instance.
 * The bean will contain the necessary data in order to invoke
 * either inbound or outbound Handler instances for a given request.
 *
 */
public class HandlerInvocationContext {
    
    private MessageContext messageContext;
    
    private HandlerChainProcessor.MEP mep;
    
    private List<Handler> handlers;
    
    private boolean isOneWay;

    public boolean isOneWay() {
        return isOneWay;
    }

    public void setOneWay(boolean isOneWay) {
        this.isOneWay = isOneWay;
    }

    public MessageContext getMessageContext() {
        return messageContext;
    }

    public void setMessageContext(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    public HandlerChainProcessor.MEP getMEP() {
        return mep;
    }

    public void setMEP(HandlerChainProcessor.MEP mep) {
        this.mep = mep;
    }

    public List<Handler> getHandlers() {
        return handlers;
    }

    public void setHandlers(List<Handler> handlers) {
        this.handlers = handlers;
    }

}
