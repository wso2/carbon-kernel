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
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.SourceBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.unitTest.TestLogger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Locale;

/**
 * MessageTests
 * Tests to create and validate Message processing
 * These are not client/server tests.
 */
public class FaultTests extends TestCase {

	private static final String faultString = "Internal server error from WAS";
	
	// String test variables
	private static final String sampleSOAP11FaultEnvelope1 = 
		"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
		+ "<soapenv:Body>"
		+ "<soapenv:Fault>"
		+ "<faultcode>soapenv:Server</faultcode>"
		+ "<faultstring>" + faultString + "sampleSOAP11FaultEnvelope1</faultstring>"
		+ "</soapenv:Fault>"
		+ "</soapenv:Body>"
		+ "</soapenv:Envelope>";
	
    private static final String sampleSOAP11FaultEnvelope2 =
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"" +
        " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"" +
		" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
		" xmlns:cwmp=\"http://cwmp.com\">" +
		" <soapenv:Header>" +
		" <cwmp:ID soapenv:mustUnderstand=\"1\">HEADERID-7867678</cwmp:ID>" +
		" </soapenv:Header>" +
		" <soapenv:Body>" +
		" <soapenv:Fault>" +
		" <faultcode>soapenv:Client</faultcode>" +
		" <faultstring>" + faultString + "sampleSOAP11FaultEnvelope2</faultstring>" +
		" <faultactor>http://gizmos.com/order</faultactor>" +
		" <detail>" +
		" <cwmp:Fault>" +
		" <cwmp:FaultCode>This is the fault code</cwmp:FaultCode>" +
		" <cwmp:FaultString>Fault Message</cwmp:FaultString>" +
		" <cwmp:Message>This is a test fault</cwmp:Message>" +
		" </cwmp:Fault>" +
		" </detail>" + /**/
		" </soapenv:Fault>" +
		" </soapenv:Body>" +
		" </soapenv:Envelope>";
	
    private final static String sampleSOAP12FaultEnvelope1 =
        //"<?xml version='1.0' encoding='UTF-8'?>"
        "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">"
        + "<env:Body>"
        + "<env:Fault>"
        + "<env:Code><env:Value>env:Receiver</env:Value></env:Code>"
        + "<env:Reason><env:Text lang=\""+ Locale.getDefault().getLanguage() +"\">"
        + faultString + "sampleSOAP12FaultEnvelope1</env:Text></env:Reason>"
        + "</env:Fault>"
        + "</env:Body>"
        + "</env:Envelope>";
    
    // missing namespace for faultcode value
    private final static String sampleSOAP12FaultEnvelope2 =
        //"<?xml version='1.0' encoding='UTF-8'?>"
        "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">"
        + "<env:Body>"
        + "<env:Fault>"
        + "<env:Code><env:Value>Sender</env:Value></env:Code>"
        + "<env:Reason><env:Text lang=\""+ Locale.getDefault().getLanguage() +"\">"
        + faultString + "sampleSOAP12FaultEnvelope2</env:Text></env:Reason>"
        + "</env:Fault>"
        + "</env:Body>"
        + "</env:Envelope>";
    
	private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	
	public FaultTests() {
		super();
	}

	public FaultTests(String arg0) {
		super(arg0);
	}
	
	/**
	 * This test effectively tests XMLFault construction from
	 * 
	 * org.apache.axiom.soap.SOAPFault soapfault, List<Block> detailBlks
	 * 
	 * which is a client-side operation.  Also tests the "serialization" of the
	 * XMLFault object into a Message object which is a server-side operation.
	 * 
	 * @throws Exception
	 */

	public void testStringInflow1() throws Exception {
		
		try {
		// On inbound, there will already be an OM
		// which represents the message.  The following code simulates the input
		// OM
		StringReader sr = new StringReader(sampleSOAP11FaultEnvelope1);
		XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
		StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow, null);
		OMElement omElement = builder.getSOAPEnvelope();
		
		// The JAX-WS layer creates a Message from the OM
		MessageFactory mf = (MessageFactory)
			FactoryRegistry.getFactory(MessageFactory.class);
		Message m = mf.createFrom(omElement, null);
		
		assertTrue(m.isFault());
		
		if (m.isFault()) {
			XMLFault x = m.getXMLFault();
			assertEquals(faultString + "sampleSOAP11FaultEnvelope1", x.getReason().getText());
			assertEquals("Server", x.getCode().
                    toQName("http://schemas.xmlsoap.org/soap/envelope/").getLocalPart());
		} else {
			fail("Message should be marked as a fault.");
		}
		
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}

	}
	
	
	public void testStringInflow2() throws Exception {

		try {
			// On inbound, there will already be an OM
			// which represents the message. The following code simulates the
			// input
			// OM
			StringReader sr = new StringReader(sampleSOAP11FaultEnvelope2);
			XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
			StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow,
					null);
			OMElement omElement = builder.getSOAPEnvelope();

			// The JAX-WS layer creates a Message from the OM
			MessageFactory mf = (MessageFactory) FactoryRegistry
					.getFactory(MessageFactory.class);
			Message m = mf.createFrom(omElement, null);

			assertTrue(m.isFault());
			
			if (m.isFault()) {
				XMLFault x = m.getXMLFault();
				assertEquals(faultString + "sampleSOAP11FaultEnvelope2", x.getReason().getText());
                assertEquals("Client", x.getCode().
                        toQName("http://schemas.xmlsoap.org/soap/envelope/").getLocalPart());
				
				// drill down to the faultcode text in the detail to make sure it's there and it's set
				Block[] blocks = x.getDetailBlocks();
				Block block = blocks[0];
				OMElement element = block.getOMElement();
				OMElement child = (OMElement)element.getChildElements().next();
				String text = child.getText();
				
				
				assertEquals("This is the fault code", text);
			} else {
				fail("Message should be marked as a fault.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}

	}
	
	/**
	 * This test effectively tests XMLFault construction from
	 * 
	 * org.apache.axiom.soap.SOAPFault soapfault, List<Block> detailBlks
	 * 
	 * which is a client-side operation.  Also tests the "serialization" of the
	 * XMLFault object into a Message object which is a server-side operation.
	 * 
	 * @throws Exception
	 */

	public void testStringInflow3() throws Exception {
		
		try {
		// On inbound, there will already be an OM
		// which represents the message.  The following code simulates the input
		// OM
		StringReader sr = new StringReader(sampleSOAP12FaultEnvelope1);
		XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
		StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow, null);
		OMElement omElement = builder.getSOAPEnvelope();
		
		// The JAX-WS layer creates a Message from the OM
		MessageFactory mf = (MessageFactory)
			FactoryRegistry.getFactory(MessageFactory.class);
		Message m = mf.createFrom(omElement, null);
		
		assertTrue(m.isFault());
		
		if (m.isFault()) {
			XMLFault x = m.getXMLFault();
			assertEquals(faultString + "sampleSOAP12FaultEnvelope1", x.getReason().getText());
            assertEquals("Receiver", x.getCode().
                    toQName("http://www.w3.org/2003/05/soap-envelope").getLocalPart());
		} else {
			fail("Message should be marked as a fault.");
		}
		
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}
    
    public void testStringInflow4() throws Exception {
        
        try {
        // On inbound, there will already be an OM
        // which represents the message.  The following code simulates the input
        // OM
        StringReader sr = new StringReader(sampleSOAP12FaultEnvelope2);
        XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow, null);
        OMElement omElement = builder.getSOAPEnvelope();
        
        // The JAX-WS layer creates a Message from the OM
        MessageFactory mf = (MessageFactory)
            FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.createFrom(omElement, null);
        
        assertTrue(m.isFault());
        
        if (m.isFault()) {
            XMLFault x = m.getXMLFault();
            assertEquals(faultString + "sampleSOAP12FaultEnvelope2", x.getReason().getText());
            assertEquals("Sender", x.getCode().
                    toQName("http://www.w3.org/2003/05/soap-envelope").getLocalPart());
        } else {
            fail("Message should be marked as a fault.");
        }
        
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
    
    
    public void testGetSOAP11XMLFaultAsOM() throws Exception {
        MessageFactory factory = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
        Message msg = factory.create(Protocol.soap11);

        XMLFaultReason reason = new XMLFaultReason("sample fault reason");
        XMLFault fault = new XMLFault(XMLFaultCode.SENDER, reason);
        msg.setXMLFault(fault);
        
        OMElement om = msg.getAsOMElement();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        om.serializeAndConsume(baos);
        
        String env = new String(baos.toByteArray());
        assertTrue(env.indexOf("faultcode") > 0);
        assertTrue(env.indexOf("faultstring") > 0);
    }
    
    public void testGetSOAP11XMLFaultAsBlock() throws Exception {
        MessageFactory factory = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
        Message msg = factory.create(Protocol.soap11);

        XMLFaultReason reason = new XMLFaultReason("sample fault reason");
        XMLFault fault = new XMLFault(XMLFaultCode.SENDER, reason);
        msg.setXMLFault(fault);
        
        BlockFactory bf = (BlockFactory) FactoryRegistry.getFactory(SourceBlockFactory.class);
        Block b = msg.getBodyBlock(null, bf);
        
        Source content = (Source) b.getBusinessObject(true);
        byte[] bytes = _getBytes(content);
        String faultContent = new String(bytes);
        
        TestLogger.logger.debug(">> fault content: " + faultContent); 
        assertTrue(faultContent.indexOf("faultcode") > 0);
        assertTrue(faultContent.indexOf("faultstring") > 0);
    }
    
    private byte[] _getBytes(Source input) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult output = new StreamResult(baos);
        
        t.transform(input, output);
        
        return baos.toByteArray(); 
    }

}
