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

package org.apache.axis2.jaxws.message;

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.message.util.Reader2Writer;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.unitTest.TestLogger;
import org.apache.log4j.BasicConfigurator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

/**
 * This suite is used to test the creation of messages based on SOAP 1.2
 * with both inbound and outbound simulations. 
 *
 */
public class SOAP12Tests extends TestCase {

    private static final String sampleText = 
        "<echo>test string</echo>";
    
    private static final String sampleSoap12EnvelopeHead =
        "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">" +
        "<soapenv:Header /><soapenv:Body>";
    
    private static final String sampleEnvelopeTail =
        "</soapenv:Body></soapenv:Envelope>";    
    
    private static final String sampleSoap12Envelope = 
        sampleSoap12EnvelopeHead + 
        sampleText + 
        sampleEnvelopeTail;
    
    private static final String SOAP12_NS_URI = 
        "http://www.w3.org/2003/05/soap-envelope";
    
    public static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    
    public SOAP12Tests(String name) {
        super(name);
    }
    
    static {
        BasicConfigurator.configure();
    }

    /**
     * Simulate creating a SOAP 1.2 message when the business object
     * provided is just the payload. 
     */
    public void testCreateSoap12FromPayload() throws Exception {
        // Create a SOAP 1.2 Message
        MessageFactory mf = (MessageFactory)
            FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.create(Protocol.soap12);
        
        // Get the BlockFactory
        XMLStringBlockFactory f = (XMLStringBlockFactory)
            FactoryRegistry.getFactory(XMLStringBlockFactory.class);
        
        // Create a Block using the sample string as the content.  This simulates
        // what occurs on the outbound JAX-WS dispatch<String> client
        Block block = f.createFrom(sampleText, null, null);
        
        // Add the block to the message as normal body content.
        m.setBodyBlock(block);
        
        // Assuming no handlers are installed, the next thing that will happen
        // is a XMLStreamReader will be requested...to go to OM.   At this point the
        // block should be consumed.
        OMElement om = m.getAsOMElement();
        
        // The block should not be consumed yet...because the message has not been read
        assertTrue(!block.isConsumed());
        
        // To check that the output is correct, get the String contents of the 
        // reader
        Reader2Writer r2w = new Reader2Writer(om.getXMLStreamReader());
        String newText = r2w.getAsString();
        TestLogger.logger.debug(newText);
        assertTrue(newText.contains(sampleText));
        assertTrue(newText.contains("soap"));
        assertTrue(newText.contains("Envelope"));
        assertTrue(newText.contains("Body"));
        
        assertTrue(m.getProtocol().equals(Protocol.soap12));
        
        SOAPEnvelope omSoapEnv = (SOAPEnvelope) m.getAsOMElement();
        OMNamespace ns = omSoapEnv.getNamespace();
        assertTrue(ns.getNamespaceURI().equals(SOAP12_NS_URI));
        
        // The block should be consumed at this point
        assertTrue(block.isConsumed());
    }
    
    /**
     * Simulate creating a SOAP 1.2 message when the business object
     * provided is the full message.
     */
    public void testCreateSoap12FromMessage() throws Exception {
        // Create a SOAP 1.2 Message
        MessageFactory mf = (MessageFactory)
            FactoryRegistry.getFactory(MessageFactory.class);
        
        // Get the BlockFactory
        XMLStringBlockFactory f = (XMLStringBlockFactory)
            FactoryRegistry.getFactory(XMLStringBlockFactory.class);
        
        // Create a Block using the sample string as the content.  This simulates
        // what occurs on the outbound JAX-WS dispatch<String> client
        Block block = f.createFrom(sampleSoap12Envelope, null, null);
        
        // Create a Message with the full XML contents that we have
        Message m = mf.createFrom(block.getXMLStreamReader(true), null);
        
        // Assuming no handlers are installed, the next thing that will happen
        // is a XMLStreamReader will be requested...to go to OM.   At this point the
        // block should be consumed.
        OMElement om = m.getAsOMElement();
        
        // To check that the output is correct, get the String contents of the 
        // reader
        Reader2Writer r2w = new Reader2Writer(om.getXMLStreamReaderWithoutCaching());
        String newText = r2w.getAsString();
        TestLogger.logger.debug(newText);
        assertTrue(newText.contains(sampleText));
        assertTrue(newText.contains("soap"));
        assertTrue(newText.contains("Envelope"));
        assertTrue(newText.contains("Body"));
        
        assertTrue(m.getProtocol().equals(Protocol.soap12));
        
        SOAPEnvelope omSoapEnv = (SOAPEnvelope) m.getAsOMElement();
        OMNamespace ns = omSoapEnv.getNamespace();
        assertTrue(ns.getNamespaceURI().equals(SOAP12_NS_URI));
        
        // The block should be consumed at this point
        assertTrue(block.isConsumed());
    }
    
    public void testGetPayloadFromSoap12() throws Exception {
        // On inbound, there will already be an OM
        // which represents the message.  The following code simulates the input
        // OM
        StringReader sr = new StringReader(sampleSoap12Envelope);
        XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow, null);
        OMElement omElement = builder.getSOAPEnvelope();
        
        // The JAX-WS layer creates a Message from the OM
        MessageFactory mf = (MessageFactory)
            FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.createFrom(omElement, null);
        
        // Make sure the right Protocol was set on the Message
        assertTrue(m.getProtocol().equals(Protocol.soap12));
        
        // Check the SOAPEnvelope to make sure we've got the right
        // protocol namespace there as well.
        SOAPEnvelope soapEnv = (SOAPEnvelope) m.getAsOMElement();
        OMNamespace ns = soapEnv.getNamespace();
        assertTrue(ns.getNamespaceURI().equals(SOAP12_NS_URI));
        
        // Assuming no handlers are installed, the next thing that will happen
        // is the proxy code will ask for the business object (String).
        XMLStringBlockFactory blockFactory = 
            (XMLStringBlockFactory) FactoryRegistry.getFactory(XMLStringBlockFactory.class);
        Block block = m.getBodyBlock(null, blockFactory);
        Object bo = block.getBusinessObject(true);
        assertTrue(bo instanceof String);
        
        // The block should be consumed
        assertTrue(block.isConsumed());
        
        // Check the String for accuracy
        assertTrue(sampleText.equals(bo));
    }
    
    public void testGetMessageFromSoap12() throws Exception {
        // On inbound, there will already be an OM
        // which represents the message.  The following code simulates the input
        // OM
        StringReader sr = new StringReader(sampleSoap12Envelope);
        XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow, null);
        OMElement omElement = builder.getSOAPEnvelope();
        
        // The JAX-WS layer creates a Message from the OM
        MessageFactory mf = (MessageFactory)
            FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.createFrom(omElement, null);
        
        // Make sure the right Protocol was set on the Message
        assertTrue(m.getProtocol().equals(Protocol.soap12));
        
        // Check the SOAPEnvelope to make sure we've got the right
        // protocol namespace there as well.
        SOAPEnvelope soapEnv = (SOAPEnvelope) m.getAsOMElement();
        OMNamespace ns = soapEnv.getNamespace();
        assertTrue(ns.getNamespaceURI().equals(SOAP12_NS_URI));
        
        // Assuming no handlers are installed, the next thing that will happen
        // is the proxy code will ask for the business object (String).
        XMLStringBlockFactory blockFactory = 
            (XMLStringBlockFactory) FactoryRegistry.getFactory(XMLStringBlockFactory.class);
        Block block = blockFactory.createFrom(m.getAsOMElement(), null, null);
        Object bo = block.getBusinessObject(true);
        assertTrue(bo instanceof String);
        
        // The block should be consumed
        assertTrue(block.isConsumed());
        
        // Check the String for accuracy
        assertTrue(((String)bo).contains("<soapenv:Body><echo>test string</echo></soapenv:Body>"));
    }
}
