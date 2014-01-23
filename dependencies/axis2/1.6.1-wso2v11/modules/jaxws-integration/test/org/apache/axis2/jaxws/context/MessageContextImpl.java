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

package org.apache.axis2.jaxws.context;

import org.apache.axis2.jaxws.context.sei.MessageContext;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;

@WebService(serviceName="MessageContextService",
        portName="MessageContextPort",
        targetNamespace = "http://context.jaxws.axis2.apache.org/",
        endpointInterface = "org.apache.axis2.jaxws.context.sei.MessageContext")
public class MessageContextImpl implements MessageContext {

    @Resource
    WebServiceContext ctxt;
    
    public static WebServiceContext webServiceContext = null;

    public void isPropertyPresent(
            Holder<String> propertyName,
            Holder<String> value,
            Holder<String> type,
            Holder<Boolean> isFound) {
        
        // Put the context in the static variable so that the test can 
        // make sure that its contents don't persist past the method invocation
        webServiceContext = ctxt;
        
        javax.xml.ws.handler.MessageContext msgCtxt = ctxt.getMessageContext();
        
        if (msgCtxt != null) {
            isFound.value = msgCtxt.containsKey(propertyName.value);
            Object val = msgCtxt.get(propertyName.value);
            System.out.println("msgCtxt.containsKey=" + isFound.value);
            System.out.println("msgCtxt.get=" + val);

            if (val != null) {
                type.value = val.getClass().getName();
                value.value = val.toString();
            }
        }
    }
}