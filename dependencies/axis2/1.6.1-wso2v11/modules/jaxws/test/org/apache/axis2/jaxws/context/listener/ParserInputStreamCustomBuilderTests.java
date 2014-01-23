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
package org.apache.axis2.jaxws.context.listener;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.ds.ParserInputStreamDataSource;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.jaxws.message.databinding.ParsedEntityReader;
import org.apache.axis2.jaxws.message.databinding.impl.ParsedEntityReaderImpl;
import org.apache.axis2.jaxws.message.factory.ParsedEntityReaderFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

public class ParserInputStreamCustomBuilderTests extends TestCase {
	private StAXSOAPModelBuilder builder = null;
	private XMLStreamReader parser = null;
	private String mockenvelope= "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
	"<soapenv:Header/>"+
	"<soapenv:Body>"+
	"<ns:invokeOp xmlns:=\"urn:sample\">Hello Provider OM</ns:invokeOp>"+
	"</soapenv:Body>"+
	"</soapenv:Envelope>";
	
	private String ENVELOPE= "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
	    "<soapenv:Header/>"+
	    "<soapenv:Body>"+
	    "<invokeOp>Hello Provider OM</invokeOp>"+
	    "</soapenv:Body>"+
	    "</soapenv:Envelope>";
        
	String mockPayload = "<invokeOp>Hello Provider OM</invokeOp>";
	
	public void testCustomBuilder(){
	    try{
	        SOAPEnvelope env = getMockEnvelope();
	        SOAPHeader header = env.getHeader();
	        SOAPBody body = env.getBody();
	        ParserInputStreamCustomBuilder customBuilder = new ParserInputStreamCustomBuilder("UTF-8");
	        InputStream payload = new ByteArrayInputStream(mockPayload.getBytes());
	        OMElement om= customBuilder.create("urn:sample", "invokeOp",(OMContainer) body, parser, OMAbstractFactory.getOMFactory(), payload);
	        assertTrue(om!=null);
	        assertTrue(om instanceof OMSourcedElement);
	        OMSourcedElement ose = (OMSourcedElement)om;
	        assertNotNull(ose.getDataSource());
	        assertTrue((ose.getDataSource()) instanceof ParserInputStreamDataSource);
	    }catch(Exception e){
	        fail(e.getMessage());
	    }
	}
	
    public void testCustomBuilderSOAPENVNamespace(){
        try{
            SOAPEnvelope env = getMockEnvelope();
            SOAPHeader header = env.getHeader();
            SOAPBody body = env.getBody();
            ParserInputStreamCustomBuilder customBuilder = new ParserInputStreamCustomBuilder("UTF-8");
            InputStream payload = new ByteArrayInputStream(mockPayload.getBytes());

            // If there is no namespace, the customer building should not occur.
            OMElement om= customBuilder.create("http://www.w3.org/2003/05/soap-envelope", "Fault",(OMContainer) body, parser, OMAbstractFactory.getOMFactory(), payload);
            assertTrue(om==null);
        }catch(Exception e){
            fail(e.getMessage());
        }
    }

	/**
     * Tests that ParsedEntityCustomBuilder.convertEntityReferences works as expected.
     */
    public void testConvertEntityReferences(){
        try{
            ParserInputStreamCustomBuilder customBuilder = new ParserInputStreamCustomBuilder("UTF-8");
            // test that all expected chars are converted
            String expectedString1 = "&lt;,&gt;,&quot;,&apos;,&amp;";
            String convertedString = customBuilder.convertEntityReferences("<,>,\",',&");
            assertTrue("Special chars didn't get converted!  " +
                    "Expected: \""+expectedString1+"\" but received: \""+convertedString+"\"", 
                    convertedString.equals(expectedString1));
            // test that a string with no special chars is unchanged
            String simpleString = "This is a simple string";
            convertedString = customBuilder.convertEntityReferences(simpleString);
            assertTrue("Simple string was changed unexpectedly.  " +
                    "Expected: \""+simpleString+"\" but received: \""+convertedString+"\"", 
                    convertedString.equals(simpleString));
            
            // test that the mockenvelope gets converted correctly
            String expectedString2 = "&lt;soapenv:Envelope xmlns:soapenv=&quot;http://schemas.xmlsoap.org/soap/envelope/&quot;&gt;&lt;soapenv:Header/&gt;&lt;soapenv:Body&gt;&lt;invokeOp&gt;Hello Provider OM&lt;/invokeOp&gt;&lt;/soapenv:Body&gt;&lt;/soapenv:Envelope&gt;";
            convertedString = customBuilder.convertEntityReferences(ENVELOPE);
            assertTrue("mockenvelope was not converted as expected.  " +
                    "Expected: \""+expectedString2+"\" but received: \""+convertedString+"\"", 
                    convertedString.equals(expectedString2));
        }catch(Exception e){
            fail(e.getMessage());
        }
    }
	private SOAPEnvelope getMockEnvelope() throws Exception{
		SOAPEnvelope env = (SOAPEnvelope)getOMBuilder().getDocumentElement();
		return env;
	}
    private StAXSOAPModelBuilder getOMBuilder() throws Exception {
    	if(builder == null){
	        builder = new StAXSOAPModelBuilder(getParser(), null);
    	}
        return builder;
    }
    
    private XMLStreamReader getParser()throws XMLStreamException{
    	if(parser == null){
    		parser =  XMLInputFactory.newInstance()
    		.createXMLStreamReader(
    				new ByteArrayInputStream(mockenvelope.getBytes()));
    	}
    	return parser;

    }
	
}
