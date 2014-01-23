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
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.jaxws.message.factory.SAAJConverterFactory;
import org.apache.axis2.jaxws.message.util.SAAJConverter;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.TypeInfo;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * SAAJConverterTests
 * 
 * Test the basic functionality of the SAAJConverter.
 * You can also use these tests to as sample code on how to use
 * the converter.
 *
 */
public class SAAJConverterTests extends TestCase {

	private static final String sampleText =
		"<pre:a xmlns:pre=\"urn://sample\">" +
		"<b>Hello</b>" +
		"<c>World</c>" +
		"</pre:a>";
	
	private static final String sampleEnvelope = 
		"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
		"<soapenv:Header /><soapenv:Body>" +
		sampleText +
		"</soapenv:Body></soapenv:Envelope>";
	
	private static final String sampleText1 =
		"<pre:a xmlns:pre=\"urn://sample\">" +
		"<b id=\"100\">Hello</b>" +
		"<c>World</c>" +
		"</pre:a>";
	
	private static final String sampleEnvelope1 = 
		"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
		"<soapenv:Header /><soapenv:Body>" +
		sampleText1 +
		"</soapenv:Body></soapenv:Envelope>";
	
	private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	
	public SAAJConverterTests() {
		super();
	}

	public SAAJConverterTests(String arg0) {
		super(arg0);
	}

	/**
	 * @testStrategy Tests conversions between SAAJ and OM SOAPEnvelopes
	 */
	public void test1() throws Exception {
		
		// Bootstrap: Create an OM SOAPEnvelope from the sample text
		StringReader sr = new StringReader(sampleEnvelope);
		XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
		StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow, null);
		org.apache.axiom.soap.SOAPEnvelope omEnvelope = builder.getSOAPEnvelope();
		
		// Step 1: Get the SAAJConverter object from the Factory
		SAAJConverterFactory f = (SAAJConverterFactory) 
			FactoryRegistry.getFactory(SAAJConverterFactory.class);
		SAAJConverter converter = f.getSAAJConverter();
		
		// Step 2: Convert the OM SOAPEnvelope to an SAAJ SOAPEnvelope
		SOAPEnvelope saajEnvelope = converter.toSAAJ(omEnvelope);
		
		// Step 2a: Simple assertion check to ensure correctness.
		String name = saajEnvelope.getBody().getFirstChild().getLocalName();
		assertTrue("a".equals(name));
		
		// Step 3: Convert the SAAJ SOAPEnvelope to an OM SOAPEnvelope
		omEnvelope = converter.toOM(saajEnvelope);
		
		// Step 3a: Simple assertion check to ensure correctness
		name = omEnvelope.getBody().getFirstElement().getLocalName();
		assertTrue("a".equals(name));
		
		// Step 4: Rinse and repeat
		saajEnvelope = converter.toSAAJ(omEnvelope);
		name = saajEnvelope.getBody().getFirstChild().getLocalName();
		assertTrue("a".equals(name));
		omEnvelope = converter.toOM(saajEnvelope);
		name = omEnvelope.getBody().getFirstElement().getLocalName();
		assertTrue("a".equals(name));
	}
	
	/**
	 * @testStrategy Tests conversions between SAAJ and OM for normal element
	 */
	public void test2() throws Exception {
		
		// Bootstrap: Create an OM SOAPEnvelope from the sample text.
		StringReader sr = new StringReader(sampleEnvelope);
		XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
		StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow, null);
		org.apache.axiom.soap.SOAPEnvelope omEnvelope = builder.getSOAPEnvelope();
		
		// Bootstrap: Get an OMElement from the body
		OMElement om = omEnvelope.getBody().getFirstElement();
		
		// Bootstrap: Get an SAAJ Body to hold the target SOAPElement
		MessageFactory msgFactory = MessageFactory.newInstance();
		SOAPMessage message = msgFactory.createMessage();
		SOAPBody body = message.getSOAPBody();
		
		// Step 1: Get the SAAJConverter object from the Factory
		SAAJConverterFactory f = (SAAJConverterFactory) 
			FactoryRegistry.getFactory(SAAJConverterFactory.class);
		SAAJConverter converter = f.getSAAJConverter();
		
		// Step 2: Convert OM to SAAJ
		SOAPElement se = converter.toSAAJ(om, body);
		
		// Step 2a: Verify
		assertTrue(se instanceof SOAPBodyElement);
		assertTrue(se.getLocalName().equals("a"));
		
		// Step 3: Convert SAAJ to OM
		om = converter.toOM(se);
		
		// Step 3a: Verify
		assertTrue(om.getLocalName().equals("a"));
		
		// Step 4: Rinse and Repeat
		se = converter.toSAAJ(om, body);
		assertTrue(se instanceof SOAPBodyElement);
		assertTrue(se.getLocalName().equals("a"));
		om = converter.toOM(se);
		assertTrue(om.getLocalName().equals("a"));
	}
	
	/**
	 * @testStrategy: Create an OMElement, without using a builder.  Verification of AXIS2-970
	 */
    public void test3() throws Exception {
    	
//    	 Step 1: Get the SAAJConverter object from the Factory
		SAAJConverterFactory f = (SAAJConverterFactory) 
			FactoryRegistry.getFactory(SAAJConverterFactory.class);
		SAAJConverter converter = f.getSAAJConverter();
		
		// Stept 2: Create OM and parent SOAPElement
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace wrapNs = fac.createOMNamespace("namespace", "prefix");
        OMElement ome = fac.createOMElement("localname", wrapNs);
        SOAPFactory sf = SOAPFactory.newInstance();
        SOAPElement se = sf.createElement("name");
        
        // Step 3: Do the conversion
        converter.toSAAJ(ome, se, sf);
    }
    
	/**
	 * @testStrategy Tests conversions between OM and SAAJ SOAPEnvelopes 
	 * and verify attribute type is correctly stored
	 */
	public void test4() throws Exception {
		// Bootstrap: Create an OM SOAPEnvelope from the sample text
		StringReader sr = new StringReader(sampleEnvelope1);
		XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
		StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow, null);
		org.apache.axiom.soap.SOAPEnvelope omEnvelope = builder.getSOAPEnvelope();
		
		// Step 1: Get the SAAJConverter object from the Factory
		SAAJConverterFactory f = (SAAJConverterFactory) 
			FactoryRegistry.getFactory(SAAJConverterFactory.class);
		SAAJConverter converter = f.getSAAJConverter();
		
		// Step 2: Convert the OM SOAPEnvelope to an SAAJ SOAPEnvelope
		SOAPEnvelope saajEnvelope = converter.toSAAJ(omEnvelope);
		
		// Step 3: Verify attribute type is stored after conversion
		Element testElement = (Element) saajEnvelope.getBody().getFirstChild().getFirstChild();
		assertTrue("b".equals(testElement.getLocalName()));
		
		List attrs = new ArrayList();
        NamedNodeMap map = testElement.getAttributes();
        
        if (map != null) {
            for (int i = 0; i < map.getLength(); i++) {
                Attr attr = (Attr)map.item(i);
                if (attr.getName().equals("xmlns") || attr.getName().startsWith("xmlns:")) {
                    // this is a namespace declaration
                } else {
                    attrs.add(attr);
                }
            }
        }
        
        Attr attr = (Attr)attrs.get(0);
        TypeInfo typeInfo = null;
        String attrType = null;
        
        try {
        	typeInfo = attr.getSchemaTypeInfo();
            if (typeInfo != null) {
    	    	attrType = typeInfo.getTypeName();
            }
        } catch (Throwable t) {
        	;
        }
        
        if (attrType == null) {
            attrType = (String) attr.getUserData(SAAJConverter.OM_ATTRIBUTE_KEY);
        }
        assert("CDATA".equals(attrType));
	}
}
