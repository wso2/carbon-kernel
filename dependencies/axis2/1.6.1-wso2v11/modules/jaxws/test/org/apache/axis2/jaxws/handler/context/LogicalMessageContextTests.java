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
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.XMLFaultCode;
import org.apache.axis2.jaxws.message.XMLFaultReason;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import test.EchoString;
import test.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.handler.LogicalMessageContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Unit tests for the creation and usage of the LogicalMessageContext that is
 * used for handler processing.
 */
public class LogicalMessageContextTests extends TestCase {
    
    private final String INPUT = "sample input";
    private final String FAULT_INPUT = "sample fault input";
    
    private final String sampleSOAP11FaultPayload =
        "<soapenv:Fault xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
        + "<faultcode>soapenv:Server</faultcode>" + "<faultstring>" + FAULT_INPUT
        + "</faultstring>" + "</soapenv:Fault>";
   
    public LogicalMessageContextTests(String name) {
        super(name);
    }
    
    /**
     * Test the javax.xml.transform.Source based APIs on the LogicalMessage interface.
     * @throws Exception
     */
    public void testGetPayloadAsSource() throws Exception {
        LogicalMessageContext lmc = createSampleContext();
        
        LogicalMessage msg = lmc.getMessage();
        assertTrue("The returned LogicalMessage was null", msg != null);
        
        Source payload = msg.getPayload();
        assertTrue("The returned payload (Source) was null", payload != null);
        
        String resultContent = _getStringFromSource(payload);
        assertTrue("The content returned was null", resultContent != null);
        assertTrue("The content returned was incomplete, unexpected element", resultContent.indexOf("echoString") > -1);
        assertTrue("The content returned was incomplete, unexpected content", resultContent.indexOf(INPUT) > -1);
    }
    
    /**
     * Tests the setting of the payload and make sure we don't cache improperly.
     * @throws Exception
     */
    public void testGetAndSetPayloadAsSource() throws Exception {
        LogicalMessageContext lmc = createSampleContext();
        
        LogicalMessage msg = lmc.getMessage();
        assertTrue("The returned LogicalMessage was null", msg != null);
        
        Source payload = msg.getPayload();
        assertTrue("The returned payload (Source) was null", payload != null);
        
        String resultContent = _getStringFromSource(payload);
        assertTrue("The content returned was null", resultContent != null);
        assertTrue("The content returned was incorrect", resultContent.indexOf(INPUT) > 0);

        // Now manipluate the content and set it back on the message.
        int start = resultContent.indexOf(INPUT);
        int end = start + INPUT.length();
        
        String newInput = "new content goes here";
        String newContent = resultContent.substring(0, start) + newInput + resultContent.substring(end);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(newContent.getBytes());
        StreamSource newPayload = new StreamSource(bais); 
        
        msg.setPayload(newPayload);
        
        // Check the payload to make sure the new content that we added 
        // was insterted correctly.
        Source payload2 = msg.getPayload();
        assertTrue("The returned payload (Source) was null", payload2 != null);
        
        String resultContent2 = _getStringFromSource(payload2);
        assertTrue("The updated content returned was null", resultContent2 != null);
        assertTrue("The updated content returned was incorrect, old content found", resultContent2.indexOf(INPUT) < 0);
        assertTrue("The updated content returned was incorrect, new content not found", resultContent2.indexOf(newInput) > -1);
    }
    
    /**
     * Test to make sure we can get the payload multiple times from the same LogicalMessage.
     * @throws Exception
     */
    public void testGetMultiplePayloadsAsSource() throws Exception {
        LogicalMessageContext lmc = createSampleContext();

        LogicalMessage msg = lmc.getMessage();
        assertTrue("The returned LogicalMessage was null", msg != null);

        int loopCount = 3;
        for (int i = 0; i < loopCount; ++i) {
            Source payload = msg.getPayload();
            assertTrue("Attempt number "  + i + " to get the payload (Source) was null", payload != null);


            String resultContent = _getStringFromSource(payload);
            assertTrue("The content returned in loop " + i + " was null", resultContent != null);
            assertTrue("The content returned in loop " + i + " was incomplete, unexpected element", resultContent.indexOf("echoString") > -1);
            assertTrue("The content returned in loop " + i + " was incomplete, unexpected content", resultContent.indexOf(INPUT) > -1);            
        }
    }
    
    /**
     * Tests the setting of the payload when the original content is a fault.
     * @throws Exception
     */
    public void testGetAndSetFaultPayloadAsSource() throws Exception {
        LogicalMessageContext lmc = createSampleFaultContext();
        
        LogicalMessage msg = lmc.getMessage();
        assertTrue("The returned LogicalMessage was null", msg != null);
        
        Source payload = msg.getPayload();
        assertTrue("The returned payload (Source) was null", payload != null);
        
        String resultContent = _getStringFromSource(payload);
        assertTrue("The content returned was null", resultContent != null);
        assertTrue("The content returned was incorrect", resultContent.indexOf(FAULT_INPUT) > 0);
        assertTrue("The content returned was incorrect, no fault found", resultContent.indexOf("Fault") > 0);
        
        // Now manipluate the content and set it back on the message.
        int start = resultContent.indexOf(FAULT_INPUT);
        int end = start + FAULT_INPUT.length();
        
        String newFaultInput = "new fault content goes here";
        String newContent = resultContent.substring(0, start) + newFaultInput + resultContent.substring(end);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(newContent.getBytes());
        StreamSource newPayload = new StreamSource(bais); 
        
        msg.setPayload(newPayload);
        
        // Check the payload to make sure the new content that we added 
        // was insterted correctly.
        Source payload2 = msg.getPayload();
        assertTrue("The returned payload (Source) was null", payload2 != null);
        
        String resultContent2 = _getStringFromSource(payload2);
        assertTrue("The updated content returned was null", resultContent2 != null);
        assertTrue("The updated content returned was incorrect, old content found", resultContent2.indexOf(FAULT_INPUT) < 0);
        assertTrue("The updated content returned was incorrect, no fault found", resultContent.indexOf("Fault") > 0);
        assertTrue("The updated content returned was incorrect, new content not found", resultContent2.indexOf(newFaultInput) > -1);
    }

// FIXME: Temporarily comment out test because of build break.    
//    /**
//     * Test the JAXB based APIs on the LogicalMessage interface.
//     * @throws Exception
//     */
//    public void testGetPayloadAsJAXB() throws Exception {
//        LogicalMessageContext lmc = createSampleContext();
//                
//        LogicalMessage msg = lmc.getMessage();
//        assertTrue("The returned LogicalMessage was null", msg != null);
//        
//        JAXBContext jbc = JAXBContext.newInstance("test");
//        
//        Object obj = msg.getPayload(jbc);
//        assertTrue("The returned payload (Object) was null", obj != null);
//        assertTrue("The returned payload (Object) was of the wrong type: " + obj.getClass().getName(), obj.getClass().equals(EchoString.class));
//        
//        EchoString echo = (EchoString) obj;
//        assertTrue("The EchoString object had null input", echo.getInput() != null);
//        assertTrue("The EchoString object had bad input: " + echo.getInput(), echo.getInput().equals(INPUT));
//    }
    
    
    public void testConvertMessageToFault() throws Exception {
        LogicalMessageContext lmc = createSampleContext();
 
        LogicalMessage msg = lmc.getMessage();
        assertTrue("The returned LogicalMessage was null", msg != null);

        Source payload = msg.getPayload();
        assertTrue("The returned payload (Source) was null", payload != null);

        String resultContent = _getStringFromSource(payload);
        assertTrue("The content returned was null", resultContent != null);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(sampleSOAP11FaultPayload.getBytes());
        StreamSource faultSource = new StreamSource(bais);
        
        msg.setPayload(faultSource);
        
        Source newFaultSource = msg.getPayload();
        assertTrue("The new fault content returned was null", faultSource != null);
        
        String newFaultContent = _getStringFromSource(newFaultSource);
        assertTrue("The new fault content returned was invalid", newFaultContent.equals(sampleSOAP11FaultPayload));
    }
    
    private LogicalMessageContext createSampleContext() throws Exception {
        MessageFactory factory = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
        Message msg = factory.create(Protocol.soap11);
        
        // Create a jaxb object
        ObjectFactory objFactory = new ObjectFactory();
        EchoString echo = objFactory.createEchoString();
        echo.setInput(INPUT);
        
        // Create the necessary JAXBContext
        JAXBContext jbc = JAXBContext.newInstance("test");
        JAXBBlockContext blockCtx = new JAXBBlockContext(jbc);
        
        // Create the Block 
        JAXBBlockFactory blockFactory = (JAXBBlockFactory) FactoryRegistry.getFactory(JAXBBlockFactory.class);
        Block block = blockFactory.createFrom(echo, blockCtx, null);
        
        msg.setBodyBlock(block);
        
        MessageContext mc = new MessageContext();
        mc.setMEPContext(new MEPContext(mc));
        mc.setMessage(msg);
        
        LogicalMessageContext lmc = MessageContextFactory.createLogicalMessageContext(mc);
        
        return lmc;
    }
    
    private LogicalMessageContext createSampleFaultContext() throws Exception {
        MessageFactory factory = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
        Message msg = factory.create(Protocol.soap11);
        
        XMLFaultReason reason = new XMLFaultReason(FAULT_INPUT);        
        XMLFault fault = new XMLFault(XMLFaultCode.SENDER, reason);
        msg.setXMLFault(fault);
        
        MessageContext mc = new MessageContext();
        mc.setMEPContext(new MEPContext(mc));
        mc.setMessage(msg);
        
        LogicalMessageContext lmc = MessageContextFactory.createLogicalMessageContext(mc);
        
        return lmc;
    }
    
    private String _getStringFromSource(Source source) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer trans = factory.newTransformer();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(baos);
        
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.transform(source, result);
        
        String content = new String(baos.toByteArray());
        
        return content;
    }
}