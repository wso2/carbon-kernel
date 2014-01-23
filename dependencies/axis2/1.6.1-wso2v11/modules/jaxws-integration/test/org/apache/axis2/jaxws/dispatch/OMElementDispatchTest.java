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

package org.apache.axis2.jaxws.dispatch;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.soap.SOAPBinding;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.message.databinding.ParsedEntityReader;
import org.apache.axis2.jaxws.message.factory.ParsedEntityReaderFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

/**
 * This class uses the JAX-WS Dispatch API to test sending and receiving
 * messages using SOAP 1.2.
 */
public class OMElementDispatchTest extends AbstractTestCase {
    
    private static final QName QNAME_SERVICE = new QName(
            "http://org/apache/axis2/jaxws/test/OMELEMENT", "OMElementService");
    private static final QName QNAME_PORT = new QName(
            "http://org/apache/axis2/jaxws/test/OMELEMENT", "OMElementPort");
    private static final String URL_ENDPOINT = "http://localhost:6060/axis2/services/OMElementProviderService.OMElementProviderPort";    
    
    private static final String sampleRequest = 
        "<test:echoOMElement xmlns:test=\"http://org/apache/axis2/jaxws/test/OMELEMENT\">" +
        "<test:input>SAMPLE REQUEST MESSAGE</test:input>" +
        "</test:echoOMElement>";
    private static final String testResponse = 
        "<test:echoOMElement xmlns:test=\"http://org/apache/axis2/jaxws/test/OMELEMENT\">" +
        "<test:output>TEST RESPONSE MESSAGE</test:output>" +
        "</test:echoOMElement>";
    private static final String sampleEnvelopeHead = 
        "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">" +
        "<soapenv:Header /><soapenv:Body>";
    private static final String sampleEnvelopeTail = 
        "</soapenv:Body></soapenv:Envelope>";
    private static final String sampleEnvelope = 
        sampleEnvelopeHead + 
        sampleRequest + 
        sampleEnvelopeTail;

    private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    
    public static Test suite() {
        return getTestSetup(new TestSuite(OMElementDispatchTest.class));
    }
    
    /**
     * Test sending a SOAP 1.2 request in PAYLOAD mode
     */
    public void testSourceDispatchPayloadMode() throws Exception {
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP12HTTP_BINDING, URL_ENDPOINT);
        Dispatch<Source> dispatch = service.createDispatch(
                QNAME_PORT, Source.class, Mode.PAYLOAD);
        
        // Create the Source object with the payload contents.  Since
        // we're in PAYLOAD mode, we don't have to worry about the envelope.
        byte[] bytes = sampleRequest.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        StreamSource request = new StreamSource(bais);
        
        // Send the SOAP 1.2 request
        Source response = dispatch.invoke(request);

        assertTrue("The response was null.  We expected content to be returned.", response != null);
        
        // Convert the response to a more consumable format
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(baos);
        
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer trans = factory.newTransformer();
        trans.transform(response, result);
        
        // Check to make sure the contents are correct.  Again, since we're
        // in PAYLOAD mode, we shouldn't have anything related to the envelope
        // in the return, just the contents of the Body.
        String responseText = baos.toString();
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("SAMPLE RESPONSE MESSAGE"));   
        
        // Invoke a second time to verify
        bais = new ByteArrayInputStream(bytes);
        request = new StreamSource(bais);
        
        // Send the SOAP 1.2 request
        response = dispatch.invoke(request);

        assertTrue("The response was null.  We expected content to be returned.", response != null);
        
        // Convert the response to a more consumable format
        baos = new ByteArrayOutputStream();
        result = new StreamResult(baos);
        
        factory = TransformerFactory.newInstance();
        trans = factory.newTransformer();
        trans.transform(response, result);
        
        // Check to make sure the contents are correct.  Again, since we're
        // in PAYLOAD mode, we shouldn't have anything related to the envelope
        // in the return, just the contents of the Body.
        responseText = baos.toString();
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("SAMPLE RESPONSE MESSAGE"));    
    }
    


    /**
     * Test sending a SOAP 1.2 request in MESSAGE mode
     */
    public void testSourceDispatchMessageMode() throws Exception {
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP12HTTP_BINDING, URL_ENDPOINT);
        Dispatch<Source> dispatch = service.createDispatch(
                QNAME_PORT, Source.class, Mode.MESSAGE);
        
        // Create the Source object with the message contents.  Since
        // we're in MESSAGE mode, we'll need to make sure we create this
        // with the right protocol.
        byte[] bytes = sampleEnvelope.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        StreamSource request = new StreamSource(bais);
        
        Source response = dispatch.invoke(request);
        
        // Convert the response to a more consumable format
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(baos);
        
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer trans = factory.newTransformer();
        trans.transform(response, result);
        
        // Check to make sure the contents of the message are correct
        String responseText = baos.toString();
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("SAMPLE RESPONSE MESSAGE"));
        
        // Check to make sure the message returned had the right protocol version
        // TODO: Need to determine whether or not we should be using the hard 
        // coded URLs here, or whether we should be using a constant for the 
        // purposes of the test.
        assertTrue(responseText.contains("http://www.w3.org/2003/05/soap-envelope"));
        assertTrue(!responseText.contains("http://schemas.xmlsoap.org/soap/envelope"));
        
        // Invoke a second time to verify
        bais = new ByteArrayInputStream(bytes);
        request = new StreamSource(bais);
        
        response = dispatch.invoke(request);
        
        // Convert the response to a more consumable format
        baos = new ByteArrayOutputStream();
        result = new StreamResult(baos);
        
        factory = TransformerFactory.newInstance();
        trans = factory.newTransformer();
        trans.transform(response, result);
        
        // Check to make sure the contents of the message are correct
        responseText = baos.toString();
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("SAMPLE RESPONSE MESSAGE"));
        
        // Check to make sure the message returned had the right protocol version
        // TODO: Need to determine whether or not we should be using the hard 
        // coded URLs here, or whether we should be using a constant for the 
        // purposes of the test.
        assertTrue(responseText.contains("http://www.w3.org/2003/05/soap-envelope"));
        assertTrue(!responseText.contains("http://schemas.xmlsoap.org/soap/envelope"));
    }
    
    /**
     * Test sending a SOAP 1.2 request in PAYLOAD mode
     */
    public void testOMElementDispatchPayloadMode() throws Exception {
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP12HTTP_BINDING, URL_ENDPOINT);
        Dispatch<OMElement> dispatch = service.createDispatch(
                QNAME_PORT, OMElement.class, Mode.PAYLOAD);
        
        // Create the OMElement object with the payload contents.  Since
        // we're in PAYLOAD mode, we don't have to worry about the envelope.
        StringReader sr = new StringReader(sampleRequest);
        XMLStreamReader inputReader = inputFactory.createXMLStreamReader(sr);
        StAXOMBuilder builder = new StAXOMBuilder(inputReader);  
        OMElement om = builder.getDocumentElement();
        
        // Send the SOAP 1.2 request
        OMElement response = dispatch.invoke(om);

        assertTrue("The response was null.  We expected content to be returned.", response != null);
        
        // Check to make sure the contents are correct.  Again, since we're
        // in PAYLOAD mode, we shouldn't have anything related to the envelope
        // in the return, just the contents of the Body.
        String responseText = response.toStringWithConsume();
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("SAMPLE RESPONSE MESSAGE"));   
        
        // Send the SOAP 1.2 request
        response = dispatch.invoke(om);

        assertTrue("The response was null.  We expected content to be returned.", response != null);
        
        // Check to make sure the contents are correct.  Again, since we're
        // in PAYLOAD mode, we shouldn't have anything related to the envelope
        // in the return, just the contents of the Body.
        responseText = response.toStringWithConsume();
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("SAMPLE RESPONSE MESSAGE"));    
    }
    


    /**
     * Test sending a SOAP 1.2 request in MESSAGE mode
     */
    public void testOMElementDispatchMessageMode() throws Exception {
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP12HTTP_BINDING, URL_ENDPOINT);
        Dispatch<OMElement> dispatch = service.createDispatch(
                QNAME_PORT, OMElement.class, Mode.MESSAGE);
        
        // Create the OMElement object with the payload contents.  Since
        // we're in PAYLOAD mode, we don't have to worry about the envelope.
        StringReader sr = new StringReader(sampleEnvelope);
        XMLStreamReader inputReader = inputFactory.createXMLStreamReader(sr);
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inputReader, null); 
        SOAPEnvelope soap12Envelope = (SOAPEnvelope) builder.getDocumentElement();
        
        
        OMElement response = dispatch.invoke(soap12Envelope);
        
        // Check to make sure the contents of the message are correct
        //String responseText = baos.toString();
        String responseText = response.toStringWithConsume();
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("SAMPLE RESPONSE MESSAGE"));
        
        // Check to make sure the message returned had the right protocol version
        // TODO: Need to determine whether or not we should be using the hard 
        // coded URLs here, or whether we should be using a constant for the 
        // purposes of the test.
        assertTrue(responseText.contains("http://www.w3.org/2003/05/soap-envelope"));
        assertTrue(!responseText.contains("http://schemas.xmlsoap.org/soap/envelope"));
        
        StringReader sr2 = new StringReader(sampleEnvelope);
        inputReader = inputFactory.createXMLStreamReader(sr2);
        builder = new StAXSOAPModelBuilder(inputReader, null);  
        SOAPEnvelope om = (SOAPEnvelope)builder.getDocumentElement();
        response = dispatch.invoke(om);
        
        // Check to make sure the contents of the message are correct
        responseText = response.toStringWithConsume();
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("SAMPLE RESPONSE MESSAGE"));
        
        // Check to make sure the message returned had the right protocol version
        // TODO: Need to determine whether or not we should be using the hard 
        // coded URLs here, or whether we should be using a constant for the 
        // purposes of the test.
        assertTrue(responseText.contains("http://www.w3.org/2003/05/soap-envelope"));
        assertTrue(!responseText.contains("http://schemas.xmlsoap.org/soap/envelope"));
    }
    
    /**
     * Test sending a SOAP 1.2 request in MESSAGE mode with
     * a Parser that can provide the InputStream for the payload
     */
    public void testOMElementDispatchWithParsedEntityReader() throws Exception {
        
        // Subsitute a ParsedEntityReader that will provide the
        // payload InputStream.  This simulates parsers that provide this
        // feature.
        ParsedEntityReaderFactory factory = (ParsedEntityReaderFactory)
        FactoryRegistry.getFactory(ParsedEntityReaderFactory.class);
        ParsedEntityReader per = new ParsedEntityReaderTest();
        factory.setParsetEntityReader(per);
        
        try {
            // Create the JAX-WS client needed to send the request
            Service service = Service.create(QNAME_SERVICE);
            service.addPort(QNAME_PORT, SOAPBinding.SOAP12HTTP_BINDING, URL_ENDPOINT);
            Dispatch<OMElement> dispatch = service.createDispatch(
                    QNAME_PORT, OMElement.class, Mode.MESSAGE);

            // Create the OMElement object with the payload contents.  Since
            // we're in PAYLOAD mode, we don't have to worry about the envelope.
            StringReader sr = new StringReader(sampleEnvelope);
            XMLStreamReader inputReader = inputFactory.createXMLStreamReader(sr);
            StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inputReader, null); 
            SOAPEnvelope soap12Envelope = (SOAPEnvelope) builder.getDocumentElement();


            // Invoke
            OMElement response = dispatch.invoke(soap12Envelope);
            

            SOAPEnvelope responseEnv = (SOAPEnvelope) response;
            SOAPBody responseBody = responseEnv.getBody();
            OMElement payload = responseBody.getFirstElement();

            // At this point, the payload should be an OMSourcedElement
            // that was created from the ParsedEntityReader's stream
            assertTrue(payload instanceof OMSourcedElement);


            // Check to make sure the contents of the message are correct
            String responseText = payload.toStringWithConsume();
            assertTrue(responseText.contains("TEST RESPONSE"));
        } finally {
            
            // Uninstall the Test ParsedEntityReader
            factory.setParsetEntityReader(null);
        }
    }
    
    /**
     * The purpose of a ParsedEntityReader is to get the 
     * InputStream from the parser if it is available.
     * Woodstox and other parsers don't provide that feature.
     * To simulate this feature, this ParserEntityReaderTest is
     * inserted to simulate getting a response from the Parser.
     */
    public class ParsedEntityReaderTest implements ParsedEntityReader {
        int count =0;
        public boolean isParsedEntityStreamAvailable() {
            return true;
        }

        public InputStream readParsedEntityStream(XMLStreamReader reader) {
            count++;
            if (count == 2) {
                return new ByteArrayInputStream(testResponse.getBytes()); 
            } else  {
                return null;
            }
        }

    }
}
