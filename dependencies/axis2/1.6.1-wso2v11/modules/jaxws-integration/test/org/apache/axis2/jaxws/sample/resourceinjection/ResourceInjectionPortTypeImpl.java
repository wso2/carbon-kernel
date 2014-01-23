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

package org.apache.axis2.jaxws.sample.resourceinjection;

import org.apache.axis2.jaxws.Constants;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.sample.resourceinjection.sei.ResourceInjectionPortType;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

/**
 * Sample Resource Injection Endpoint
 *
 */
@WebService(serviceName="ResourceInjectionService",
            endpointInterface="org.apache.axis2.jaxws.sample.resourceinjection.sei.ResourceInjectionPortType")
            public class ResourceInjectionPortTypeImpl implements ResourceInjectionPortType {

    // The ctx variable is marked with the @Resource annotation.
    // The runtime will automatically inject (set) this variable to the
    // current WebServiceContext
    @Resource
    public WebServiceContext ctx = null;
    
    /**
     * Sample method that ensures that WebServiceContext was properly set.
     */
    public String testInjection(String arg) {
        
        
        if (ctx == null) {
            return "FAILURE: The WebServiceContext was not set";
        }
        
        MessageContext msgContext = ctx.getMessageContext();
        if (msgContext == null) {
            return "FAILURE: The WebServiceContext does not have a MessageContext";
        }
        
        String initCalledValue = (String) msgContext.get("INIT_CALLED");
        if (initCalledValue == null) {
            return "FAILURE: The @PostConstruct initialize method was not invoked";
        }
        QName wsdlOperation = (QName) msgContext.get(MessageContext.WSDL_OPERATION);
        
        if (wsdlOperation == null) {
            return "FAILURE: The WebServiceContext's MessageContext " +
                        "does not have the correct wsdlOperation";
        }
        
        String response = "SUCCESS: " + wsdlOperation.getLocalPart();
        
        // Set a flag to force filtering of JAXB data.
        // Also set a illegal characters in the response string
        // to verify that the illegal character is removed.
        msgContext.put(Constants.JAXWS_JAXB_WRITE_REMOVE_ILLEGAL_CHARS, Boolean.TRUE);
        char[] chars = new char[] {0x15}; // 0x15 is not a valid xml character
        String insert = new String(chars);
        response = insert + response + insert;

        return response;
    }

    @PostConstruct
    public void initialize(){
        //Called after resource injection and before a method is called.
        if (ctx != null && ctx.getMessageContext() != null) {
            ctx.getMessageContext().put("INIT_CALLED", "INIT_CALLED_VALUE");
        }
    }

    @PreDestroy
    public void distructor(){
        //Called before the scope of request or session or application ends.

        TestLogger.logger.debug("Calling PreDestroy ");

    }

}
