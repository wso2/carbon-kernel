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

package org.apache.axis2.jaxws.context.factory;

import org.apache.axis2.jaxws.context.WebServiceContextImpl;
import org.apache.axis2.jaxws.context.utils.ContextUtils;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.handler.LogicalMessageContext;
import org.apache.axis2.jaxws.handler.SoapMessageContext;

import javax.xml.ws.WebServiceContext;

public class MessageContextFactory {

    public MessageContextFactory() {
        super();

    }

    public static WebServiceContext createWebServiceContext() {
        return new WebServiceContextImpl();
    }

    /**
     * Creates a SOAPMessageContext based on the input core MEPContext.  
     * 
     * @param mepCtx
     * @return
     */
    public static SoapMessageContext createSoapMessageContext(
            org.apache.axis2.jaxws.core.MessageContext jaxwsMessageContext) {
        SoapMessageContext soapCtx = new SoapMessageContext(jaxwsMessageContext);
        ContextUtils.addProperties(soapCtx, jaxwsMessageContext);
        return soapCtx;
    }
    
    /**
     * Creates a LogicalMessageContext based on the input core MEPContext.
     * 
     * @param mepCtx
     * @return
     */
    public static LogicalMessageContext createLogicalMessageContext(MessageContext mc) {
        return new LogicalMessageContext(mc);
    }

}
