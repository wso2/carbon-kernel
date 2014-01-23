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

package org.apache.axis2.jaxws.core;

import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.jaxws.client.async.AsyncResponse;

import javax.xml.ws.handler.Handler;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * An implementation of the InvocationContext interface.
 *
 * @see InvocationContext
 */
public class InvocationContextImpl implements InvocationContext {

    private List<Handler> handlers;
    private MessageContext requestMsgCtx;
    private MessageContext responseMsgCtx;
    private Executor executor;
    private AsyncResponse asyncResponse;

    private ServiceClient serviceClient; //FIXME: This is temporary

    public InvocationContextImpl() {
        //do nothing
    }

    /** @see InvocationContext#getHandlers() */
    public List<Handler> getHandlers() {
        return handlers;
    }

    /**
     * Sets the list of hanlders for this InvocationContext
     *
     * @param list
     */
    public void setHandlers(List<Handler> handlers) {
        this.handlers = handlers;
    }

    /** @see InvocationContext#setRequestMessageContext(MessageContext) */
    public void setRequestMessageContext(MessageContext ctx) {
        requestMsgCtx = ctx;
        if (ctx != null) {
            requestMsgCtx.setInvocationContext(this);
        }
    }

    /** @see InvocationContext#setResponseMessageContext(MessageContext) */
    public void setResponseMessageContext(MessageContext ctx) {
        responseMsgCtx = ctx;
        if (ctx != null) {
            responseMsgCtx.setInvocationContext(this);
        }
    }

    /** @see InvocationContext#getResponseMessageContext() */
    public MessageContext getResponseMessageContext() {
        return responseMsgCtx;
    }

    /** @see InvocationContext#getRequestMessageContext() */
    public MessageContext getRequestMessageContext() {
        return requestMsgCtx;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor e) {
        executor = e;
    }

    public AsyncResponse getAsyncResponseListener() {
        return asyncResponse;
    }

    public void setAsyncResponseListener(AsyncResponse ar) {
        asyncResponse = ar;
    }

    // FIXME: This is temporary
    public ServiceClient getServiceClient() {
        return serviceClient;
    }

    // FIXME: This is temporary
    public void setServiceClient(ServiceClient client) {
        serviceClient = client;
    }
}
