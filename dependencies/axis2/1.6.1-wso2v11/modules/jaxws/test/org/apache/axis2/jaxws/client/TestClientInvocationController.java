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

package org.apache.axis2.jaxws.client;

import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.controller.InvocationController;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import java.util.concurrent.Future;

public class TestClientInvocationController implements InvocationController {

    private InvocationContext cachedCtx;
    
    public InvocationContext invoke(InvocationContext ic) {
        cachedCtx = ic;
        
        MessageContext request = ic.getRequestMessageContext();        
        MessageContext response = new MessageContext();
        
        response.setEndpointDescription(request.getEndpointDescription());
        response.setMessage(request.getMessage());        
        
        ic.setResponseMessageContext(response);
        return ic;
    }

    public Future<?> invokeAsync(InvocationContext ic, AsyncHandler asyncHandler) {
        cachedCtx = ic;
        return null;
    }

    public Response invokeAsync(InvocationContext ic) {
        cachedCtx = ic;
        return null;
    }

    public void invokeOneWay(InvocationContext ic) throws Exception {
        cachedCtx = ic;

    }
    
    public InvocationContext getInvocationContext() {
        return cachedCtx;
    }

}
