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

package org.apache.axis2.jaxws.client.proxy;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.client.async.AsyncResponse;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.OperationDescription;

/**
 * ProxyAsyncListener will be used to create response object when client does response.get(); The
 * Class will return the data type associated with Response<T> Generic Class. Example
 * Response<Float> will return a Float object to client on Response.get() call.
 */
public class ProxyAsyncListener extends AsyncResponse {

    JAXWSProxyHandler handler = null;
    Object[] inputArgs = null;
    OperationDescription operationDesc = null;

    public ProxyAsyncListener(OperationDescription od) {
        super(od.getEndpointInterfaceDescription().getEndpointDescription());
        operationDesc = od;
    }

    public JAXWSProxyHandler getHandler() {
        return handler;
    }

    public void setHandler(JAXWSProxyHandler handler) {
        this.handler = handler;
    }

    public void setInputArgs(Object[] inputArgs) {
        this.inputArgs = inputArgs;
    }

    @Override
    public Object getResponseValueObject(MessageContext mc) {
        try {
            //Delegate the request to create respose to proxyHandler 
            //since it has all the logic written to create response for Sync 
            //and oneWay.
            return handler.createResponse(null, inputArgs, mc, operationDesc);
        }
        catch (Throwable e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    @Override
    public Throwable getFaultResponse(MessageContext mc) {
        return JAXWSProxyHandler.getFaultResponse(mc, operationDesc);
    }
}
