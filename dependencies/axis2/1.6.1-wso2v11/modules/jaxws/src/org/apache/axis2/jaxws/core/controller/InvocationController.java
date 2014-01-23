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

package org.apache.axis2.jaxws.core.controller;

import org.apache.axis2.jaxws.core.InvocationContext;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import java.util.concurrent.Future;

/**
 * The <tt>InvocationController</tt> is an interface modeling the invocation of a
 * target web service.  All of the information that the InvocationController needs 
 * should exist within the InvocatonContext that is passed in to the various invoke 
 * methods.
 * <p/>
 * The request information is passed in within the InvocationContext.  The InvocationController
 * assumes that there is a MessageContext within that InvocationContext that is populated with all
 * of the information that it needs to invoke.  If not, an error will be returned.  Once the
 * response comes back, the information for that response will be held inside of the MessageContext
 * representing the response, that exists in the InvocationContext.
 * <p/>
 * The InvocationController supports four different invocation patterns:
 * <p/>
 * 1) synchronous - This is represented by the {@link #invoke(InvocationContext)} method.  This is a
 * blocking, request/response call to the web service.
 * <p/>
 * 2) one-way - This is represented by the {@link #invokeOneWay(InvocationContext)} method.  This is
 * a one-way invocation that only returns errors related to sending the message.  If an error occurs
 * while processing, the client will not be notified.
 * <p/>
 * 3) asynchronous (callback) - {@link #invokeAsync(InvocationContext, AsyncHandler)}
 * <p/>
 * 4) asynchronous (polling) - {@link #invokeAsync(InvocationContext)}
 */
public interface InvocationController {

    /**
     * Performs a synchronous (blocking) invocation of a target service.  The InvocationContext
     * passed in should contain a valid MessageContext containing the properties and message to be
     * sent for the request.  The response contents will be processed and placed in the
     * InvocationContext as well.
     *
     * @param ic
     * @return
     */
    public InvocationContext invoke(InvocationContext ic);

    /**
     * Performs a one-way invocation of the client.  This is SHOULD NOT be a robust invocation, so
     * any fault that occurs during the processing of the request will not be returned to the
     * client.  Errors returned to the client are problems that occurred during the sending of the
     * message to the server.
     *
     * @param ic
     */
    public void invokeOneWay(InvocationContext ic) throws Exception;

    /**
     * Performs an asynchronous (non-blocking) invocation of the client based on a callback model.
     * The AsyncHandler that is passed in is the callback that the client programmer supplied when
     * they invoked their JAX-WS Dispatch or their SEI-based dynamic proxy.
     *
     * @param ic
     * @param callback
     * @return
     */
    public Response invokeAsync(InvocationContext ic);

    /**
     * Performs an asynchronous (non-blocking) invocation of the client based on a polling model.
     * The Response object that is returned allows the client programmer to poll against it to see
     * if a response has been sent back by the server.
     *
     * @param ic
     * @return
     */
    public Future<?> invokeAsync(InvocationContext ic, AsyncHandler asyncHandler);

}
