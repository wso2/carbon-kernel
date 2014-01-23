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

package org.apache.axis2.jaxws.sample.headershandler;

import java.util.concurrent.Future;

import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.WebServiceContext;

import org.test.headershandler.HeadersHandlerResponse;


@WebService(serviceName="HeadersHandlerService",endpointInterface="org.apache.axis2.jaxws.sample.headershandler.HeadersHandlerPortType")
@HandlerChain(file = "HeadersHandlers.xml", name = "")
public class HeadersHandlerPortTypeImpl implements HeadersHandlerPortType {

    private WebServiceContext ctx;
    
    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.sample.headershandler.HeadersHandlerPortType#headersHandler(int, int)
     */
    public int headersHandler(int arg0, int arg1) throws HeadersHandlerFault_Exception {
        return (int) (arg0 + arg1);
    }

    public Future<?> headersHandlerAsync(int arg0, int arg1, AsyncHandler<HeadersHandlerResponse> asyncHandler) {
        return null;
    }

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.headershandler.HeadersHandlerPortType#oneWayInt(int)
	 */
	public void oneWayInt(int arg0) {
        return;
	}
    
    @Resource
    public void setCtx(WebServiceContext ctx) {
        this.ctx = ctx;
    }

}
