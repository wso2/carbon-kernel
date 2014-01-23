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
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.datasource.jaxb.JAXBCustomBuilder;
import org.apache.axis2.datasource.jaxb.JAXBDSContext;
import org.apache.axis2.datasource.jaxb.JAXBDataSource;
import org.apache.axis2.jaxws.addressing.SubmissionEndpointReference;
import org.apache.axis2.jaxws.addressing.SubmissionEndpointReferenceBuilder;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.databinding.JAXBUtils;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.SAAJConverterFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.message.util.SAAJConverter;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.unitTest.TestLogger;

import test.EchoStringResponse;
import test.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.TreeSet;

/**
 * MessageTests
 * Tests to create and validate Message processing
 * These are not client/server tests.  
 * Instead the tests simulate the processing of a Message during client/server processing.
 */
public class MessageTests extends TestCase {

	// String test variables
	private static final String soap11env = "http://schemas.xmlsoap.org/soap/envelope/";
	private static final String soap12env = "http://www.w3.org/2003/05/soap-envelope";
    private static final String sampleEnvelopeHead11 =
        "<soapenv:Envelope xmlns:soapenv=\"" + soap11env + "\">" +
        "<soapenv:Header /><soapenv:Body>";
    
    private static final String sampleEnvelopeHead12 =
        "<soapenv:Envelope xmlns:soapenv=\"" + soap12env + "\">" +
        "<soapenv:Header /><soapenv:Body>";
    
    private static final String sampleEnvelopeTail = 
        "</soapenv:Body></soapenv:Envelope>";
    
    private static final String sampleText =
		"<pre:a xmlns:pre=\"urn://sample\">" +
		"<b>Hello</b>" +
		"<c>World</c>" +
		"</pre:a>";
    
    private static final String sampleDouble =
        "<pre:a xmlns:pre=\"urn://sample\">" +
        "<b>Hello</b>" +
        "<c>World</c>" +
        "</pre:a>" +
        "<pre:aa xmlns:pre=\"urn://sample\">" +
        "<b>Hello</b>" +
        "<c>World</c>" +
        "</pre:aa>";
	
    private static final String sampleEnvelope11 = 
        sampleEnvelopeHead11 +
        sampleText +
        sampleEnvelopeTail;
    
    private static final String sampleEnvelope12 = 
        sampleEnvelopeHead12 +
        sampleText +
        sampleEnvelopeTail;
        
    private static final String sampleJAXBText = 
        "<echoStringResponse xmlns=\"http://test\">" +
        "<echoStringReturn>sample return value</echoStringReturn>" + 
        "</echoStringResponse>";
    
    private static final String sampleJAXBEnvelope11 = 
        sampleEnvelopeHead11 + 
        sampleJAXBText + 
        sampleEnvelopeTail;
    
    private static final String sampleJAXBEnvelope12 = 
        sampleEnvelopeHead12 + 
        sampleJAXBText + 
        sampleEnvelopeTail;

    private static final String sampleEnvelopeNoHeader11 =
        "<soapenv:Envelope xmlns:soapenv=\""+ soap11env +"\">" +
        "<soapenv:Body>" + 
        sampleText + 
        "</soapenv:Body></soapenv:Envelope>";
    
    private static final String sampleEnvelopeNoHeader12 =
        "<soapenv:Envelope xmlns:soapenv=\""+ soap12env +"\">" +
        "<soapenv:Body>" + 
        sampleText + 
        "</soapenv:Body></soapenv:Envelope>";
    
    
    private static final QName sampleQName = new QName("urn://sample", "a");

    private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    
    private W3CEndpointReference w3cEPR;
    private SubmissionEndpointReference subEPR;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        W3CEndpointReferenceBuilder w3cBuilder = new W3CEndpointReferenceBuilder();
        w3cBuilder = w3cBuilder.address("http://somewhere.com/somehow");
        w3cBuilder = w3cBuilder.serviceName(new QName("http://test", "TestService"));
        w3cBuilder = w3cBuilder.endpointName(new QName("http://test", "TestPort"));
        w3cEPR = w3cBuilder.build();
        
        SubmissionEndpointReferenceBuilder subBuilder = new SubmissionEndpointReferenceBuilder();
        subBuilder = subBuilder.address("http://somewhere.com/somehow");
        subBuilder = subBuilder.serviceName(new QName("http://test", "TestService"));
        subBuilder = subBuilder.endpointName(new QName("http://test", "TestPort"));
        subEPR = subBuilder.build();
    }

    public MessageTests() {
        super();
    }

    public MessageTests(String arg0) {
        super(arg0);
    }

    /**
     * Create a Block representing an XMLString and simulate a normal Dispatch<String> flow. In
     * addition the test makes sure that the XMLString block is not expanded during this process.
     * (Expanding the block degrades performance).
     * 
     * @throws Exception
     */
    public void testStringOutflow() throws Exception {

        // Create a SOAP 1.1 Message
        MessageFactory mf = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.create(Protocol.soap11);

        // Get the BlockFactory
        XMLStringBlockFactory f =
                (XMLStringBlockFactory) FactoryRegistry.getFactory(XMLStringBlockFactory.class);

        // Create a Block using the sample string as the content. This simulates
        // what occurs on the outbound JAX-WS dispatch<String> client
        Block block = f.createFrom(sampleText, null, null);

        // Add the block to the message as normal body content.
        m.setBodyBlock(block);

        // Check to see if the message is a fault. The client/server will always call this method.
        // The Message must respond appropriately without doing a conversion.
        boolean isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                   "SPINE".equals(m.getXMLPartContentType()));

        // On an outbound flow, we need to convert the Message
        // to an OMElement, specifically an OM SOAPEnvelope,
        // so we can set it on the Axis2 MessageContext
        org.apache.axiom.soap.SOAPEnvelope env =
                (org.apache.axiom.soap.SOAPEnvelope) m.getAsOMElement();

        // Check to see if the message is a fault. The client/server will always call this method.
        // The Message must respond appropriately without doing a conversion.
        isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                   "OM".equals(m.getXMLPartContentType()));

        // PERFORMANCE CHECK:
        // The element in the body should be an OMSourcedElement
        OMElement o = env.getBody().getFirstElement();
        assertTrue(o instanceof OMSourcedElementImpl);
        assertTrue(((OMSourcedElementImpl) o).isExpanded() == false);

        // Serialize the Envelope using the same mechanism as the
        // HTTP client.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        env.serializeAndConsume(baos, new OMOutputFormat());

        String newText = baos.toString();
        TestLogger.logger.debug(newText);
        assertTrue(newText.contains(sampleText));
        assertTrue(newText.contains("soap"));
        assertTrue(newText.contains("Envelope"));
        assertTrue(newText.contains("Body"));
    }

    /**
     * Create a Block representing an XMLString and simulate a normal Dispatch<String> flow with an
     * application handler.
     * 
     * @throws Exception
     */
    public void testStringOutflow2() throws Exception {

        // Create a SOAP 1.1 Message
        MessageFactory mf = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.create(Protocol.soap11);

        // Get the BlockFactory
        XMLStringBlockFactory f =
                (XMLStringBlockFactory) FactoryRegistry.getFactory(XMLStringBlockFactory.class);

        // Create a Block using the sample string as the content. This simulates
        // what occurs on the outbound JAX-WS dispatch<String> client
        Block block = f.createFrom(sampleText, null, null);

        // Add the block to the message as normal body content.
        m.setBodyBlock(block);

        // If there is a JAX-WS handler, the Message is converted into a SOAPEnvelope
        SOAPEnvelope soapEnvelope = m.getAsSOAPEnvelope();

        // Check to see if the message is a fault. The client/server will always call this method.
        // The Message must respond appropriately without doing a conversion.
        boolean isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                   "SOAPENVELOPE".equals(m.getXMLPartContentType()));

        // Normally the handler would not touch the body...but for our scenario, assume that it
        // does.
        String name = soapEnvelope.getBody().getFirstChild().getLocalName();
        assertTrue("a".equals(name));

        // The block should be consumed at this point
        assertTrue(block.isConsumed());

        // After the handler processing the message is obtained as an OM
        OMElement om = m.getAsOMElement();

        // Serialize the Envelope using the same mechanism as the
        // HTTP client.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        om.serializeAndConsume(baos, new OMOutputFormat());

        // To check that the output is correct, get the String contents of the
        // reader
        String newText = baos.toString();
        TestLogger.logger.debug(newText);
        assertTrue(newText.contains(sampleText));
        assertTrue(newText.contains("soap"));
        assertTrue(newText.contains("Envelope"));
        assertTrue(newText.contains("Body"));

    }
    
    /**
     * Create a Block representing an empty XMLString and simulate a 
     * normal Dispatch<String> flow with an application handler.
     * @throws Exception
     * 
     * DISABLED THIS TEST. THE TEST IS NOT VALID BECAUSE AN ATTEMPT WAS 
     * MADE TO ADD A BLOCK THAT IS NOT AN ELEMENT.
     */
    public void _testStringOutflowEmptyString() throws Exception {
        
        // Create a SOAP 1.1 Message
        MessageFactory mf = (MessageFactory)
            FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.create(Protocol.soap11);
        
        // Get the BlockFactory
        XMLStringBlockFactory f = (XMLStringBlockFactory)
            FactoryRegistry.getFactory(XMLStringBlockFactory.class);
        
        // Sample text is whitespace.  There is no element
        
        String whiteSpaceText = "<!-- Comment -->";
        // Create a Block using the sample string as the content.  This simulates
        // what occurs on the outbound JAX-WS dispatch<String> client
        Block block = f.createFrom(whiteSpaceText, null, null);
        
        // Add the block to the message as normal body content.
        m.setBodyBlock(block);
        
        // If there is a JAX-WS handler, the Message is converted into a SOAPEnvelope
        SOAPEnvelope soapEnvelope = m.getAsSOAPEnvelope();
        
        // Check to see if the message is a fault.  The client/server will always call this method.
        // The Message must respond appropriately without doing a conversion.
        boolean isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                    "SOAPENVELOPE".equals(m.getXMLPartContentType()));
        
        // Normally the handler would not touch the body...but for our scenario, 
        // assume that it does.
        // The whitespace is not preserved, so there should be no first child in the body
        assertTrue(soapEnvelope.getBody().getFirstChild() == null);
        
        // The block should be consumed at this point
        assertTrue(block.isConsumed());
        
        // After the handler processing the message is obtained as an OM
        OMElement om = m.getAsOMElement();
                
        // Serialize the Envelope using the same mechanism as the 
        // HTTP client.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        om.serializeAndConsume(baos, new OMOutputFormat());
        
        // To check that the output is correct, get the String contents of the 
        // reader
        String newText = baos.toString();
        TestLogger.logger.debug(newText);
        assertTrue(newText.contains("soap"));
        assertTrue(newText.contains("Envelope"));
        assertTrue(newText.contains("Body"));
        
    }
	
    /**
     * Create a Block representing an XMLString with 2 elements and simulate a 
     * normal Dispatch<String> flow with an application handler.
     * @throws Exception
     * 
     * @REVIEW This test is disabled because (a) it fails and (b) we don't believe this
     * is allowed due by WSI.
     */
    public void _testStringOutflowDoubleElement() throws Exception {
        
        // Create a SOAP 1.1 Message
        MessageFactory mf = (MessageFactory)
            FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.create(Protocol.soap11);
        
        // Get the BlockFactory
        XMLStringBlockFactory f = (XMLStringBlockFactory)
            FactoryRegistry.getFactory(XMLStringBlockFactory.class);
        
        // Create a Block using the sample string as the content.  This simulates
        // what occurs on the outbound JAX-WS dispatch<String> client
        // In this case the sample string contains 2 elements a and aa
        Block block = f.createFrom(this.sampleDouble, null, null);
        
        // Add the block to the message as normal body content.
        m.setBodyBlock(block);
        
        // If there is a JAX-WS handler, the Message is converted into a SOAPEnvelope
        SOAPEnvelope soapEnvelope = m.getAsSOAPEnvelope();
        
        // Check to see if the message is a fault.  The client/server will always call this method.
        // The Message must respond appropriately without doing a conversion.
        boolean isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                    "SOAPENVELOPE".equals(m.getXMLPartContentType()));
        
        // Normally the handler would not touch the body...but for our scenario, 
        // assume that it does.
        String name = soapEnvelope.getBody().getFirstChild().getLocalName();
        assertTrue("a".equals(name));
        name = soapEnvelope.getBody().getLastChild().getLocalName();
        assertTrue("aa".equals(name));
        
        // The block should be consumed at this point
        assertTrue(block.isConsumed());
        
        // After the handler processing the message is obtained as an OM
        OMElement om = m.getAsOMElement();
                
        // Serialize the Envelope using the same mechanism as the 
        // HTTP client.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        om.serializeAndConsume(baos, new OMOutputFormat());
        
        // To check that the output is correct, get the String contents of the 
        // reader
        String newText = baos.toString();
        TestLogger.logger.debug(newText);
        assertTrue(newText.contains(sampleText));
        assertTrue(newText.contains("soap"));
        assertTrue(newText.contains("Envelope"));
        assertTrue(newText.contains("Body"));
        
    }
    
	/**
     * Create a Block representing an XMLString and simulate a normal Dispatch<String> input flow
     * 
     * @throws Exception
     */
    public void testStringInflow_soap11() throws Exception {
        _testStringInflow(sampleEnvelope11);
    }

    public void testStringInflow_soap12() throws Exception {
        _testStringInflow(sampleEnvelope12);
    }

    public void _testStringInflow(String sampleEnvelope) throws Exception {

        // On inbound, there will already be an OM
        // which represents the message. The following code simulates the input
        // OM
        StringReader sr = new StringReader(sampleEnvelope);
        XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow, null);
        OMElement omElement = builder.getSOAPEnvelope();

        // The JAX-WS layer creates a Message from the OM
        MessageFactory mf = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.createFrom(omElement, null);

        // Check to see if the message is a fault. The client/server will always call this method.
        // The Message must respond appropriately without doing a conversion.
        boolean isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                   "OM".equals(m.getXMLPartContentType()));

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
        assertTrue(sampleText.equals(bo.toString()));

    }

    /**
     * Create a Block representing an XMLString and simulate a normal Dispatch<String> input flow
     * with a JAX-WS Handler
     * 
     * @throws Exception
     */
    public void testStringInflow2_soap11() throws Exception {
        _testStringInflow2(sampleEnvelope11);
    }

    public void testStringInflow2_soap12() throws Exception {
        // Only run test if an SAAJ 1.3 MessageFactory is available
        javax.xml.soap.MessageFactory mf = null;
        try {
            mf = getSAAJConverter().createMessageFactory(soap12env);
        } catch (Exception e) {
        }
        if (mf != null) {
            _testStringInflow2(sampleEnvelope12);
        }
    }

    public void _testStringInflow2(String sampleEnvelope) throws Exception {

        // On inbound, there will already be an OM
        // which represents the message. The following code simulates the input
        // OM
        StringReader sr = new StringReader(sampleEnvelope);
        XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow, null);
        OMElement omElement = builder.getSOAPEnvelope();

        // The JAX-WS layer creates a Message from the OM
        MessageFactory mf = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.createFrom(omElement, null);

        // Check to see if the message is a fault. The client/server will always call this method.
        // The Message must respond appropriately without doing a conversion.
        boolean isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                   "OM".equals(m.getXMLPartContentType()));

        // If there is a JAX-WS handler, the Message is converted into a SOAPEnvelope
        SOAPEnvelope soapEnvelope = m.getAsSOAPEnvelope();

        // Check to see if the message is a fault. The client/server will always call this method.
        // The Message must respond appropriately without doing a conversion.
        isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                   "SOAPENVELOPE".equals(m.getXMLPartContentType()));

        // Normally the handler would not touch the body...but for our scenario, assume that it
        // does.
        String name = soapEnvelope.getBody().getFirstChild().getLocalName();
        assertTrue("a".equals(name));

        // The next thing that will happen
        // is the proxy code will ask for the business object (String).
        XMLStringBlockFactory blockFactory =
                (XMLStringBlockFactory) FactoryRegistry.getFactory(XMLStringBlockFactory.class);
        Block block = m.getBodyBlock(null, blockFactory);
        Object bo = block.getBusinessObject(true);
        assertTrue(bo instanceof String);

        // The block should be consumed
        assertTrue(block.isConsumed());

        // Check the String for accuracy
        assertTrue(sampleText.equals(bo.toString()));

    }

    /**
     * Create a Block representing an XMLString and simulate a normal Dispatch<String> input flow
     * with a JAX-WS Handler that needs the whole Message
     * 
     * @throws Exception
     */
    public void testStringInflow3_soap11() throws Exception {
        _testStringInflow3(sampleEnvelope11);
    }

    public void testStringInflow3_soap12() throws Exception {
        // Only run test if an SAAJ 1.3 MessageFactory is available
        javax.xml.soap.MessageFactory mf = null;
        try {
            mf = getSAAJConverter().createMessageFactory(soap12env);
        } catch (Exception e) {
        }
        if (mf != null) {
            _testStringInflow3(sampleEnvelope12);
        }
    }

    public void _testStringInflow3(String sampleEnvelope) throws Exception {

        // On inbound, there will already be an OM
        // which represents the message. The following code simulates the input
        // OM
        StringReader sr = new StringReader(sampleEnvelope);
        XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow, null);
        OMElement omElement = builder.getSOAPEnvelope();

        // The JAX-WS layer creates a Message from the OM
        MessageFactory mf = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.createFrom(omElement, null);

        // Check to see if the message is a fault. The client/server will always call this method.
        // The Message must respond appropriately without doing a conversion.
        boolean isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                   "OM".equals(m.getXMLPartContentType()));

        // If there is a JAX-WS handler, the Message is converted into a SOAPEnvelope
        SOAPMessage sm = m.getAsSOAPMessage();

        // Check to see if the message is a fault. The client/server will always call this method.
        // The Message must respond appropriately without doing a conversion.
        isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                   "SOAPENVELOPE".equals(m.getXMLPartContentType()));

        // Normally the handler would not touch the body...but for our scenario, assume that it
        // does.
        String name = sm.getSOAPBody().getFirstChild().getLocalName();
        assertTrue("a".equals(name));

        // The next thing that will happen
        // is the proxy code will ask for the business object (String).
        XMLStringBlockFactory blockFactory =
                (XMLStringBlockFactory) FactoryRegistry.getFactory(XMLStringBlockFactory.class);
        Block block = m.getBodyBlock(null, blockFactory);
        Object bo = block.getBusinessObject(true);
        assertTrue(bo instanceof String);

        // The block should be consumed
        assertTrue(block.isConsumed());

        // Check the String for accuracy
        assertTrue(sampleText.equals(bo.toString()));

    }
    
    /**
     * Create a Block representing an XMLString, but this time use one that
     * doesn't have a &lt;soap:Header&gt; element in it.
     * @throws Exception
     */
	public void testStringInflow4_soap11() throws Exception {
		_testStringInflow4(sampleEnvelopeNoHeader11);
	}
	public void testStringInflow4_soap12() throws Exception {
		_testStringInflow4(sampleEnvelopeNoHeader12);
	}
	public void _testStringInflow4(String sampleEnvelopeNoHeader) throws Exception {
        // On inbound, there will already be an OM
        // which represents the message.  The following code simulates the input
        // OM
        StringReader sr = new StringReader(sampleEnvelopeNoHeader);
        XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow, null);
        OMElement omElement = builder.getSOAPEnvelope();
        
        // The JAX-WS layer creates a Message from the OM
        MessageFactory mf = (MessageFactory)
            FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.createFrom(omElement, null);
        
        // Check to see if the message is a fault.  The client/server will always call this method.
        // The Message must respond appropriately without doing a conversion.
        boolean isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                    "OM".equals(m.getXMLPartContentType()));
        
        // The next thing that will happen
        // is the proxy code will ask for the business object (String).
        XMLStringBlockFactory blockFactory = 
            (XMLStringBlockFactory) FactoryRegistry.getFactory(XMLStringBlockFactory.class);
        Block block = m.getBodyBlock(null, blockFactory);
        Object bo = block.getBusinessObject(true);
        assertTrue(bo instanceof String);
        
        // The block should be consumed
        assertTrue(block.isConsumed());
        
        // Check the String for accuracy
        assertTrue(sampleText.equals(bo.toString()));
        
    }
    
    /**
     * Create a JAXBBlock containing a JAX-B business object 
     * and simulate a normal Dispatch<Object> output flow
     * @throws Exception
     */
    public void testJAXBOutflow() throws Exception {
        // Create a SOAP 1.1 Message
        MessageFactory mf = (MessageFactory)
            FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.create(Protocol.soap11);
        
        // Get the BlockFactory
        JAXBBlockFactory bf = (JAXBBlockFactory)
            FactoryRegistry.getFactory(JAXBBlockFactory.class);
        
        // Create the JAX-B object
        ObjectFactory of = new ObjectFactory();
        EchoStringResponse obj = of.createEchoStringResponse();
        obj.setEchoStringReturn("sample return value");
        
        // Create the JAXBContext
        JAXBBlockContext context = 
            new JAXBBlockContext(EchoStringResponse.class.getPackage().getName());
        
        // Create a JAXBBlock using the Echo object as the content.  This simulates
        // what occurs on the outbound JAX-WS Dispatch<Object> client
        Block block = bf.createFrom(obj, context, null);
        
        // Add the block to the message as normal body content.
        m.setBodyBlock(block);
        
        // Check to see if the message is a fault.  The client/server will always call this method.
        // The Message must respond appropriately without doing a conversion.
        boolean isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                    "SPINE".equals(m.getXMLPartContentType()));
        
        // On an outbound flow, we need to convert the Message 
        // to an OMElement, specifically an OM SOAPEnvelope, 
        // so we can set it on the Axis2 MessageContext
        org.apache.axiom.soap.SOAPEnvelope env = 
            (org.apache.axiom.soap.SOAPEnvelope) m.getAsOMElement();
        
        // Check to see if the message is a fault.  The client/server will always call this method.
        // The Message must respond appropriately without doing a conversion.
        isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                    "OM".equals(m.getXMLPartContentType()));
        
        // Serialize the Envelope using the same mechanism as the 
        // HTTP client.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        env.serializeAndConsume(baos, new OMOutputFormat());
        
        // To check that the output is correct, get the String contents of the 
        // reader
        String newText = baos.toString();
        TestLogger.logger.debug(newText);
        assertTrue(newText.contains(sampleJAXBText));
        assertTrue(newText.contains("soap"));
        assertTrue(newText.contains("Envelope"));
        assertTrue(newText.contains("Body"));
    }
    
    /**
     * Create a JAXBBlock containing a JAX-B business object 
     * and simulate a normal Dispatch<Object> output flow
     * @throws Exception
     */
    public void testJAXBOutflow_W3CEndpointReference() throws Exception {
        // Create a SOAP 1.1 Message
        MessageFactory mf = (MessageFactory)
            FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.create(Protocol.soap11);
        
        // Get the BlockFactory
        JAXBBlockFactory bf = (JAXBBlockFactory)
            FactoryRegistry.getFactory(JAXBBlockFactory.class);
        
        // Create the JAX-B object... a W3CEndpointReference
        W3CEndpointReference obj = w3cEPR;
       
        
        // Create the JAXBContext
        Class[] classes = new Class[] {W3CEndpointReference.class};
        //JAXBContext jaxbContext = JAXBContext.newInstance(classes);
        //JAXBBlockContext context = new JAXBBlockContext(jaxbContext);
        JAXBBlockContext context = new JAXBBlockContext("javax.xml.ws.wsaddressing");
        
        TestLogger.logger.debug("JAXBContext= " + context);
        
        // Create a JAXBBlock using the Echo object as the content.  This simulates
        // what occurs on the outbound JAX-WS Dispatch<Object> client
        Block block = bf.createFrom(obj, context, null);
        
        // Add the block to the message as normal body content.
        m.setBodyBlock(block);
        
        // Check to see if the message is a fault.  The client/server will always call this method.
        // The Message must respond appropriately without doing a conversion.
        boolean isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                    "SPINE".equals(m.getXMLPartContentType()));
        
        // On an outbound flow, we need to convert the Message 
        // to an OMElement, specifically an OM SOAPEnvelope, 
        // so we can set it on the Axis2 MessageContext
        org.apache.axiom.soap.SOAPEnvelope env = 
            (org.apache.axiom.soap.SOAPEnvelope) m.getAsOMElement();
        
        // Check to see if the message is a fault.  The client/server will always call this method.
        // The Message must respond appropriately without doing a conversion.
        isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                    "OM".equals(m.getXMLPartContentType()));
        
        // Serialize the Envelope using the same mechanism as the 
        // HTTP client.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        env.serializeAndConsume(baos, new OMOutputFormat());
        
        // To check that the output is correct, get the String contents of the 
        // reader
        String newText = baos.toString();
        TestLogger.logger.debug(newText);
        assertTrue(newText.contains("http://somewhere.com/somehow"));
        assertTrue(newText.contains("soap"));
        assertTrue(newText.contains("Envelope"));
        assertTrue(newText.contains("Body"));
    }
    
    /**
     * Same as JAXBOutputflow, but has an additional check
     * to make sure that the JAXB serialization is deferrred
     * until the actual serialization of the message.
     * @throws Exception
     */
    public void testJAXBOutflowPerf() throws Exception {
        // Create a SOAP 1.1 Message
        MessageFactory mf = (MessageFactory)
            FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.create(Protocol.soap11);
        
        // Get the BlockFactory
        JAXBBlockFactory bf = (JAXBBlockFactory)
            FactoryRegistry.getFactory(JAXBBlockFactory.class);
        
        // Create the JAX-B object
        ObjectFactory of = new ObjectFactory();
        EchoStringResponse obj = of.createEchoStringResponse();
        obj.setEchoStringReturn("sample return value");
        
        // Create the JAXBContext
        JAXBBlockContext context = 
            new JAXBBlockContext(EchoStringResponse.class.getPackage().getName());
       
        // Create a JAXBBlock using the Echo object as the content.  This simulates
        // what occurs on the outbound JAX-WS Dispatch<Object> client
        Block block = bf.createFrom(obj, context, null);
        
        // Add the block to the message as normal body content.
        m.setBodyBlock(block);
        
        // Check to see if the message is a fault.  The client/server will always call this method.
        // The Message must respond appropriately without doing a conversion.
        boolean isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                    "SPINE".equals(m.getXMLPartContentType()));
        
        // On an outbound flow, we need to convert the Message 
        // to an OMElement, specifically an OM SOAPEnvelope, 
        // so we can set it on the Axis2 MessageContext
        org.apache.axiom.soap.SOAPEnvelope env = 
            (org.apache.axiom.soap.SOAPEnvelope) m.getAsOMElement();
        
        // Check to see if the message is a fault.  The client/server will always call this method.
        // The Message must respond appropriately without doing a conversion.
        isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                    "OM".equals(m.getXMLPartContentType()));
        
        // PERFORMANCE CHECK:
        // The element in the body should be an OMSourcedElement
        OMElement o = env.getBody().getFirstElement();
        assertTrue(o instanceof OMSourcedElementImpl);
        assertTrue(((OMSourcedElementImpl)o).isExpanded() == false);
        
        // Serialize the Envelope using the same mechanism as the 
        // HTTP client.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        env.serializeAndConsume(baos, new OMOutputFormat());
        
        // To check that the output is correct, get the String contents of the 
        // reader
        String newText = baos.toString();
        TestLogger.logger.debug(newText);
        assertTrue(newText.contains(sampleJAXBText));
        assertTrue(newText.contains("soap"));
        assertTrue(newText.contains("Envelope"));
        assertTrue(newText.contains("Body"));
    }
    
    private final int NO_PERSIST       = 0;
    private final int PERSIST         = 1;
    private final int SAVE_AND_PERSIST = 2;
    public void testJAXBInflow_soap11() throws Exception {
        _testJAXBInflow(sampleJAXBEnvelope11, NO_PERSIST, false);
    }
    public void testJAXBInflow_soap11_withCustomBuilder() throws Exception {
        _testJAXBInflow(sampleJAXBEnvelope11, NO_PERSIST, true);
    }
    public void testJAXBInflow_soap12() throws Exception {
        _testJAXBInflow(sampleJAXBEnvelope12, NO_PERSIST, false);
    }
    public void testJAXBInflow_soap11_withPersist() throws Exception {
        _testJAXBInflow(sampleJAXBEnvelope11, PERSIST, false);
    }
    public void testJAXBInflow_soap12_withPersist() throws Exception {
        _testJAXBInflow(sampleJAXBEnvelope12, PERSIST, false);
    }
    public void testJAXBInflow_soap11_withSaveAndPersist() throws Exception {
        _testJAXBInflow(sampleJAXBEnvelope11, SAVE_AND_PERSIST, false);
    }
    public void testJAXBInflow_soap12_withSaveAndPersist() throws Exception {
        _testJAXBInflow(sampleJAXBEnvelope12, SAVE_AND_PERSIST, false);
    }
    public void _testJAXBInflow(String sampleJAXBEnvelope, int persist, boolean installJAXBCustomBuilder) throws Exception {
        // Create a SOAP OM out of the sample incoming XML.  This
        // simulates what Axis2 will be doing with the inbound message. 
        StringReader sr = new StringReader(sampleJAXBEnvelope);
        XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow, null);
        OMElement omElement = builder.getSOAPEnvelope();
        
        JAXBDSContext jds = null;
        if (installJAXBCustomBuilder) {
            jds = new JAXBDSContext(EchoStringResponse.class.getPackage().getName());
            JAXBCustomBuilder jcb = new JAXBCustomBuilder(jds);
            builder.registerCustomBuilderForPayload(jcb);
        }
        
        // Create a SOAP 1.1 Message from the sample incoming XML
        MessageFactory mf = (MessageFactory)
            FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.createFrom(omElement, null);
        
        // Check to see if the message is a fault.  The client/server will always call this method.
        // The Message must respond appropriately without doing a conversion.
        boolean isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                    "OM".equals(m.getXMLPartContentType()));
        
        if (installJAXBCustomBuilder) {
            // The JAXBDSContext and the JAXBUtils access the JAXBContext
            // for the "test" package.
            // The JAXBContext creation is very expensive.
            // However the JAXBContext can also be very large.
            // 
            // For these reasons, the JAXBUtils code caches the JAXBContext values.
            // And, the JAXBDSContext and JAXBUtils code use WeakReferences to refer 
            // to the JAXBContext (so that it is easily gc'd).
            
            // The following code checks makes sure that the caching and gc is correct.
            
            // Get the JAXBContext
            JAXBContext context = jds.getJAXBContext();
            
            // Get the identity hash code.  This is an indicator of the unique memory pointer
            // for the context.
            int contextPointer = System.identityHashCode(context);
            
            // Release our access to the context.
            // Now the only accesses in the system should be "soft refs"
            context = null;
            
            // Force garbage collection
            System.gc();
            
            // Get a new context from JAXBUtils
            TreeSet<String> packages = new TreeSet<String>();
            packages.add(EchoStringResponse.class.getPackage().getName());
            context = JAXBUtils.getJAXBContext(packages);
            
            // This new context should have a different pointer than the original
            int contextPointer2 = System.identityHashCode(context);
            
            // The following statement is no longer valid.  The 
            // JAXBContext is stored in a SoftReference (which will only
            // get gc'd if memory is low).  
            //assertTrue(contextPointer != contextPointer2);
            
            // Release the hold on the context
            context = null;
            
            // Now call JAXBUtils again to get a JAXBContext.
            // Since there was no intervening gc(), it should return the cached value
            context = JAXBUtils.getJAXBContext(packages);
            int contextPointer3 = System.identityHashCode(context);
            assertTrue(contextPointer3 == contextPointer2);
            
           
        }
        
        String saveMsgText = "";
        if (persist == SAVE_AND_PERSIST) {
            // Simulate saving the message so that it can be fully rebuilt.
            saveMsgText = m.getAsOMElement().toString();
        }
        
        Object customBuiltObject = null;
        if (installJAXBCustomBuilder) {
            OMElement om = m.getAsOMElement();
            om = ((org.apache.axiom.soap.SOAPEnvelope) om).getBody().getFirstElement();
            if (om instanceof OMSourcedElement &&
                ((OMSourcedElement) om).getDataSource() instanceof JAXBDataSource) {
                customBuiltObject = ((JAXBDataSource) ((OMSourcedElement) om).getDataSource()).getObject();
            }
        }
        
        
        // Get the BlockFactory
        JAXBBlockFactory bf = (JAXBBlockFactory)
            FactoryRegistry.getFactory(JAXBBlockFactory.class);
        
        // Create the JAXBContext instance that will be used
        // to deserialize the JAX-B object content in the message.
        JAXBBlockContext context = 
            new JAXBBlockContext(EchoStringResponse.class.getPackage().getName());
        
        // Get the JAXBBlock that wraps the content
        Block b = m.getBodyBlock(context, bf);
     
        // Check to see if the message is a fault.  The client/server will always call this method.
        // The Message must respond appropriately without doing a conversion.
        isFault = m.isFault();
        assertTrue(!isFault);
        assertTrue("XMLPart Representation is " + m.getXMLPartContentType(),
                    "SPINE".equals(m.getXMLPartContentType()));
        
        // Get the business object from the block, which should be a 
        // JAX-B object
        Object bo = b.getBusinessObject(false);
        m.setPostPivot();
        
        // Simulate restoring the message
        if (persist == SAVE_AND_PERSIST) {
            sr = new StringReader(saveMsgText);
            XMLStreamReader saveMsgReader = inputFactory.createXMLStreamReader(sr);
            builder = new StAXSOAPModelBuilder(saveMsgReader, null);
            omElement = builder.getSOAPEnvelope();
            m = mf.createFrom(omElement, null);
        } 
        
        if (installJAXBCustomBuilder) {
            // If installed jaxb custom builder, then custom built object should be same object as the business object
            assertTrue (customBuiltObject == bo);
        }
        
        
        // Check to make sure the right object was returned
        assertNotNull(bo);
        assertTrue(bo instanceof EchoStringResponse);
        
        // Check to make sure the content of that object is correct
        EchoStringResponse esr = (EchoStringResponse) bo;
        assertNotNull(esr.getEchoStringReturn());
        assertTrue(esr.getEchoStringReturn().equals("sample return value"));
        
        // Simulate outbound
        if (persist == PERSIST) {
            String persistMsg = m.getAsOMElement().toString();
            // We should be able to persist the message, 
            // and the persisted message WILL contain the echoStringResponse contents
            assertTrue(persistMsg.contains("Body"));
            assertTrue(persistMsg.contains("echoStringResponse"));
            assertTrue(persistMsg.contains("sample return value"));
            
        } else if (persist == SAVE_AND_PERSIST) {
            String persistMsg = m.getAsOMElement().toString();
            // We should be able to persist the message, 
            // and the persisted message WILL contain the echoStringResponse contents
            assertTrue(persistMsg.contains("Body"));
            assertTrue(persistMsg.contains("echoStringResponse"));
            assertTrue(persistMsg.contains("sample return value"));
        }
        
        // After persistance: Sandesha may inspect the body.  Make sure this does not cause an error
        org.apache.axiom.soap.SOAPEnvelope env = (org.apache.axiom.soap.SOAPEnvelope) m.getAsOMElement();
        QName qName = new QName("uri://fake", "fake");
        env.getBody().getFirstChildWithName(qName);
    }

    SAAJConverter converter = null;

    private SAAJConverter getSAAJConverter() {
        if (converter == null) {
            SAAJConverterFactory factory =
                    (SAAJConverterFactory) FactoryRegistry.getFactory(SAAJConverterFactory.class);
            converter = factory.getSAAJConverter();
        }
        return converter;
    }
}
