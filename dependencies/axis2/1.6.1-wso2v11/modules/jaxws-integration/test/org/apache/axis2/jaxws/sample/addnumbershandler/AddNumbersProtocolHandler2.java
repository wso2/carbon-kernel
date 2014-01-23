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

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.axis2.jaxws.utility.JavaUtils;

public class AddNumbersProtocolHandler2 implements javax.xml.ws.handler.soap.SOAPHandler<SOAPMessageContext> {

    HandlerTracker tracker = new HandlerTracker(AddNumbersProtocolHandler2.class.getSimpleName());
    
    public static boolean throwException = false;
    
    public void close(MessageContext messagecontext) {
        tracker.close();
    }

    public Set<QName> getHeaders() {
        tracker.getHeaders();
        return null;
    }
    
    public boolean handleFault(SOAPMessageContext messagecontext) {
        Boolean outbound = (Boolean) messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        tracker.handleFault(outbound);
        return true;
    }

    public boolean handleMessage(SOAPMessageContext messagecontext) {
        Boolean outbound = (Boolean) messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        tracker.handleMessage(outbound);
        
        if (!outbound && throwException) {
            RuntimeException e = new NullPointerException();
            throw e;
        }
        return true;
    }

}
