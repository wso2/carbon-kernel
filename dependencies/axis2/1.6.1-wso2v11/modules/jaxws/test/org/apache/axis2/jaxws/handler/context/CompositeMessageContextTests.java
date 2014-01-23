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

package org.apache.axis2.jaxws.handler.context;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.context.factory.MessageContextFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.handler.MEPContext;
import org.apache.axis2.jaxws.handler.SoapMessageContext;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.XMLFaultCode;
import org.apache.axis2.jaxws.message.XMLFaultReason;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.handler.LogicalMessageContext;
import java.io.ByteArrayOutputStream;

/**
 * A test suite for scenarios where the internal MessageContext is converted
 * between handler context formats (SOAPMessageContext and LogicalMessageContext).
 * 
 * The flows that need to be tested here are:
 * 
 * 1) INBOUND: The MessageContext will be converted from a LogicalMessageContext
 *             to a SOAPMessageContext.  This drives converting the OM message 
 *             to an SAAJ SOAPMessage among other things.
 *    
 *    specific tests:         
 *    - Normal message
 *    - Fault message
 *    
 * 2) OUTBOUND: The MessageContext will be converted from a SOAPMessageContext 
 *              to a LogicalMessageContext.  This will drive conversion from 
 *              an SAAJ SOAPMessage back to an OM (there are some very tricky 
 *              pieces of code contained within that scenario). 
 *              
 *    specific test:
 *    - Normal message
 *    - Fault message
 */
public class CompositeMessageContextTests extends TestCase {

    private final String FAULT_INPUT = "sample fault input";
    
    /**
     * A test that mimics the inbound flow through a handler chain.
     */
    public void testInboundFaultFlow() throws Exception {
        MessageContext mc = createSampleFaultMessageContext();
        
        LogicalMessageContext lmc = MessageContextFactory.createLogicalMessageContext(mc);
        LogicalMessage lm = lmc.getMessage();
        Source payload = lm.getPayload();
        assertTrue("The returned payload (Source) was null", payload != null);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(baos);
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.transform(payload, result);
        
        String content = new String(baos.toByteArray());
        assertTrue("The returned content (String) from the payload was null", content != null);
        assertTrue("The <faultcode> element was not found", content.indexOf("faultcode") > -1);
        assertTrue("The <faultstring> element was not found", content.indexOf("faultstring") > -1);
        assertTrue("The fault did not contain the expected fault string", content.indexOf(FAULT_INPUT) > -1);
                
        SoapMessageContext smc = MessageContextFactory.createSoapMessageContext(mc);
        SOAPMessage sm = smc.getMessage();
        assertTrue("The returned SOAPMessage was null", sm != null);
        assertTrue("The SOAPMessage did not contain a SOAPBody", sm.getSOAPBody() != null);
        assertTrue("The SOAPBody did not contain a SOAPFault", sm.getSOAPBody().getFault() != null);
    }
    
    /**
     * A test that mimics the outbound flow through a handler chain.
     */
    public void testOutboundFaultFlow() throws Exception {
        MessageContext mc = createSampleFaultMessageContext();
        
        SoapMessageContext smc = MessageContextFactory.createSoapMessageContext(mc);
        SOAPMessage sm = smc.getMessage();
        assertTrue("The returned SOAPMessage was null", sm != null);
        assertTrue("The SOAPMessage did not contain a SOAPBody", sm.getSOAPBody() != null);
        assertTrue("The SOAPBody did not contain a SOAPFault", sm.getSOAPBody().getFault() != null);
        
        LogicalMessageContext lmc = MessageContextFactory.createLogicalMessageContext(mc);
        LogicalMessage lm = lmc.getMessage();
        Source payload = lm.getPayload();
        assertTrue("The returned payload (Source) was null", payload != null);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(baos);
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.transform(payload, result);
        
        String content = new String(baos.toByteArray());
        assertTrue("The returned content (String) from the payload was null", content != null);
        assertTrue("The <faultcode> element was not found", content.indexOf("faultcode") > -1);
        assertTrue("The <faultstring> element was not found", content.indexOf("faultstring") > -1);
        assertTrue("The fault did not contain the expected fault string", content.indexOf(FAULT_INPUT) > -1);
    }
    
    private MessageContext createSampleFaultMessageContext() throws Exception {
        MessageFactory factory = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
        Message msg = factory.create(Protocol.soap11);

        XMLFaultReason reason = new XMLFaultReason(FAULT_INPUT);
        XMLFault fault = new XMLFault(XMLFaultCode.SENDER, reason);
        msg.setXMLFault(fault);

        MessageContext mc = new MessageContext();
        mc.setMEPContext(new MEPContext(mc));
        mc.setMessage(msg);

        return mc;
    }
}