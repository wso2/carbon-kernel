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
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.jaxws.message.databinding.JAXBBlockContext;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.headers.ConfigBody;
import org.apache.axis2.jaxws.message.headers.ConfigHeader;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.unitTest.TestLogger;
import org.test.stock1.ObjectFactory;
import org.test.stock1.StockPrice;

import javax.jws.soap.SOAPBinding.Style;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;

/**
 * MessageTests
 * Tests to create and validate Message processing
 * These are not client/server tests.  Instead the tests simulate the processing of a Message during
 * client/server processing.
 */
public class MessageRPCTests extends TestCase {

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
        "<m:op xmlns:m='urn://sample'>" +
		"<m:param xmlns:pre='urn://stock1.test.org' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:type='pre:StockPrice'>" +
		"<price>100</price>" +
		"</m:param>" +
        "</m:op>";
	
    private static final String sampleEnvelope11 = 
        sampleEnvelopeHead11 +
        sampleText +
        sampleEnvelopeTail;
    
    private static final String sampleEnvelope12 = 
        sampleEnvelopeHead12 +
        sampleText +
        sampleEnvelopeTail;
        
	
	private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	
	public MessageRPCTests() {
		super();
	}

	public MessageRPCTests(String arg0) {
		super(arg0);
	}
	
	
    
    /**
     * Create a JAXBBlock containing a JAX-B business object 
     * and simulate a normal Proxy output flow
     * @throws Exception
     */
    public void testJAXBOutflow() throws Exception {
        // Create a SOAP 1.1 Message
        MessageFactory mf = (MessageFactory)
            FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.create(Protocol.soap11);
        
        // Indicate that the style is RPC, Indicate the Operation QName
        m.setStyle(Style.RPC);
        QName opQName = new QName("urn://sample", "op", "m");
        m.setOperationElement(opQName);
        
        // Get the BlockFactory
        JAXBBlockFactory bf = (JAXBBlockFactory)
            FactoryRegistry.getFactory(JAXBBlockFactory.class);
        
        // Create the JAXBContext
        JAXBBlockContext context = new JAXBBlockContext(StockPrice.class.getPackage().getName());
        
        // Create the JAX-B object
        ObjectFactory of = new ObjectFactory();
        StockPrice obj = of.createStockPrice();
        obj.setPrice("100");
        
        // Create the JAX-B Element
        QName paramQName = new QName("urn://sample", "param", "m");
        JAXBElement e = new JAXBElement(paramQName, StockPrice.class, obj);
        
        // Create a JAXBBlock using the param object as the content.  This simulates
        // what occurs on the outbound JAX-WS Proxy client
        Block block = bf.createFrom(e, context, null);
        
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
        assertTrue(newText.contains("m:op"));
        assertTrue(newText.contains("m:param"));
        assertTrue(newText.contains("100"));
        assertTrue(newText.contains("Envelope"));
        assertTrue(newText.contains("Body"));
    }
    
    /**
     * Create a JAXBBlock containing a JAX-B business object 
     * and simulate a normal Proxy output flow
     * @throws Exception
     */
    public void testJAXBOutflowPerf() throws Exception {
        // Create a SOAP 1.1 Message
        MessageFactory mf = (MessageFactory)
            FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.create(Protocol.soap11);
        
        // Indicate that the style is RPC, Indicate the Operation QName
        m.setStyle(Style.RPC);
        QName opQName = new QName("urn://sample", "op", "m");
        m.setOperationElement(opQName);
        
        // Get the BlockFactory
        JAXBBlockFactory bf = (JAXBBlockFactory)
            FactoryRegistry.getFactory(JAXBBlockFactory.class);
        
        // Create the JAXBContext
        JAXBBlockContext context = new JAXBBlockContext(StockPrice.class.getPackage().getName());
        
        // Create the JAX-B object
        ObjectFactory of = new ObjectFactory();
        StockPrice obj = of.createStockPrice();
        obj.setPrice("100");
        
        // Create the JAX-B Element
        QName paramQName = new QName("urn://sample", "param", "m");
        JAXBElement e = new JAXBElement(paramQName, StockPrice.class, obj);
        
        // Create a JAXBBlock using the param object as the content.  This simulates
        // what occurs on the outbound JAX-WS Proxy client
        Block block = bf.createFrom(e, context, null);
        
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
        // The param in the body should be an OMSourcedElement
        OMElement o = env.getBody().getFirstElement().getFirstElement();
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
        assertTrue(newText.contains("m:op"));
        assertTrue(newText.contains("m:param"));
        assertTrue(newText.contains("100"));
        assertTrue(newText.contains("Envelope"));
        assertTrue(newText.contains("Body"));
    }
    
    public void testJAXBInflow_soap11() throws Exception {
        _testJAXBInflow(sampleEnvelope11);
    }
    public void testJAXBInflow_soap12() throws Exception {
        _testJAXBInflow(sampleEnvelope12);
    }
    public void _testJAXBInflow(String sampleJAXBEnvelope) throws Exception {
        // Create a SOAP OM out of the sample incoming XML.  This
        // simulates what Axis2 will be doing with the inbound message. 
        StringReader sr = new StringReader(sampleJAXBEnvelope);
        XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow, null);
        OMElement omElement = builder.getSOAPEnvelope();
        
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
        
        // Indicate that the message should be accessed as RPC
        m.setStyle(Style.RPC);
        
        // Get the BlockFactory
        JAXBBlockFactory bf = (JAXBBlockFactory)
            FactoryRegistry.getFactory(JAXBBlockFactory.class);
        
        // Create the JAXBContext instance that will be used
        // to deserialize the JAX-B object content in the message.
        JAXBBlockContext context = new JAXBBlockContext(StockPrice.class.getPackage().getName());
        
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
        Object bo = b.getBusinessObject(true);
        
        // Check to make sure the right object was returned
        assertNotNull(bo);
        if (bo instanceof JAXBElement) {
            bo = ((JAXBElement) bo).getValue();
        }
        assertTrue(bo instanceof StockPrice);
        
        
        // Check to make sure the content of that object is correct
        StockPrice obj = (StockPrice) bo;
        assertNotNull(obj);
        assertTrue(obj.getPrice().equals("100"));
    }
    
    public void testJAXBHeader() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "\t<soapenv:Header>\n" +
                "\t\t<ns2:ConfigHeader2 xmlns:ns2=\"http://headers.message.jaxws.axis2.apache.org/types4\">\n" +
                "\t\t\t<message>configHeader2</message>\n" +
                "\t\t</ns2:ConfigHeader2>\n" +
                "\t\t<ns2:ConfigHeader3 xmlns:ns2=\"http://headers.message.jaxws.axis2.apache.org/types4\">\n" +
                "\t\t\t<message>xyz</message>\n" +
                "\t\t</ns2:ConfigHeader3>\n" +
                "\t</soapenv:Header>\n" +
                "\t<soapenv:Body>\n" +
                "\t\t<rpcOp:ConfigResponse xmlns:rpcOp=\"http://headers.message.jaxws.axis2.apache.org/types4\">\n" +
                "\t\t<rpcParam:ConfigBody xmlns:rpcParam=\"http://headers.message.jaxws.axis2.apache.org/types4\">\n" +
                "\t\t\t<message>Got it</message>\n" +
                "\t\t</rpcParam:ConfigBody>\n" +
                "\t\t</rpcParam:ConfigResponse>\n" +
                "\t</soapenv:Body>\n" +
                "</soapenv:Envelope>";

        // Create a SOAP OM out of the sample incoming XML.  This
        // simulates what Axis2 will be doing with the inbound message. 
        StringReader sr = new StringReader(xml);
        XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow, null);
        OMElement omElement = builder.getSOAPEnvelope();
        
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
        
        // Indicate that the message should be accessed as RPC
        m.setStyle(Style.RPC);
        
        // Get the BlockFactory
        JAXBBlockFactory bf = (JAXBBlockFactory)
            FactoryRegistry.getFactory(JAXBBlockFactory.class);
        
        // Create the JAXBContext instance that will be used
        // to deserialize the JAX-B object content in the message.
        JAXBBlockContext context = new JAXBBlockContext(ConfigHeader.class.getPackage().getName());
        
        // Check to see if the message is a fault.  The client/server will always call this method.
        // The Message must respond appropriately without doing a conversion.
        isFault = m.isFault();
        assertTrue(!isFault);

        // Get the JAXBBlock that wraps the content
        Block block = m.getHeaderBlock("http://headers.message.jaxws.axis2.apache.org/types4","ConfigHeader2", context, bf);

        // Get the business object from the block, which should be a 
        // JAX-B object
        Object bo = block.getBusinessObject(true);
        
        // Check to make sure the right object was returned
        assertNotNull(bo);
        if (bo instanceof JAXBElement) {
            bo = ((JAXBElement) bo).getValue();
        }
        assertTrue(bo instanceof ConfigHeader);
        
        Block block2 = m.getBodyBlock(context, bf);
        Object b2 = block2.getBusinessObject(true);
        if (b2 instanceof JAXBElement) {
            b2 = ((JAXBElement) b2).getValue();
        }
        assertTrue(b2 instanceof ConfigBody);
    }
}
