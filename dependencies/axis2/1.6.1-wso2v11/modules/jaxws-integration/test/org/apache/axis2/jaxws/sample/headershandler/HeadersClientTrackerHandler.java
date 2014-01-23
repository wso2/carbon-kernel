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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.axis2.Constants;

public class HeadersClientTrackerHandler implements
        javax.xml.ws.handler.soap.SOAPHandler<SOAPMessageContext> {
    
    public void close(MessageContext messagecontext) {
    }

    public boolean handleFault(SOAPMessageContext messagecontext) {
        return true;
    }

    public Set getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext messagecontext) {
        Boolean outbound = (Boolean) messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outbound) {

            // this is the first client outbound handler hit
            
            Map<QName, List<String>> requestHeaders = (Map<QName, List<String>>)messagecontext.get(Constants.JAXWS_OUTBOUND_SOAP_HEADERS);

            // this should generate an exception.  We have protection built in to prevent using both
            // the SOAPHeadersAdapter and SAAJ in the same handler method
            
            List<String> list1 = requestHeaders.get(TestHeaders.ACOH1_HEADER_QNAME);

            try {
                messagecontext.getMessage().getSOAPHeader();
            } catch (SOAPException e) {
                throw new ProtocolException(e);
            }

        }
        else {  // client inbound response
        }
        return true;
    }

}
