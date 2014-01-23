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

package org.apache.axis2.jaxws.sample.addnumbershandler;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Set;

/*
 * You can't actually specify whether a handler is for client or server,
 * you just have to check in the handleMessage and/or handleFault to make
 * sure what direction we're going.
 */

public class AddNumbersClientProtocolHandler implements javax.xml.ws.handler.soap.SOAPHandler<SOAPMessageContext> {

    HandlerTracker tracker = new HandlerTracker(AddNumbersClientProtocolHandler.class.getSimpleName());
    boolean forcePivot = false;
    
    private final String PIVOT_MESSAGE = 
    "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Body>"+
    "<p:addNumbersHandlerResponse xmlns:p=\"http://org/test/addnumbershandler\">" +
    "<p:return>-99</p:return>" +
    "</p:addNumbersHandlerResponse>" +
    "</SOAP-ENV:Body></SOAP-ENV:Envelope>";
        
    
    
    public void close(MessageContext messagecontext) {
        tracker.close();
    }

    public boolean handleFault(SOAPMessageContext messagecontext) {
        Boolean outbound = (Boolean) messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        tracker.handleFault(outbound);
        return true;
    }

    public Set getHeaders() {
        tracker.getHeaders();
        return null;
    }

    public boolean handleMessage(SOAPMessageContext messagecontext) {
        Boolean outbound = (Boolean) messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        tracker.handleMessage(outbound);
        if (outbound && !forcePivot) {
            String appProp1 = "AddNumbersClientProtocolHandlerOutboundAppScopedProperty";
            messagecontext.put(appProp1, "myVal");
            messagecontext.setScope(appProp1, Scope.APPLICATION);
            
            String appProp2 = "AddNumbersClientProtocolHandlerOutboundHandlerScopedProperty";
            messagecontext.put(appProp2, "client apps can't see this");
            return true;
        }
        else if (!forcePivot) {  // client inbound response
            String appProp1 = "AddNumbersClientProtocolHandlerInboundAppScopedProperty";
            messagecontext.put(appProp1, "myVal");
            messagecontext.setScope(appProp1, Scope.APPLICATION);
            
            String appProp2 = "AddNumbersClientProtocolHandlerInboundHandlerScopedProperty";
            messagecontext.put(appProp2, "client apps can't see this");
            return true;
        } else {
            // Change the message and reverse the chain
            // I am changing the message at the SOAPPart level to make sure the new message "sticks"
            InputStream is = new ByteArrayInputStream(PIVOT_MESSAGE.getBytes()); 
            Source source = new StreamSource(is);
        
            SOAPMessage message = messagecontext.getMessage();
            message.removeAllAttachments();
            SOAPPart soapPart = message.getSOAPPart();
            try {
                soapPart.setContent(source);
            } catch (SOAPException e) {
                throw new RuntimeException(e);
            }
            return false;
        }
        
    }

    public void setPivot(boolean value) {
        forcePivot = value;
    }
}
