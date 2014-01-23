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

import org.apache.axis2.jaxws.handler.AttachmentsAdapter;
import org.apache.axis2.jaxws.handler.LogicalMessageContext;
import org.apache.axis2.jaxws.marshaller.impl.alt.MethodMarshallerUtils;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.util.XMLFaultUtils;
import org.apache.axis2.jaxws.utility.SAAJFactory;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.ByteArrayOutputStream;
import java.io.StringBufferInputStream;
import java.util.Map;
import java.util.StringTokenizer;

/*
 * You can't actually specify whether a handler is for client or server,
 * you just have to check in the handleMessage and/or handleFault to make
 * sure what direction we're going.
 */

public class AddNumbersClientLogicalHandler 
implements javax.xml.ws.handler.LogicalHandler<LogicalMessageContext> {

    HandlerTracker tracker = new HandlerTracker(AddNumbersClientLogicalHandler.class.getSimpleName());
    
    public void close(MessageContext messagecontext) {
        tracker.close();
    }

    public boolean handleFault(LogicalMessageContext messagecontext) {
        Boolean outbound = (Boolean) messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        tracker.handleFault(outbound);
        return true;
    }

    public boolean handleMessage(LogicalMessageContext messagecontext) {
        Boolean outbound = 
            (Boolean)messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        tracker.handleMessage(outbound);
        if (!outbound) {  // inbound response on the client
            
            // make sure standard property is available
            
            Object bob = messagecontext.get(LogicalMessageContext.HTTP_RESPONSE_CODE);
            if (bob == null) {
                throw new NullPointerException("bob is null");
            }
            
            // previously caused a NPE due to internal Properties.putAll(map);
            // where 'map' had a key/value pair with null value.  So, internally
            // we now use HashMap instead of Properties.
            int size = messagecontext.size();
            
            /*
             * These props were set on the outbound flow.  Inbound flow handlers
             * should have access to them.
             */
            String propKey = "AddNumbersClientProtocolHandlerOutboundAppScopedProperty";
            String myClientVal = (String)messagecontext.get(propKey);
            if (myClientVal == null) {
                throw new RuntimeException("Property " + propKey + " was null.  " +
                                "MEPContext is not searching hard enough for the property.");
            }
            
            // Check for the presences of the attachment property
            propKey = MessageContext.INBOUND_MESSAGE_ATTACHMENTS;
            Map map = (Map) messagecontext.get(propKey);
            if (map == null) {
                throw new RuntimeException("Property " + propKey + " was null");
            }
            if (!(map instanceof AttachmentsAdapter)) {
                throw new RuntimeException("Expected AttachmentAdapter for Property " + 
                                           propKey);
            }
            propKey = "AddNumbersClientProtocolHandlerOutboundHandlerScopedProperty";
            myClientVal = (String)messagecontext.get(propKey);
            if (myClientVal == null) {
                throw new RuntimeException("Property " + propKey + " was null.  " +
                                "MEPContext is not searching hard enough for the property.");
            }
            
            /*
             * These props were set on the inbound flow.  Inbound flow handlers
             * should have access to them.
             */
            propKey = "AddNumbersClientProtocolHandlerInboundAppScopedProperty";
            myClientVal = (String)messagecontext.get(propKey);
            if (myClientVal == null) {
                throw new RuntimeException("Property " + propKey + " was null.  " +
                                "MEPContext is not searching hard enough for the property.");
            }
            propKey = "AddNumbersClientProtocolHandlerInboundHandlerScopedProperty";
            myClientVal = (String)messagecontext.get(propKey);
            if (myClientVal == null) {
                throw new RuntimeException("Property " + propKey + " was null.  " +
                                "MEPContext is not searching hard enough for the property.");
            }
            LogicalMessage msg = messagecontext.getMessage();
            String st = getStringFromSourcePayload(msg.getPayload());
            String txt = String.valueOf(Integer.valueOf(getFirstArg(st)) - 1);
            st = replaceFirstArg(st, txt);
            msg.setPayload(new StreamSource(new StringBufferInputStream(st)));
        }
        else {
            LogicalMessage msg = messagecontext.getMessage();
            
            Source s = msg.getPayload();
            s = msg.getPayload();
            
            String st = getStringFromSourcePayload(msg.getPayload());
            if (st.contains(">99</arg0>")) {
                throw new ProtocolException("I don't like the value 99");
            } else if (st.contains(">999</arg0>")) {
                XMLFault xmlFault = MethodMarshallerUtils.createXMLFaultFromSystemException(new RuntimeException("I don't like the value 999"));
                try {
                    javax.xml.soap.MessageFactory mf = SAAJFactory.createMessageFactory(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE);
                    SOAPMessage message = mf.createMessage();
                    SOAPBody body = message.getSOAPBody();
                    SOAPFault soapFault = XMLFaultUtils.createSAAJFault(xmlFault, body);
                    throw new SOAPFaultException(soapFault);
                } catch (SOAPException soape) {
                    throw new RuntimeException("Got SOAPException.  That's bad.");
                }
            }
            
            // Check for the presences of the attachment property
            String propKey = MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS;
            Map map = (Map) messagecontext.get(propKey);
            if (map == null) {
                throw new RuntimeException("Property " + propKey + " was null");
            }
            if (!(map instanceof AttachmentsAdapter)) {
                throw new RuntimeException("Expected AttachmentAdapter for Property " +
                                           propKey);
            }
        }
        return true;
    }
    
    private static String getFirstArg(String payloadString) {
        StringTokenizer st = new StringTokenizer(payloadString, ">");
        st.nextToken();  // skip first token.
        st.nextToken();  // skip second
        String tempString = st.nextToken();
        String returnString = new StringTokenizer(tempString, "<").nextToken();
        return returnString;
    }
    
    private static String replaceFirstArg(String payloadString, String newArg) {
        String firstArg = getFirstArg(payloadString);
        payloadString = payloadString.replaceFirst(firstArg, newArg);
        return payloadString;
    }
    
    private static String getStringFromSourcePayload(Source payload) {
        try {

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer trans = factory.newTransformer();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(baos);

            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.transform(payload, result);

            return new String(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
