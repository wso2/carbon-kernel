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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringBufferInputStream;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.PostConstruct;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;

import org.apache.axis2.jaxws.handler.AttachmentsAdapter;
import org.apache.axis2.jaxws.handler.LogicalMessageContext;

public class AddNumbersLogicalHandler2 implements javax.xml.ws.handler.LogicalHandler<LogicalMessageContext> {

    private int deduction = 1;
    
    HandlerTracker tracker = new HandlerTracker(AddNumbersLogicalHandler2.class.getSimpleName());
    
    @PostConstruct
    public void postConstruct() {
        tracker.postConstruct();
        deduction = 2;
    }
    
    public void close(MessageContext messagecontext) {
        tracker.close();
    }

    public boolean handleFault(LogicalMessageContext messagecontext) {
        Boolean outbound = (Boolean) messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        tracker.handleFault(outbound);
        if (outbound) {  // outbound response if we're on the server
            LogicalMessage msg = messagecontext.getMessage();
            String st = getStringFromSourcePayload(msg.getPayload());
            st = st.replaceFirst("blarg", "AddNumbersLogicalHandler2 was here");
            msg.setPayload(new StreamSource(new ByteArrayInputStream(st.getBytes())));
        }
        return true;
    }

    /*
     * this test handleMessage method is obviously not what a customer might write, but it does
     * the trick for kicking the tires in the handler framework.  The AddNumbers service takes two
     * ints as incoming params, adds them, and returns the sum.  This method subtracts 1 from the 
     * first int on the inbound request, and subtracts "deduction" from the int on the outbound
     * response.  So the client app should expect a sum 3 less than a sum with this handler 
     * manipulating the SOAP message.
     */
    public boolean handleMessage(LogicalMessageContext messagecontext) {
        Boolean outbound = (Boolean)messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        tracker.handleMessage(outbound);
        if (!outbound) {  // inbound request if we're on the server
            
            LogicalMessage msg = messagecontext.getMessage();
            String st = getStringFromSourcePayload(msg.getPayload());
            if (st.contains("<arg0>99</arg0>")) {
                tracker.log("THROWING PROTOCOLEXCEPTION", outbound);
                throw new ProtocolException("I don't like the value 99");
            }
            String txt = String.valueOf(Integer.valueOf(getFirstArg(st)) - 1);
            st = replaceFirstArg(st, txt);
            msg.setPayload(new StreamSource(new StringBufferInputStream(st)));
            
            messagecontext.put("AddNumbersLogicalHandlerInboundAppScopedProperty", "blargval");
            messagecontext.setScope("AddNumbersLogicalHandlerInboundAppScopedProperty", 
                                    Scope.APPLICATION);
            messagecontext.put("AddNumbersLogicalHandlerInboundHandlerScopedProperty", 
                               "blargval");
            
            // Check for the presences of the attachment property
            String propKey = MessageContext.INBOUND_MESSAGE_ATTACHMENTS;
            Map map = (Map) messagecontext.get(propKey);
            if (map == null) {
                throw new RuntimeException("Property " + propKey + " was null");
            }
            if (!(map instanceof AttachmentsAdapter)) {
                throw new RuntimeException("Expected AttachmentAdapter for Property " + 
                                           propKey);
            }

        } else { // outbound response if we're on the server
            LogicalMessage msg = messagecontext.getMessage();
            String st = getStringFromSourcePayload(msg.getPayload());
            String txt = String.valueOf(Integer.valueOf(getFirstArg(st)) - deduction);
            st = replaceFirstArg(st, txt);
            msg.setPayload(new StreamSource(new StringBufferInputStream(st)));
            
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

}
