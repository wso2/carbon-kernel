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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.message.util.Reader2Writer;
import org.xml.sax.InputSource;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayInputStream;
import java.util.concurrent.Future;

/**
 * This class tests the JAX-WS Dispatch<Source> with content in various 
 * forms of a javax.xml.transform.sax.SAXSource.
 */
public class SAXSourceDispatchTests extends AbstractTestCase{

    private static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();

    public static Test suite() {
        return getTestSetup(new TestSuite(SAXSourceDispatchTests.class));
    }

	public void testSyncPayloadMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                                                       Source.class, Service.Mode.PAYLOAD);

        // Create a SAXSource out of the string content
        byte[] bytes = DispatchTestConstants.sampleBodyContent.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        InputSource input = new InputSource(stream);
        Source request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking sync Dispatch");
        Source response = dispatch.invoke(request);

        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        XMLStreamReader reader = inputFactory.createXMLStreamReader(response);
        Reader2Writer r2w = new Reader2Writer(reader);
        String responseText = r2w.getAsString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
        
        // Invoke a second time
        stream = new ByteArrayInputStream(bytes);
        input = new InputSource(stream);
        request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking sync Dispatch");
        response = dispatch.invoke(request);

        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        reader = inputFactory.createXMLStreamReader(response);
        r2w = new Reader2Writer(reader);
        responseText = r2w.getAsString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
        
     }

	public void testSyncMessageMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.MESSAGE);
		
        // Create a SAXSource out of the string content
        byte[] bytes = DispatchTestConstants.sampleSoapMessage.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        InputSource input = new InputSource(stream);
        Source request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking sync Dispatch with Message Mode");
		Source response = dispatch.invoke(request);

        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        XMLStreamReader reader = inputFactory.createXMLStreamReader(response);
        Reader2Writer r2w = new Reader2Writer(reader);
        String responseText = r2w.getAsString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
        
        
        // Invoke a second time
        stream = new ByteArrayInputStream(bytes);
        input = new InputSource(stream);
        request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking sync Dispatch with Message Mode");
        response = dispatch.invoke(request);

        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        reader = inputFactory.createXMLStreamReader(response);
        r2w = new Reader2Writer(reader);
        responseText = r2w.getAsString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
	}

    public void testAsyncCallbackPayloadMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.PAYLOAD);
        
        // Setup the callback for async responses
        AsyncCallback<Source> callbackHandler = new AsyncCallback<Source>();
        
        // Create a SAXSource out of the string content
        byte[] bytes = DispatchTestConstants.sampleBodyContent.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        InputSource input = new InputSource(stream);
        Source request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch");
        Future<?> monitor = dispatch.invokeAsync(request, callbackHandler);

        while (!monitor.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        Source response = callbackHandler.getValue();
        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        XMLStreamReader reader = inputFactory.createXMLStreamReader(response);
        Reader2Writer r2w = new Reader2Writer(reader);
        String responseText = r2w.getAsString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
        
        
        
        // Invoke a second time
        callbackHandler = new AsyncCallback<Source>();
        stream = new ByteArrayInputStream(bytes);
        input = new InputSource(stream);
        request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch");
        monitor = dispatch.invokeAsync(request, callbackHandler);

        while (!monitor.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        response = callbackHandler.getValue();
        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        reader = inputFactory.createXMLStreamReader(response);
        r2w = new Reader2Writer(reader);
        responseText = r2w.getAsString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
    }
    
    public void testAsyncCallbackMessageMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.MESSAGE);
		
        // Setup the callback for async responses
        AsyncCallback<Source> callbackHandler = new AsyncCallback<Source>();
        
        // Create a SAXSource out of the string content
        byte[] bytes = DispatchTestConstants.sampleSoapMessage.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        InputSource input = new InputSource(stream);
        Source request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch");
        Future<?> monitor = dispatch.invokeAsync(request, callbackHandler);

        while (!monitor.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        Source response = callbackHandler.getValue();
        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        XMLStreamReader reader = inputFactory.createXMLStreamReader(response);
        Reader2Writer r2w = new Reader2Writer(reader);
        String responseText = r2w.getAsString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
        
        
        // Invoke a second time to verify
        callbackHandler = new AsyncCallback<Source>();
        
        stream = new ByteArrayInputStream(bytes);
        input = new InputSource(stream);
        request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch");
        monitor = dispatch.invokeAsync(request, callbackHandler);

        while (!monitor.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        response = callbackHandler.getValue();
        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        reader = inputFactory.createXMLStreamReader(response);
        r2w = new Reader2Writer(reader);
        responseText = r2w.getAsString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
	}
    
    public void testAsyncPollingPayloadMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.PAYLOAD);
        
        // Create a SAXSource out of the string content
        byte[] bytes = DispatchTestConstants.sampleBodyContent.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        InputSource input = new InputSource(stream);
        Source request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking async (polling) Dispatch");
        Response<Source> asyncResponse = dispatch.invokeAsync(request);

        while (!asyncResponse.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        Source response = asyncResponse.get();
        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        XMLStreamReader reader = inputFactory.createXMLStreamReader(response);
        Reader2Writer r2w = new Reader2Writer(reader);
        String responseText = r2w.getAsString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
        
        
        // Invoke a second time to verify
        stream = new ByteArrayInputStream(bytes);
        input = new InputSource(stream);
        request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking async (polling) Dispatch");
        asyncResponse = dispatch.invokeAsync(request);

        while (!asyncResponse.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        response = asyncResponse.get();
        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        reader = inputFactory.createXMLStreamReader(response);
        r2w = new Reader2Writer(reader);
        responseText = r2w.getAsString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
    }
    
    public void testAsyncPollingMessageMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.MESSAGE);
        
        // Create a SAXSource out of the string content
        byte[] bytes = DispatchTestConstants.sampleSoapMessage.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        InputSource input = new InputSource(stream);
        Source request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch");
        Response<Source> asyncResponse = dispatch.invokeAsync(request);

        while (!asyncResponse.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        Source response = asyncResponse.get();
        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        XMLStreamReader reader = inputFactory.createXMLStreamReader(response);
        Reader2Writer r2w = new Reader2Writer(reader);
        String responseText = r2w.getAsString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
        
        
        // Invoke a second time to verify
        stream = new ByteArrayInputStream(bytes);
        input = new InputSource(stream);
        request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch");
        asyncResponse = dispatch.invokeAsync(request);

        while (!asyncResponse.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        response = asyncResponse.get();
        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        reader = inputFactory.createXMLStreamReader(response);
        r2w = new Reader2Writer(reader);
        responseText = r2w.getAsString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
    }
    
    public void testOneWayPayloadMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.PAYLOAD);
        
        // Create a SAXSource out of the string content
        byte[] bytes = DispatchTestConstants.sampleBodyContent.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        InputSource input = new InputSource(stream);
        Source request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking One Way Dispatch");
        dispatch.invokeOneWay(request);
        
        
        // Invoke a second time to verify
        stream = new ByteArrayInputStream(bytes);
        input = new InputSource(stream);
        request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking One Way Dispatch");
        dispatch.invokeOneWay(request);
    }
    
    public void testOneWayMessageMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.MESSAGE);
        
        // Create a SAXSource out of the string content
        byte[] bytes = DispatchTestConstants.sampleSoapMessage.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        InputSource input = new InputSource(stream);
        Source request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking One Way Dispatch");
	dispatch.invokeOneWay(request);
                
        // Invoke a second time to verify
        stream = new ByteArrayInputStream(bytes);
        input = new InputSource(stream);
        request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking One Way Dispatch");
        dispatch.invokeOneWay(request);
	}
    
    public void testBadSAXSource() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.MESSAGE);
        
        // Create an empty (invalid) SAXSource
        Source request = new SAXSource();
        
        try {
            dispatch.invoke(request);
            fail("WebServiceException was expected");
        } catch (WebServiceException e) {
            TestLogger.logger.debug("A Web Service Exception was expected: " + e.toString());
            assertTrue(e.getMessage() != null);
        } catch (Exception e) {
            fail("WebServiceException was expected, but received:" + e);
        }
        
        // Invoke a second time to verify
        try {
            dispatch.invoke(request);
            fail("WebServiceException was expected");
        } catch (WebServiceException e) {
            TestLogger.logger.debug("A Web Service Exception was expected: " + e.toString());
            assertTrue(e.getMessage() != null);
        } catch (Exception e) {
            fail("WebServiceException was expected, but received:" + e);
        }
        
        try {
            dispatch.invokeOneWay(request);
            fail("WebServiceException was expected");
        } catch (WebServiceException e) {
            TestLogger.logger.debug("A Web Service Exception was expected: " + e.toString());
            assertTrue(e.getMessage() != null);
        } catch (Exception e) {
            fail("WebServiceException was expected, but received:" + e);
        }
        
    }
    
}
