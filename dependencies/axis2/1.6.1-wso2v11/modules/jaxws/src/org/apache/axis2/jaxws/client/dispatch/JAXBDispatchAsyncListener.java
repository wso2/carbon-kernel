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

package org.apache.axis2.jaxws.client.dispatch;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.client.async.AsyncResponse;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;

import javax.xml.bind.JAXBContext;
import javax.xml.ws.Service.Mode;

/**
 * The JAXBDispatchAsyncListener is an extension of the {@link org.apache.axis2.jaxws.client.async.AsyncResponse}
 * class to provide JAX-B specific function when processing an async response.
 */
public class JAXBDispatchAsyncListener extends AsyncResponse {

    private Mode mode;
    private JAXBContext jaxbContext;

    public JAXBDispatchAsyncListener(EndpointDescription ed) {
        super(ed);
    }

    public void setMode(Mode m) {
        mode = m;
    }

    public void setJAXBContext(JAXBContext jbc) {
        jaxbContext = jbc;
    }

    public Object getResponseValueObject(MessageContext mc) {
        try {
            return JAXBDispatch.getValue(mc.getMessage(), mode, jaxbContext);
        } finally {
            // Free the incoming stream
            try {
                mc.freeInputStream();
            }
            catch (Throwable t) {
                throw ExceptionFactory.makeWebServiceException(t);
            }
        }
    }

    public Throwable getFaultResponse(MessageContext mc) {
        return BaseDispatch.getFaultResponse(mc);
    }
}
