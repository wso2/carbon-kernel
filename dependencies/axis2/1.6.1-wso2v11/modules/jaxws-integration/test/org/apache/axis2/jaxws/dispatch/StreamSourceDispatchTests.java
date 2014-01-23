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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.Future;

/**
 * This class tests the JAX-WS Dispatch<Source> functionality with various
 * forms of a StreamSource object. 
 *
 */
public class StreamSourceDispatchTests extends AbstractTestCase {

    private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    
    
    public static Test suite() {
        return getTestSetup(new TestSuite(StreamSourceDispatchTests.class));
    }

	/**
     * Invoke a Dispatch<Source> synchronously with the content in PAYLOAD mode.
	 */
    public void testSyncPayloadMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
		svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
		Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, Source.class, 
                Service.Mode.PAYLOAD);
		
        // Create a StreamSource with the desired content
        byte[] bytes = DispatchTestConstants.sampleBodyContent.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        Source srcStream = new StreamSource((InputStream) stream);
        
        // Invoke the Dispatch<Source>
        TestLogger.logger.debug(">> Invoking sync Dispatch with PAYLOAD mode");
		Source response = dispatch.invoke(srcStream);
		assertNotNull(response);
        
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
        srcStream = new StreamSource((InputStream) stream);
        
        // Invoke the Dispatch<Source>
        TestLogger.logger.debug(">> Invoking sync Dispatch with PAYLOAD mode");
        response = dispatch.invoke(srcStream);
        assertNotNull(response);
        
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

    /**
     * Invoke a Dispatch<Source> synchronously with the content in MESSAGE mode.
     */
    public void testSyncMessageMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
		Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
		svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
		Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, Source.class,
				Mode.MESSAGE);
		
		// Create a StreamSource with the desired content
        byte[] bytes = DispatchTestConstants.sampleSoapMessage.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        Source srcStream = new StreamSource((InputStream) stream);

        TestLogger.logger.debug(">> Invoking sync Dispatch with MESSAGE Mode");
		StreamSource response = (StreamSource) dispatch.invoke(srcStream);
        assertNotNull(response);

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
        srcStream = new StreamSource((InputStream) stream);

        TestLogger.logger.debug(">> Invoking sync Dispatch with MESSAGE Mode");
        response = (StreamSource) dispatch.invoke(srcStream);
        assertNotNull(response);

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

    /**
     * Invoke a Dispatch<Source> asynchronously with the content in PAYLOAD mode.
     */
    public void testAsyncCallbackPayloadMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, Source.class,
                Service.Mode.PAYLOAD);
        
        // We'll need a callback instance to handle the async responses
        AsyncCallback<Source> callbackHandler = new AsyncCallback<Source>();
        
        // Create a StreamSource with the desired content
        byte[] bytes = DispatchTestConstants.sampleBodyContent.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        Source srcStream = new StreamSource((InputStream) stream);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch with PAYLOAD mode");
        Future<?> monitor = dispatch.invokeAsync(srcStream, callbackHandler);

        // Wait for the async response to be returned
        while (!monitor.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        Source response = callbackHandler.getValue();
        assertNotNull(response);
        
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
        srcStream = new StreamSource((InputStream) stream);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch with PAYLOAD mode");
        monitor = dispatch.invokeAsync(srcStream, callbackHandler);

        // Wait for the async response to be returned
        while (!monitor.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        response = callbackHandler.getValue();
        assertNotNull(response);
        
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
    
    /**
     * Invoke a Dispatch<Source> asynchronously with the content in MESSAGE mode.
     */
	public void testAsyncCallbackMessageMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts 
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
		svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, Source.class,
                Mode.MESSAGE);
        
        // We'll need a callback instance to handle the async responses
        AsyncCallback<Source> callbackHandler = new AsyncCallback<Source>();

        // Create a StreamSource with the desired content
        byte[] bytes = DispatchTestConstants.sampleSoapMessage.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		Source srcStream = new StreamSource((InputStream) stream);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch with MESSAGE mode");
        Future<?> monitor = dispatch.invokeAsync(srcStream, callbackHandler);

        // Wait for the async response to be returned
        while (!monitor.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        Source response = callbackHandler.getValue();
        assertNotNull(response);

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
        // We'll need a callback instance to handle the async responses
        callbackHandler = new AsyncCallback<Source>();

        // Create a StreamSource with the desired content
        bytes = DispatchTestConstants.sampleSoapMessage.getBytes();
        stream = new ByteArrayInputStream(bytes);
        srcStream = new StreamSource((InputStream) stream);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch with MESSAGE mode");
        monitor = dispatch.invokeAsync(srcStream, callbackHandler);

        // Wait for the async response to be returned
        while (!monitor.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        response = callbackHandler.getValue();
        assertNotNull(response);

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

    /**
     * Invoke a Dispatch<Source> asynchronously with the content in PAYLOAD mode.
     */
    public void testAsyncPollingPayloadMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, Source.class,
                Service.Mode.PAYLOAD);
        // Create a StreamSource with the desired content
        byte[] bytes = DispatchTestConstants.sampleBodyContent.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        Source srcStream = new StreamSource((InputStream) stream);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch with PAYLOAD mode");
        Response<Source> asyncResponse = dispatch.invokeAsync(srcStream);

        // Wait for the async response to be returned
        while (!asyncResponse.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        Source response = asyncResponse.get();
        assertNotNull(response);
        
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
        srcStream = new StreamSource((InputStream) stream);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch with PAYLOAD mode");
        asyncResponse = dispatch.invokeAsync(srcStream);

        // Wait for the async response to be returned
        while (!asyncResponse.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        response = asyncResponse.get();
        assertNotNull(response);
        
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
    
    /**
     * Invoke a Dispatch<Source> asynchronously with the content in MESSAGE mode.
     */
    public void testAsyncPollingMessageMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        // Initialize the JAX-WS client artifacts 
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, Source.class,
                Mode.MESSAGE);

        // Create a StreamSource with the desired content
        byte[] bytes = DispatchTestConstants.sampleSoapMessage.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        Source srcStream = new StreamSource((InputStream) stream);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch with MESSAGE mode");
        Response<Source> asyncResponse = dispatch.invokeAsync(srcStream);

        // Wait for the async response to be returned
        while (!asyncResponse.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        Source response = asyncResponse.get();
        assertNotNull(response);

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
        srcStream = new StreamSource((InputStream) stream);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch with MESSAGE mode");
        asyncResponse = dispatch.invokeAsync(srcStream);

        // Wait for the async response to be returned
        while (!asyncResponse.isDone()) {
            TestLogger.logger.debug(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        response = asyncResponse.get();
        assertNotNull(response);

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
    
    /**
     * Invoke a Dispatch<Source> one-way operation
     */
    public void testOneWayPayloadMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());

        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, Source.class,
                                                       Service.Mode.PAYLOAD);

        // Create a StreamSource with the desired content
        byte[] bytes = DispatchTestConstants.sampleBodyContent.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        Source srcStream = new StreamSource((InputStream) stream);

        TestLogger.logger.debug(">> Invoking One Way Dispatch");
        dispatch.invokeOneWay(srcStream);
        
        // Invoke a second time to verify
        stream = new ByteArrayInputStream(bytes);
        srcStream = new StreamSource((InputStream) stream);

        TestLogger.logger.debug(">> Invoking One Way Dispatch");
        dispatch.invokeOneWay(srcStream);
    }
}
